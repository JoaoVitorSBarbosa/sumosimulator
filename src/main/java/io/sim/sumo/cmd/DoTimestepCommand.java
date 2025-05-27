package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Simulation;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to execute a simulation step (do_timestep).
 * Returns true if the step was executed, false otherwise (e.g., connection closed).
 */
public class DoTimestepCommand extends SumoCommand<Boolean> {

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            if (!sumo.isClosed()) {
                sumo.do_timestep();
                complete(true);
            } else {
                complete(false); // Indicate simulation likely ended
            }
        } catch (Exception e) {
            System.err.println("Error executing DoTimestepCommand: " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

