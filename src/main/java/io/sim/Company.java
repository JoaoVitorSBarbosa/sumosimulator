package io.sim;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import de.tudresden.sumo.objects.SumoStringList;
import io.sim.sumo.SumoCommandExecutor;
import io.sim.sumo.cmd.DoTimestepCommand;
import io.sim.sumo.cmd.GetArrivedIDListCommand;
import io.sim.sumo.cmd.GetArrivedNumberCommand;
import io.sim.sumo.cmd.GetTimeCommand;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

public class Company extends Thread {
    private double valorFinal = 347.47;
    private String senhaCompany = "079816";
    private String contaCompany = "7559858051";
    private int conta;
    private BotPayment bot;
    private ArrayList<Driver> funcionarios;
    private Collection<Rota> rotasAguardando;
    private Collection<Rota> rotasEmExecucao;
    private Collection<Rota> rotasExecutadas;
    private ArrayList<Car> carros;
    private Collection<TransportService> servicos;
    private volatile Boolean on_off;
    private SumoColor green;
    private SumoCommandExecutor sumoExecutor;
    private int aqRate;
    private String senha;
    private int arrived;
    private static ServerSocket server;
    private boolean simulationComplete = false;
    private long lastActivityTime = 0;
    private static final long INACTIVITY_TIMEOUT = 10000; // 10 seconds of inactivity before considering simulation complete

    public class CompanyServer extends Servidor {
        public CompanyServer() {
            super("12347");
        }

        @Override
        protected void handleClientMessage(Socket clientSocket, String message, BufferedWriter writer) {
        }

    }

    private class BotPayment extends Client {
        private double valorKM = 3.25;
        private ManipuladorCSV csvBanco;
        private int numPagamentos;

        public class Relatorio {
            private int idPagamento;
            private Double timeStamp;
            private String nomeSender;
            private String nomeRec;
            private String contaBancoRec;
            private String contaBancoSender;
            private Double valor;
            private String senhaSender;

            public Relatorio(int idPagamento, Double timeStamp, String nomeSender, String nomeRec, String contaBancoRec,
                    String contaBancoSender,
                    Double valor, String senhaSender) {
                this.idPagamento = idPagamento;
                this.timeStamp = timeStamp;
                this.nomeRec = nomeRec;
                this.contaBancoRec = contaBancoRec;
                this.valor = valor;
                this.senhaSender = senhaSender;
                this.contaBancoSender = contaBancoSender;
                this.nomeSender = nomeSender;
            }
        }

        public BotPayment(String porta) {
            super(porta);
            csvBanco = new ManipuladorCSV("reports/company.csv");
            csvBanco.delete();
            String[] cabecalho = { "ID Pagamento", "TimeStamp", "ID Motorista", "Nome Motorista", "Valor" };
            csvBanco.writeCSV(cabecalho);
            numPagamentos = 0;
        }

        public void pagar(Double timeStamp, String idDriver, String driver, String contaBancoDriver, Double valor,
                String senha) {
            Relatorio rel;
            try {
                rel = new Relatorio(numPagamentos, timeStamp, "Company", driver, contaBancoDriver, contaCompany, valor,
                        Codec.toHexString(Codec.getSHA(senha)));

                var gson = new Gson();

                String msg = gson.toJson(rel);
                numPagamentos++;
                sendMessage(msg);
                ralatarPagamento(timeStamp, driver, idDriver, valor);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        public void ralatarPagamento(Double timeStamp, String driver, String idDriver, Double quantia) {
            String[] info = { String.valueOf(numPagamentos), String.valueOf(timeStamp), driver, idDriver,
                    String.valueOf(quantia) };
            csvBanco.appendCSV(info);
        }
    }

    private ArrayList<Driver> getDrivers() {
        ArrayList<Driver> drivers = new ArrayList<Driver>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse("data/pessoas.xml");
            NodeList nList = doc.getElementsByTagName("pessoa");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;
                    NamedNodeMap nodes = elem.getAttributes();
                    Node nodeID = nodes.item(1);
                    Node nodeNome = nodes.item(2);
                    Node nodeSenha = nodes.item(3);
                    Node nodeConta = nodes.item(0);
                    Driver driver = new Driver(nodeID.getNodeValue(), nodeNome.getNodeValue(), nodeSenha.getNodeValue(),
                            nodeConta.getNodeValue(), "12347");
                    drivers.add(driver);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return drivers;
    }

    private ArrayList<Car> getCars() {
        ArrayList<Car> autos = new ArrayList<Car>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse("data/dados.xml");
            NodeList nList = doc.getElementsByTagName("vehicle");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;
                    NamedNodeMap nodes = elem.getAttributes();
                    Node nodeID = nodes.item(1);
                    autos.add(new Car(true, nodeID.getNodeValue(), green,
                            String.valueOf(funcionarios.get(i).getDriverId()), this.sumoExecutor, aqRate, "12347"));
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return autos;
    }

    public Company(SumoCommandExecutor sumoExecutor) {
        this.sumoExecutor = sumoExecutor;
        CompanyServer cs = new CompanyServer();
        cs.start();

        this.on_off = true;
        aqRate = 1000;
        green = new SumoColor(0, 255, 0, 126);

        this.funcionarios = getDrivers();
        this.carros = getCars();
        rotasAguardando = Collections.synchronizedCollection(new ArrayList<Rota>());
        rotasEmExecucao = Collections.synchronizedCollection(new ArrayList<Rota>());
        rotasExecutadas = Collections.synchronizedCollection(new ArrayList<Rota>());
        this.servicos = Collections.synchronizedCollection(new ArrayList<TransportService>());
        bot = new BotPayment("12346");
        bot.start();
        senha = "079816";
        for (int i = 0; i < carros.size(); i++) {
            Rota rota = new Rota("data/dados.xml", carros.get(i).getIdAuto());
            rotasAguardando.add(rota);
            servicos.add(new TransportService(true, carros.get(i).getIdAuto(), rota, carros.get(i), this.sumoExecutor));
        }
        
        // Initialize the last activity time
        updateActivityTimestamp();
    }

    @Override
    public void run() {
        System.out.println("Company thread started with " + funcionarios.size() + " drivers and " + 
                           carros.size() + " cars.");
        
        // Start driver threads
        for (Driver funcs : funcionarios) {
            funcs.start();
            try { Thread.sleep(10); } catch (InterruptedException e) { }
        }
        
        // Start transport service threads
        for (TransportService service : servicos) {
            service.start();
            rotasEmExecucao.add(service.getRota());
            rotasAguardando.remove(service.getRota());
            try { Thread.sleep(10); } catch (InterruptedException e) { }
        }
        
        System.out.println("All drivers and transport services started. Beginning simulation loop.");
        
        // Main simulation loop
        while (this.on_off) {
            try {
                // Submit timestep command and wait for it to complete
                CompletableFuture<Boolean> timestepFuture = sumoExecutor.submitCommand(new DoTimestepCommand());
                boolean stepOk = timestepFuture.get(); // Blocks until the step is done

                if (!stepOk) {
                    System.out.println("Simulation timestep failed or SUMO connection closed. Stopping Company.");
                    this.on_off = false;
                    break;
                }
                
                // Process any vehicles that have completed their routes
                boolean hadActivity = removeCorridasFinalizadas();
                
                // Update activity timestamp if we had any activity
                if (hadActivity) {
                    updateActivityTimestamp();
                    simulationComplete = false;
                }
                
                // Check if simulation should continue
                checkSimulationStatus();
                
            } catch (Exception e) {
                System.err.println("Error in Company main loop: " + e.getMessage());
                e.printStackTrace();
                // Don't exit on error, just continue the loop
            }
            
            try {
                Thread.sleep(50); // Short sleep to prevent CPU hogging
            } catch (InterruptedException e) {
                System.out.println("Company thread interrupted.");
                this.on_off = false;
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Company thread ending. Simulation complete.");
    }

    private void updateActivityTimestamp() {
        lastActivityTime = System.currentTimeMillis();
    }
    
    private void checkSimulationStatus() {
        // Check if all routes are complete
        if (rotasEmExecucao.isEmpty() && !rotasExecutadas.isEmpty()) {
            if (!simulationComplete) {
                simulationComplete = true;
                System.out.println("All routes completed. Waiting for inactivity timeout before ending simulation.");
                updateActivityTimestamp(); // Reset the timer when we first detect completion
            } else {
                // Check if we've been inactive for the timeout period
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastActivityTime > INACTIVITY_TIMEOUT) {
                    System.out.println("Inactivity timeout reached. Ending simulation.");
                    this.on_off = false;
                }
            }
        } else {
            // Still have active routes, reset the completion flag
            simulationComplete = false;
        }
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSenha() {
        return senha;
    }

    public void pagar(String idDriver, double valor) {
        Driver driverProcurado = null;
        for (Driver driver : funcionarios) {
            if (driver.getDriverId().equals(idDriver)) {
                driverProcurado = driver;
                break; // Exit loop once found
            }
        }

        if (driverProcurado == null) {
            System.err.println("Cannot pay driver: ID " + idDriver + " not found.");
            return;
        }

        try {
            CompletableFuture<Double> timeFuture = sumoExecutor.submitCommand(new GetTimeCommand());
            Double timeStamp = timeFuture.get();
            System.out.println("Paying driver " + driverProcurado.getNome() + " (ID: " + idDriver + ") amount " + valor);
            bot.pagar(timeStamp, idDriver, driverProcurado.getNome(), driverProcurado.getConta(), valor,
                    getSenha());
        } catch (NumberFormatException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean getOnOff() {
        return on_off;
    }

    // This method was causing premature termination - removed and replaced with more robust logic
    // public boolean getHaveCar() {
    //     return arrived <= carros.size();
    // }

    /**
     * Process vehicles that have completed their routes.
     * @return true if any vehicles were processed, false otherwise
     */
    public boolean removeCorridasFinalizadas() {
        boolean hadActivity = false;
        
        try {
            CompletableFuture<SumoStringList> arrivedIDsFuture = sumoExecutor
                    .submitCommand(new GetArrivedIDListCommand());
            SumoStringList arrivedIDs = arrivedIDsFuture.get();

            if (arrivedIDs == null || arrivedIDs.isEmpty()) {
                return false; // No activity
            }

            CompletableFuture<Integer> arrivedNumFuture = sumoExecutor.submitCommand(new GetArrivedNumberCommand());
            int arrivedNum = arrivedNumFuture.get();
            arrived += arrivedNum;
            
            System.out.println("Vehicles arrived this step: " + arrivedIDs.size() + " (Total arrived: " + arrived + ")");
            hadActivity = true;

            List<Rota> rotasParaRemover = new ArrayList<>();

            synchronized (rotasEmExecucao) {
                for (String arrivedID : arrivedIDs) {
                    Rota rotaEncontrada = null;
                    TransportService servicoAssociado = null;

                    for (Rota rota : rotasEmExecucao) {
                        if (rota.getIDRota().equals(arrivedID)) {
                            rotaEncontrada = rota;
                            break;
                        }
                    }

                    if (rotaEncontrada != null) {
                        rotasParaRemover.add(rotaEncontrada);
                        for (TransportService servico : servicos) {
                            if (servico.getIdTransportService().equals(arrivedID)) {
                                servicoAssociado = servico;
                                break;
                            }
                        }

                        if (servicoAssociado != null) {
                            Car carro = servicoAssociado.getAuto();
                            if (carro != null) {
                                System.out.println("Processing arrival for car: " + carro.getIdAuto());
                                carro.setOn_off(false); 
                                carro.interrupt(); 
                                pagar(carro.getDriverId(), valorFinal); 
                            } else {
                                System.err.println("Error: Service " + arrivedID + " has no associated car.");
                            }
                        } else {
                            System.err.println("Error: Arrived route " + arrivedID + " has no associated service.");
                        }
                    } else {
                        System.err.println(
                                "Warning: Arrived vehicle ID " + arrivedID + " not found in executing routes.");
                    }
                }

                if (!rotasParaRemover.isEmpty()) {
                    rotasEmExecucao.removeAll(rotasParaRemover);
                    rotasExecutadas.addAll(rotasParaRemover);
                    System.out.println("Removed " + rotasParaRemover.size() + " routes. Executing: "
                            + rotasEmExecucao.size() + ", Finished: " + rotasExecutadas.size());
                }
            }

        } catch (ExecutionException e) {
            System.err.println("Error getting SUMO arrival data: " + e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for SUMO arrival data.");
        } catch (Exception e) {
            System.err.println("Unexpected error in removeCorridasFinalizadas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return hadActivity;
    }
    
    /**
     * Shutdown method for Company
     */
    public void shutdown() {
        System.out.println("Company shutdown requested.");
        this.on_off = false; // Signal main loop to stop
        this.interrupt(); // Interrupt if sleeping or waiting in loop
    }
}
