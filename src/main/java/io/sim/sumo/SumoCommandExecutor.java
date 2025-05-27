package io.sim.sumo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Executes SUMO commands sequentially in a dedicated thread 
 * to ensure thread-safe interaction with the SumoTraciConnection.
 */
public class SumoCommandExecutor extends Thread {

    private final SumoTraciConnection sumo;
    private final BlockingQueue<SumoCommand<?>> commandQueue;
    private volatile boolean running = true;

    public SumoCommandExecutor(SumoTraciConnection sumo) {
        this.sumo = sumo;
        // Use a LinkedBlockingQueue for unbounded command queuing
        this.commandQueue = new LinkedBlockingQueue<>();
        // Set as daemon so it doesn't prevent JVM shutdown
        this.setDaemon(true); 
        this.setName("SumoCommandExecutorThread");
        System.out.println("SumoCommandExecutor initialized.");
    }

    /**
     * Submits a command for execution and returns a Future for the result.
     * The command will be placed in the queue and executed by the executor thread.
     *
     * @param <T> The type of the result expected from the command.
     * @param command The SumoCommand to execute.
     * @return A CompletableFuture that will eventually hold the result of the command execution.
     */
    public <T> CompletableFuture<T> submitCommand(SumoCommand<T> command) {
        if (!running) {
            command.getFuture().completeExceptionally(new IllegalStateException("Executor is not running."));
        } else {
            try {
                // Offer the command to the queue. If the queue is full (unlikely for LinkedBlockingQueue unless out of memory),
                // it might fail, but typically it blocks or adds successfully.
                // Using put to ensure it blocks if needed, though unlikely here.
                commandQueue.put(command);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                command.getFuture().completeExceptionally(e);
            } catch (Exception e) {
                 command.getFuture().completeExceptionally(e);
            }
        }
        return command.getFuture();
    }

    @Override
    public void run() {
        System.out.println("SumoCommandExecutor thread started.");
        while (running) {
            try {
                // Take a command from the queue, blocking until one is available.
                SumoCommand<?> command = commandQueue.take();
                
                // Execute the command and complete its future
                command.execute(sumo);

            } catch (InterruptedException e) {
                System.out.println("SumoCommandExecutor interrupted. Shutting down...");
                running = false;
                Thread.currentThread().interrupt(); // Preserve interrupt status
            } catch (Exception e) {
                // Log unexpected errors during command processing
                System.err.println("Error executing SUMO command: " + e.getMessage());
                e.printStackTrace(); 
                // We might want to complete the future exceptionally here if we can get it,
                // but the command object might be in an inconsistent state.
                // For now, just log and continue the loop unless it's fatal.
            }
        }
        System.out.println("SumoCommandExecutor thread finished.");
        // Clear queue on exit? Maybe notify pending futures?
        cleanupQueue();
    }

    private void cleanupQueue() {
        System.out.println("Cleaning up command queue...");
        SumoCommand<?> command;
        while ((command = commandQueue.poll()) != null) {
            command.getFuture().completeExceptionally(new IllegalStateException("Executor shut down before command execution."));
        }
         System.out.println("Command queue cleanup complete.");
    }

    /**
     * Signals the executor thread to stop processing commands and terminate.
     */
    public void shutdown() {
        System.out.println("Shutdown requested for SumoCommandExecutor.");
        running = false;
        this.interrupt(); // Interrupt the thread if it's blocked on queue.take()
    }
}

