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
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Thread{
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String serverAddress;
    private int serverPort;
    private volatile boolean connected = false;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int RETRY_DELAY_MS = 1000;
    private final AtomicBoolean listenerRunning = new AtomicBoolean(false);
    private Thread listenerThread;

    public Client(String port) {
        this.serverAddress = "127.0.0.1";
        try {
            this.serverPort = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number format: " + port);
            this.serverPort = -1;
        }
    }
    
    /**
     * Attempts to connect to the server with retry logic
     * @return true if connection was successful, false otherwise
     */
    public boolean connectWithRetry() {
        if (connected) {
            System.out.println("Already connected to " + serverAddress + ":" + serverPort);
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
    
    /**
     * Attempts to connect to the server once
     * @return true if connection was successful, false otherwise
     */
    public boolean connect() {
        if (connected) {
            System.out.println("Already connected to " + serverAddress + ":" + serverPort);
            return true;
        }
        
        if (serverPort < 0 || serverPort > 65535) {
             System.err.println("Invalid port number: " + serverPort);
             return false;
        }
        
        try {
            System.out.println("Attempting to connect to " + serverAddress + ":" + serverPort + "...");
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, serverPort), 5000);
            
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            
            connected = true;
            System.out.println("Connection Successful to " + serverAddress + ":" + serverPort);
            
            return true;

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
        } catch (SocketException e) {
             System.err.println("Socket error during connection: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Connection failed to " + serverAddress + ":" + serverPort + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Connection parameter error: " + e.getMessage());
        }
        
        closeConnection();
        return false;
    }

    /**
     * Sends a message to the server
     * @param msg The message to send
     * @return true if message was sent successfully, false otherwise
     */
    public boolean sendMessage(String msg) {
        if (!ensureConnected()) {
            System.err.println("Cannot send message: Not connected.");
            return false;
        }
        
        try {
            writer.write(msg + "\n");
            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            closeConnection();
            return false;
        }
    }
    
    /**
     * Ensures the client is connected before attempting operations
     * Will try to reconnect if not connected
     * @return true if connected (or reconnected), false otherwise
     */
    public boolean ensureConnected() {
        if (connected && socket != null && !socket.isClosed()) {
            return true;
        }
        
        // Not connected, try to reconnect
        System.out.println("Connection lost or not established. Attempting to reconnect...");
        return connectWithRetry();
    }
    
    /**
     * Receives a message from the server
     * @return The message received, or null if an error occurred
     */
    public String receiveMessage() {
        if (!ensureConnected()) {
            System.err.println("Cannot receive message: Not connected.");
            return null;
        }
        
        try {
            return reader.readLine();
        } catch (SocketException e) {
             System.err.println("Socket error while reading: " + e.getMessage());
             closeConnection();
             return null;
        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    /**
     * Starts a background thread to listen for messages from the server
     */
    public void startListening() {
        if (!ensureConnected()) {
            System.err.println("Cannot start listener: Not connected.");
            return;
        }
        
        if (listenerRunning.get()) {
            System.out.println("Listener already running.");
            return;
        }
        
        listenerThread = new Thread(() -> {
            listenerRunning.set(true);
            System.out.println("Client listener started for " + serverAddress + ":" + serverPort);
            
            while (connected && listenerRunning.get()) {
                String message = receiveMessage();
                if (message != null) {
                    handleServerMessage(message);
                } else {
                    System.out.println("Server disconnected or stream closed.");
                    closeConnection();
                    break;
                }
            }
            
            listenerRunning.set(false);
            System.out.println("Client listener stopped for " + serverAddress + ":" + serverPort);
        });
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Handles messages received from the server
     * Override this method in subclasses to provide custom handling
     * @param message The message received from the server
     */
    protected void handleServerMessage(String message) {
        System.out.println("Received from server: " + message);
    }
    
    /**
     * Closes the connection to the server
     */
    public void closeConnection() {
        if (!connected) return;
        
        connected = false;
        System.out.println("Closing connection to " + serverAddress + ":" + serverPort);
        
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Connection closed to " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        writer = null;
        reader = null;
        socket = null;
        
        // Stop the listener thread if it's running
        if (listenerRunning.get()) {
            listenerRunning.set(false);
            if (listenerThread != null) {
                listenerThread.interrupt();
            }
        }
    }

    /**
     * Checks if the client is connected to the server
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
