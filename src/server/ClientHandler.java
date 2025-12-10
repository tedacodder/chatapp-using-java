package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Client handler setup failed.");
        }
    }

    @Override
    public void run() {
        try {
            String msg;

            while ((msg = reader.readLine()) != null) {
                System.out.println("Client: " + msg);
                server.broadcast(msg, this);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            server.removeClient(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String msg) {
        writer.println(msg);
    }
}
