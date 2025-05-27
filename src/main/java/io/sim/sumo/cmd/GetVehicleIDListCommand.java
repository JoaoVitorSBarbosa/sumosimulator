package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the list of all vehicle IDs currently in the simulation.
 */
public class GetVehicleIDListCommand extends SumoCommand<SumoStringList> {

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            SumoStringList idList = (SumoStringList) sumo.do_job_get(Vehicle.getIDList());
            complete(idList);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehicleIDListCommand: " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

