package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to set the color of a vehicle.
 */
public class SetVehicleColorCommand extends SumoCommand<Void> { // Returns nothing

    private final String vehID;
    private final SumoColor color;

    public SetVehicleColorCommand(String vehID, SumoColor color) {
        this.vehID = vehID;
        // SumoColor might be mutable, consider creating a defensive copy if necessary
        // For now, assume it's used safely or is immutable enough.
        this.color = color; 
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            sumo.do_job_set(Vehicle.setColor(vehID, color));
            complete(null); // Complete with null for Void type
        } catch (Exception e) {
            System.err.println("Error executing SetVehicleColorCommand for vehicle " + vehID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

