package io.sim;

import java.util.*;

/**
 * Classe responsável pelo Escalonamento de Tempo Real (AV2 - Parte II)
 */
public class RealTimeScheduler {
    
    private List<RealTimeTask> tasks;
    private int numberOfProcessors;
    private double totalUtilization;
    
    /**
     * Classe interna para tarefas de tempo real
     */
    public static class RealTimeTask {
        public String taskId;
        public int priority;
        public double period;
        public double deadline;
        public double executionTime;
        public double responseTime;
        public boolean schedulable;
        public List<String> dependencies;
        
        public RealTimeTask(String taskId, int priority, double period, double deadline, double executionTime) {
            this.taskId = taskId;
            this.priority = priority;
            this.period = period;
            this.deadline = deadline;
            this.executionTime = executionTime;
            this.schedulable = false;
            this.dependencies = new ArrayList<>();
        }
        
        public void addDependency(String dependentTaskId) {
            dependencies.add(dependentTaskId);
        }
        
        // Getters públicos
        public String getTaskId() { return taskId; }
        public int getPriority() { return priority; }
        public double getPeriod() { return period; }
        public double getDeadline() { return deadline; }
        public double getExecutionTime() { return executionTime; }
        public double getResponseTime() { return responseTime; }
        public boolean isSchedulable() { return schedulable; }
        public List<String> getDependencies() { return dependencies; }
    }
    
    public RealTimeScheduler(int numberOfProcessors) {
        this.numberOfProcessors = numberOfProcessors;
        this.tasks = new ArrayList<>();
        initializeTasks();
    }
    
    /**
     * Inicializa as 10 tarefas conforme especificação AV2
     */
    private void initializeTasks() {
        // T1: Coleta de dados dos sensores
        RealTimeTask t1 = new RealTimeTask("T1", 1, 100, 80, 20);
        
        // T2: Processamento de dados de velocidade
        RealTimeTask t2 = new RealTimeTask("T2", 2, 150, 120, 30);
        t2.addDependency("T1");
        
        // T3: Cálculo de velocidade ótima
        RealTimeTask t3 = new RealTimeTask("T3", 3, 200, 160, 40);
        t3.addDependency("T2");
        
        // T4: Reconciliação de dados
        RealTimeTask t4 = new RealTimeTask("T4", 4, 250, 200, 50);
        t4.addDependency("T3");
        
        // T5: Controle de velocidade
        RealTimeTask t5 = new RealTimeTask("T5", 5, 300, 240, 60);
        t5.addDependency("T4");
        
        // T6: Monitoramento de combustível
        RealTimeTask t6 = new RealTimeTask("T6", 6, 180, 150, 35);
        t6.addDependency("T1");
        
        // T7: Análise de posição
        RealTimeTask t7 = new RealTimeTask("T7", 7, 220, 180, 45);
        t7.addDependency("T2");
        
        // T8: Comunicação com servidor
        RealTimeTask t8 = new RealTimeTask("T8", 8, 400, 320, 70);
        t8.addDependency("T5");
        
        // T9: Geração de relatórios
        RealTimeTask t9 = new RealTimeTask("T9", 9, 500, 400, 80);
        t9.addDependency("T6");
        t9.addDependency("T7");
        
        // T10: Backup de dados
        RealTimeTask t10 = new RealTimeTask("T10", 10, 600, 480, 90);
        t10.addDependency("T8");
        t10.addDependency("T9");
        
        tasks.addAll(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10));
        
        System.out.printf("Inicializadas %d tarefas de tempo real\n", tasks.size());
    }
    
    /**
     * Executa análise de escalonabilidade
     */
    public void executeSchedulabilityAnalysis() {
        System.out.println("Executando análise de escalonabilidade...");
        
        // Calcular utilização total
        calculateTotalUtilization();
        
        // Testar diferentes configurações de processadores
        testProcessorConfigurations();
        
        // Análise detalhada para configuração atual
        analyzeCurrentConfiguration();
        
        System.out.println("Análise de escalonabilidade concluída");
    }
    
    /**
     * Calcula utilização total do sistema
     */
    private void calculateTotalUtilization() {
        totalUtilization = tasks.stream()
            .mapToDouble(task -> task.executionTime / task.period)
            .sum();
        
        System.out.printf("Utilização total: %.2f%% (%d processadores)\n", 
            (totalUtilization / numberOfProcessors) * 100, numberOfProcessors);
    }
    
    /**
     * Testa diferentes configurações de processadores
     */
    private void testProcessorConfigurations() {
        System.out.println("\n=== TESTE DE CONFIGURAÇÕES ===");
        
        int[] processorConfigs = {1, 2, 3, numberOfProcessors};
        
        for (int processors : processorConfigs) {
            double utilizationPercentage = (totalUtilization / processors) * 100;
            boolean feasible = utilizationPercentage <= 100;
            
            System.out.printf("%d processador(es): Utilização %.1f%% - %s\n",
                processors, utilizationPercentage, 
                feasible ? "VIÁVEL" : "INVIÁVEL");
        }
    }
    
    /**
     * Análise detalhada para configuração atual
     */
    private void analyzeCurrentConfiguration() {
        System.out.printf("\n=== ANÁLISE DETALHADA (%d PROCESSADORES) ===\n", numberOfProcessors);
        
        // Ordenar tarefas por prioridade (Rate Monotonic)
        tasks.sort(Comparator.comparingInt(task -> task.priority));
        
        int schedulableTasks = 0;
        
        for (RealTimeTask task : tasks) {
            // Calcular tempo de resposta
            task.responseTime = calculateResponseTime(task);
            
            // Verificar escalonabilidade
            task.schedulable = task.responseTime <= task.deadline;
            
            if (task.schedulable) {
                schedulableTasks++;
            }
            
            System.out.printf("%s: R=%.1fms, D=%.1fms, P=%d - %s\n",
                task.taskId, task.responseTime, task.deadline, task.priority,
                task.schedulable ? "ESCALONÁVEL" : "NÃO ESCALONÁVEL");
        }
        
        System.out.printf("\nResultado: %d/%d tarefas escalonáveis (%.1f%%)\n",
            schedulableTasks, tasks.size(), (double) schedulableTasks / tasks.size() * 100);
    }
    
    /**
     * Calcula tempo de resposta usando análise Rate Monotonic
     */
    private double calculateResponseTime(RealTimeTask task) {
        double responseTime = task.executionTime;
        
        // Adicionar interferência de tarefas de maior prioridade
        for (RealTimeTask higherPriorityTask : tasks) {
            if (higherPriorityTask.priority < task.priority) {
                double interference = Math.ceil(task.period / higherPriorityTask.period) * higherPriorityTask.executionTime;
                responseTime += interference;
            }
        }
        
        // Adicionar atraso por dependências
        double dependencyDelay = calculateDependencyDelay(task);
        responseTime += dependencyDelay;
        
        // Considerar múltiplos processadores
        if (numberOfProcessors > 1) {
            responseTime = responseTime / numberOfProcessors;
        }
        
        return responseTime;
    }
    
    /**
     * Calcula atraso devido a dependências
     */
    private double calculateDependencyDelay(RealTimeTask task) {
        double maxDependencyTime = 0;
        
        for (String dependencyId : task.dependencies) {
            RealTimeTask dependencyTask = findTaskById(dependencyId);
            if (dependencyTask != null) {
                maxDependencyTime = Math.max(maxDependencyTime, dependencyTask.executionTime);
            }
        }
        
        return maxDependencyTime * 0.1; // 10% de overhead por dependência
    }
    
    /**
     * Encontra tarefa por ID
     */
    private RealTimeTask findTaskById(String taskId) {
        return tasks.stream()
            .filter(task -> task.taskId.equals(taskId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Demonstra cenários de não-escalonabilidade
     */
    public void demonstrateNonSchedulability() {
        System.out.println("\n=== DEMONSTRAÇÃO DE NÃO-ESCALONABILIDADE ===");
        
        List<RealTimeTask> nonSchedulableTasks = tasks.stream()
            .filter(task -> !task.schedulable)
            .toList();
        
        if (nonSchedulableTasks.isEmpty()) {
            System.out.println("Todas as tarefas são escalonáveis na configuração atual");
            return;
        }
        
        for (RealTimeTask task : nonSchedulableTasks) {
            System.out.printf("%s: Tempo de resposta %.1fms > Deadline %.1fms\n",
                task.taskId, task.responseTime, task.deadline);
            
            // Analisar causas
            analyzeCauses(task);
        }
    }
    
    /**
     * Analisa causas de não-escalonabilidade
     */
    private void analyzeCauses(RealTimeTask task) {
        System.out.printf("Causas para %s:\n", task.taskId);
        
        // Verificar utilização
        double taskUtilization = task.executionTime / task.period;
        if (taskUtilization > 1.0 / numberOfProcessors) {
            System.out.printf("Utilização alta: %.2f%% (limite: %.2f%%)\n",
                taskUtilization * 100, (1.0 / numberOfProcessors) * 100);
        }
        
        // Verificar interferência
        double interference = 0;
        for (RealTimeTask higherTask : tasks) {
            if (higherTask.priority < task.priority) {
                interference += Math.ceil(task.period / higherTask.period) * higherTask.executionTime;
            }
        }
        
        if (interference > task.deadline * 0.5) {
            System.out.printf("Interferência alta: %.1fms\n", interference);
        }
        
        // Verificar dependências
        if (!task.dependencies.isEmpty()) {
            System.out.printf("Dependências: %s\n", String.join(", ", task.dependencies));
        }
    }
    
    /**
     * Salva relatório de escalonamento
     */
    public void saveSchedulingReport(String outputPath) {
        ManipuladorCSV report = new ManipuladorCSV(outputPath);
        
        // Cabeçalho
        String[] header = {"TaskId", "Priority", "Period", "Deadline", "ExecutionTime", 
                          "ResponseTime", "Schedulable", "Dependencies", "Utilization"};
        report.writeCSV(header);
        
        // Dados
        for (RealTimeTask task : tasks) {
            double utilization = task.executionTime / task.period;
            String[] data = {
                task.taskId,
                String.valueOf(task.priority),
                String.format("%.1f", task.period),
                String.format("%.1f", task.deadline),
                String.format("%.1f", task.executionTime),
                String.format("%.1f", task.responseTime),
                String.valueOf(task.schedulable),
                String.join(";", task.dependencies),
                String.format("%.3f", utilization)
            };
            report.appendCSV(data);
        }
        
        System.out.printf("Relatório de escalonamento salvo: %s\n", outputPath);
    }
    
    /**
     * Imprime resumo do escalonamento
     */
    public void printSchedulingSummary() {
        System.out.println("\n=== RESUMO DO ESCALONAMENTO ===");
        
        long schedulableCount = tasks.stream().mapToLong(task -> task.schedulable ? 1 : 0).sum();
        double schedulabilityRate = (double) schedulableCount / tasks.size() * 100;
        
        System.out.printf("Processadores: %d\n", numberOfProcessors);
        System.out.printf("Utilização total: %.2f%%\n", (totalUtilization / numberOfProcessors) * 100);
        System.out.printf("Tarefas escalonáveis: %d/%d (%.1f%%)\n", 
            schedulableCount, tasks.size(), schedulabilityRate);
        
        if (schedulabilityRate < 100) {
            System.out.printf("Tarefas não escalonáveis: %d\n", tasks.size() - schedulableCount);
            System.out.println("Sugestões: Aumentar processadores ou ajustar deadlines");
        }
    }
    
    // Getters públicos
    public List<RealTimeTask> getTasks() { return tasks; }
    public int getNumberOfProcessors() { return numberOfProcessors; }
    public double getTotalUtilization() { return totalUtilization; }
}

