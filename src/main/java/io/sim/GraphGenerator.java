package io.sim;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Classe responsável pela geração dos 3 gráficos específicos
 * Modificada para mostrar estatísticas por sensor baseadas nas 10 medições
 */
public class GraphGenerator {
    
    private static final int CHART_WIDTH = 1000;
    private static final int CHART_HEIGHT = 700;
    private static final int MARGIN = 80;
    
    // Cores dos gráficos
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color GRID_COLOR = new Color(240, 240, 240);
    private static final Color AXIS_COLOR = Color.BLACK;
    private static final Color OPTIMAL_SPEED_COLOR = new Color(92, 184, 92);    // Verde
    private static final Color REAL_SPEED_COLOR = new Color(51, 122, 183);      // Azul
    private static final Color MEASURED_SPEED_COLOR = new Color(217, 83, 79);   // Vermelho
    
    // Cores para diferentes sensores
    private static final Color[] SENSOR_COLORS = {
        new Color(51, 122, 183),   // Azul
        new Color(217, 83, 79),    // Vermelho
        new Color(92, 184, 92),    // Verde
        new Color(240, 173, 78),   // Laranja
        new Color(156, 39, 176),   // Roxo
        new Color(23, 162, 184),   // Ciano
        new Color(108, 117, 125),  // Cinza
        new Color(220, 53, 69),    // Vermelho escuro
        new Color(40, 167, 69),    // Verde escuro
        new Color(255, 193, 7)     // Amarelo
    };
    
    private String outputDirectory;
    
    public GraphGenerator(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        
        // Criar diretório se não existir
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Gera os 3 gráficos
     */
    public void generateAllGraphs(DataReconciliation dataReconciliation, RealTimeScheduler scheduler) {
        
        // 1. Gráfico de Velocidades Ótimas
        generateOptimalSpeedsChart(dataReconciliation);
        
        // 2. Gráfico de Estatísticas de Reconciliação
        generateReconciliationStatisticsChart(dataReconciliation);
        
        // 3. Gráfico de Medição dos Sensores de Velocidade
        generateSensorMeasurementsChart(dataReconciliation);
        
    }
    
    /**
     * 1. Gráfico de Velocidades Ótimas
     */
    private void generateOptimalSpeedsChart(DataReconciliation dataReconciliation) {
        BufferedImage image = new BufferedImage(CHART_WIDTH, CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        setupGraphics(g2d);
        
        // Área do gráfico
        int chartX = MARGIN;
        int chartY = MARGIN;
        int chartWidth = CHART_WIDTH - 2 * MARGIN;
        int chartHeight = CHART_HEIGHT - 2 * MARGIN;
        
        // Obter dados
        List<DataReconciliation.SensorReading> readings = dataReconciliation.getSensorReadings();
        
        if (readings.isEmpty()) {
            drawNoDataMessage(g2d, "Nenhum dado disponivel para velocidades otimas");
            saveImage(image, outputDirectory + "/1_velocidades_otimas.png");
            g2d.dispose();
            return;
        }
        
        // Preparar dados
        List<Double> distances = new ArrayList<>();
        List<Double> realSpeeds = new ArrayList<>();
        List<Double> optimalSpeeds = new ArrayList<>();
        
        // Usar distância como eixo X em vez de tempo
        for (DataReconciliation.SensorReading reading : readings) {
            // Pegar apenas a última execução para o gráfico de velocidades ótimas
            if (reading.getRunNumber() == 10) {
                distances.add(reading.getDistance());
                realSpeeds.add(reading.getRealSpeed());
                optimalSpeeds.add(reading.getOptimalSpeed());
            }
        }
        
        // Encontrar valores mínimos e máximos
        double minDist = distances.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxDist = distances.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double minSpeed = Math.min(
            realSpeeds.stream().mapToDouble(Double::doubleValue).min().orElse(0),
            optimalSpeeds.stream().mapToDouble(Double::doubleValue).min().orElse(0)
        ) - 5;
        double maxSpeed = Math.max(
            realSpeeds.stream().mapToDouble(Double::doubleValue).max().orElse(100),
            optimalSpeeds.stream().mapToDouble(Double::doubleValue).max().orElse(100)
        ) + 5;
        
        // Desenhar grade e eixos
        drawGrid(g2d, chartX, chartY, chartWidth, chartHeight);
        drawAxes(g2d, chartX, chartY, chartWidth, chartHeight, minDist, maxDist, minSpeed, maxSpeed,
                "Distancia (km)", "Velocidade (km/h)");
        
        // Desenhar linhas
        drawLine(g2d, chartX, chartY, chartWidth, chartHeight, distances, realSpeeds,
                minDist, maxDist, minSpeed, maxSpeed, REAL_SPEED_COLOR);
        
        drawLine(g2d, chartX, chartY, chartWidth, chartHeight, distances, optimalSpeeds,
                minDist, maxDist, minSpeed, maxSpeed, OPTIMAL_SPEED_COLOR);
        
        // Título e legenda
        drawTitle(g2d, "Velocidades Otimas vs Velocidades Reais");
        drawLegend(g2d, new String[]{"Velocidade Real", "Velocidade Otima"}, 
                  new Color[]{REAL_SPEED_COLOR, OPTIMAL_SPEED_COLOR}, CHART_WIDTH - 200, chartY + 50);
        
        // Estatísticas
        drawOptimalSpeedStatistics(g2d, realSpeeds, optimalSpeeds, CHART_WIDTH - 250, chartY + 150);
        
        g2d.dispose();
        saveImage(image, outputDirectory + "/1_velocidades_otimas.png");
    }
    
    /**
     * 2. Gráfico de Estatísticas de Reconciliação
     */
    private void generateReconciliationStatisticsChart(DataReconciliation dataReconciliation) {
        BufferedImage image = new BufferedImage(CHART_WIDTH, CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        setupGraphics(g2d);
        
        // Área do gráfico
        int chartX = MARGIN;
        int chartY = MARGIN;
        int chartWidth = CHART_WIDTH - 2 * MARGIN;
        int chartHeight = CHART_HEIGHT - 2 * MARGIN;
        
        // Obter sensores com leituras
        List<DataReconciliation.DistanceSensor> sensors = new ArrayList<>();
        for (DataReconciliation.DistanceSensor sensor : dataReconciliation.getDistanceSensors()) {
            if (sensor.getReadingCount() > 0) {
                sensors.add(sensor);
            }
        }
        
        if (sensors.isEmpty()) {
            drawNoDataMessage(g2d, "Nenhum sensor com leituras disponivel");
            saveImage(image, outputDirectory + "/2_estatisticas_reconciliacao.png");
            g2d.dispose();
            return;
        }
        
        // Preparar dados para o gráfico de barras agrupadas
        String[] sensorIds = sensors.stream().map(DataReconciliation.DistanceSensor::getId).toArray(String[]::new);
        double[] meanSpeeds = sensors.stream().mapToDouble(DataReconciliation.DistanceSensor::getMeanSpeed).toArray();
        double[] stdDevs = sensors.stream().mapToDouble(DataReconciliation.DistanceSensor::getStandardDeviation).toArray();
        double[] precisions = sensors.stream().mapToDouble(s -> s.getPrecision() * 100).toArray();
        
        // Encontrar valor máximo para escala
        double maxValue = Math.max(
            Arrays.stream(meanSpeeds).max().orElse(100),
            Math.max(
                Arrays.stream(stdDevs).max().orElse(20),
                Arrays.stream(precisions).max().orElse(100)
            )
        );
        maxValue = Math.ceil(maxValue / 10) * 10; // Arredondar para cima
        
        // Desenhar grade e eixos
        drawGrid(g2d, chartX, chartY, chartWidth, chartHeight);
        drawAxes(g2d, chartX, chartY, chartWidth, chartHeight, 0, sensorIds.length, 0, maxValue,
                "Sensores", "Valores");
        
        // Desenhar barras agrupadas
        int groupWidth = chartWidth / (sensorIds.length + 1);
        int barWidth = groupWidth / 4;
        
        for (int i = 0; i < sensorIds.length; i++) {
            int groupX = chartX + (i + 1) * groupWidth - groupWidth / 2;
            
            // Barra 1: Velocidade Média
            int barHeight1 = (int) ((meanSpeeds[i] / maxValue) * chartHeight);
            int barX1 = groupX - barWidth - 5;
            int barY1 = chartY + chartHeight - barHeight1;
            g2d.setColor(REAL_SPEED_COLOR);
            g2d.fillRect(barX1, barY1, barWidth, barHeight1);
            
            // Valor no topo da barra
            g2d.setColor(AXIS_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String valueStr1 = String.format("%.1f", meanSpeeds[i]);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(valueStr1, barX1 + barWidth/2 - fm.stringWidth(valueStr1)/2, barY1 - 5);
            
            // Barra 2: Desvio Padrão
            int barHeight2 = (int) ((stdDevs[i] / maxValue) * chartHeight);
            int barX2 = groupX;
            int barY2 = chartY + chartHeight - barHeight2;
            g2d.setColor(MEASURED_SPEED_COLOR);
            g2d.fillRect(barX2, barY2, barWidth, barHeight2);
            
            // Valor no topo da barra
            g2d.setColor(AXIS_COLOR);
            String valueStr2 = String.format("%.1f", stdDevs[i]);
            g2d.drawString(valueStr2, barX2 + barWidth/2 - fm.stringWidth(valueStr2)/2, barY2 - 5);
            
            // Barra 3: Precisão
            int barHeight3 = (int) ((precisions[i] / maxValue) * chartHeight);
            int barX3 = groupX + barWidth + 5;
            int barY3 = chartY + chartHeight - barHeight3;
            g2d.setColor(OPTIMAL_SPEED_COLOR);
            g2d.fillRect(barX3, barY3, barWidth, barHeight3);
            
            // Valor no topo da barra
            g2d.setColor(AXIS_COLOR);
            String valueStr3 = String.format("%.1f", precisions[i]);
            g2d.drawString(valueStr3, barX3 + barWidth/2 - fm.stringWidth(valueStr3)/2, barY3 - 5);
            
            // Label do sensor
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            fm = g2d.getFontMetrics();
            g2d.drawString(sensorIds[i], groupX - fm.stringWidth(sensorIds[i])/2, chartY + chartHeight + 20);
        }
        
        // Título
        drawTitle(g2d, "Estatisticas de Reconciliacao por Sensor (10 execucoes)");
        
        // Legenda
        drawLegend(g2d, new String[]{"Velocidade Media (km/h)", "Desvio Padrao (km/h)", "Precisao (%)"}, 
                  new Color[]{REAL_SPEED_COLOR, MEASURED_SPEED_COLOR, OPTIMAL_SPEED_COLOR}, 
                  CHART_WIDTH - 250, chartY + 50);
        
        // Informações adicionais
        drawSensorStatisticsInfo(g2d, sensors, CHART_WIDTH - 300, chartY + 150);
        
        g2d.dispose();
        saveImage(image, outputDirectory + "/2_estatisticas_reconciliacao.png");
    }
    
    /**
     * 3. Gráfico de Medição dos Sensores de Velocidade
     */
    private void generateSensorMeasurementsChart(DataReconciliation dataReconciliation) {
        BufferedImage image = new BufferedImage(CHART_WIDTH, CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        setupGraphics(g2d);
        
        // Área do gráfico
        int chartX = MARGIN;
        int chartY = MARGIN;
        int chartWidth = CHART_WIDTH - 2 * MARGIN;
        int chartHeight = CHART_HEIGHT - 2 * MARGIN;
        
        // Obter sensores com leituras
        List<DataReconciliation.DistanceSensor> sensors = new ArrayList<>();
        for (DataReconciliation.DistanceSensor sensor : dataReconciliation.getDistanceSensors()) {
            if (sensor.getReadingCount() > 0) {
                sensors.add(sensor);
            }
        }
        
        if (sensors.isEmpty()) {
            drawNoDataMessage(g2d, "Nenhum sensor com leituras disponivel");
            saveImage(image, outputDirectory + "/3_medicao_sensores_velocidade.png");
            g2d.dispose();
            return;
        }
        
        // Preparar dados para o gráfico de dispersão
        Map<String, List<Double>> sensorRuns = new HashMap<>();
        Map<String, List<Double>> sensorSpeeds = new HashMap<>();
        
        // Inicializar listas para cada sensor
        for (DataReconciliation.DistanceSensor sensor : sensors) {
            sensorRuns.put(sensor.getId(), new ArrayList<>());
            sensorSpeeds.put(sensor.getId(), new ArrayList<>());
        }
        
        // Preencher dados
        for (DataReconciliation.DistanceSensor sensor : sensors) {
            for (DataReconciliation.SensorReading reading : sensor.getReadings()) {
                sensorRuns.get(sensor.getId()).add((double) reading.getRunNumber());
                sensorSpeeds.get(sensor.getId()).add(reading.getMeasuredSpeed());
            }
        }
        
        // Encontrar valores mínimos e máximos
        double minRun = 1;
        double maxRun = 10;
        double minSpeed = sensorSpeeds.values().stream()
            .flatMap(List::stream)
            .mapToDouble(Double::doubleValue)
            .min().orElse(0) - 5;
        double maxSpeed = sensorSpeeds.values().stream()
            .flatMap(List::stream)
            .mapToDouble(Double::doubleValue)
            .max().orElse(100) + 5;
        
        // Desenhar grade e eixos
        drawGrid(g2d, chartX, chartY, chartWidth, chartHeight);
        drawAxes(g2d, chartX, chartY, chartWidth, chartHeight, minRun, maxRun, minSpeed, maxSpeed,
                "Execucao", "Velocidade (km/h)");
        
        // Desenhar pontos para cada sensor
        int sensorIndex = 0;
        for (DataReconciliation.DistanceSensor sensor : sensors) {
            Color sensorColor = SENSOR_COLORS[sensorIndex % SENSOR_COLORS.length];
            
            List<Double> runs = sensorRuns.get(sensor.getId());
            List<Double> speeds = sensorSpeeds.get(sensor.getId());
            
            for (int i = 0; i < Math.min(runs.size(), speeds.size()); i++) {
                double run = runs.get(i);
                double speed = speeds.get(i);
                
                int x = chartX + (int) (((run - minRun) / (maxRun - minRun)) * chartWidth);
                int y = chartY + chartHeight - (int) (((speed - minSpeed) / (maxSpeed - minSpeed)) * chartHeight);
                
                g2d.setColor(sensorColor);
                g2d.fillOval(x - 4, y - 4, 8, 8);
                
                // Desenhar linha vertical para cada execução
                if (i == 0 || runs.get(i) != runs.get(i-1)) {
                    g2d.setColor(new Color(200, 200, 200, 100));
                    g2d.drawLine(x, chartY, x, chartY + chartHeight);
                    
                    g2d.setColor(AXIS_COLOR);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    g2d.drawString(String.format("%.0f", run), x - 3, chartY - 5);
                }
            }
            
            sensorIndex++;
        }
        
        // Título e legenda
        drawTitle(g2d, "Medicoes dos Sensores de Velocidade (10 execucoes)");
        
        // Criar arrays para legenda
        String[] sensorLabels = new String[sensors.size()];
        Color[] sensorColorArray = new Color[sensors.size()];
        
        for (int i = 0; i < sensors.size(); i++) {
            sensorLabels[i] = sensors.get(i).getId() + " (" + sensors.get(i).getDistancePosition() + " km)";
            sensorColorArray[i] = SENSOR_COLORS[i % SENSOR_COLORS.length];
        }
        
        drawLegend(g2d, sensorLabels, sensorColorArray, CHART_WIDTH - 250, chartY + 50);
        
        // Estatísticas dos sensores
        drawSensorMeasurementStatistics(g2d, sensors, CHART_WIDTH - 280, chartY + 150);
        
        g2d.dispose();
        saveImage(image, outputDirectory + "/3_medicao_sensores_velocidade.png");
    }
    
    // Métodos auxiliares para desenho
    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, CHART_WIDTH, CHART_HEIGHT);
    }
    
    private void drawGrid(Graphics2D g2d, int chartX, int chartY, int chartWidth, int chartHeight) {
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1));
        
        for (int i = 0; i <= 10; i++) {
            int x = chartX + (i * chartWidth / 10);
            g2d.drawLine(x, chartY, x, chartY + chartHeight);
            
            int y = chartY + (i * chartHeight / 10);
            g2d.drawLine(chartX, y, chartX + chartWidth, y);
        }
    }
    
    private void drawAxes(Graphics2D g2d, int chartX, int chartY, int chartWidth, int chartHeight,
                         double minX, double maxX, double minY, double maxY, String xLabel, String yLabel) {
        g2d.setColor(AXIS_COLOR);
        g2d.setStroke(new BasicStroke(2));
        
        g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);
        g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight);
        
        // Labels dos eixos
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        
        int xLabelX = chartX + (chartWidth - fm.stringWidth(xLabel)) / 2;
        g2d.drawString(xLabel, xLabelX, chartY + chartHeight + 50);
        
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.rotate(-Math.PI / 2);
        int yLabelY = chartY + (chartHeight + fm.stringWidth(yLabel)) / 2;
        g2dRotated.drawString(yLabel, -yLabelY, 25);
        g2dRotated.dispose();
    }
    
    private void drawLine(Graphics2D g2d, int chartX, int chartY, int chartWidth, int chartHeight,
                         List<Double> xData, List<Double> yData, double minX, double maxX, 
                         double minY, double maxY, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        
        if (xData.size() < 2 || yData.size() < 2) return;
        
        for (int i = 0; i < Math.min(xData.size(), yData.size()) - 1; i++) {
            int x1 = chartX + (int) (((xData.get(i) - minX) / (maxX - minX)) * chartWidth);
            int y1 = chartY + chartHeight - (int) (((yData.get(i) - minY) / (maxY - minY)) * chartHeight);
            int x2 = chartX + (int) (((xData.get(i + 1) - minX) / (maxX - minX)) * chartWidth);
            int y2 = chartY + chartHeight - (int) (((yData.get(i + 1) - minY) / (maxY - minY)) * chartHeight);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
    
    private void drawTitle(Graphics2D g2d, String title) {
        g2d.setColor(AXIS_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (CHART_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 30);
    }
    
    private void drawLegend(Graphics2D g2d, String[] labels, Color[] colors, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        for (int i = 0; i < labels.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fillRect(x, y + i * 20, 15, 15);
            
            g2d.setColor(AXIS_COLOR);
            g2d.drawString(labels[i], x + 20, y + i * 20 + 12);
        }
    }
    
    private void drawOptimalSpeedStatistics(Graphics2D g2d, List<Double> realSpeeds, List<Double> optimalSpeeds, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(AXIS_COLOR);
        g2d.drawString("Estatisticas de Velocidade:", x, y);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        double realAvg = realSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double optimalAvg = optimalSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double fuelSaving = (realAvg - optimalAvg) / realAvg * 100;
        
        g2d.drawString(String.format("Velocidade Real Media: %.2f km/h", realAvg), x, y + 20);
        g2d.drawString(String.format("Velocidade Otima Media: %.2f km/h", optimalAvg), x, y + 35);
        g2d.drawString(String.format("Economia Estimada: %.2f%%", fuelSaving), x, y + 50);
        g2d.drawString(String.format("Total de Pontos: %d", realSpeeds.size()), x, y + 65);
    }
    
    private void drawSensorStatisticsInfo(Graphics2D g2d, List<DataReconciliation.DistanceSensor> sensors, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(AXIS_COLOR);
        g2d.drawString("Informacoes dos Sensores:", x, y);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        int totalReadings = sensors.stream().mapToInt(DataReconciliation.DistanceSensor::getReadingCount).sum();
        double avgPrecision = sensors.stream().mapToDouble(s -> s.getPrecision() * 100).average().orElse(0);
        double avgError = sensors.stream().mapToDouble(DataReconciliation.DistanceSensor::getMeanError).average().orElse(0);
        
        g2d.drawString(String.format("Total de Sensores: %d", sensors.size()), x, y + 20);
        g2d.drawString(String.format("Total de Leituras: %d", totalReadings), x, y + 35);
        g2d.drawString(String.format("Precisao Media: %.2f%%", avgPrecision), x, y + 50);
        g2d.drawString(String.format("Erro Medio: %.2f km/h", avgError), x, y + 65);
        g2d.drawString("Baseado em 10 execucoes por sensor", x, y + 80);
    }
    
    private void drawSensorMeasurementStatistics(Graphics2D g2d, List<DataReconciliation.DistanceSensor> sensors, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(AXIS_COLOR);
        g2d.drawString("Estatisticas por Sensor:", x, y);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        int row = 0;
        for (DataReconciliation.DistanceSensor sensor : sensors) {
            if (row >= 5) {
                // Continuar na próxima coluna
                x += 150;
                row = 0;
            }
            
            g2d.drawString(String.format("%s: %.1f km/h, %d leituras", 
                sensor.getId(), sensor.getMeanSpeed(), sensor.getReadingCount()), 
                x, y + 20 + row * 15);
            
            row++;
        }
    }
    
    private void drawNoDataMessage(Graphics2D g2d, String message) {
        g2d.setColor(AXIS_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (CHART_WIDTH - fm.stringWidth(message)) / 2;
        int y = CHART_HEIGHT / 2;
        g2d.drawString(message, x, y);
    }
    
    private void saveImage(BufferedImage image, String path) {
        try {
            ImageIO.write(image, "PNG", new File(path));
            System.out.printf("Grafico salvo: %s\n", path);
        } catch (IOException e) {
            System.err.printf("Erro ao salvar grafico %s: %s\n", path, e.getMessage());
        }
    }
}

