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
