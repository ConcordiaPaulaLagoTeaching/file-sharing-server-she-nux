package ca.concordia.server;

import java.io.PrintWriter;

import ca.concordia.filesystem.FileSystemManager;

public class ListCommandHandler {
    //Djessica
   /**
     * Send the list of files to the client using the server's FileSystemManager instance.
     * This avoids getting a separate instance with different init args.
     */
    public static void handle(PrintWriter writer, FileSystemManager fsManager) {
        String files = fsManager.listFiles();
        writer.println(files);
        writer.flush();
    }
}
