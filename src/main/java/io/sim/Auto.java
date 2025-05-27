package io.sim;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import io.sim.sumo.cmd.*;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.sumo.SumoCommandExecutor;

public class Auto extends Client {
	private double valorMaxTanque = 50.0;
	private String idAuto;
	private SumoColor colorAuto;
	private String driverID;
	private SumoCommandExecutor sumoExecutor;
	private Double lastTimeStep;
	private boolean on_off;
	private long acquisitionRate;
	private int fuelType; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; // price in liters
	private int personCapacity; // the total number of persons that can ride in this vehicle
	private int personNumber; // the total number of persons which are riding in this vehicle

	private double tanque;
	ManipuladorCSV manipulador;

	public Auto(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID,
			SumoCommandExecutor _sumoExecutor,
			long _acquisitionRate,
			int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber,
			String port) {

		super(port);
		this.on_off = _on_off;
		this.idAuto = _idAuto;
		this.colorAuto = _colorAuto;
		this.driverID = _driverID;
		this.sumoExecutor = _sumoExecutor;
		this.acquisitionRate = _acquisitionRate;
		this.tanque = valorMaxTanque;

		if ((_fuelType < 1) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}

		if ((_fuelPreferential < 1) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}

		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.lastTimeStep = 0.0;

		manipulador = new ManipuladorCSV("reports/cars/" + idAuto + ".csv");
		String[] cabecalho = { "Timestamp", "ID Car", "ID Route", "Speed", "Distance", "FuelConsumption", "CO2Emission",
				"longitude(lon)", "latitude(lat)", "Tanque" };
		manipulador.writeCSV(cabecalho);
	}

	@Override
	public void run() {
		System.out.println("Auto thread started for: " + idAuto);
		while (this.on_off && !Thread.currentThread().isInterrupted()) {
			try {
				long sleepTime = acquisitionRate > 0 ? acquisitionRate : 100;
				Thread.sleep(sleepTime);


				if (!isVehicleInSimulation()) {
					this.on_off = false;
					break;
				}

				this.atualizaSensores();

			} catch (InterruptedException e) {
				this.on_off = false;
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				e.printStackTrace();
				this.on_off = false; 
			}
		}
	}


	private boolean isVehicleInSimulation() throws InterruptedException, ExecutionException {
		CompletableFuture<SumoStringList> idListFuture = sumoExecutor.submitCommand(new GetVehicleIDListCommand());
		SumoStringList idList = idListFuture.get();
		return idList != null && idList.contains(this.idAuto);
	}



	private void atualizaSensores() {
		try {
			CompletableFuture<Double> timeFuture = sumoExecutor.submitCommand(new GetTimeCommand());
			CompletableFuture<Double> fuelConsumptionFuture = sumoExecutor
					.submitCommand(new GetVehicleFuelConsumptionCommand(this.idAuto));
			CompletableFuture<SumoPosition2D> positionFuture = sumoExecutor
					.submitCommand(new GetVehiclePositionCommand(this.idAuto));
			CompletableFuture<String> routeIDFuture = sumoExecutor
					.submitCommand(new GetVehicleRouteIDCommand(this.idAuto));
			CompletableFuture<Double> speedFuture = sumoExecutor.submitCommand(new GetVehicleSpeedCommand(this.idAuto));
			CompletableFuture<Double> distanceFuture = sumoExecutor
					.submitCommand(new GetVehicleDistanceCommand(this.idAuto));
			CompletableFuture<Double> co2EmissionFuture = sumoExecutor
					.submitCommand(new GetVehicleCO2EmissionCommand(this.idAuto));


			CompletableFuture<Void> allFutures = CompletableFuture.allOf(
					timeFuture, fuelConsumptionFuture, positionFuture,
					routeIDFuture, speedFuture, distanceFuture, co2EmissionFuture);


			allFutures.get();

			Double currentTime = timeFuture.get();
			Double fuelConsumption = fuelConsumptionFuture.get();
			SumoPosition2D sumoPosition2D = positionFuture.get();
			String routeID = routeIDFuture.get();
			Double speed = speedFuture.get();
			Double distance = distanceFuture.get();
			Double co2Emission = co2EmissionFuture.get();

			decrementarTanque(fuelConsumption, currentTime);

			String[] dados = {
					String.valueOf(currentTime),
					this.idAuto,
					routeID,
					String.valueOf(speed),
					String.valueOf(distance),
					String.valueOf(fuelConsumption),
					String.valueOf(co2Emission),
					String.valueOf(sumoPosition2D.x),
					String.valueOf(sumoPosition2D.y),
					String.format("%.4f", tanque)
			};

			manipulador.appendCSV(dados);

			sumoExecutor.submitCommand(new SetVehicleSpeedModeCommand(this.idAuto, 0));
			sumoExecutor.submitCommand(new SetVehicleSpeedCommand(this.idAuto, 10)); // Example fixed speed

		} catch (ExecutionException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			this.on_off = false; 
		} catch (Exception e) {
			e.printStackTrace();
			this.on_off = false;
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

	public void decrementarTanque(double valor, double stepAtual) { // valor é em mg/s;
		try {
			Double tempo = stepAtual - lastTimeStep; // segundos
			Double densidade = 750.0; // g/L
			Double massa = tempo * valor; // mg
			// massa = massa / 1000; //g
			Double volume = massa / densidade;

			tanque = tanque - volume;
			if (tanque <= 0) {
				tanque = 0;
			}
			System.out.println("At: " + stepAtual + " Ant: " + lastTimeStep + " Tanque: " + String.valueOf(tanque)
					+ " Volume: " + String.valueOf(volume));
			lastTimeStep = stepAtual;

			if (precisaAbastecer()) {
				abastecer();
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Boolean precisaAbastecer() {
		return tanque < 3;
	}

	public void abastecer() {

		try {
			CompletableFuture<Void> stopFuture = sumoExecutor.submitCommand(new SetVehicleSpeedCommand(idAuto, 0.0));
			stopFuture.get();
			Thread.sleep(2000);
			tanque = valorMaxTanque;
			CompletableFuture<Void> resumeFuture = sumoExecutor.submitCommand(new SetVehicleSpeedCommand(idAuto, 10.0));
			resumeFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}
}