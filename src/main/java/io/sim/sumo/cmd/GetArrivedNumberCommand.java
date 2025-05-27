package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Simulation;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the number of arrived vehicles in the last step.
 */
public class GetArrivedNumberCommand extends SumoCommand<Integer> {

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            Integer arrivedNumber = (Integer) sumo.do_job_get(Simulation.getArrivedNumber());
            complete(arrivedNumber);
        } catch (Exception e) {
            System.err.println("Error executing GetArrivedNumberCommand: " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

