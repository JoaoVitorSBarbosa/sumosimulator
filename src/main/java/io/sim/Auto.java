package io.sim;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;

public class Auto extends Thread {

	private String idAuto;
	private SumoColor colorAuto;
	private String driverID;
	private SumoTraciConnection sumo;

	private boolean on_off;
	private long acquisitionRate;
	private int fuelType; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; // price in liters
	private int personCapacity; // the total number of persons that can ride in this vehicle
	private int personNumber; // the total number of persons which are riding in this vehicle

	private double tanque;

	public Auto(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo,
			long _acquisitionRate,
			int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) {

		this.on_off = _on_off;
		this.idAuto = _idAuto;
		this.colorAuto = _colorAuto;
		this.driverID = _driverID;
		this.sumo = _sumo;
		this.acquisitionRate = _acquisitionRate;
		this.tanque = 10;

		if ((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}

		if ((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}

		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
	}

	@Override
	public void run() {
		try {
			while (this.on_off) {
				Auto.sleep(acquisitionRate);

				this.atualizaSensores();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean hasArrived() {
		boolean arrived = true;
		try {
			
			String[] listaStrings;
			String corridasFinalizadas = String.valueOf(sumo.do_job_get(Vehicle.getIDList()));
			System.out.println(corridasFinalizadas);
			if (!corridasFinalizadas.equals("[]")) {
				corridasFinalizadas = corridasFinalizadas.substring(0, corridasFinalizadas.length() - 1);
				corridasFinalizadas = corridasFinalizadas.substring(1, corridasFinalizadas.length());

				listaStrings = corridasFinalizadas.split(",");

				for (String s : listaStrings) {
					if (s.equals(idAuto)) {
						arrived = false;
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arrived;
	}

	private void atualizaSensores() {

		if (!Thread.currentThread().isInterrupted()) {
			try {
				if (!this.getSumo().isClosed()) {
					if (!hasArrived()) {
						SumoPosition2D sumoPosition2D;
						decrementarTanque(
								Double.parseDouble(
										String.valueOf(sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)))));
						sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
						String[] dados = {
								String.valueOf(sumo.do_job_get(Simulation.getCurrentTime())),
								String.valueOf(this.idAuto),
								String.valueOf(this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto))),
								String.valueOf(sumo.do_job_get(Vehicle.getSpeed(this.idAuto))),
								String.valueOf(sumo.do_job_get(Vehicle.getDistance(this.idAuto))),
								String.valueOf(sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto))),
								String.valueOf(sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto))),
								String.valueOf(sumoPosition2D.x),
								String.valueOf(sumoPosition2D.y) };

						ReportsController.getArquivoCars().appendCSV(dados);

						sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
						sumo.do_job_set(Vehicle.setSpeed(this.idAuto, 10));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdAuto() {
		return this.idAuto;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if ((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	public SumoColor getColorAuto() {
		return this.colorAuto;
	}

	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	public void setFuelPreferential(int _fuelPreferential) {
		if ((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}

	public String getDriverId() {
		return driverID;
	}

	public void decrementarTanque(double valor) {
		tanque = tanque - valor;
	}

	public void incrementarTanque(double valor) {
		tanque = tanque + valor;
	}

	public Boolean precisaAbastecer() {
		return tanque < 3;
	}

}