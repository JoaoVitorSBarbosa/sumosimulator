package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to set the speed mode of a specific vehicle.
 */
public class SetVehicleSpeedModeCommand extends SumoCommand<Void> { // Returns nothing

    private final String vehicleID;
    private final int speedMode;

    public SetVehicleSpeedModeCommand(String vehicleID, int speedMode) {
        this.vehicleID = vehicleID;
        this.speedMode = speedMode;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            sumo.do_job_set(Vehicle.setSpeedMode(vehicleID, speedMode));
            complete(null); // Complete with null for Void type
        } catch (Exception e) {
            // System.err.println("Error executing SetVehicleSpeedModeCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

