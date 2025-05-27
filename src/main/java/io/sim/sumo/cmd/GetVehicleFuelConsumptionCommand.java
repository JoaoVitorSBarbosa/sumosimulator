package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the fuel consumption of a specific vehicle.
 */
public class GetVehicleFuelConsumptionCommand extends SumoCommand<Double> {

    private final String vehicleID;

    public GetVehicleFuelConsumptionCommand(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            Double fuelConsumption = (Double) sumo.do_job_get(Vehicle.getFuelConsumption(vehicleID));
            complete(fuelConsumption);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehicleFuelConsumptionCommand for " + vehicleID + ": " + e.getMessage());
            // Avoid excessive logging in command itself, let caller handle future exception
            completeExceptionally(e);
        }
    }
}

