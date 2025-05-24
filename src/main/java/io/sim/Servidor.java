package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor extends Thread {
    private static ArrayList<BufferedWriter> clientes;

    private String nome;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;

    public Servidor(Socket con) {
        this.con = con;
        try {
            in = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try {
            OutputStream ou = this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou, "UTF-8");
            BufferedWriter bfw = new BufferedWriter(ouw);
            clientes.add(bfw);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg);
        bw.flush();
    }
}
