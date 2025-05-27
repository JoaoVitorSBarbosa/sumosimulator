package io.sim.sumo.cmd;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.sumo.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Command to add a route to the simulation.
 */
public class AddRouteCommand extends SumoCommand<Void> { // Returns nothing

    private final String routeID;
    private final SumoStringList edges;

    public AddRouteCommand(String routeID, SumoStringList edges) {
        this.routeID = routeID;
        // Create a copy to ensure thread safety if the original list is modified elsewhere
        this.edges = new SumoStringList(edges);
    }

    @Override
    public void execute(SumoTraciConnection sumo) {
        try {
            sumo.do_job_set(Route.add(routeID, edges));
            complete(null); // Complete with null for Void type
        } catch (Exception e) {
            System.err.println("Error executing AddRouteCommand for route " + routeID + ": " + e.getMessage());
            completeExceptionally(e);
        }
    }
}

