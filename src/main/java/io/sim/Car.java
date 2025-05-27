package io.sim;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.sumo.SumoCommandExecutor;

public class Car extends Auto {
    public Car(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoCommandExecutor _sumoExecutor, long _acquisitionRate, String port) {
        super(_on_off, _idAuto, _colorAuto, _driverID, _sumoExecutor, _acquisitionRate, 2, 2, 5.87, 5, 1, port);
    }
}
