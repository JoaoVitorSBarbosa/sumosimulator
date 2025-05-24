package io.sim;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;


import it.polito.appeal.traci.SumoTraciConnection;
import java.util.ArrayList;

import de.tudresden.sumo.cmd.Simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class EnvSimulator extends Thread{
	private Company company;
	private ArrayList<Rota> listaRotas;
    private SumoTraciConnection sumo;
	private AlphaBank banco;
	private static ServerSocket server;

    public EnvSimulator() {
		
    }

    public void run(){
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";

		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		//sumo.addOption("quit-on-end", "1"); // auto-close on end

		try {
			sumo.runServer(12345);
		} catch (IOException e) {
			e.printStackTrace();
		}
		company = new Company(sumo);
		company.start();

		banco = new AlphaBank();
		company = new Company(sumo);
    }
	

}
