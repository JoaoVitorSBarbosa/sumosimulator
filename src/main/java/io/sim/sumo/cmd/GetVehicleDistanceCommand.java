package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the distance traveled by a specific vehicle.
 */
public class GetVehicleDistanceCommand extends SumoCommand<Double> {

    private final String vehicleID;

    public GetVehicleDistanceCommand(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            Double distance = (Double) sumo.do_job_get(Vehicle.getDistance(vehicleID));
            complete(distance);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehicleDistanceCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

