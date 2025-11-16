package ca.concordia.server;

import ca.concordia.filesystem.FileSystemManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final FileSystemManager fsManager;

    public ClientHandler(Socket clientSocket, FileSystemManager fsManager) {
        this.clientSocket = clientSocket;
        this.fsManager = fsManager;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println("Received from client: " + line);
                String[] parts = line.split(" ");
                String command = parts[0].toUpperCase();

                switch (command) {

                    case "CREATE":
                        fsManager.createFile(parts[1]);
                        writer.println("SUCCESS: File '" + parts[1] + "' created.");
                        break;

                    case "DELETE":
                        fsManager.deleteFile(parts[1]);
                        writer.println("SUCCESS: File '" + parts[1] + "' deleted.");
                        break;

                    case "WRITE":
                        StringBuilder sb = new StringBuilder();
                        for (int i = 2; i < parts.length; i++) {
                            sb.append(parts[i]);
                            if (i < parts.length - 1) sb.append(" ");
                        }
                        byte[] data = sb.toString().getBytes();
                        fsManager.writeFile(parts[1], data);
                        writer.println("SUCCESS: Written to file '" + parts[1] + "'");
                        break;

                    case "READ":
                        byte[] content = fsManager.readFile(parts[1]);
                        if (content != null) {
                            writer.println("SUCCESS: Content of '" + parts[1] + "': " + new String(content));
                        } else {
                            writer.println("ERROR: File '" + parts[1] + "' not found.");
                        }
                        break;

                    case "LIST":
                        writer.println(fsManager.listFiles());
                        break;

                    case "QUIT":
                        writer.println("SUCCESS: Disconnecting.");
                        return;

                    default:
                        writer.println("ERROR: Unknown command.");
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }
}
