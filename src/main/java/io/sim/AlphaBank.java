package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class AlphaBank extends Thread {
    private int contasCont;
    private ArrayList<Cliente> listaClientes;
    Client cli;
    Servidor serverBank;
    private static ServerSocket server;
    private static ArrayList<BufferedWriter> clientes;
    private String nome;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;

    private class Cliente extends Thread {
        private String nome;
        private String conta;
        private double saldo;
        private String senha;

        public Cliente(String nome, String conta, int saldo) {
            this.nome = nome;
            this.conta = conta;
            this.saldo = saldo;
        }

        public double getSaldo() {
            return saldo;
        }

        public String getNome() {
            return nome;
        }

        public String getConta() {
            return conta;
        }

        public void depositar(int valor) {
            saldo = saldo + valor;
        }

        public void sacar(int valor, String senhaHash) {
            if (this.senha.equals(senhaHash)) {
                saldo = saldo - valor;
            }
            System.out.println("As senhas não batem");
        }

        public void setSenha(String senha) {
            try {
                this.senha = Codec.toHexString(Codec.getSHA(senha));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public AlphaBank() {
        contasCont = 0;
        listaClientes = new ArrayList<Cliente>();
        try {
            server = new ServerSocket(12346);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        clientes = new ArrayList<BufferedWriter>();
        while (true) {
            System.out.println("Aguardando conexão...");
            Socket con;
            try {
                con = server.accept();
                System.out.println("Cliente conectado...");
                Thread serverBank = new Servidor(con);
                serverBank.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
           
        }
    }

    public void run() {
        cli = new Client();

        try {
            cli.start();
            cli.run();
            cli.escutar();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public int addClient(String nome) {
        String contaString = String.format("%05d", contasCont);
        listaClientes.add(new Cliente(nome, contaString, 0));
        contasCont++;
        return contasCont - 1;
    }

    public double getSaldo(String conta) {
        Cliente cli = listaClientes.get(listaClientes.indexOf(conta));
        return cli.getSaldo();
    }

    public void depositar(String conta, int valor) {
        Cliente cli = listaClientes.get(listaClientes.indexOf(conta));
        cli.depositar(valor);
    }

    public void sacar(String conta, int valor, String senhaHash) {
        Cliente cli = listaClientes.get(listaClientes.indexOf(conta));
        cli.sacar(valor, senhaHash);
    }
}
