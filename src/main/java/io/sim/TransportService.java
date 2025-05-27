package io.sim;

import io.sim.sumo.SumoCommandExecutor;
import io.sim.sumo.cmd.AddRouteCommand;
import io.sim.sumo.cmd.AddVehicleFullCommand;
import io.sim.sumo.cmd.SetVehicleColorCommand;

import java.util.concurrent.CompletableFuture;

import de.tudresden.sumo.objects.SumoStringList;

public class TransportService extends Thread {

    private String idTransportService;
    private volatile boolean on_off;
    private SumoCommandExecutor sumoExecutor;
    private Car auto;
    private Rota rota;
    private volatile boolean initialized = false;

    public TransportService(boolean _on_off, String _idTransportService, Rota _rota, Car _auto,
                            SumoCommandExecutor _sumoExecutor) {
        this.on_off = _on_off;
        this.idTransportService = _idTransportService;
        this.rota = _rota;
        this.auto = _auto;
        this.sumoExecutor = _sumoExecutor;
    }

    @Override
    public void run() {
        try {
            CompletableFuture<Boolean> initFuture = this.initializeRoutesAndVehicle();
            boolean success = initFuture.get(); 
            if (success) {
                initialized = true;
                this.auto.start();
            } else {
                this.on_off = false; 
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.on_off = false;
        }
    }

    private CompletableFuture<Boolean> initializeRoutesAndVehicle() {
        SumoStringList edgeList = new SumoStringList();
        String[] routeData = this.rota.getRota();

        if (routeData == null || routeData.length < 2) {
             System.err.println("Route " + this.rota.getIdRota() + " has no edges defined or invalid format.");
             return CompletableFuture.completedFuture(false);
        }
        
        // The second element (index 1) contains the space-separated list of edges
        String edgesString = routeData[1];
        if (edgesString == null || edgesString.trim().isEmpty()) {
            System.err.println("Route " + this.rota.getIdRota() + " has empty edges string.");
            return CompletableFuture.completedFuture(false);
        }
        
        // Split the edges string by spaces and add each edge to the list
        String[] edges = edgesString.trim().split("\\s+");
        for (String edge : edges) {
            if (!edge.trim().isEmpty()) {
                edgeList.add(edge.trim());
            }
        }

        if (edgeList.isEmpty()) {
            System.err.println("Route " + this.rota.getIdRota() + " edge list is empty after processing.");
            return CompletableFuture.completedFuture(false);
        }

        System.out.println("Adding route " + this.rota.getIdRota() + " with " + edgeList.size() + " edges: " + edgeList);

        // Create commands
        AddRouteCommand addRouteCmd = new AddRouteCommand(this.rota.getIdRota(), edgeList);
        AddVehicleFullCommand addVehicleCmd = new AddVehicleFullCommand(
                this.auto.getIdAuto(),          // vehID
                this.rota.getIdRota(),          // routeID 
                "DEFAULT_VEHTYPE",              // typeID 
                "now",                          // depart  
                "0",                            // departLane 
                "0",                            // departPos 
                "0",                            // departSpeed
                "current",                      // arrivalLane 
                "max",                          // arrivalPos 
                "current",                      // arrivalSpeed 
                "",                             // fromTaz 
                "",                             // toTaz 
                "",                             // line 
                this.auto.getPersonCapacity(),  // personCapacity 
                this.auto.getPersonNumber()     // personNumber
        );
        SetVehicleColorCommand setColorCmd = new SetVehicleColorCommand(this.auto.getIdAuto(), this.auto.getColorAuto());
		return sumoExecutor.submitCommand(addRouteCmd)
            .thenCompose(voidResult -> {
                System.out.println("Route added for: " + this.rota.getIdRota());
                return sumoExecutor.submitCommand(addVehicleCmd);
            })
            .thenCompose(voidResult -> {
                 System.out.println("Vehicle added for: " + this.auto.getIdAuto());
                return sumoExecutor.submitCommand(setColorCmd);
            })
            .thenApply(voidResult -> {
                 System.out.println("Vehicle color set for: " + this.auto.getIdAuto());
                return true;
            })
            .exceptionally(ex -> {
                System.err.println("Error during route/vehicle initialization for " + this.auto.getIdAuto() + ": " + ex.getMessage());
                return false;
            });
    }

    public void shutdown() {
        System.out.println("Shutdown requested for TransportService: " + idTransportService);
        this.on_off = false;
        if (auto != null && auto.isAlive()) {
            auto.setOn_off(false); 
            auto.interrupt();      
        }
        this.interrupt(); 
    }

    public boolean isOn_off() {
        return on_off;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getIdTransportService() {
        return this.idTransportService;
    }

    public Car getAuto() {
        return this.auto;
    }

    public Rota getRota() {
        return this.rota;
    }
}
