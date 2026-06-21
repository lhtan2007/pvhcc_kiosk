package org.example.kiosk_client.helper;

public class KioskNetConnector implements Runnable {
    private static volatile boolean isRunning = true;
    public void stop() {
        isRunning = false;
    }
    @Override
    public void run() {
        while(isRunning) {
            boolean isServerOnline = NetworkInitializer.getInstance().connect();
            if(!isServerOnline) {
                System.err.println("CẢNH BÁO: Hiện tại không thể kết nối tới Server. Kiosk sẽ chạy ở chế độ offline.");
            }
        }
    }
}
