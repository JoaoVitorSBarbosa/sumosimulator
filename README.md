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

mvn install:install-file -Dfile="./lib/sumo/libsumo-1.18.0-sources.jar" -DgroupId="libsumo-1.18.0-sources" -DartifactId="libsumo-1.18.0-sources" -Dversion="libsumo-1.18.0-sources" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libsumo-1.18.0.jar" -DgroupId="libsumo-1.18.0" -DartifactId="libsumo-1.18.0" -Dversion="libsumo-1.18.0" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libtraci-1.18.0-sources.jar" -DgroupId="libtraci-1.18.0-sources" -DartifactId="libtraci-1.18.0-sources" -Dversion="libtraci-1.18.0-sources" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/libtraci-1.18.0.jar" -DgroupId="libtraci-1.18.0" -DartifactId="libtraci-1.18.0" -Dversion="libtraci-1.18.0" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/lisum-core.jar" -DgroupId="lisum-core" -DartifactId="lisum-core" -Dversion="lisum-core" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/lisum-gui.jar" -DgroupId="lisum-gui" -DartifactId="lisum-gui" -Dversion="lisum-gui" -Dpackaging="jar" -DgeneratePom=true

mvn install:install-file -Dfile="./lib/sumo/TraaS.jar" -DgroupId="TraaS" -DartifactId="TraaS" -Dversion="TraaS" -Dpackaging="jar" -DgeneratePom=true

mvn clean install

# Passo 9 - Executar o programa

Reinicie o VS Code e execute o programa:
