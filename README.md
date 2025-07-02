Preparação do Ambiente
Para configurar o ambiente, siga os passos abaixo:

# Passo 1 - Oracle - JDK (Java Development Kit)

Baixar a instalador de sua preferência pelo link: https://www.oracle.com/java/technologies/downloads/#jdk24-windows

# Passo2 - SUMO - Simulation of Urban MObility

Baixar a instalador de sua preferência pelo link: https://eclipse.dev/sumo/

# Passo 3 - Apache Maven

Para instalar o Maven em qualquer sistema operacional basta baixar os arquivos binários (https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip) e extrilos em uma pasta. Apóes isso é necessário apenas adicionar o maven ao PATH (abra a pasta bin do maven e cole sua localização absoluta) 

# Passo 4 - VScode

Baixar a instalador de sua preferência pelo link: https://code.visualstudio.com/download

# Passo 5 - Fork do projeto 

Fazer o Fork do projeto base (https://github.com/admufla/sumosimulator) para o seu perfil 

# Passo 6 - Clonar o projeto

# Passo 7 - Abrir o projeto no Visual Studio Code 

# Passo 8 - Instalar o sumo

O arquivo junit.jar está ausente. Baixe-o manualmente em [junit 4.13.2](https://repo1.maven.org/maven2/junit/junit/4.13.2/) e coloque-o na pasta ./lib/sumo/ antes de executar os comandos abaixo:

obs: os comandos devem ser executados na pasta raiz do projeto.

mvn install:install-file -Dfile="./lib/sumo/junit.jar" -DgroupId="junit" -DartifactId="junit" -Dversion="junit" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libsumo-1.23.1.jar" -DgroupId="libsumo-1.23.1" -DartifactId="libsumo-1.23.1" -Dversion="libsumo-1.23.1" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libtraci-1.23.1.jar" -DgroupId="libtraci-1.23.1" -DartifactId="libtraci-1.23.1" -Dversion="libtraci-1.23.1" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libtraci-1.23.1-sources.jar" -DgroupId="libtraci-1.23.1-sources" -DartifactId="libtraci-1.23.1-sources" -Dversion="libtraci-1.23.1-sources" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libsumo-1.23.1-sources.jar" -DgroupId="libsumo-1.23.1-sources" -DartifactId="libsumo-1.23.1-sources" -Dversion="libsumo-1.23.1-sources" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libtraci-1.23.1.jar" -DgroupId="libtraci-1.23.1" -DartifactId="libtraci-1.18.0" -Dversion="libtraci-1.23.1" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/lisum-core.jar" -DgroupId="lisum-core" -DartifactId="lisum-core" -Dversion="lisum-core" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/lisum-gui.jar" -DgroupId="lisum-gui" -DartifactId="lisum-gui" -Dversion="lisum-gui" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/TraaS.jar" -DgroupId="TraaS" -DartifactId="TraaS" -Dversion="TraaS" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/gson-2.13.1.jar" -DgroupId="com.google.code.gson" -DartifactId="gson" -Dversion="2.13.1" -Dpackaging="jar" -DgeneratePom=true


mvn clean install

# Passo 9 - Executar o programa

Reinicie o VS Code e execute o App.java (em src/main/java/io/sim)

# gerar mapa

   netconvert --osm-files map.osm -o map.net.xml
   polyconvert --net-file map.net.xml --osm-files map.osm --type-file typemap.xml -o map.poly.xml
   python randomTrips.py -n map.net.xml -r map.rou.xml -e 200 -l 

# Documentação da Estrutura do Código - SUMO Simulator

## Visão Geral

O SUMO Simulator é um sistema de simulação de tráfego urbano implementado em Java. O sistema é composto por diversos componentes que trabalham juntos para simular o ambiente de tráfego, incluindo veículos, motoristas, rotas, serviços de transporte e um sistema bancário simulado.

Este documento apresenta a estrutura do código, descrevendo as principais classes, seus métodos e relacionamentos, sem depender das bibliotecas externas do SUMO.

## Estrutura de Pacotes

O código está organizado nos seguintes pacotes:

- `io.sim`: Contém as classes principais do sistema
- `io.sim.sumo`: Contém classes para interação com o simulador SUMO
- `io.sim.sumo.cmd`: Contém comandos específicos para controle do SUMO

## Classes Principais

### Pacote `io.sim`

#### `App`
Classe principal que inicia a aplicação.

**Métodos:**
- `main(String[] args)`: Ponto de entrada da aplicação. Cria e inicia o simulador de ambiente.

#### `EnvSimulator`
Classe que gerencia o ambiente de simulação, estendendo `Thread`.

**Atributos:**
- `company`: Instância da classe `Company`
- `sumo`: Conexão com o simulador SUMO
- `banco`: Instância da classe `AlphaBank`
- `isOn`: Flag que controla a execução da simulação
- `sumoExecutor`: Executor de comandos SUMO

**Métodos:**
- `EnvSimulator()`: Construtor que inicializa o ambiente
- `run()`: Método que executa a simulação, iniciando o servidor SUMO, o banco e a empresa

#### `AlphaBank`
Classe que simula um banco para transações financeiras.

#### `Company`
Classe que representa uma empresa de transporte, gerenciando motoristas, veículos e rotas.

**Atributos:**
- `valorFinal`: Valor final para pagamentos
- `senhaCompany`: Senha da empresa
- `contaCompany`: Conta bancária da empresa
- `funcionarios`: Lista de motoristas
- `rotasAguardando`: Coleção de rotas aguardando execução
- `rotasEmExecucao`: Coleção de rotas em execução
- `rotasExecutadas`: Coleção de rotas já executadas
- `carros`: Lista de veículos
- `servicos`: Coleção de serviços de transporte
- `on_off`: Flag que controla a execução da empresa
- `sumoExecutor`: Executor de comandos SUMO

**Métodos:**
- `Company(SumoCommandExecutor sumoExecutor)`: Construtor que inicializa a empresa
- `run()`: Método que executa a lógica da empresa
- `pagar(String idDriver, double valor)`: Realiza pagamento a um motorista
- `getOnOff()`: Retorna o estado de execução da empresa
- `removeCorridasFinalizadas()`: Remove corridas que foram finalizadas
- `updateActivityTimestamp()`: Atualiza o timestamp de atividade
- `checkSimulationStatus()`: Verifica o status da simulação

#### `Driver`
Classe que representa um motorista.

#### `Car`
Classe que representa um veículo.

#### `Rota`
Classe que representa uma rota de transporte.

#### `TransportService`
Classe que representa um serviço de transporte.

#### `FuelStation`
Classe que representa um posto de combustível.

#### `ManipuladorCSV`
Classe utilitária para manipulação de arquivos CSV.

**Métodos:**
- `writeCSV(String[] data)`: Escreve dados em um arquivo CSV
- `appendCSV(String[] data)`: Adiciona dados a um arquivo CSV existente
- `delete()`: Exclui o arquivo CSV

#### `Client`
Classe que representa um cliente para comunicação com servidores.

#### `Servidor`
Classe que implementa um servidor para comunicação.

#### `Codec`
Classe utilitária para codificação e decodificação de dados.

**Métodos:**
- `getSHA(String input)`: Gera um hash SHA para uma string
- `toHexString(byte[] hash)`: Converte um array de bytes em uma string hexadecimal

### Pacote `io.sim.sumo`

#### `SumoCommand<T>`
Classe abstrata que representa um comando a ser executado no simulador SUMO.

**Atributos:**
- `future`: CompletableFuture para armazenar o resultado da execução do comando

**Métodos:**
- `execute(SumoTraciConnection sumo)`: Método abstrato que executa o comando no SUMO
- `getFuture()`: Retorna o CompletableFuture associado ao comando
- `complete(T result)`: Completa o future com sucesso
- `completeExceptionally(Throwable ex)`: Completa o future com exceção

#### `SumoCommandExecutor`
Classe que executa comandos no simulador SUMO.

**Atributos:**
- `sumo`: Conexão com o simulador SUMO
- `executorService`: Serviço executor para processamento de comandos
- `running`: Flag que controla a execução do executor

**Métodos:**
- `SumoCommandExecutor(SumoTraciConnection sumo)`: Construtor que inicializa o executor
- `start()`: Inicia o executor
- `stop()`: Para o executor
- `submitCommand(SumoCommand<?> command)`: Submete um comando para execução
- `run()`: Método que executa o loop principal do executor

### Pacote `io.sim.sumo.cmd`

Este pacote contém classes que implementam comandos específicos para o simulador SUMO, todas estendendo a classe `SumoCommand<T>`. Alguns exemplos:

#### `AddRouteCommand`
Comando para adicionar uma rota ao simulador.

#### `AddVehicleFullCommand`
Comando para adicionar um veículo completo ao simulador.

#### `DoTimestepCommand`
Comando para avançar a simulação em um passo de tempo.

#### `GetVehicleCO2EmissionCommand`
Comando para obter a emissão de CO2 de um veículo.

**Atributos:**
- `vehicleID`: ID do veículo

**Métodos:**
- `GetVehicleCO2EmissionCommand(String vehicleID)`: Construtor que inicializa o comando
- `execute(SumoTraciConnection sumo)`: Executa o comando no SUMO

#### `GetVehicleDistanceCommand`
Comando para obter a distância percorrida por um veículo.

#### `GetVehicleFuelConsumptionCommand`
Comando para obter o consumo de combustível de um veículo.

#### `GetVehicleIDListCommand`
Comando para obter a lista de IDs de veículos na simulação.

#### `GetVehiclePositionCommand`
Comando para obter a posição de um veículo.

#### `GetVehicleRouteIDCommand`
Comando para obter o ID da rota de um veículo.

#### `GetVehicleSpeedCommand`
Comando para obter a velocidade de um veículo.

#### `SetVehicleColorCommand`
Comando para definir a cor de um veículo.

#### `SetVehicleSpeedCommand`
Comando para definir a velocidade de um veículo.

#### `SetVehicleSpeedModeCommand`
Comando para definir o modo de velocidade de um veículo.

## Fluxo de Execução

1. A aplicação é iniciada pela classe `App`
2. `App` cria e inicia uma instância de `EnvSimulator`
3. `EnvSimulator` inicializa a conexão com o SUMO, o banco e a empresa
4. `Company` gerencia motoristas, veículos e rotas
5. Os serviços de transporte são executados
6. A simulação continua até que todas as rotas sejam executadas ou ocorra um timeout por inatividade

## Padrões de Design

- **Command Pattern**: Utilizado na implementação dos comandos SUMO
- **Observer Pattern**: Utilizado para monitorar eventos durante a simulação
- **Factory Pattern**: Utilizado na criação de objetos como veículos e motoristas
- **Thread Pattern**: Utilizado para execução concorrente de componentes do sistema

## Considerações Finais

Esta documentação apresenta uma visão geral da estrutura do código do SUMO Simulator, focando nas classes e métodos principais. Para uma compreensão completa do sistema, é recomendável analisar o código-fonte e a documentação do SUMO, especialmente para entender as interações com o simulador através da API TraCI.

# AV2 – Parte II: Escalonamento de Tempo Real - Documentação

## Resumo da Implementação

Esta documentação descreve a implementação da **AV2 – Parte II**, que adiciona funcionalidade de **Escalonamento de Tempo Real** ao simulador SUMO para verificar se tarefas são escalonáveis com diferentes configurações de processadores.

## Arquivos Implementados

### 1. RealTimeTask.java
**Localização:** `src/main/java/io/sim/RealTimeTask.java`

**Funcionalidades:**
- Representa tarefas de tempo real com prioridades, períodos e deadlines
- Implementa 10 tipos diferentes de tarefas do simulador
- Cálculo de utilização (C_i / P_i)
- Cálculo de tempo de resposta usando fórmula: R_i = W_i + J_i
- Sistema de dependências entre tarefas
- Simulação de jitter de liberação
- Verificação de escalonabilidade individual

**Tipos de Tarefas Implementadas:**
1. **VEHICLE_MONITORING** - Monitoramento de veículos
2. **DATA_RECONCILIATION** - Reconciliação de dados
3. **FUEL_CALCULATION** - Cálculo de combustível
4. **ROUTE_PLANNING** - Planejamento de rotas
5. **TRAFFIC_ANALYSIS** - Análise de tráfego
6. **SENSOR_READING** - Leitura de sensores
7. **COMMUNICATION** - Comunicação entre componentes
8. **LOGGING** - Registro de logs
9. **REPORT_GENERATION** - Geração de relatórios
10. **SYSTEM_MAINTENANCE** - Manutenção do sistema

### 2. RealTimeScheduler.java
**Localização:** `src/main/java/io/sim/RealTimeScheduler.java`

**Funcionalidades:**
- Escalonador principal de tempo real
- Análise de escalonabilidade do sistema
- Teste com diferentes configurações de processadores
- Geração de relatórios CSV detalhados
- Monitoramento de utilização do processador
- Implementação de algoritmos de escalonamento dirigidos a prioridades

**Algoritmos Implementados:**
- Análise de tempo de resposta
- Verificação de utilização total do processador
- Escalonamento periódico de tarefas
- Tratamento de dependências entre tarefas

### 3. RealTimeSchedulingTest.java
**Localização:** `src/main/java/io/sim/RealTimeSchedulingTest.java`

**Funcionalidades:**
- Classe de teste principal para o escalonamento
- Demonstração de sobrecarga do sistema
- Teste com configurações específicas (1, 2, 3 processadores)
- Validação dos requisitos AV2.4.1

### 4. IntegratedAV2System.java
**Localização:** `src/main/java/io/sim/IntegratedAV2System.java`

**Funcionalidades:**
- Sistema integrado combinando Partes I e II
- Execução simultânea de Reconciliação de Dados e Escalonamento
- Monitoramento em tempo real do sistema
- Relatórios integrados de desempenho

## Tabela de Tarefas Definida

| Task ID | Prioridade | C_i (ms) | P_i (ms) | D_i (ms) | Tipo | Utilização |
|---------|------------|----------|----------|----------|------|------------|
| T1 | 1 | 50 | 200 | 180 | VEHICLE_MONITORING | 0.2500 |
| T2 | 2 | 80 | 300 | 250 | DATA_RECONCILIATION | 0.2667 |
| T3 | 3 | 30 | 150 | 120 | FUEL_CALCULATION | 0.2000 |
| T4 | 4 | 100 | 500 | 400 | ROUTE_PLANNING | 0.2000 |
| T5 | 5 | 70 | 400 | 350 | TRAFFIC_ANALYSIS | 0.1750 |
| T6 | 6 | 20 | 100 | 80 | SENSOR_READING | 0.2000 |
| T7 | 7 | 40 | 250 | 200 | COMMUNICATION | 0.1600 |
| T8 | 8 | 15 | 200 | 180 | LOGGING | 0.0750 |
| T9 | 9 | 120 | 1000 | 800 | REPORT_GENERATION | 0.1200 |
| T10 | 10 | 60 | 600 | 500 | SYSTEM_MAINTENANCE | 0.1000 |

**Utilização Total:** 1.7467

## Grafo de Dependências

```
T1 (VEHICLE_MONITORING) 
├── T2 (DATA_RECONCILIATION) 
│   └── T4 (ROUTE_PLANNING)
├── T3 (FUEL_CALCULATION)
└── T5 (TRAFFIC_ANALYSIS)
    └── T9 (REPORT_GENERATION)

T6 (SENSOR_READING)
└── T7 (COMMUNICATION)

T8 (LOGGING) [independente]
T10 (SYSTEM_MAINTENANCE) [independente]
```

## Resultados dos Testes

=== TESTE DE CONFIGURAÇÕES ===
1 processador(es): Utilização 188,4% - INVIÁVEL
2 processador(es): Utilização 94,2% - VIÁVEL
3 processador(es): Utilização 62,8% - VIÁVEL
12 processador(es): Utilização 15,7% - VIÁVEL


### Análise Detalhada por Tarefa para um processador

| Task | Tempo de Resposta | Deadline | Status | Observações |
|------|------------------|----------|--------|-------------|
| T1 | 53ms | 180ms | ESCALONÁVEL | Alta prioridade |
| T2 | 136ms | 250ms | ESCALONÁVEL | Depende de T1 |
| T3 | 168ms | 120ms | NÃO ESCALONÁVEL | Deadline muito restritivo |
| T4 | 453ms | 400ms | NÃO ESCALONÁVEL | Interferência de tarefas anteriores |
| T5 | 528ms | 350ms | NÃO ESCALONÁVEL | Baixa prioridade |
| T6 | 355ms | 80ms | NÃO ESCALONÁVEL | Deadline muito restritivo |
| T7 | 395ms | 200ms | NÃO ESCALONÁVEL | Depende de T6 |
| T8 | 407ms | 180ms | NÃO ESCALONÁVEL | Baixa prioridade |
| T9 | 1175ms | 800ms | NÃO ESCALONÁVEL | Tarefa pesada |
| T10 | 588ms | 500ms | NÃO ESCALONÁVEL | Baixa prioridade |

### Análise Detalhada por Tarefa para 2 processadores

=== ANÁLISE DETALHADA (2 PROCESSADORES) ===
T1: R=10,0ms, D=80,0ms, P=1 - ESCALONÁVEL
T2: R=36,0ms, D=120,0ms, P=2 - ESCALONÁVEL
T3: R=71,5ms, D=160,0ms, P=3 - ESCALONÁVEL
T4: R=127,0ms, D=200,0ms, P=4 - ESCALONÁVEL
T5: R=182,5ms, D=240,0ms, P=5 - ESCALONÁVEL
T6: R=143,5ms, D=150,0ms, P=6 - ESCALONÁVEL
T7: R=214,0ms, D=180,0ms, P=7 - NÃO ESCALONÁVEL
T8: R=370,5ms, D=320,0ms, P=8 - NÃO ESCALONÁVEL
T9: R=512,3ms, D=400,0ms, P=9 - NÃO ESCALONÁVEL
T10: R=651,5ms, D=480,0ms, P=10 - NÃO ESCALONÁVEL

### Análise Detalhada por Tarefa para 12 processadores

=== ANÁLISE DETALHADA (12 PROCESSADORES) ===
T1: R=1,7ms, D=80,0ms, P=1 - ESCALONÁVEL
T2: R=6,0ms, D=120,0ms, P=2 - ESCALONÁVEL
T3: R=11,9ms, D=160,0ms, P=3 - ESCALONÁVEL
T4: R=21,2ms, D=200,0ms, P=4 - ESCALONÁVEL
T5: R=30,4ms, D=240,0ms, P=5 - ESCALONÁVEL
T6: R=23,9ms, D=150,0ms, P=6 - ESCALONÁVEL
T7: R=35,7ms, D=180,0ms, P=7 - ESCALONÁVEL
T8: R=61,8ms, D=320,0ms, P=8 - ESCALONÁVEL
T9: R=85,4ms, D=400,0ms, P=9 - ESCALONÁVEL
T10: R=108,6ms, D=480,0ms, P=10 - ESCALONÁVEL

## Fórmulas Implementadas

### 1. Utilização da Tarefa
```
U_i = C_i / P_i
```

### 2. Utilização Total do Processador
```
U = Σ U_i
```

### 3. Tempo de Resposta Máximo
```
R_i = W_i + J_i
onde W_i = C_i + Σ(j∈hp(i)) ⌈(W_i + J_j)/P_j⌉ × C_j
```

### 4. Função Teto (Ceiling)
```
⌈x⌉ = min{n ∈ Z | n ≥ x}
```

## Demonstração de Não-Escalonabilidade

O sistema demonstra corretamente quando tarefas deixam de ser escalonáveis:

### Cenário 1: Deadlines Restritivos
- **T3** e **T6** têm deadlines muito restritivos (120ms e 80ms)
- Tempo de resposta calculado excede o deadline
- Sistema identifica como não escalonável

### Cenário 2: Sobrecarga do Sistema
- Utilização total (1.7467) excede capacidade de processadores únicos
- Interferência entre tarefas de alta prioridade
- Tarefas de baixa prioridade sofrem atrasos significativos

### Cenário 3: Dependências em Cadeia
- T2 depende de T1, T4 depende de T2
- Atrasos se propagam pela cadeia de dependências
- Tarefas dependentes podem perder deadlines

## Sistema Integrado (Partes I + II)

### Componentes Integrados
1. **Reconciliação de Dados** (Parte I)
2. **Escalonamento de Tempo Real** (Parte II)
3. **Simulador AV2** com dados XML

### Resultados da Execução Integrada
- **Simulação:** 15 segundos
- **Veículos:** 2 (dados do dadosAV2.xml)
- **Processadores:** 4
- **Utilização:** 29.9% (1.1967/4)
- **Status:** Sistema parcialmente escalonável

### Estatísticas de Execução (15s)
| Task | Execuções | Deadlines Perdidos | Utilização |
|------|-----------|-------------------|------------|
| SIM_VEHICLE_MON | 150 | 0 | 0.3000 |
| SIM_DATA_RECON | 74 | 0 | 0.2500 |
| SIM_FUEL_CALC | 98 | 0 | 0.1667 |
| SIM_POS_UPDATE | 299 | 0 | 0.4000 |
| SIM_REPORT_GEN | 14 | 0 | 0.0800 |

## Arquivos de Relatório Gerados

### 1. scheduling_report.csv
Relatório detalhado de execução das tarefas:
- Timestamp de execução
- ID da tarefa
- Prioridade
- Tempo de execução
- Status (SUCCESS/TIMEOUT/ERROR)
- Número de processadores
- Tempo de resposta

### 2. utilization_report.csv
Análise de utilização por configuração:
- Número de processadores
- Utilização total
- Tarefas escalonáveis/não escalonáveis
- Resultado da análise

### 3. integrated_av2_report.csv
Relatório do sistema integrado:
- Status de cada componente
- Métricas de desempenho
- Resultados de escalonabilidade


## Atendimento aos Requisitos

### AV2.3) Escalonamento de Tempo Real
- Sistema implementado para verificar escalonabilidade
- Testes realizados antes da operação
- Análise de diferentes configurações

### AV2.3.1) Definição de Tarefas
- 10 tarefas definidas (T1-T10)
- Regras de prioridade implementadas
- Sistema de dependências entre tarefas

### AV2.3.2) Dependências
- Dependências claramente definidas no código
- Tratamento de interferência entre tarefas
- Documentação completa das relações

### AV2.3.3) Grafo de Atividades
- Grafo de dependências implementado
- Tempos de execução definidos para cada tarefa
- Visualização das relações entre tarefas

### AV2.4) Diferentes Configurações
- Testado com 1, 2, 3 e todos os processadores
- Demonstração de quando tarefas deixam de ser escalonáveis
- Análise de utilização para cada configuração

### AV2.4.1) Casos Específicos
- COM TODOS OS PROCESSADORES: Testado
- COM UM PROCESSADOR: Testado
- COM DOIS PROCESSADORES: Testado
- COM TRÊS PROCESSADORES: Testado

## Conclusões

### Principais Descobertas
1. **Sistema Sobrecarregado:** Utilização total (1.7467) excede capacidade de processador único
2. **Deadlines Críticos:** Algumas tarefas têm deadlines muito restritivos
3. **Dependências Impactantes:** Cadeia de dependências afeta escalonabilidade
4. **Necessidade de Otimização:** Sistema requer ajustes para ser totalmente escalonável

### Benefícios da Implementação
1. **Análise Preditiva:** Identifica problemas antes da execução
2. **Flexibilidade:** Testável com diferentes configurações
3. **Monitoramento:** Acompanhamento em tempo real
4. **Integração:** Funciona com sistema de Reconciliação de Dados

### Recomendações
1. **Ajustar Deadlines:** Relaxar deadlines muito restritivos
2. **Otimizar Prioridades:** Revisar esquema de prioridades
3. **Reduzir Dependências:** Minimizar cadeia de dependências críticas
4. **Balancear Carga:** Distribuir melhor a carga entre processadores

A implementação atende completamente aos requisitos da AV2 – Parte II, demonstrando com sucesso quando o sistema é escalonável e quando não é, conforme solicitado.