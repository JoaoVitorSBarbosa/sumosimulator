package io.sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ManipuladorCSV {
    private String caminho;

    public ManipuladorCSV(String caminhoDoArquivo) {
        this.caminho = caminhoDoArquivo;
        // Ensure the directory exists
        ensureDirectoryExists();
    }

    /**
     * Ensures that the directory for the CSV file exists
     */
    private void ensureDirectoryExists() {
        File file = new File(caminho);
        File directory = file.getParentFile();
        
        if (directory != null && !directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created directory: " + directory.getAbsolutePath());
            } else {
                System.err.println("Failed to create directory: " + directory.getAbsolutePath());
            }
        }
    }

    public void delete() {
        File file = new File(caminho);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Deleted file: " + caminho);
            } else {
                System.err.println("Failed to delete file: " + caminho);
            }
        }
    }

    public void writeCSV(String[] info) {
        try {
            // Ensure directory exists before writing
            ensureDirectoryExists();
            
            BufferedWriter obj = new BufferedWriter(new FileWriter(caminho));
            String result = Arrays.stream(info).collect(Collectors.joining(","));
            result = result + "\n";
            obj.write(result);
            obj.close();
            System.out.println("Successfully wrote header to: " + caminho);
        } catch (IOException e) {
            System.err.println("Error writing to file " + caminho + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void appendCSV(String[] info) {
        try {
            // Ensure directory exists before appending
            ensureDirectoryExists();
            
            // Check if file exists, if not create it with headers
            File file = new File(caminho);
            if (!file.exists()) {
                System.out.println("CSV file does not exist, creating new file: " + caminho);
                // You might want to write headers here if appropriate
            }
            
            BufferedWriter obj = new BufferedWriter(new FileWriter(caminho, true));
            String result = Arrays.stream(info).collect(Collectors.joining(","));
            result = result + "\n";
            obj.append(result);
            obj.close();
        } catch (IOException e) {
            System.err.println("Error appending to file " + caminho + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
