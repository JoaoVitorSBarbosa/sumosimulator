package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the list of arrived vehicle IDs.
 */
public class GetArrivedIDListCommand extends SumoCommand<SumoStringList> {

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            SumoStringList arrivedIDs = (SumoStringList) sumo.do_job_get(Simulation.getArrivedIDList());
            complete(arrivedIDs);
        } catch (Exception e) {
            System.err.println("Error executing GetArrivedIDListCommand: " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

