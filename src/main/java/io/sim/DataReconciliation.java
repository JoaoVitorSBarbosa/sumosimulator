package io.sim;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.sim.sumo.SumoCommandExecutor;
import io.sim.sumo.cmd.*;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;

/**
 * Classe responsável pela Reconciliação de Dados (AV2 - Parte I)
 * Integrada com SUMO para obter dados reais de velocidade e posição
 */
public class DataReconciliation {
    
    private SumoCommandExecutor sumoExecutor;
    private String vehicleId;
    private List<SensorReading> sensorReadings;
    private List<FlowMeter> flowMeters;
    private double routeLength;
    private double averageSpeed = 80.0; // km/h
    private double simulationTime;
    
    // Estatísticas de reconciliação
    private double meanSpeed;
    private double standardDeviation;
    private double bias;
    private double precision;
    private double uncertainty;
    
    /**
     * Classe interna para leituras de sensores
     */
    public static class SensorReading {
        public double timestamp;
        public String vehicleId;
        public double realSpeed;
        public double measuredSpeed;
        public double optimalSpeed;
        public SumoPosition2D position;
        public double distance;
        public double fuelConsumption;
        public double error;
        
        public SensorReading(double timestamp, String vehicleId) {
            this.timestamp = timestamp;
            this.vehicleId = vehicleId;
        }
        
        // Getters públicos
        public double getTimestamp() { return timestamp; }
        public String getVehicleId() { return vehicleId; }
        public double getRealSpeed() { return realSpeed; }
        public double getMeasuredSpeed() { return measuredSpeed; }
        public double getOptimalSpeed() { return optimalSpeed; }
        public SumoPosition2D getPosition() { return position; }
        public double getDistance() { return distance; }
        public double getFuelConsumption() { return fuelConsumption; }
        public double getError() { return error; }
    }
    
    /**
     * Classe interna para medidores de fluxo
     */
    public static class FlowMeter {
        public String id;
        public double position;
        public double measuredFlow;
        public double reconciledFlow;
        public double uncertainty;
        
        public FlowMeter(String id, double position) {
            this.id = id;
            this.position = position;
        }
        
        // Getters públicos
        public String getId() { return id; }
        public double getPosition() { return position; }
        public double getMeasuredFlow() { return measuredFlow; }
        public double getReconciledFlow() { return reconciledFlow; }
        public double getUncertainty() { return uncertainty; }
    }
    
    public DataReconciliation(SumoCommandExecutor sumoExecutor, String vehicleId) {
        this.sumoExecutor = sumoExecutor;
        this.vehicleId = vehicleId;
        this.sensorReadings = new ArrayList<>();
        this.flowMeters = new ArrayList<>();
        
        // Inicializar medidores de fluxo
        initializeFlowMeters();
    }
    
    /**
     * Inicializa os medidores de fluxo ao longo da rota
     */
    private void initializeFlowMeters() {
        for (int i = 0; i < 6; i++) {
            String meterId = String.format("FM_%02d", i + 1);
            double position = (double) i / 5.0; // Posições de 0.0 a 1.0
            flowMeters.add(new FlowMeter(meterId, position));
        }
        System.out.printf(" Inicializados %d medidores de fluxo\n", flowMeters.size());
    }
    
    /**
     * Calcula o tempo de simulação baseado no comprimento da rota e velocidade média
     */
    public void calculateSimulationTime() throws InterruptedException, ExecutionException {
        // Obter informações da rota do veículo
        CompletableFuture<String> routeIdFuture = sumoExecutor.submitCommand(new GetVehicleRouteIDCommand(vehicleId));
        String routeId = routeIdFuture.get();
        
        // Para simplificar, vamos estimar o comprimento da rota
        // Em uma implementação real, você obteria isso do SUMO
        this.routeLength = estimateRouteLength();
        
        // Calcular tempo: tempo = distância / velocidade
        this.simulationTime = (routeLength / averageSpeed) * 3600; // converter para segundos
        
        System.out.printf("Comprimento estimado da rota: %.2f km\n", routeLength);
        System.out.printf("Velocidade média: %.1f km/h\n", averageSpeed);
        System.out.printf("Tempo de simulação calculado: %.1f segundos\n", simulationTime);
    }
    
    /**
     * Estima o comprimento da rota baseado no número de edges
     */
    private double estimateRouteLength() {
        // Estimativa baseada no número de edges (29 edges do XML)
        // Assumindo comprimento médio de 1 km por edge
        return 29.0; // km
    }
    
    /**
     * Executa a coleta de dados da simulação SUMO
     */
    public void executeDataCollection() throws InterruptedException, ExecutionException {
        System.out.println("Iniciando coleta de dados da simulação SUMO...");
        System.out.printf("Veículo monitorado: %s\n", vehicleId);
        
        double startTime = getCurrentSimulationTime();
        double endTime = startTime + simulationTime;
        double currentTime = startTime;
        
        int readingCount = 0;
        
        while (currentTime < endTime && isVehicleInSimulation()) {
            try {
                // Coletar dados do SUMO a cada segundo
                SensorReading reading = collectSumoData(currentTime);
                if (reading != null) {
                    sensorReadings.add(reading);
                    readingCount++;
                    
                    // Log a cada 5 leituras
                    if (readingCount % 5 == 0) {
                        System.out.printf("Leitura %d: Tempo=%.1fs, Vel=%.1f km/h, Pos=(%.1f,%.1f), Dist=%.2f km\n",
                            readingCount, reading.timestamp, reading.realSpeed, 
                            reading.position.x, reading.position.y, reading.distance);
                    }
                }
                
                // Avançar simulação SUMO
                sumoExecutor.submitCommand(new DoTimestepCommand()).get();
                currentTime = getCurrentSimulationTime();
                
                // Pequena pausa para não sobrecarregar
                Thread.sleep(100);
                
            } catch (Exception e) {
                System.err.printf("Erro na coleta de dados no tempo %.1f: %s\n", currentTime, e.getMessage());
                break;
            }
        }
        
        System.out.printf("Coleta concluída: %d leituras coletadas\n", sensorReadings.size());
    }
    
    /**
     * Coleta dados do SUMO para um timestamp específico
     */
    private SensorReading collectSumoData(double timestamp) throws InterruptedException, ExecutionException {
        try {
            // Coletar dados em paralelo do SUMO
            CompletableFuture<Double> speedFuture = sumoExecutor.submitCommand(new GetVehicleSpeedCommand(vehicleId));
            CompletableFuture<SumoPosition2D> positionFuture = sumoExecutor.submitCommand(new GetVehiclePositionCommand(vehicleId));
            CompletableFuture<Double> distanceFuture = sumoExecutor.submitCommand(new GetVehicleDistanceCommand(vehicleId));
            CompletableFuture<Double> fuelFuture = sumoExecutor.submitCommand(new GetVehicleFuelConsumptionCommand(vehicleId));
            
            // Aguardar todos os resultados
            CompletableFuture.allOf(speedFuture, positionFuture, distanceFuture, fuelFuture).get();
            
            // Obter resultados
            double realSpeed = speedFuture.get() * 3.6; // converter m/s para km/h
            SumoPosition2D position = positionFuture.get();
            double distance = distanceFuture.get() / 1000.0; // converter m para km
            double fuelConsumption = fuelFuture.get();
            
            // Criar leitura do sensor
            SensorReading reading = new SensorReading(timestamp, vehicleId);
            reading.realSpeed = realSpeed;
            reading.position = position;
            reading.distance = distance;
            reading.fuelConsumption = fuelConsumption;
            
            // Calcular velocidade ótima baseada na tabela Fox 1.0
            reading.optimalSpeed = calculateOptimalSpeed(realSpeed);
            
            // Simular ruído do sensor (±2 km/h)
            double noise = (Math.random() - 0.5) * 4.0;
            reading.measuredSpeed = realSpeed + noise;
            reading.error = Math.abs(reading.measuredSpeed - realSpeed);
            
            return reading;
            
        } catch (Exception e) {
            System.err.printf(" Erro ao coletar dados do SUMO: %s\n", e.getMessage());
            return null;
        }
    }
    
    /**
     * Calcula velocidade ótima baseada na tabela Fox 1.0
     */
    private double calculateOptimalSpeed(double currentSpeed) {
        // Tabela de consumo Fox 1.0 (L/100km)
        if (currentSpeed <= 60) return 60;
        if (currentSpeed <= 80) return 75;
        if (currentSpeed <= 100) return 85;
        return 90; // Velocidade máxima recomendada
    }
    
    /**
     * Verifica se o veículo ainda está na simulação
     */
    private boolean isVehicleInSimulation() throws InterruptedException, ExecutionException {
        CompletableFuture<SumoStringList> idListFuture = sumoExecutor.submitCommand(new GetVehicleIDListCommand());
        SumoStringList idList = idListFuture.get();
        return idList != null && idList.contains(vehicleId);
    }
    
    /**
     * Obtém o tempo atual da simulação SUMO
     */
    private double getCurrentSimulationTime() throws InterruptedException, ExecutionException {
        CompletableFuture<Double> timeFuture = sumoExecutor.submitCommand(new GetTimeCommand());
        return timeFuture.get();
    }
    
    /**
     * Executa a reconciliação de dados
     */
    public void executeReconciliation() {
        System.out.println("🔄 Executando reconciliação de dados...");
        
        if (sensorReadings.isEmpty()) {
            System.out.println(" Nenhuma leitura disponível para reconciliação");
            return;
        }
        
        // Calcular estatísticas básicas
        calculateStatistics();
        
        // Reconciliar medidores de fluxo
        reconcileFlowMeters();
        
        System.out.println(" Reconciliação de dados concluída");
        printStatistics();
    }
    
    /**
     * Calcula estatísticas de reconciliação
     */
    private void calculateStatistics() {
        List<Double> measuredSpeeds = new ArrayList<>();
        List<Double> realSpeeds = new ArrayList<>();
        
        for (SensorReading reading : sensorReadings) {
            measuredSpeeds.add(reading.measuredSpeed);
            realSpeeds.add(reading.realSpeed);
        }
        
        // Média das velocidades medidas
        meanSpeed = measuredSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        
        // Desvio padrão
        double variance = measuredSpeeds.stream()
            .mapToDouble(speed -> Math.pow(speed - meanSpeed, 2))
            .average().orElse(0);
        standardDeviation = Math.sqrt(variance);
        
        // Polarização (bias)
        double realMean = realSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        bias = meanSpeed - realMean;
        
        // Precisão
        precision = 1.0 / (1.0 + standardDeviation / meanSpeed);
        
        // Incerteza
        uncertainty = Math.sqrt(Math.pow(standardDeviation, 2) + Math.pow(bias, 2));
    }
    
    /**
     * Reconcilia os medidores de fluxo
     */
    private void reconcileFlowMeters() {
        for (FlowMeter meter : flowMeters) {
            // Simular medição de fluxo baseada nas leituras próximas
            double totalFlow = 0;
            int count = 0;
            
            for (SensorReading reading : sensorReadings) {
                // Calcular posição relativa (0.0 a 1.0)
                double relativePosition = reading.distance / routeLength;
                
                // Se a leitura está próxima do medidor
                if (Math.abs(relativePosition - meter.position) <= 0.1) {
                    totalFlow += reading.realSpeed;
                    count++;
                }
            }
            
            if (count > 0) {
                meter.measuredFlow = totalFlow / count;
                // Aplicar reconciliação (simplificada)
                meter.reconciledFlow = meter.measuredFlow * (1.0 + bias / meanSpeed);
                meter.uncertainty = standardDeviation;
            }
        }
    }
    
    /**
     * Imprime estatísticas de reconciliação
     */
    private void printStatistics() {
        System.out.println("\n -----ESTATÍSTICAS DE RECONCILIAÇÃO------");
        System.out.printf("Velocidade média: %.2f km/h\n", meanSpeed);
        System.out.printf("Desvio padrão: %.2f km/h\n", standardDeviation);
        System.out.printf("Polarização (bias): %.2f km/h\n", bias);
        System.out.printf("Precisão: %.2f%%\n", precision * 100);
        System.out.printf("Incerteza: %.2f km/h\n", uncertainty);
        System.out.printf("Comprimento da rota: %.2f km\n", routeLength);
        System.out.printf("Tempo de simulação: %.1f segundos\n", simulationTime);
        System.out.printf("Total de leituras: %d\n", sensorReadings.size());
    }
    
    /**
     * Salva relatório de reconciliação
     */
    public void saveReconciliationReport(String outputPath) {
        ManipuladorCSV report = new ManipuladorCSV(outputPath);
        
        // Cabeçalho
        String[] header = {"Timestamp", "VehicleId", "RealSpeed", "MeasuredSpeed", "OptimalSpeed", 
                          "PositionX", "PositionY", "Distance", "FuelConsumption", "Error"};
        report.writeCSV(header);
        
        // Dados
        for (SensorReading reading : sensorReadings) {
            String[] data = {
                String.format("%.1f", reading.timestamp),
                reading.vehicleId,
                String.format("%.2f", reading.realSpeed),
                String.format("%.2f", reading.measuredSpeed),
                String.format("%.2f", reading.optimalSpeed),
                String.format("%.2f", reading.position.x),
                String.format("%.2f", reading.position.y),
                String.format("%.3f", reading.distance),
                String.format("%.4f", reading.fuelConsumption),
                String.format("%.2f", reading.error)
            };
            report.appendCSV(data);
        }
        
        System.out.printf(" Relatório salvo: %s\n", outputPath);
    }
    
    // Getters públicos para acesso aos dados
    public List<SensorReading> getSensorReadings() { return sensorReadings; }
    public List<FlowMeter> getFlowMeters() { return flowMeters; }
    public double getMeanSpeed() { return meanSpeed; }
    public double getStandardDeviation() { return standardDeviation; }
    public double getBias() { return bias; }
    public double getPrecision() { return precision; }
    public double getUncertainty() { return uncertainty; }
    public double getRouteLength() { return routeLength; }
    public double getSimulationTime() { return simulationTime; }
}

