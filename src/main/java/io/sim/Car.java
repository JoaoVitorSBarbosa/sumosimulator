package io.sim;

import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Car extends Auto {
    public Car(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate) {
        super(_on_off, _idAuto, _colorAuto, _driverID, _sumo, _acquisitionRate, 2, 2, 5.87, 5, 1);
    }
}
