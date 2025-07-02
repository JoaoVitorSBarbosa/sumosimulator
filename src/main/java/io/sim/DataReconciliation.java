package io.sim;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.sim.sumo.SumoCommandExecutor;
import io.sim.sumo.cmd.*;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;

/**
 * Classe responsável pela Reconciliação de Dados (AV2 - Parte I)
 * Integrada com SUMO para obter dados reais de velocidade e posição
 * Modificada para suportar múltiplos veículos em uma única simulação
 */
public class DataReconciliation {
    
    private SumoCommandExecutor sumoExecutor;
    private String baseVehicleId; // ID base do veículo (ex: "0")
    private List<String> vehicleIds; // Lista de IDs de veículos (ex: "0_run1", "0_run2", etc.)
    private List<SensorReading> sensorReadings;
    private List<DistanceSensor> distanceSensors;
    private double routeLength;
    private double averageSpeed = 80.0; // km/h
    private double simulationTime;
    private int totalRuns;
    private Map<String, Integer> vehicleRunMap; // Mapa de ID do veículo para número da execução
    
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
        public int runNumber; // Número da execução
        public double realSpeed;
        public double measuredSpeed;
        public double optimalSpeed;
        public SumoPosition2D position;
        public double distance;
        public double fuelConsumption;
        public double error;
        public String sensorId; // ID do sensor que fez a leitura
        
        public SensorReading(double timestamp, String vehicleId, int runNumber, String sensorId) {
            this.timestamp = timestamp;
            this.vehicleId = vehicleId;
            this.runNumber = runNumber;
            this.sensorId = sensorId;
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
        public String getSensorId() { return sensorId; }
        public int getRunNumber() { return runNumber; }
    }
    
    /**
     * Classe interna para sensores baseados em distância
     */
    public static class DistanceSensor {
        public String id;
        public double distancePosition; // Posição em km na rota
        public List<SensorReading> readings; // Leituras deste sensor
        
        // Estatísticas específicas deste sensor
        public double meanSpeed;
        public double standardDeviation;
        public double bias;
        public double precision;
        public double uncertainty;
        public double meanError;
        
        public DistanceSensor(String id, double distancePosition) {
            this.id = id;
            this.distancePosition = distancePosition;
            this.readings = new ArrayList<>();
        }
        
        /**
         * Calcula estatísticas para este sensor baseadas em todas as leituras
         */
        public void calculateStatistics() {
            if (readings.isEmpty()) return;
            
            // Calcular média das velocidades medidas
            meanSpeed = readings.stream()
                .mapToDouble(SensorReading::getMeasuredSpeed)
                .average()
                .orElse(0);
            
            // Calcular desvio padrão
            standardDeviation = Math.sqrt(
                readings.stream()
                    .mapToDouble(r -> Math.pow(r.getMeasuredSpeed() - meanSpeed, 2))
                    .average()
                    .orElse(0)
            );
            
            // Calcular polarização (bias)
            double realMean = readings.stream()
                .mapToDouble(SensorReading::getRealSpeed)
                .average()
                .orElse(0);
            bias = meanSpeed - realMean;
            
            // Calcular precisão
            precision = 1.0 / (1.0 + standardDeviation / meanSpeed);
            
            // Calcular incerteza
            uncertainty = Math.sqrt(Math.pow(standardDeviation, 2) + Math.pow(bias, 2));
            
            // Calcular erro médio
            meanError = readings.stream()
                .mapToDouble(SensorReading::getError)
                .average()
                .orElse(0);
        }
        
        // Getters públicos
        public String getId() { return id; }
        public double getDistancePosition() { return distancePosition; }
        public List<SensorReading> getReadings() { return readings; }
        public double getMeanSpeed() { return meanSpeed; }
        public double getStandardDeviation() { return standardDeviation; }
        public double getBias() { return bias; }
        public double getPrecision() { return precision; }
        public double getUncertainty() { return uncertainty; }
        public double getMeanError() { return meanError; }
        public int getReadingCount() { return readings.size(); }
    }
    
    /**
     * Construtor para múltiplos veículos em uma única simulação
     * @param sumoExecutor Executor de comandos SUMO
     * @param baseVehicleId ID base do veículo (ex: "0")
     * @param totalRuns Número total de execuções
     */
    public DataReconciliation(SumoCommandExecutor sumoExecutor, String baseVehicleId, int totalRuns) {
        this.sumoExecutor = sumoExecutor;
        this.baseVehicleId = baseVehicleId;
        this.totalRuns = totalRuns;
        this.sensorReadings = new ArrayList<>();
        this.distanceSensors = new ArrayList<>();
        this.vehicleIds = new ArrayList<>();
        this.vehicleRunMap = new HashMap<>();
        
        // Gerar IDs de veículos para todas as execuções
        for (int run = 1; run <= totalRuns; run++) {
            String vehicleId = baseVehicleId + "_run" + run;
            vehicleIds.add(vehicleId);
            vehicleRunMap.put(vehicleId, run);
        }
        
        System.out.printf("Inicializado sistema para %d execucoes com veiculos: %s\n", 
                         totalRuns, String.join(", ", vehicleIds));
    }
    
    /**
     * Inicializa os sensores distribuídos a cada 5km ao longo da rota
     */
    public void initializeDistanceSensors() {
        // Limpar sensores existentes
        distanceSensors.clear();
        
        // Calcular número de sensores baseado no comprimento da rota
        int numSensors = (int) Math.ceil(routeLength / 5.0);
        
        // Criar sensores a cada 5km
        for (int i = 0; i < numSensors; i++) {
            String sensorId = String.format("S%02d", i + 1);
            double position = i * 5.0; // Posição em km (0, 5, 10, 15, ...)
            distanceSensors.add(new DistanceSensor(sensorId, position));
        }
        
        System.out.printf("Inicializados %d sensores a cada 5km ao longo da rota de %.1f km\n", 
                         distanceSensors.size(), routeLength);
        
        // Listar sensores
        for (DistanceSensor sensor : distanceSensors) {
            System.out.printf("Sensor %s: posicao %.1f km\n", sensor.getId(), sensor.getDistancePosition());
        }
    }
    
    /**
     * Calcula o tempo de simulação baseado no comprimento da rota e velocidade média
     */
    public void calculateSimulationTime() throws InterruptedException, ExecutionException {
        // Obter informações da rota do primeiro veículo disponível
        String firstVehicleId = findFirstAvailableVehicle();
        if (firstVehicleId == null) {
            throw new RuntimeException("Nenhum veiculo disponivel na simulacao");
        }
        
        CompletableFuture<String> routeIdFuture = sumoExecutor.submitCommand(new GetVehicleRouteIDCommand(firstVehicleId));
        String routeId = routeIdFuture.get();
        
        // Para simplificar, vamos estimar o comprimento da rota
        // Em uma implementação real, você obteria isso do SUMO
        this.routeLength = estimateRouteLength();
        
        // Calcular tempo: tempo = distância / velocidade
        this.simulationTime = (routeLength / averageSpeed) * 3600; // converter para segundos
        
        System.out.printf("Comprimento estimado da rota: %.2f km\n", routeLength);
        System.out.printf("Velocidade media: %.1f km/h\n", averageSpeed);
        System.out.printf("Tempo de simulacao calculado: %.1f segundos\n", simulationTime);
        
        // Inicializar sensores baseados em distância
        initializeDistanceSensors();
    }
    
    /**
     * Encontra o primeiro veículo disponível na simulação
     */
    private String findFirstAvailableVehicle() throws InterruptedException, ExecutionException {
        CompletableFuture<SumoStringList> idListFuture = sumoExecutor.submitCommand(new GetVehicleIDListCommand());
        SumoStringList idList = idListFuture.get();
        
        if (idList == null || idList.size() == 0) {
            return null;
        }
        
        // Verificar se algum dos nossos veículos está na simulação
        for (String vehicleId : vehicleIds) {
            if (idList.contains(vehicleId)) {
                return vehicleId;
            }
        }
        
        return null;
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
     * Executa a coleta de dados para todos os veículos em uma única simulação
     */
    public void executeDataCollection() throws InterruptedException, ExecutionException {
        System.out.println("Iniciando coleta de dados para todos os veiculos em uma unica simulacao...");
        
        double startTime = getCurrentSimulationTime();
        double endTime = startTime + (simulationTime * totalRuns) + 1000; // Adicionar margem de segurança
        double currentTime = startTime;
        
        int totalReadingCount = 0;
        Set<String> completedVehicles = new HashSet<>();
        
        while (currentTime < endTime && completedVehicles.size() < totalRuns) {
            try {
                // Obter lista de veículos atualmente na simulação
                CompletableFuture<SumoStringList> idListFuture = sumoExecutor.submitCommand(new GetVehicleIDListCommand());
                SumoStringList idList = idListFuture.get();
                
                // Processar cada veículo da nossa lista que está na simulação
                for (String vehicleId : vehicleIds) {
                    if (idList.contains(vehicleId) && !completedVehicles.contains(vehicleId)) {
                        // Obter número da execução para este veículo
                        int runNumber = vehicleRunMap.get(vehicleId);
                        
                        // Coletar dados deste veículo
                        SensorReading reading = collectVehicleData(vehicleId, currentTime, runNumber);
                        if (reading != null) {
                            // Verificar se o veículo está próximo de algum sensor de distância
                            DistanceSensor nearestSensor = findNearestSensor(reading.distance);
                            
                            if (nearestSensor != null) {
                                // Atualizar ID do sensor na leitura
                                reading.sensorId = nearestSensor.id;
                                
                                // Adicionar leitura à lista geral
                                sensorReadings.add(reading);
                                
                                // Adicionar leitura ao sensor específico
                                nearestSensor.readings.add(reading);
                                
                                totalReadingCount++;
                                
                                // Log a cada 200 leituras para não poluir a saída
                                if (totalReadingCount % 200 == 0) {
                                    System.out.printf("Leitura %d: Veiculo=%s, Run=%d, Tempo=%.0fs, Vel=%.1f km/h, Dist=%.2f km, Sensor=%s\n",
                                        totalReadingCount, vehicleId, runNumber, reading.timestamp, reading.realSpeed, 
                                        reading.distance, reading.sensorId);
                                }
                            }
                        }
                    } else if (!idList.contains(vehicleId) && vehicleIds.contains(vehicleId) && !completedVehicles.contains(vehicleId)) {
                        // Veículo já apareceu na simulação mas não está mais presente
                        // Isso significa que ele completou sua rota
                        int runNumber = vehicleRunMap.get(vehicleId);
                        System.out.printf("Veiculo %s (execucao %d) completou sua rota\n", vehicleId, runNumber);
                        completedVehicles.add(vehicleId);
                    }
                }
                
                // Avançar simulação SUMO
                sumoExecutor.submitCommand(new DoTimestepCommand()).get();
                currentTime = getCurrentSimulationTime();
                
                // Pequena pausa para não sobrecarregar
                Thread.sleep(10);
                
            } catch (Exception e) {
                System.err.printf("Erro na coleta de dados no tempo %.1f: %s\n", currentTime, e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.printf("Coleta concluida: %d leituras coletadas de %d veiculos\n", 
                         totalReadingCount, completedVehicles.size());
    }
    
    /**
     * Encontra o sensor mais próximo da distância atual
     * Um sensor é considerado "próximo" se o veículo estiver a no máximo 0.5km de distância
     */
    private DistanceSensor findNearestSensor(double currentDistance) {
        for (DistanceSensor sensor : distanceSensors) {
            if (Math.abs(currentDistance - sensor.distancePosition) <= 0.5) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * Coleta dados de um veículo específico
     */
    private SensorReading collectVehicleData(String vehicleId, double timestamp, int runNumber) throws InterruptedException, ExecutionException {
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
            
            // Criar leitura do sensor (sem ID de sensor ainda)
            SensorReading reading = new SensorReading(timestamp, vehicleId, runNumber, "");
            reading.realSpeed = realSpeed;
            reading.position = position;
            reading.distance = distance;
            reading.fuelConsumption = fuelConsumption;
            
            // Simular ruído do sensor (±2 km/h)
            Random random = new Random();
            double noise = (random.nextGaussian() * 2.0);
            reading.measuredSpeed = Math.max(0, realSpeed + noise);
            
            // Calcular velocidade ótima baseada na tabela de consumo
            reading.optimalSpeed = calculateOptimalSpeed(realSpeed);
            
            // Calcular erro
            reading.error = Math.abs(reading.measuredSpeed - reading.realSpeed);
            
            return reading;
            
        } catch (Exception e) {
            // Ignorar erros comuns quando o veículo não está mais na simulação
            if (!e.getMessage().contains("not known")) {
                System.err.printf("Erro ao coletar dados do veiculo %s no tempo %.1f: %s\n", 
                                 vehicleId, timestamp, e.getMessage());
            }
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
        System.out.println("Executando reconciliacao de dados...");
        
        if (sensorReadings.isEmpty()) {
            System.out.println("Nenhuma leitura disponivel para reconciliacao");
            return;
        }
        
        // Calcular estatísticas para cada sensor
        for (DistanceSensor sensor : distanceSensors) {
            sensor.calculateStatistics();
        }
        
        // Calcular estatísticas globais
        calculateGlobalStatistics();
        
        System.out.println("Reconciliacao de dados concluida");
        printStatistics();
    }
    
    /**
     * Calcula estatísticas globais de reconciliação
     */
    private void calculateGlobalStatistics() {
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
     * Imprime estatísticas de reconciliação
     */
    private void printStatistics() {
        System.out.println("\n-----ESTATISTICAS GLOBAIS DE RECONCILIACAO------");
        System.out.printf("Velocidade media: %.2f km/h\n", meanSpeed);
        System.out.printf("Desvio padrao: %.2f km/h\n", standardDeviation);
        System.out.printf("Polarizacao (bias): %.2f km/h\n", bias);
        System.out.printf("Precisao: %.2f%%\n", precision * 100);
        System.out.printf("Incerteza: %.2f km/h\n", uncertainty);
        System.out.printf("Comprimento da rota: %.2f km\n", routeLength);
        System.out.printf("Tempo de simulacao: %.1f segundos\n", simulationTime);
        System.out.printf("Total de leituras: %d\n", sensorReadings.size());
        
        System.out.println("\n-----ESTATISTICAS POR SENSOR------");
        for (DistanceSensor sensor : distanceSensors) {
            if (sensor.getReadingCount() > 0) {
                System.out.printf("\nSensor %s (posicao %.1f km):\n", sensor.getId(), sensor.getDistancePosition());
                System.out.printf("  Leituras: %d\n", sensor.getReadingCount());
                System.out.printf("  Velocidade media: %.2f km/h\n", sensor.getMeanSpeed());
                System.out.printf("  Desvio padrao: %.2f km/h\n", sensor.getStandardDeviation());
                System.out.printf("  Polarizacao (bias): %.2f km/h\n", sensor.getBias());
                System.out.printf("  Precisao: %.2f%%\n", sensor.getPrecision() * 100);
                System.out.printf("  Incerteza: %.2f km/h\n", sensor.getUncertainty());
                System.out.printf("  Erro medio: %.2f km/h\n", sensor.getMeanError());
            }
        }
    }
    
    /**
     * Salva relatório de reconciliação
     */
    public void saveReconciliationReport(String outputPath) {
        ManipuladorCSV report = new ManipuladorCSV(outputPath);
        
        // Cabeçalho
        String[] header = {"RunNumber", "SensorId", "Timestamp", "VehicleId", "RealSpeed", "MeasuredSpeed", "OptimalSpeed", 
                          "PositionX", "PositionY", "Distance", "FuelConsumption", "Error"};
        report.writeCSV(header);
        
        // Dados
        for (SensorReading reading : sensorReadings) {
            String[] data = {
                String.valueOf(reading.runNumber),
                reading.sensorId,
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
        
        // Salvar relatório de estatísticas por sensor
        saveSensorStatisticsReport(outputPath.replace(".csv", "_sensor_stats.csv"));
        
        System.out.printf("Relatorio salvo: %s\n", outputPath);
    }
    
    /**
     * Salva relatório de estatísticas por sensor
     */
    private void saveSensorStatisticsReport(String outputPath) {
        ManipuladorCSV report = new ManipuladorCSV(outputPath);
        
        // Cabeçalho
        String[] header = {"SensorId", "Position", "ReadingCount", "MeanSpeed", "StandardDeviation", 
                          "Bias", "Precision", "Uncertainty", "MeanError"};
        report.writeCSV(header);
        
        // Dados
        for (DistanceSensor sensor : distanceSensors) {
            if (sensor.getReadingCount() > 0) {
                String[] data = {
                    sensor.getId(),
                    String.format("%.1f", sensor.getDistancePosition()),
                    String.valueOf(sensor.getReadingCount()),
                    String.format("%.2f", sensor.getMeanSpeed()),
                    String.format("%.2f", sensor.getStandardDeviation()),
                    String.format("%.2f", sensor.getBias()),
                    String.format("%.2f", sensor.getPrecision() * 100),
                    String.format("%.2f", sensor.getUncertainty()),
                    String.format("%.2f", sensor.getMeanError())
                };
                report.appendCSV(data);
            }
        }
        
        System.out.printf("Relatorio de estatisticas por sensor salvo: %s\n", outputPath);
    }
    
    /**
     * Extrai o número da execução do ID do veículo
     */
    private int extractRunNumber(String vehicleId) {
        Pattern pattern = Pattern.compile("_run(\\d+)$");
        Matcher matcher = pattern.matcher(vehicleId);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1; // Default para veículos sem número de execução
    }
    
    // Getters públicos para acesso aos dados
    public List<SensorReading> getSensorReadings() { return sensorReadings; }
    public List<DistanceSensor> getDistanceSensors() { return distanceSensors; }
    public double getMeanSpeed() { return meanSpeed; }
    public double getStandardDeviation() { return standardDeviation; }
    public double getBias() { return bias; }
    public double getPrecision() { return precision; }
    public double getUncertainty() { return uncertainty; }
    public double getRouteLength() { return routeLength; }
    public double getSimulationTime() { return simulationTime; }
    public int getTotalRuns() { return totalRuns; }
    public List<String> getVehicleIds() { return vehicleIds; }
}

