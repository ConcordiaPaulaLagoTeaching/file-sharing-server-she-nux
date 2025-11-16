package ca.concordia.server;
import ca.concordia.filesystem.FileSystemManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

   

    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Listening on port 12345...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Handling client: " + clientSocket);
                try (
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Received from client: " + line);
                        String[] parts = line.split(" ");
                        String command = parts[0].toUpperCase();

                        //Djessica
                        //updating command handling so it recognises all other comnands, so DELETE, LIST, WRITE...

                        switch (command) {
                            case "CREATE":
                                try {
                                    fsManager.createFile(parts[1]);
                                    writer.println("SUCCESS: File '" + parts[1] + "' created.");
                                } catch (Exception e) {
                                    writer.println("ERROR: Could not create file '" + parts[1] + "'. " + e.getMessage());
                                }
                                writer.flush();
                                break;


                            //TODO: Implement other commands READ, WRITE, DELETE, LIST

                            case "DELETE":
                                try {
                                        fsManager.deleteFile(parts[1]);
                                        writer.println("SUCCESS: File '" + parts[1] + "' deleted.");
                                    } catch (Exception e) {
                                        writer.println("ERROR: Could not delete file '" + parts[1] + "'. " + e.getMessage());
                                    }
                                    writer.flush();
                                break;

                            case "WRITE":
                                 try {
                                        StringBuilder sb = new StringBuilder();
                                        for (int i = 2; i < parts.length; i++) {
                                            sb.append(parts[i]);
                                            if (i < parts.length - 1) sb.append(" ");
                                        }
                                        byte[] data = sb.toString().getBytes();
                                        fsManager.writeFile(parts[1], data);
                                        writer.println("SUCCESS: Written to file '" + parts[1] + "'");
                                    } catch (Exception e) {
                                             writer.println("ERROR: Could not write to file '" + parts[1] + "'. " + e.getMessage());
                                          }
                                    writer.flush();
                                break;

                            case "READ":
                                try {
                                    byte[] content = fsManager.readFile(parts[1]);
                                    if (content != null) {
                                        String fileContent = new String(content);
                                        writer.println("SUCCESS: Content of '" + parts[1] + "': " + fileContent);
                                    } else {
                                        writer.println("ERROR: File '" + parts[1] + "' not found.");
                                    }
                                } catch (Exception e) {
                                     writer.println("ERROR: Could not read file '" + parts[1] + "'. " + e.getMessage());
                                    }
                                writer.flush();
                                break;

                            // end of change - Djess


                            case "QUIT":
                                writer.println("SUCCESS: Disconnecting.");
                                writer.flush();
                                return;
                            case "LIST":
                                    //Djess
                             try {
                                      ListCommandHandler.handle(writer, fsManager);
                                 } catch (Exception e) {
                                writer.println("ERROR: Could not list files. " + e.getMessage());
                                writer.flush();
                            }
                            break;
                                    // End
                            default:
                                 writer.println("ERROR: Unknown command.");
                                 writer.flush();
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not start server on port " + port);
        }
    }

}
