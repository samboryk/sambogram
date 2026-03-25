package pr.sambogram;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("--- Sambogram Server запущено ---");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Помилка сервера: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            if (client.isAuthenticated()) {
                client.sendMessage(message);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private boolean authenticated = false;

        public ClientHandler(Socket socket) { this.socket = socket; }
        public boolean isAuthenticated() { return authenticated; }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String message;
                while ((message = in.readLine()) != null) {


                    if (!authenticated) {
                        if (message.startsWith("AUTH:")) {
                            String[] parts = message.split(":", 3);
                            if (parts.length == 3 && DatabaseManager.authenticateUser(parts[1], parts[2])) {
                                this.username = parts[1];
                                this.authenticated = true;
                                clients.add(this);

                                out.println("SERVER: Авторизація успішна!");

                                List<String> history = DatabaseManager.getChatHistory(50);
                                for (String histMsg : history) {
                                    out.println(histMsg);
                                }
                            } else {
                                out.println("SERVER: Невірний логін або пароль!");
                                socket.close();
                                return;
                            }
                        }
                    } else {

                        if (message.startsWith("MSG:")) {
                            String content = message.substring(4);
                            DatabaseManager.saveTextMessage(this.username, "ALL", content);
                            broadcast(this.username + ": " + content);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Клієнт відключився.");
            } finally {
                clients.remove(this);
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}