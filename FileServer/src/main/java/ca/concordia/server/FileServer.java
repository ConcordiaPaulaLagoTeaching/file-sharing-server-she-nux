package ca.concordia.server;

import ca.concordia.filesystem.FileSystemManager;

import java.net.ServerSocket;
import java.net.Socket;

// Djessica
// Server spawns a separate thread for each client

public class FileServer {

    private final FileSystemManager fsManager;
    private final int port;

    public FileServer(int port, String fileSystemName, int totalSize) {
        // Initialize the FileSystemManager
        try {
            this.fsManager = FileSystemManager.getInstance(fileSystemName, totalSize);
            this.port = port;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize FileSystemManager");
        }
    }

    // Start listening for client connections
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Listening on port " + port + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Each client gets its own thread 
                Thread t = new Thread(new ClientHandler(clientSocket, fsManager));
                t.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not start server on port " + port);
        }
    }
}
