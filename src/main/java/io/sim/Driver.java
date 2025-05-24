package io.sim;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Driver extends Thread {
    private int conta;
    private String id;
    private String nome;
    private String senhaBanco;

    public Driver(String id, String nome) {
        this.id = id;
        this.nome = nome;
        conta = 0;
        senhaBanco = "";
    }
    public void setConta(int conta) {
        this.conta = conta;
    }
    public int getConta() {
        return conta;
    }
    public String getNome() {
        return nome;
    }
    public String getDriverId() {
        return id;
    }
    public void setSenhaBanco(String senha) {
        senhaBanco = senha;
    }
    public String getSenhaBanco() {
        return senhaBanco;
    }
}