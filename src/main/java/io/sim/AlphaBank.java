package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class AlphaBank extends Servidor {
    private double valorInicial = 300;
    private int contasCont;
    private ArrayList<Cliente> listaClientes;
    Client cli;
    Servidor serverBank;
    private int idPagamento;
    private ManipuladorCSV manipulador;

    private class Cliente {
        private String nome;
        private String conta;
        private double saldo;
        private String senha;
        private String ID;

        public Cliente(String ID, String nome, String senha, String conta, double saldo) {
            this.ID = ID;
            this.nome = nome;
            this.senha = senha;
            this.conta = conta;
            this.saldo = saldo;
        }

        public double getSaldo() {
            return saldo;
        }

        public String getConta() {
            return conta;
        }

        public void depositar(Double valor) {
            saldo = saldo + valor;
        }

        public void sacar(Double valor) {
            saldo = saldo - valor;

        }

        public String getSenha() {
            return senha;
        }
    }

    public AlphaBank() {
        super("12346");
        idPagamento = 0;
        contasCont = 0;
        listaClientes = new ArrayList<Cliente>();
        addClients();
        manipulador = new ManipuladorCSV("reports/banco.csv");
        String[] cabecalho = { "ID Pagamento", "TimeStamp", "Nome Pagador", "Nome Recebedor", "Valor" };
        manipulador.writeCSV(cabecalho);

    }

    private void addClients() {
        try {
            listaClientes.add(new Cliente("1000", "Company",
                    Codec.toHexString(Codec.getSHA("079816")), "7559858051", 10000000));
            listaClientes.add(new Cliente("1001", "Posto",
                    Codec.toHexString(Codec.getSHA("984321")), "1551363287", 10000000));
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
                    listaClientes.add(new Cliente(nodeID.getNodeValue(), nodeNome.getNodeValue(),
                            Codec.toHexString(Codec.getSHA(nodeSenha.getNodeValue())), nodeConta.getNodeValue(),
                            valorInicial));
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
    }

    @Override
    protected void handleClientMessage(Socket clientSocket, String message, BufferedWriter writer) {

        idPagamento++;
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String comando = jsonObject.get("comando").getAsString();
        if (comando.equals("pagar")) {

            String nomeSender = jsonObject.get("nomeSender").getAsString();
            String nomeRec = jsonObject.get("nomeRec").getAsString();
            String timeStamp = jsonObject.get("timeStamp").getAsString();
            String contaBancoSender = jsonObject.get("contaBancoSender").getAsString();
            String contaBancoRec = jsonObject.get("contaBancoRec").getAsString();
            String valor = jsonObject.get("valor").getAsString();
            String senhasender = jsonObject.get("senhaSender").getAsString();

            for (Cliente cli : listaClientes) {
                if (cli.getConta().equals(contaBancoSender)) {
                    if (cli.getSenha().equals(senhasender)) {
                        cli.sacar(Double.parseDouble(valor));
                    } else {
                        System.out.println("Senha Banco: " + cli.getSenha() + " Senha Req: " + senhasender);
                    }
                }
                if (cli.getConta().equals(contaBancoRec)) {
                    cli.depositar(Double.parseDouble(valor));
                }
            }
            String[] res = { String.valueOf(idPagamento), timeStamp, nomeSender, nomeRec, String.valueOf(valor) };
            manipulador.appendCSV(res);
        }

    }
}
