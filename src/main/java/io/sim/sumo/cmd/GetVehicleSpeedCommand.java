package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the current speed of a specific vehicle.
 */
public class GetVehicleSpeedCommand extends SumoCommand<Double> {

    private final String vehicleID;

    public GetVehicleSpeedCommand(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            Double speed = (Double) sumo.do_job_get(Vehicle.getSpeed(vehicleID));
            complete(speed);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehicleSpeedCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

