package ca.concordia.server;

import java.io.PrintWriter;

import ca.concordia.filesystem.FileSystemManager;

public class ListCommandHandler {
    //Djessica
    // modifying so that it actually calls FileSystemManager.listFiles()

    // Get the singleton instance of FileSystemManager
    private static FileSystemManager fsManager = FileSystemManager.getInstance(null, 0);

    public static void handle(PrintWriter writer) {
        
        // Get the list of files
        String files = fsManager.listFiles();

        // Send it to the client
        writer.println(files);
        writer.flush();
    }
}
