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
            obj = new BufferedWriter(new FileWriter(caminho));
            String result = Arrays.stream(info).collect(Collectors.joining(","));
            result = result + "\n";
            obj.write(result);
            obj.close();
        } catch (IOException e) {
            System.out.println("Erro ao escrever o arquivo: " + e.getMessage());
        }
    }
    public synchronized void appendCSV(String[] info) {
        //System.out.println(Arrays.stream(info).collect(Collectors.joining(",")));
        try {
            obj = new BufferedWriter(new FileWriter(caminho,true));
            String result = Arrays.stream(info).collect(Collectors.joining(","));
            result = result + "\n";
            obj.append(result);
            obj.close();
        } catch (IOException e) {
            System.out.println("Erro ao escrever o arquivo: " + e.getMessage());
        }
    }
}
