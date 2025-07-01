package io.sim;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Classe responsável pela geração dos 3 gráficos específicos
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
            drawNoDataMessage(g2d, "Nenhum dado disponível para velocidades ótimas");
            saveImage(image, outputDirectory + "/1_velocidades_otimas.png");
            g2d.dispose();
            return;
        }
        
        // Preparar dados
        List<Double> timestamps = new ArrayList<>();
        List<Double> realSpeeds = new ArrayList<>();
        List<Double> optimalSpeeds = new ArrayList<>();
        
        for (DataReconciliation.SensorReading reading : readings) {
            timestamps.add(reading.getTimestamp());
            realSpeeds.add(reading.getRealSpeed());
            optimalSpeeds.add(reading.getOptimalSpeed());
        }
        
        // Encontrar valores mínimos e máximos
        double minTime = timestamps.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxTime = timestamps.stream().mapToDouble(Double::doubleValue).max().orElse(1);
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
        drawAxes(g2d, chartX, chartY, chartWidth, chartHeight, minTime, maxTime, minSpeed, maxSpeed,
                "Tempo (s)", "Velocidade (km/h)");
        
        // Desenhar linhas
        drawLine(g2d, chartX, chartY, chartWidth, chartHeight, timestamps, realSpeeds,
                minTime, maxTime, minSpeed, maxSpeed, REAL_SPEED_COLOR);
        
        drawLine(g2d, chartX, chartY, chartWidth, chartHeight, timestamps, optimalSpeeds,
                minTime, maxTime, minSpeed, maxSpeed, OPTIMAL_SPEED_COLOR);
        
        // Título e legenda
        drawTitle(g2d, "Velocidades Ótimas vs Velocidades Reais");
        drawLegend(g2d, new String[]{"Velocidade Real", "Velocidade Ótima"}, 
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
        
        // Dados das estatísticas
        String[] labels = {"Média", "Desvio Padrão", "Polarização", "Precisão", "Incerteza"};
        double[] values = {
            dataReconciliation.getMeanSpeed(),
            dataReconciliation.getStandardDeviation(),
            Math.abs(dataReconciliation.getBias()),
            dataReconciliation.getPrecision() * 100,
            dataReconciliation.getUncertainty()
        };
        Color[] colors = {
            new Color(51, 122, 183),   // Azul
            new Color(217, 83, 79),    // Vermelho
            new Color(92, 184, 92),    // Verde
            new Color(240, 173, 78),   // Laranja
            new Color(156, 39, 176)    // Roxo
        };
        
        // Encontrar valor máximo para escala
        double maxValue = Arrays.stream(values).max().orElse(100);
        maxValue = Math.ceil(maxValue / 10) * 10; // Arredondar para cima
        
        // Desenhar grade e eixos
        drawGrid(g2d, chartX, chartY, chartWidth, chartHeight);
        drawAxes(g2d, chartX, chartY, chartWidth, chartHeight, 0, labels.length, 0, maxValue,
                "Métricas de Reconciliação", "Valores");
        
        // Desenhar barras
        int barWidth = chartWidth / (labels.length + 1);
        for (int i = 0; i < labels.length; i++) {
            int barHeight = (int) ((values[i] / maxValue) * chartHeight);
            int barX = chartX + (i + 1) * barWidth - barWidth / 4;
            int barY = chartY + chartHeight - barHeight;
            
            g2d.setColor(colors[i]);
            g2d.fillRect(barX, barY, barWidth / 2, barHeight);
            
            // Valor no topo da barra
            g2d.setColor(AXIS_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String valueStr = String.format("%.2f", values[i]);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(valueStr, barX + barWidth / 4 - fm.stringWidth(valueStr) / 2, barY - 5);
            
            // Label da barra
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            fm = g2d.getFontMetrics();
            g2d.drawString(labels[i], barX + barWidth / 4 - fm.stringWidth(labels[i]) / 2, 
                          chartY + chartHeight + 20);
        }
        
        // Título
        drawTitle(g2d, "Estatísticas de Reconciliação de Dados");
        
        // Informações adicionais
        drawReconciliationInfo(g2d, dataReconciliation, CHART_WIDTH - 300, chartY + 100);
        
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
        
        // Obter dados
        List<DataReconciliation.SensorReading> readings = dataReconciliation.getSensorReadings();
        
        if (readings.isEmpty()) {
            drawNoDataMessage(g2d, "Nenhum dado disponível para sensores");
            saveImage(image, outputDirectory + "/3_medicao_sensores_velocidade.png");
            g2d.dispose();
            return;
        }
        
        // Preparar dados
        List<Double> timestamps = new ArrayList<>();
        List<Double> realSpeeds = new ArrayList<>();
        List<Double> measuredSpeeds = new ArrayList<>();
        
        for (DataReconciliation.SensorReading reading : readings) {
            timestamps.add(reading.getTimestamp());
            realSpeeds.add(reading.getRealSpeed());
            measuredSpeeds.add(reading.getMeasuredSpeed());
        }
        
        // Encontrar valores mínimos e máximos
        double minTime = timestamps.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxTime = timestamps.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double minSpeed = Math.min(
            realSpeeds.stream().mapToDouble(Double::doubleValue).min().orElse(0),
            measuredSpeeds.stream().mapToDouble(Double::doubleValue).min().orElse(0)
        ) - 5;
        double maxSpeed = Math.max(
            realSpeeds.stream().mapToDouble(Double::doubleValue).max().orElse(100),
            measuredSpeeds.stream().mapToDouble(Double::doubleValue).max().orElse(100)
        ) + 5;
        
        // Desenhar grade e eixos
        drawGrid(g2d, chartX, chartY, chartWidth, chartHeight);
        drawAxes(g2d, chartX, chartY, chartWidth, chartHeight, minTime, maxTime, minSpeed, maxSpeed,
                "Tempo (s) - Medições a cada 1 segundo", "Velocidade (km/h)");
        
        // Desenhar linhas
        drawLine(g2d, chartX, chartY, chartWidth, chartHeight, timestamps, realSpeeds,
                minTime, maxTime, minSpeed, maxSpeed, REAL_SPEED_COLOR);
        
        drawLine(g2d, chartX, chartY, chartWidth, chartHeight, timestamps, measuredSpeeds,
                minTime, maxTime, minSpeed, maxSpeed, MEASURED_SPEED_COLOR);
        
        // Desenhar pontos de medição
        drawMeasurementPoints(g2d, chartX, chartY, chartWidth, chartHeight, timestamps, measuredSpeeds,
                             minTime, maxTime, minSpeed, maxSpeed);
        
        // Título e legenda
        drawTitle(g2d, "Medições dos Sensores de Velocidade (1 medição por segundo)");
        String[] labels = {"Velocidade Real", "Velocidade Medida", "Pontos de Medição"};
        Color[] colors = {REAL_SPEED_COLOR, MEASURED_SPEED_COLOR, MEASURED_SPEED_COLOR};
        drawLegend(g2d, labels, colors, CHART_WIDTH - 250, chartY + 50);
        
        // Estatísticas dos sensores
        drawSensorStatistics(g2d, readings, CHART_WIDTH - 280, chartY + 150);
        
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
    
    private void drawMeasurementPoints(Graphics2D g2d, int chartX, int chartY, int chartWidth, int chartHeight,
                                     List<Double> timestamps, List<Double> measuredSpeeds,
                                     double minTime, double maxTime, double minSpeed, double maxSpeed) {
        g2d.setColor(MEASURED_SPEED_COLOR);
        
        for (int i = 0; i < timestamps.size(); i++) {
            double time = timestamps.get(i);
            double speed = measuredSpeeds.get(i);
            
            int x = chartX + (int) (((time - minTime) / (maxTime - minTime)) * chartWidth);
            int y = chartY + chartHeight - (int) (((speed - minSpeed) / (maxSpeed - minSpeed)) * chartHeight);
            
            g2d.fillOval(x - 3, y - 3, 6, 6);
            
            // Linha vertical a cada 5 segundos
            if (i % 5 == 0) {
                g2d.setColor(new Color(200, 200, 200, 100));
                g2d.drawLine(x, chartY, x, chartY + chartHeight);
                
                g2d.setColor(AXIS_COLOR);
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                g2d.drawString(String.format("%.0fs", time), x - 8, chartY - 5);
                g2d.setColor(MEASURED_SPEED_COLOR);
            }
        }
    }
    
    private void drawTitle(Graphics2D g2d, String title) {
        g2d.setColor(AXIS_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (CHART_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 35);
    }
    
    private void drawLegend(Graphics2D g2d, String[] labels, Color[] colors, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i < labels.length; i++) {
            g2d.setColor(colors[i]);
            g2d.drawString("■ " + labels[i], x, y + i * 20);
        }
    }
    
    private void drawOptimalSpeedStatistics(Graphics2D g2d, List<Double> realSpeeds, List<Double> optimalSpeeds, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(AXIS_COLOR);
        g2d.drawString("Estatísticas:", x, y);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        double avgReal = realSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgOptimal = optimalSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double fuelSavings = ((avgReal - avgOptimal) / avgReal) * 100;
        
        g2d.drawString(String.format("Vel. Real Média: %.2f km/h", avgReal), x, y + 20);
        g2d.drawString(String.format("Vel. Ótima Média: %.2f km/h", avgOptimal), x, y + 35);
        g2d.drawString(String.format("Economia Estimada: %.1f%%", Math.max(0, fuelSavings)), x, y + 50);
        g2d.drawString(String.format("Pontos Analisados: %d", realSpeeds.size()), x, y + 65);
    }
    
    private void drawReconciliationInfo(Graphics2D g2d, DataReconciliation dataReconciliation, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(AXIS_COLOR);
        g2d.drawString("Informações da Reconciliação:", x, y);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString(String.format("Medidores de Fluxo: %d", dataReconciliation.getFlowMeters().size()), x, y + 20);
        g2d.drawString(String.format("Leituras Coletadas: %d", dataReconciliation.getSensorReadings().size()), x, y + 35);
        g2d.drawString(String.format("Comprimento da Rota: %.1f km", dataReconciliation.getRouteLength()), x, y + 50);
        g2d.drawString(String.format("Tempo de Simulação: %.1f s", dataReconciliation.getSimulationTime()), x, y + 65);
    }
    
    private void drawSensorStatistics(Graphics2D g2d, List<DataReconciliation.SensorReading> readings, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(AXIS_COLOR);
        g2d.drawString("Estatísticas Temporais:", x, y);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        double avgError = readings.stream().mapToDouble(DataReconciliation.SensorReading::getError).average().orElse(0);
        double maxError = readings.stream().mapToDouble(DataReconciliation.SensorReading::getError).max().orElse(0);
        double minError = readings.stream().mapToDouble(DataReconciliation.SensorReading::getError).min().orElse(0);
        
        g2d.drawString(String.format("Medições: %d (1 por segundo)", readings.size()), x, y + 20);
        g2d.drawString(String.format("Erro Médio: %.2f km/h", avgError), x, y + 35);
        g2d.drawString(String.format("Erro Máximo: %.2f km/h", maxError), x, y + 50);
        g2d.drawString(String.format("Erro Mínimo: %.2f km/h", minError), x, y + 65);
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
            System.out.printf("Gráfico salvo: %s\n", path);
        } catch (IOException e) {
            System.err.printf("Erro ao salvar gráfico %s: %s\n", path, e.getMessage());
        }
    }
}

