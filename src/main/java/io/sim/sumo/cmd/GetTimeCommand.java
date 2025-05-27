package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Simulation;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the current simulation time.
 */
public class GetTimeCommand extends SumoCommand<Double> {

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            // Simulation.getCurrentTime() might return Integer, Simulation.getTime() returns Double
            // Let's use getTime() for consistency with Auto.java usage
            Double currentTime = (Double) sumo.do_job_get(Simulation.getTime());
            complete(currentTime);
        } catch (Exception e) {
            System.err.println("Error executing GetTimeCommand: " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

