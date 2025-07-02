package io.sim;

import it.polito.appeal.traci.SumoTraciConnection;
import io.sim.sumo.SumoCommandExecutor;
import io.sim.sumo.cmd.*;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStringList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe principal do sistema AV2 integrado com SUMO
 * Combina Reconciliação de Dados (Parte I) e Escalonamento de Tempo Real (Parte II)
 * Modificada para executar 10 vezes a mesma rota em uma única simulação
 */
public class AV2Main extends Thread{

    private SumoTraciConnection sumo;
    private SumoCommandExecutor sumoExecutor;
    private DataReconciliation dataReconciliation;
    private RealTimeScheduler realTimeScheduler;
    private GraphGenerator graphGenerator;
    private Company company;
    private AlphaBank banco;
    private Rota rota;
    private Car auto;

    private static final String BASE_VEHICLE_ID = "0";
    private static final String OUTPUT_DIR = "reports/av2";
    private static final int TOTAL_RUNS = 10;
    private static final String XML_FILE = "data/dados.xml";
    private SumoColor green;

    public static void main(String[] args) {
        AV2Main av2System = new AV2Main();
        av2System.run();
    }

    public void run() {
        green = new SumoColor(0, 255, 0, 126);
            try {
                // Inicializar SUMO e componentes
                initializeSumo();
                initializeComponents();
                
                sleep(500);
                
                // Executar Reconciliação de Dados (Parte I) com 10 veículos em uma única simulação
                executeDataReconciliation();

                
                // Executar Escalonamento de Tempo Real (Parte II)
                executeRealTimeScheduling();

                // Gerar gráficos
                generateGraphs();

                // Gerar relatórios finais
                generateFinalReports();

                // Printar o sumário final
                printFinalSummary();
            } catch (Exception e) {
                System.err.println("Erro no sistema AV2: " + e.getMessage());
                e.printStackTrace();
            } finally {
                closeSim();
            }
    }

    /**
     * Inicializa conexão com SUMO usando o arquivo XML com 10 veículos
     */
    private void initializeSumo() throws IOException {
        String sumo_bin = "sumo-gui";
        String config_file = "map/map.sumo.cfg";

        // Configurar conexão SUMO
        sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1");
        sumo.addOption("quit-on-end", "1");
        
        // Usar o arquivo XML com 10 veículos
        sumo.addOption("route-files", XML_FILE);

        // Iniciar servidor SUMO
        sumo.runServer(12345);

        // Criar executor de comandos
        sumoExecutor = new SumoCommandExecutor(sumo);
        sumoExecutor.start();

        System.out.println("SUMO inicializado e conectado com arquivo de rotas: " + XML_FILE);
    }

    /**
     * Inicializa componentes do sistema
     */
    private void initializeComponents() {
        // Criar diretório de saída
        java.io.File outputDir = new java.io.File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Inicializar componentes com suporte para múltiplos veículos
        dataReconciliation = new DataReconciliation(sumoExecutor, BASE_VEHICLE_ID, TOTAL_RUNS);
        realTimeScheduler = new RealTimeScheduler(12);
        graphGenerator = new GraphGenerator(OUTPUT_DIR);
        banco = new AlphaBank();
        banco.start();
        company = new Company(this.sumoExecutor);
        company.start();
        System.out.println("Componentes inicializados para " + TOTAL_RUNS + " execucoes");
    }

    /**
     * Executa Reconciliação de Dados (Parte I) - 10 veículos em uma única simulação
     * Sensores distribuídos a cada 5km
     */
    private void executeDataReconciliation() throws InterruptedException, ExecutionException {
        System.out.println("=== INICIANDO SIMULACAO COM 10 VEICULOS EM UMA UNICA EXECUCAO ===");
        
        // Calcular tempo de simulação baseado na rota
        dataReconciliation.calculateSimulationTime();
        
        // Executar coleta de dados para todos os veículos em uma única simulação
        System.out.println("Coletando dados de todos os veiculos...");
        dataReconciliation.executeDataCollection();
        
        System.out.println("Iniciando reconciliacao de dados com todos os dados coletados...");
        
        // Executar reconciliação com todos os dados coletados
        dataReconciliation.executeReconciliation();

        // Salvar relatório
        dataReconciliation.saveReconciliationReport(OUTPUT_DIR + "/reconciliation_report.csv");

        System.out.println("Reconciliacao de dados concluida");
    }

    /**
     * Executa Escalonamento de Tempo Real (Parte II)
     */
    private void executeRealTimeScheduling() {
        // Executar análise de escalonabilidade
        realTimeScheduler.executeSchedulabilityAnalysis();

        // Demonstrar cenários de não-escalonabilidade
        realTimeScheduler.demonstrateNonSchedulability();

        // Imprimir resumo
        realTimeScheduler.printSchedulingSummary();

        // Salvar relatório
        realTimeScheduler.saveSchedulingReport(OUTPUT_DIR + "/scheduling_report.csv");

        System.out.println("Escalonamento de tempo real concluido");
    }

    /**
     * Chama a função para gerar os gráficos
     */
    private void generateGraphs() {
        // Gerar os 3 gráficos
        graphGenerator.generateAllGraphs(dataReconciliation, realTimeScheduler);

        System.out.println("Graficos gerados com sucesso");
    }

    /**
     * Gera relatórios finais consolidados
     */
    private void generateFinalReports() {
        // Relatório consolidado
        generateConsolidatedReport();

        // Relatório de estatísticas
        generateStatisticsReport();

        System.out.println("Relatorios finais gerados");
    }

    /**
     * Gera relatório consolidado
     */
    private void generateConsolidatedReport() {
        ManipuladorCSV report = new ManipuladorCSV(OUTPUT_DIR + "/consolidated_report.csv");

        String[] header = { "Component", "Status", "Details", "Value", "Unit" };
        report.writeCSV(header);

        // Dados da reconciliação
        String[] reconciliationData = {
                "Data_Reconciliation", "COMPLETED", "Sensor_Readings",
                String.valueOf(dataReconciliation.getSensorReadings().size()), "readings"
        };
        report.appendCSV(reconciliationData);

        String[] meanSpeedData = {
                "Data_Reconciliation", "COMPLETED", "Mean_Speed",
                String.format("%.2f", dataReconciliation.getMeanSpeed()), "km/h"
        };
        report.appendCSV(meanSpeedData);
        
        String[] totalRunsData = {
                "Data_Reconciliation", "COMPLETED", "Total_Runs",
                String.valueOf(dataReconciliation.getTotalRuns()), "runs"
        };
        report.appendCSV(totalRunsData);

        // Dados do escalonamento
        long schedulableTasks = realTimeScheduler.getTasks().stream()
                .mapToLong(task -> task.isSchedulable() ? 1 : 0).sum();

        String[] schedulingData = {
                "Real_Time_Scheduling", "COMPLETED", "Schedulable_Tasks",
                String.valueOf(schedulableTasks), "tasks"
        };
        report.appendCSV(schedulingData);

        String[] utilizationData = {
                "Real_Time_Scheduling", "COMPLETED", "System_Utilization",
                String.format("%.2f",
                        (realTimeScheduler.getTotalUtilization() / realTimeScheduler.getNumberOfProcessors()) * 100),
                "percent"
        };
        report.appendCSV(utilizationData);

        // Dados dos gráficos
        String[] graphsData = {
                "Graphs_Generated", "COMPLETED", "Specific_Charts", "3", "files"
        };
        report.appendCSV(graphsData);

        System.out.printf("Relatorio consolidado salvo: %s\n", OUTPUT_DIR + "/consolidated_report.csv");
    }

    /**
     * Gera relatório de estatísticas
     */
    private void generateStatisticsReport() {
        ManipuladorCSV report = new ManipuladorCSV(OUTPUT_DIR + "/statistics_summary.csv");

        String[] header = { "Metric", "Value", "Unit", "Description" };
        report.writeCSV(header);

        // Estatísticas da reconciliação
        String[][] reconciliationStats = {
                { "Route_Length", String.format("%.2f", dataReconciliation.getRouteLength()), "km",
                        "Comprimento total da rota" },
                { "Simulation_Time", String.format("%.1f", dataReconciliation.getSimulationTime()), "seconds",
                        "Tempo por execucao" },
                { "Total_Runs", String.valueOf(dataReconciliation.getTotalRuns()), "runs",
                        "Numero total de execucoes" },
                { "Mean_Speed", String.format("%.2f", dataReconciliation.getMeanSpeed()), "km/h",
                        "Velocidade media medida" },
                { "Standard_Deviation", String.format("%.2f", dataReconciliation.getStandardDeviation()), "km/h",
                        "Desvio padrao das velocidades" },
                { "Precision", String.format("%.2f", dataReconciliation.getPrecision() * 100), "percent",
                        "Precisao da reconciliacao" },
                { "Uncertainty", String.format("%.2f", dataReconciliation.getUncertainty()), "km/h",
                        "Incerteza dos dados" }
        };

        for (String[] stat : reconciliationStats) {
            report.appendCSV(stat);
        }

        // Estatísticas do escalonamento
        long schedulableTasks = realTimeScheduler.getTasks().stream()
                .mapToLong(task -> task.isSchedulable() ? 1 : 0).sum();

        String[][] schedulingStats = {
                { "Total_Tasks", String.valueOf(realTimeScheduler.getTasks().size()), "tasks",
                        "Total de tarefas analisadas" },
                { "Schedulable_Tasks", String.valueOf(schedulableTasks), "tasks", "Tarefas escalonaveis" },
                { "Processors", String.valueOf(realTimeScheduler.getNumberOfProcessors()), "units",
                        "Numero de processadores" },
                { "Total_Utilization", String.format("%.2f", realTimeScheduler.getTotalUtilization()), "ratio",
                        "Utilizacao total do sistema" },
                { "Utilization_Percentage",
                        String.format("%.2f",
                                (realTimeScheduler.getTotalUtilization() / realTimeScheduler.getNumberOfProcessors())
                                        * 100),
                        "percent", "Percentual de utilizacao" }
        };

        for (String[] stat : schedulingStats) {
            report.appendCSV(stat);
        }

        System.out.printf("Relatorio de estatisticas salvo: %s\n", OUTPUT_DIR + "/statistics_summary.csv");
    }

    /**
     * Limpeza e finalização
     */
    private void closeSim() {
        try {
            if (sumo != null && !sumo.isClosed()) {
                sumo.close();
            }

            System.out.println("Limpeza concluida");

        } catch (Exception e) {
            System.err.println("Erro na limpeza: " + e.getMessage());
        }
    }

    /**
     * Imprime resumo final do sistema
     */
    private void printFinalSummary() {
        System.out.println("\n=== RESUMO FINAL DO SISTEMA ===");

        // Resumo da reconciliação
        System.out.printf("Reconciliacao: %d leituras de %d execucoes, %.2f km/h media\n",
                dataReconciliation.getSensorReadings().size(),
                dataReconciliation.getTotalRuns(),
                dataReconciliation.getMeanSpeed());
                
        // Resumo dos sensores
        System.out.println("\nEstatisticas por sensor:");
        for (DataReconciliation.DistanceSensor sensor : dataReconciliation.getDistanceSensors()) {
            if (sensor.getReadingCount() > 0) {
                System.out.printf("Sensor %s: %d leituras, %.2f km/h media, %.2f desvio padrao\n",
                    sensor.getId(), sensor.getReadingCount(), sensor.getMeanSpeed(), sensor.getStandardDeviation());
            }
        }

        // Resumo do escalonamento
        long schedulableTasks = realTimeScheduler.getTasks().stream()
                .mapToLong(task -> task.isSchedulable() ? 1 : 0).sum();

        System.out.printf("\nEscalonamento: %d/%d tarefas escalonaveis\n",
                schedulableTasks, realTimeScheduler.getTasks().size());
                
        System.out.println("\n=== SISTEMA FINALIZADO COM SUCESSO ===");
    }
}

