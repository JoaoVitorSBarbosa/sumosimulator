package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to add a vehicle with full parameters to the simulation.
 */
public class AddVehicleFullCommand extends SumoCommand<Void> { // Returns nothing

    private final String vehID;
    private final String routeID;
    private final String typeID;
    private final String depart;
    private final String departLane;
    private final String departPos;
    private final String departSpeed;
    private final String arrivalLane;
    private final String arrivalPos;
    private final String arrivalSpeed;
    private final String fromTaz;
    private final String toTaz;
    private final String line;
    private final int personCapacity;
    private final int personNumber;

    public AddVehicleFullCommand(String vehID, String routeID, String typeID, String depart, String departLane,
                               String departPos, String departSpeed, String arrivalLane, String arrivalPos,
                               String arrivalSpeed, String fromTaz, String toTaz, String line,
                               int personCapacity, int personNumber) {
        this.vehID = vehID;
        this.routeID = routeID;
        this.typeID = typeID;
        this.depart = depart;
        this.departLane = departLane;
        this.departPos = departPos;
        this.departSpeed = departSpeed;
        this.arrivalLane = arrivalLane;
        this.arrivalPos = arrivalPos;
        this.arrivalSpeed = arrivalSpeed;
        this.fromTaz = fromTaz;
        this.toTaz = toTaz;
        this.line = line;
        this.personCapacity = personCapacity;
        this.personNumber = personNumber;
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            sumo.do_job_set(Vehicle.addFull(vehID, routeID, typeID, depart, departLane, departPos, departSpeed,
                                          arrivalLane, arrivalPos, arrivalSpeed, fromTaz, toTaz, line,
                                          personCapacity, personNumber));
            complete(null); // Complete with null for Void type
        } catch (Exception e) {
            System.err.println("Error executing AddVehicleFullCommand for vehicle " + vehID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

