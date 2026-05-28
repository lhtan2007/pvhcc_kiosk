package org.example.manager_server.helper;

import java.net.Socket;
import java.net.ServerSocket;

public class ServerInitializer {
    private static final int PORT = 9999;
    private boolean isRunning = false;
    private ServerSocket serverSocket;

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Máy chủ Kiosk đã khởi động và đang nhận kết nối qua cổng " + PORT + "...");
            while(isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có một Kiosk mới vừa kết nối từ: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        }
        catch (Exception ignored) {

        }
        finally {
            stopServer();
        }
    }
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Đã tắt máy chủ mạng.");
            }
        }
        catch (Exception e) {
            System.err.println("Lỗi khi đóng ServerSocket: " + e.getMessage());
        }
    }
}
