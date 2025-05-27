package io.sim;


import it.polito.appeal.traci.SumoTraciConnection;

import io.sim.sumo.SumoCommandExecutor;


import java.io.IOException;

public class EnvSimulator extends Thread {
	private Company company;
	private SumoTraciConnection sumo;
	private AlphaBank banco;
	private boolean isOn;
	private SumoCommandExecutor sumoExecutor;

	public EnvSimulator() {
		isOn = true;
	}

	public void run() {
		String sumo_bin = "sumo-gui";
		String config_file = "map/map.sumo.cfg";

		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

		try {
			sumo.runServer(12345);
			this.sumoExecutor = new SumoCommandExecutor(this.sumo);
            this.sumoExecutor.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		banco = new AlphaBank();
		banco.start();
		company = new Company(this.sumoExecutor);
		company.start();

		while (isOn) {

			if (!company.getOnOff()) {
				if (!sumo.isClosed()) {
					sumo.close(); // Fecha o sumo se acabou os carros
					isOn = false;
				}
			}
		}
	}

}
