package client;

import java.io.*;
import java.net.*;

public class ChatClient {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatClient(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to server.");
        }
    }

    public void send(String msg) {
        writer.println(msg);
    }

    public BufferedReader getReader() {
        return reader;
    }
}
