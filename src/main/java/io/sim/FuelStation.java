package io.sim;

public class FuelStation extends Client {
    private String conta;
    private String senha;

    public FuelStation(String porta) {
        super(porta);
        conta = "1551363287";
        senha = "984321";
    }
    public String getConta() {
        return conta;
    }
    public String getSenha() {
        return senha;
    }
}

