package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to set the speed of a specific vehicle.
 */
public class SetVehicleSpeedCommand extends SumoCommand<Void> { // Returns nothing

    private final String vehicleID;
    private final double speed;

    public SetVehicleSpeedCommand(String vehicleID, double speed) {
        this.vehicleID = vehicleID;
        this.speed = speed;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            sumo.do_job_set(Vehicle.setSpeed(vehicleID, speed));
            complete(null); // Complete with null for Void type
        } catch (Exception e) {
            // System.err.println("Error executing SetVehicleSpeedCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

