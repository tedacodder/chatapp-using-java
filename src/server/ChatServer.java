package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public ChatServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Could not start server", e);
        }
    }

    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                ClientHandler client = new ClientHandler(socket, this);
                clients.add(client);
                new Thread(client).start();

            } catch (IOException e) {
                System.out.println("Error accepting client!");
            }
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler c : clients) {
            if (c != sender) {
                c.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
