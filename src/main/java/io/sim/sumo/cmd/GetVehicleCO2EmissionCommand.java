package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to retrieve the CO2 emission of a specific vehicle.
 */
public class GetVehicleCO2EmissionCommand extends SumoCommand<Double> {

    private final String vehicleID;

    public GetVehicleCO2EmissionCommand(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            Double co2Emission = (Double) sumo.do_job_get(Vehicle.getCO2Emission(vehicleID));
            complete(co2Emission);
        } catch (Exception e) {
            // System.err.println("Error executing GetVehicleCO2EmissionCommand for " + vehicleID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

