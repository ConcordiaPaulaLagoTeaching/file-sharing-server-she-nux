package ca.concordia.server;

import ca.concordia.filesystem.FileSystemManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Handles one client per thread. Read/write properly synchronized.
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final FileSystemManager fsManager;

    // Multiple readers allowed, only one writer at a time
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

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
                try {
                    System.out.println("Received from client: " + line);

                    String[] parts = line.split(" ");
                    String command = parts[0].toUpperCase();

                    switch (command) {

                        case "CREATE":
                            rwLock.writeLock().lock(); // only one creator at a time
                            try {
                                fsManager.createFile(parts[1]);
                                writer.println("SUCCESS: File '" + parts[1] + "' created.");
                            } finally {
                                rwLock.writeLock().unlock();
                            }
                            break;

                        case "DELETE":
                            rwLock.writeLock().lock(); // safe delete
                            try {
                                fsManager.deleteFile(parts[1]);
                                writer.println("SUCCESS: File '" + parts[1] + "' deleted.");
                            } finally {
                                rwLock.writeLock().unlock();
                            }
                            break;

                        case "WRITE":
                            rwLock.writeLock().lock(); // exclusive write
                            try {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 2; i < parts.length; i++) {
                                    sb.append(parts[i]);
                                    if (i < parts.length - 1) sb.append(" ");
                                }
                                fsManager.writeFile(parts[1], sb.toString().getBytes());
                                writer.println("SUCCESS: Written to file '" + parts[1] + "'");
                            } finally {
                                rwLock.writeLock().unlock();
                            }
                            break;

                        case "READ":
                            rwLock.readLock().lock(); // multiple reads allowed
                            try {
                                byte[] content = fsManager.readFile(parts[1]);
                                if (content != null) {
                                    writer.println("SUCCESS: Content of '" + parts[1] + "': " + new String(content));
                                } else {
                                    writer.println("ERROR: File '" + parts[1] + "' not found.");
                                }
                            } finally {
                                rwLock.readLock().unlock();
                            }
                            break;

                        case "LIST":
                            rwLock.readLock().lock(); // safe to read all files
                            try {
                               String[] files = fsManager.listFiles();
                                if (files.length == 0) writer.println("No files");
                                else writer.println(String.join(",", files));

                            } finally {
                                rwLock.readLock().unlock();
                            }
                            break;

                        case "QUIT":
                            writer.println("SUCCESS: Disconnecting.");
                            return; 

                        default:
                            writer.println("ERROR: Unknown command.");
                    }

                } catch (Exception e) {
                    // handle error for this command, server stays alive
                    writer.println("ERROR: " + e.getMessage());
                    System.out.println("Error handling client request: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("Failed to close client socket: " + e.getMessage());
            }
        }
    }
}