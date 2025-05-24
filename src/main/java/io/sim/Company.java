package io.sim;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.xs.StringList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Company extends Thread {
    private int conta;
    private BotPayment bot;
    private ArrayList<Driver> funcionarios;
    private ArrayList<Rota> rotasAguardando;
    private ArrayList<Rota> rotasEmExecucao;
    private ArrayList<Rota> rotasExecutadas;
    private ArrayList<Car> carros;
    private ArrayList<TransportService> servicos;
    private Boolean on_off;
    SumoColor green;
    private SumoTraciConnection sumo;
    private int aqRate;
    private String senha;

    private class BotPayment extends Thread {
        private double valorKM = 3.25;
        private Client cliBot;
        private ManipuladorCSV csvBanco;

        public BotPayment() {
            csvBanco = new ManipuladorCSV("reports/company.csv");
            csvBanco.delete();
            String[] cabecalho = { "ID Pagamento", "ID Motorista", "Nome Motorista", "Valor" };
            csvBanco.writeCSV(cabecalho);
        }

        public void run() {
            try {
                cliBot.start();
                cliBot.run();
                cliBot.escutar();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        // public pagar(String driver, String idDriver, String contaBancoDriver, String
        // senhaCompany) {

        // }
        // public void ralatarPagamento(String driver, String idDriver, double quantia)
        // {

        // }
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
                    Node nodeID = nodes.item(0);
                    Node nodeNome = nodes.item(1);
                    drivers.add(new Driver(nodeID.getNodeValue(), nodeNome.getNodeValue()));
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
                            String.valueOf(funcionarios.get(i).getDriverId()), sumo, aqRate));
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

    public Company(SumoTraciConnection sumo) {
        this.on_off = true;
        aqRate = 500;
        green = new SumoColor(0, 255, 0, 126);
        this.sumo = sumo;
        this.funcionarios = getDrivers();
        this.carros = getCars();
        rotasAguardando = new ArrayList<Rota>();
        rotasEmExecucao = new ArrayList<Rota>();
        rotasExecutadas = new ArrayList<Rota>();
        this.servicos = new ArrayList<TransportService>();
        conta = 0;
        bot = new BotPayment();

        ReportsController.getArquivoCars().delete();
        String[] cabecalho = { "Timestamp", "ID Car", "ID Route", "Speed", "Distance", "FuelConsumption", "CO2Emission",
                "longitude(lon)", "latitude(lat)" };
        ReportsController.getArquivoCars().writeCSV(cabecalho);

        for (int i = 0; i < carros.size(); i++) {
            Rota rota = new Rota("data/dados.xml", carros.get(i).getIdAuto());
            rotasAguardando.add(rota);
            servicos.add(new TransportService(true, carros.get(i).getIdAuto(), rota, carros.get(i), sumo));
        }

    }

    @Override
    public void run() {
        for (int i = 0; i < funcionarios.size(); i++) {
            funcionarios.get(i).start();
        }
        for (int i = 0; i < servicos.size(); i++) {
            servicos.get(i).start();
            rotasEmExecucao.add(servicos.get(i).getRota());
            rotasAguardando.remove(servicos.get(i).getRota());
        }
        while (this.on_off) {
            try {
                removeCorridasFinalizadas();
                this.sumo.do_timestep();
            } catch (Exception e) {
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (sumo.isClosed()) {
                this.on_off = false;
            }
        }
    }

    public void setConta(int conta) {
        this.conta = conta;
    }

    public int getConta() {
        return conta;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSenha() {
        return senha;
    }

    public void pagar(String idDriver) {

    }

    public void removeCorridasFinalizadas() {
        try {
            String[] listaStrings;
            String corridasFinalizadas = String.valueOf(sumo.do_job_get(Simulation.getArrivedIDList()));
            if (!corridasFinalizadas.equals("[]")) {
                corridasFinalizadas = corridasFinalizadas.substring(0, corridasFinalizadas.length() - 1);
                corridasFinalizadas = corridasFinalizadas.substring(1, corridasFinalizadas.length());

                listaStrings = corridasFinalizadas.split(",");

                for (String s : listaStrings) {
                    System.out.println(s);
                    boolean found = false;
                    int idx = 0;

                    for (Rota rota : rotasEmExecucao) {

                        if (rota.getIDRota().equals(s)) {
                            found = true;
                            rotasExecutadas.add(rotasEmExecucao.remove(idx));
                            
                            servicos.get(idx).getAuto().setOn_off(false); //Para a thread
                        }
                        if (!found) {
                            idx++;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
