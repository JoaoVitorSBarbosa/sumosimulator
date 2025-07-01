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
 * Combina Reconciliação de Dados (Parte I) e Escalonamento de Tempo Real (Parte
 * II)
 * com dados reais de velocidade e posição do SUMO
 */
public class AV2Main {

    private SumoTraciConnection sumo;
    private SumoCommandExecutor sumoExecutor;
    private DataReconciliation dataReconciliation;
    private RealTimeScheduler realTimeScheduler;
    private GraphGenerator graphGenerator;
    private Company company;
    private AlphaBank banco;
    private Rota rota;
    private Car auto;

    private static final String VEHICLE_ID = "0";
    private static final String OUTPUT_DIR = "reports/av2";
    private static final int AQ_RATE = 500;
    private static final int NUM_SIMS = 10;
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

                waitForVehicleInSimulation();
                // Executar Reconciliação de Dados (Parte I)
                executeDataReconciliation();

                // FExecutar Escalonamento de Tempo Real (Parte II)
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
     * Inicializa conexão com SUMO
     */
    private void initializeSumo() throws IOException {

        String sumo_bin = "sumo-gui";
        String config_file = "map/map.sumo.cfg";

        // Configurar conexão SUMO
        sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1");
        sumo.addOption("quit-on-end", "1");

        // Iniciar servidor SUMO
        sumo.runServer(12345);

        // Criar executor de comandos
        sumoExecutor = new SumoCommandExecutor(sumo);
        sumoExecutor.start();

        System.out.println("SUMO inicializado e conectado");
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

        // Inicializar componentes
        dataReconciliation = new DataReconciliation(sumoExecutor, VEHICLE_ID);
        realTimeScheduler = new RealTimeScheduler(1);
        graphGenerator = new GraphGenerator(OUTPUT_DIR);
        banco = new AlphaBank();
        banco.start();
        company = new Company(this.sumoExecutor);
        company.start();
        System.out.println("Componentes inicializados");
    }

    private void waitForVehicleInSimulation() throws InterruptedException, ExecutionException {
        System.out.printf("Aguardando veículo %s aparecer na simulação...\n", VEHICLE_ID);

        int maxAttempts = 100;
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                // Avançar um passo da simulação
                sumoExecutor.submitCommand(new DoTimestepCommand()).get();

                // Verificar se veículo está presente
                var idListFuture = sumoExecutor.submitCommand(new GetVehicleIDListCommand());
                var idList = idListFuture.get();

                if (idList != null && idList.contains(VEHICLE_ID)) {
                    System.out.printf(" Veículo %s encontrado na simulação\n", VEHICLE_ID);
                    return;
                }

                attempts++;
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.printf("⚠️  Tentativa %d falhou: %s\n", attempts, e.getMessage());
                attempts++;
            }
        }

        throw new RuntimeException("Veículo não encontrado após " + maxAttempts + " tentativas");
    }

    /**
     * Executa Reconciliação de Dados (Parte I)
     */
    private void executeDataReconciliation() throws InterruptedException, ExecutionException {
        // Calcular tempo de simulação baseado na rota
        dataReconciliation.calculateSimulationTime();

        // Executar coleta de dados da simulação SUMO
        dataReconciliation.executeDataCollection();

        // Executar reconciliação
        dataReconciliation.executeReconciliation();

        // Salvar relatório
        dataReconciliation.saveReconciliationReport(OUTPUT_DIR + "/reconciliation.csv");

        System.out.println("Reconciliação de dados concluída");
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
        realTimeScheduler.saveSchedulingReport(OUTPUT_DIR + "/scheduling.csv");

        System.out.println("Escalonamento de tempo real concluído");
    }

    /**
     * Chama a função para gerar os gráficos
     */
    private void generateGraphs() {

        // Gerar os 3 gráficos
        graphGenerator.generateAllGraphs(dataReconciliation, realTimeScheduler);

        System.out.println("Gráficos gerados com sucesso");
    }

    /**
     * Gera relatórios finais consolidados
     */
    private void generateFinalReports() {

        // Relatório consolidado
        generateConsolidatedReport();

        // Relatório de estatísticas
        generateStatisticsReport();

        System.out.println("Relatórios finais gerados!!");
    }

    /**
     * Gera relatório consolidado
     */
    private void generateConsolidatedReport() {
        ManipuladorCSV report = new ManipuladorCSV(OUTPUT_DIR + "/consolidated.csv");

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

        System.out.printf("Relatório consolidado salvo: %s\n", OUTPUT_DIR + "/consolidated.csv");
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
                        "Tempo total de simulação" },
                { "Mean_Speed", String.format("%.2f", dataReconciliation.getMeanSpeed()), "km/h",
                        "Velocidade média medida" },
                { "Standard_Deviation", String.format("%.2f", dataReconciliation.getStandardDeviation()), "km/h",
                        "Desvio padrão das velocidades" },
                { "Precision", String.format("%.2f", dataReconciliation.getPrecision() * 100), "percent",
                        "Precisão da reconciliação" },
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
                { "Schedulable_Tasks", String.valueOf(schedulableTasks), "tasks", "Tarefas escalonáveis" },
                { "Processors", String.valueOf(realTimeScheduler.getNumberOfProcessors()), "units",
                        "Número de processadores" },
                { "Total_Utilization", String.format("%.2f", realTimeScheduler.getTotalUtilization()), "ratio",
                        "Utilização total do sistema" },
                { "Utilization_Percentage",
                        String.format("%.2f",
                                (realTimeScheduler.getTotalUtilization() / realTimeScheduler.getNumberOfProcessors())
                                        * 100),
                        "percent", "Percentual de utilização" }
        };

        for (String[] stat : schedulingStats) {
            report.appendCSV(stat);
        }

        System.out.printf(" Relatório de estatísticas salvo: %s\n", OUTPUT_DIR + "/statistics_summary.csv");
    }

    /**
     * Limpeza e finalização
     */
    private void closeSim() {
        try {
            if (sumo != null && !sumo.isClosed()) {
                sumo.close();
            }

            System.out.println("Limpeza concluída");

        } catch (Exception e) {
            System.err.println("Erro na limpeza: " + e.getMessage());
        }
    }

    /**
     * Imprime resumo final do sistema
     */
    private void printFinalSummary() {

        // Resumo da reconciliação
        System.out.printf("Reconciliação: %d leituras, %.2f km/h média\n",
                dataReconciliation.getSensorReadings().size(),
                dataReconciliation.getMeanSpeed());

        // Resumo do escalonamento
        long schedulableTasks = realTimeScheduler.getTasks().stream()
                .mapToLong(task -> task.isSchedulable() ? 1 : 0).sum();

        System.out.printf("Escalonamento: %d/%d tarefas escalonáveis\n",
                schedulableTasks, realTimeScheduler.getTasks().size());
    }
}
