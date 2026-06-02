package org.example.manager_server;

import org.example.manager_server.helper.ServerInitializer;

public class MainServer {
    public static void main(String[] args) {
        ServerInitializer server = new ServerInitializer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stopServer();
        }));
        server.startServer();
    }
}
