package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client extends Thread {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader; // Added for receiving messages
    private String serverAddress;
    private int serverPort;
    private volatile boolean connected = false; // Flag to track connection status
    private static final int MAX_RETRY_ATTEMPTS = 5; // Maximum number of connection retry attempts
    private static final int RETRY_DELAY_MS = 1000; // Delay between retries in milliseconds

    public Client(String port) {
        this.serverAddress = "127.0.0.1";
        try {
            this.serverPort = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number format: " + port);
            // Handle error appropriately, maybe throw exception or set default
            this.serverPort = -1; // Indicate invalid port
        }
    }
    
    @Override
    public void run() {
        // Try to connect with retries
        connectWithRetry();
        startListening();
    }
    
    // New method that implements connection retry logic
    public boolean connectWithRetry() {
        if (connected) {
            System.out.println("Already connected.");
            return true;
        }
        
        if (serverPort < 0 || serverPort > 65535) {
             System.err.println("Invalid port number: " + serverPort);
             return false;
        }
        
        int attempts = 0;
        boolean success = false;
        
        while (attempts < MAX_RETRY_ATTEMPTS && !success) {
            attempts++;
            System.out.println("Connection attempt " + attempts + " of " + MAX_RETRY_ATTEMPTS + 
                               " to " + serverAddress + ":" + serverPort);
            
            success = connect();
            
            if (!success && attempts < MAX_RETRY_ATTEMPTS) {
                System.out.println("Connection failed. Retrying in " + (RETRY_DELAY_MS/1000) + " seconds...");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Retry interrupted: " + e.getMessage());
                    return false;
                }
            }
        }
        
        if (!success) {
            System.err.println("Failed to connect after " + attempts + " attempts. Giving up.");
        }
        
        return success;
    }
    
    // Connect method now handles stream initialization
    public boolean connect() {
        if (connected) {
            System.out.println("Already connected.");
            return true;
        }
        if (serverPort < 0 || serverPort > 65535) {
             System.err.println("Invalid port number: " + serverPort);
             return false;
        }
        try {
            System.out.println("Attempting to connect to " + serverAddress + ":" + serverPort + "...");
            socket = new Socket();
            // Use a reasonable timeout
            socket.connect(new InetSocketAddress(serverAddress, serverPort), 5000); // 5-second timeout
            
            // Initialize streams ONCE after successful connection
            // Use UTF-8 encoding for consistency
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); // Initialize reader too
            
            connected = true;
            System.out.println("Connection Successful!");
            
            // Optional: Start a separate thread to listen for incoming messages if needed
            // startListening(); 
            
            return true;

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
        } catch (SocketException e) {
             System.err.println("Socket error during connection: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Connection failed to " + serverAddress + ":" + serverPort + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Connection parameter error (e.g., port out of range): " + e.getMessage());
        }
        // Ensure cleanup if connection failed partway
        closeConnection(); 
        return false;
    }

    // Send message method now uses the initialized writer and adds newline
    public boolean sendMessage(String msg) {
        if (!connected || writer == null) {
            System.err.println("Cannot send message: Not connected.");
            return false;
        }
        try {
            //System.out.println("Sending: " + msg);
            writer.write(msg + "\n"); // *** Add newline character ***
            writer.flush(); // Ensure data is sent immediately
            return true;
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            // Consider the connection potentially broken
            closeConnection(); 
            return false;
        }
    }
    
    // Optional: Method to read a message from the server
    public String receiveMessage() {
        if (!connected || reader == null) {
            System.err.println("Cannot receive message: Not connected.");
            return null;
        }
        try {
            return reader.readLine(); // Reads one line
        } catch (SocketException e) {
             System.err.println("Socket error while reading: " + e.getMessage() + " (Connection likely closed)");
             closeConnection();
             return null;
        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
            closeConnection(); 
            return null;
        }
    }

    // Optional: Method to continuously listen for messages in a background thread
    public void startListening() {
        if (!connected) return;
        Thread listenerThread = new Thread(() -> {
            System.out.println("Client listener started...");
            while (connected) {
                String message = receiveMessage();
                if (message != null) {
                    System.out.println("Received from server: " + message);
                    // Process the received message here
                } else {
                    // readLine returned null, usually means end of stream/connection closed
                    System.out.println("Server disconnected or stream closed.");
                    closeConnection(); // Ensure cleanup
                    break; // Exit listener loop
                }
            }
            System.out.println("Client listener stopped.");
        });
        listenerThread.setDaemon(true); // Allow application to exit even if listener is running
        listenerThread.start();
    }
    // Close connection method
    public void closeConnection() {
        if (!connected) return;
        connected = false; // Set flag immediately
        System.out.println("Closing connection...");
        try {
            // Close streams first (closing writer/reader often closes underlying stream)
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        // Nullify references
        writer = null;
        reader = null;
        socket = null;
    }

    public boolean isConnected() {
        return connected;
    }
}
