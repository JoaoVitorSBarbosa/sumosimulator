package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoPosition2D;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the position of a specific vehicle.
 */
public class GetVehiclePositionCommand extends SumoCommand<SumoPosition2D> {

    private final String vehicleID;

    public GetVehiclePositionCommand(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            SumoPosition2D position = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(vehicleID));
            complete(position);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehiclePositionCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

