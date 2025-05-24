package io.sim;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.*;

public class Client extends Thread  {
    private JTextArea texto;
    private JTextField txtMsg;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;
    private String txtIP;
    private String txtPorta;

    public Client() {
        txtIP = "127.0.0.1";
        txtPorta = "12346";

    }

    public void run() {
        try {
            conectar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void conectar() throws IOException {

        socket = new Socket(txtIP, Integer.parseInt(txtPorta));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.flush();
    }

    public void enviarMensagem(String msg) throws IOException {

        bfw.write(msg);
        bfw.flush();
        txtMsg.setText("");
    }

    public void escutar() throws IOException {

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";

        while (!"Sair".equalsIgnoreCase(msg))

            if (bfr.ready()) {
                msg = bfr.readLine();
                if (msg.equals("Sair"))
                    texto.append("Servidor caiu! \r\n");
                else
                    texto.append(msg + "\r\n");
            }
    }

}
