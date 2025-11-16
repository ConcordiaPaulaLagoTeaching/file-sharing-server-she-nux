package ca.concordia.server;
import ca.concordia.filesystem.FileSystemManager;


import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private FileSystemManager fsManager;
    private int port;
    public FileServer(int port, String fileSystemName, int totalSize){
        // Initialize the FileSystemManager
        try {
        	this.fsManager = FileSystemManager.getInstance(fileSystemName, totalSize);
        	this.port = port;
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Failed to initialize FileSystemManager");
        }
        }

        //Djessica
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Listening on port 12345...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Handling client: " + clientSocket);

                // MULTITHREADING — each client gets its own thread
                Thread t = new Thread(new ClientHandler(clientSocket, fsManager));
                t.start();
            }

         } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not start server on port " + port);
          }
    }

}
