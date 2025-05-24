package io.sim;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ManipuladorCSV {
    private BufferedWriter obj;
    private String caminho; 

    public ManipuladorCSV(String caminhoDoArquivo) {
        this.caminho = caminhoDoArquivo;
        try {
            obj = new BufferedWriter(new FileWriter(caminho));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete()  {
        File file = new File(caminho);
        if (file.exists()) {
            file.delete();
        }

        // Verifica se o arquivo existe e tenta deletá-lo
       
    }
    
    public void writeCSV(String[] info) {
        try {
            String result = Arrays.stream(info).collect(Collectors.joining(","));
            result = result + "\n";
            obj.write(result);
        } catch (IOException e) {
            System.out.println("Erro ao escrever o arquivo: " + e.getMessage());
        }
    }
    public void appendCSV(String[] info) {
        try {
            String result = Arrays.stream(info).collect(Collectors.joining(","));
            result = result + "\n";
            obj.append(result);
        } catch (IOException e) {
            System.out.println("Erro ao escrever o arquivo: " + e.getMessage());
        }
    }
}
