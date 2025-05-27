package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the current route ID of a specific vehicle.
 */
public class GetVehicleRouteIDCommand extends SumoCommand<String> {

    private final String vehicleID;

    public GetVehicleRouteIDCommand(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            String routeID = (String) sumo.do_job_get(Vehicle.getRouteID(vehicleID));
            complete(routeID);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehicleRouteIDCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

