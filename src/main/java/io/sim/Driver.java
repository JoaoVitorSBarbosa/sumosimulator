package io.sim;

public class Driver extends Client {
    private String conta;
    private String id;
    private String nome;
    private String senhaBanco;

    public Driver(String id, String nome, String senha, String conta, String porta) {
        super(porta);
        this.id = id;
        this.nome = nome;
        this.conta = conta;
        this.senhaBanco = senha;
    }
    public String getConta() {
        return conta;
    }
    public String getNome() {
        return nome;
    }
    public String getDriverId() {
        return id;
    }
    public String getSenhaBanco() {
        return senhaBanco;
    }
}