package io.sim.sumo;

import java.util.concurrent.CompletableFuture;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Represents a command to be executed against the SUMO simulation via TraCI.
 * Each command encapsulates the logic for a specific SUMO interaction 
 * and holds a CompletableFuture to provide the result asynchronously.
 *
 * @param <T> The type of the result returned by the command execution.
 */
public abstract class SumoCommand<T> {
    // CompletableFuture to hold the result or exception of the command execution.
    private final CompletableFuture<T> future = new CompletableFuture<>();

    /**
     * Executes the specific SUMO TraCI command using the provided connection.
     * Implementations should perform the necessary sumo.do_job_get or sumo.do_job_set calls.
     * Upon completion, they must complete the future with the result or an exception.
     *
     * @param sumo The SumoTraciConnection instance to use for executing the command.
     */
    public abstract void execute(SumoTraciConnection sumo);

    /**
     * Returns the CompletableFuture associated with this command.
     * External callers can use this future to asynchronously retrieve the result
     * or handle exceptions from the command execution.
     *
     * @return The CompletableFuture<T> for this command.
     */
    public CompletableFuture<T> getFuture() {
        return future;
    }

    /**
     * Helper method for subclasses to complete the future successfully.
     * @param result The result of the command execution.
     */
    protected void complete(T result) {
        future.complete(result);
    }

    /**
     * Helper method for subclasses to complete the future exceptionally.
     * @param ex The exception that occurred during command execution.
     */
    protected void completeExceptionally(Throwable ex) {
        future.completeExceptionally(ex);
    }
}

