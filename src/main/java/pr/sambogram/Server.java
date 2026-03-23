package pr.sambogram;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("--- Sambogram Server запущено на порту " + port + " ---");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler); // Додаємо нового юзера в список
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Помилка сервера: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Отримано: " + message);
                    broadcast(message); // ОСЬ ТУТ МАГІЯ: розсилаємо всім!
                }
            } catch (IOException e) {
                System.out.println("Клієнт відключився.");
            } finally {
                clients.remove(this); // Видаляємо зі списку при виході
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}