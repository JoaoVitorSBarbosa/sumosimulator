package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Servidor extends Thread {
    // Use a thread-safe list for client writers
    protected final List<BufferedWriter> clientes = Collections.synchronizedList(new ArrayList<>());
    protected final String porta;
    protected ServerSocket server;
    // Use a thread pool for handling clients for better resource management
    protected ExecutorService clientHandlerPool;
    protected volatile boolean running = true;

    public Servidor(String porta) {
        this.porta = porta;
        // Use a fixed thread pool. Adjust size as needed.
        this.clientHandlerPool = Executors.newCachedThreadPool(); 
    }

    // Initialization separated for clarity and potential override
    protected boolean initializeServer() {
        try {
            int portNumber = Integer.parseInt(porta);
            if (portNumber < 0 || portNumber > 65535) {
                 throw new IllegalArgumentException("Port number out of range: " + porta);
            }
            server = new ServerSocket(portNumber);
            System.out.println(getClass().getSimpleName() + " started on port: " + porta);
            running = true;
            return true;
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number format: " + porta);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + porta + " - " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        if (!initializeServer()) {
            System.err.println("Server initialization failed. Exiting.");
            return;
        }
        
        System.out.println(getClass().getSimpleName() + " waiting for connections on port " + porta + "...");
        while (running && !server.isClosed()) {
            try {
                Socket clientSocket = server.accept(); // Blocks until a connection is made
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                // Create and submit a new client handler task to the thread pool
                ClientHandler clientHandler = createClientHandler(clientSocket);
                clientHandlerPool.submit(clientHandler);

            } catch (SocketException e) {
                if (!running || server.isClosed()) {
                    System.out.println("Server socket closed or shutdown initiated. Stopping accept loop.");
                } else {
                    System.err.println("SocketException during accept: " + e.getMessage());
                }
                // Assuming shutdown if exception occurs while running is true
                running = false; 
            } catch (IOException e) {
                 if (running) {
                    System.err.println("Accept failed or I/O error: " + e.getMessage());
                    // Consider whether to stop or continue after accept errors
                 }
            }
        }
        System.out.println(getClass().getSimpleName() + " accept loop finished.");
        shutdownClientHandlers(); // Ensure pool is shut down when loop exits
    }

    // Factory method to allow subclasses to provide their own ClientHandler implementation if needed
    protected ClientHandler createClientHandler(Socket clientSocket) {
        return new ClientHandler(clientSocket, this); // Pass Servidor instance
    }

    /**
     * Handles an incoming message from a specific client.
     * Subclasses should override this method to implement custom message processing logic.
     * The default implementation simply logs the message.
     *
     * @param clientSocket The socket of the client who sent the message.
     * @param message The message string received from the client.
     * @param writer The BufferedWriter associated with this client, for sending responses.
     */
    protected void handleClientMessage(Socket clientSocket, String message, BufferedWriter writer) {
        System.out.println("Default Handler - Message Received from " + 
                           clientSocket.getInetAddress().getHostAddress() + ": " + message);
    }
    
    /**
     * Called when a client disconnects or an error occurs in its handler.
     * Subclasses can override this to perform cleanup or logging specific to a client departure.
     * 
     * @param clientSocket The socket of the disconnecting client.
     * @param writer The writer associated with the client (may be null if setup failed).
     */
    protected void handleClientDisconnect(Socket clientSocket, BufferedWriter writer) {
         System.out.println("Default Handler - Client disconnected: " + 
                           (clientSocket != null ? clientSocket.getInetAddress().getHostAddress() : "unknown"));
         if (writer != null) {
             clientes.remove(writer);
             System.out.println("Client writer removed. Current client count: " + clientes.size());
         }
    }



    // Graceful shutdown method
    public void shutdown() {
        System.out.println("Shutting down server " + getClass().getSimpleName() + "...");
        running = false; // Signal loops to stop
        try {
            if (server != null && !server.isClosed()) {
                server.close(); // Close the server socket, stops the accept loop
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        shutdownClientHandlers();
        System.out.println("Server " + getClass().getSimpleName() + " shut down complete.");
    }
    
    private void shutdownClientHandlers() {
         // Shutdown the executor service gracefully
        if (clientHandlerPool != null && !clientHandlerPool.isShutdown()) {
            System.out.println("Shutting down client handler pool...");
            clientHandlerPool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!clientHandlerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Client handler pool did not terminate in time, forcing shutdown...");
                    clientHandlerPool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!clientHandlerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("Client handler pool did not terminate after forcing.");
                    }
                } else {
                     System.out.println("Client handler pool shut down gracefully.");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                clientHandlerPool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
        // Close remaining client connections (optional, as pool shutdown should handle it)
        closeAllClientConnections();
    }
    
    private void closeAllClientConnections() {
         synchronized (clientes) {
            System.out.println("Closing all client connections...");
            // Iterate over a copy to avoid ConcurrentModificationException if remove happens elsewhere
            List<BufferedWriter> writersToClose = new ArrayList<>(clientes);
            for (BufferedWriter bw : writersToClose) {
                try {
                    bw.close(); // This should close the underlying socket stream
                } catch (IOException e) {
                    System.err.println("Error closing a client writer during shutdown: " + e.getMessage());
                }
            }
            clientes.clear();
            System.out.println("All client connections closed.");
        }
    }

    // --- Inner Class for Handling Client Connections ---
    // Made protected to allow potential subclassing if absolutely necessary, 
    // but overriding handleClientMessage in Servidor subclass is preferred.
    protected static class ClientHandler implements Runnable {
        protected final Socket clientSocket;
        protected final Servidor serverInstance; // Reference to the Servidor instance
        protected BufferedWriter writer = null;
        protected BufferedReader reader = null;

        public ClientHandler(Socket socket, Servidor serverInstance) {
            this.clientSocket = socket;
            this.serverInstance = serverInstance;
        }

        @Override
        public void run() {
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            System.out.println("Client handler started for: " + clientIp);
            
            try {
                // Initialize streams using try-with-resources for automatic closing
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));

                // Add this client's writer to the server's list
                serverInstance.clientes.add(writer);
                System.out.println("Client writer added for " + clientIp + ". Current client count: " + serverInstance.clientes.size());

                String line;
                // Loop to continuously read messages from the client
                while (serverInstance.running && (line = reader.readLine()) != null) {
                    // *** Call the customizable message handler in the Servidor instance ***
                    serverInstance.handleClientMessage(clientSocket, line, writer);
                }

            } catch (SocketException e) {
                if (serverInstance.running) {
                   System.err.println("SocketException for client " + clientIp + ": " + e.getMessage() + " (Client likely disconnected)");
                }
            } catch (IOException e) {
                 if (serverInstance.running) {
                    System.err.println("IOException in ClientHandler for " + clientIp + ": " + e.getMessage());
                 }
            } catch (Exception e) {
                 // Catch unexpected errors
                 System.err.println("Unexpected error in ClientHandler for " + clientIp + ": " + e.getMessage());
                 e.printStackTrace();
            } finally {
                // Clean up regardless of how the loop/try block exited
                cleanup();
            }
            System.out.println("Client handler finished for: " + clientIp);
        }
        
        protected void cleanup() {
             // Call the server's disconnect handler
             serverInstance.handleClientDisconnect(clientSocket, writer); 
             // Ensure resources are closed (try-with-resources handles streams)
             try {
                 if (clientSocket != null && !clientSocket.isClosed()) {
                     clientSocket.close();
                 }
             } catch (IOException e) {
                 System.err.println("Error closing client socket during cleanup: " + e.getMessage());
             }
             // Nullify references
             writer = null;
             reader = null;
        }
    }
}

