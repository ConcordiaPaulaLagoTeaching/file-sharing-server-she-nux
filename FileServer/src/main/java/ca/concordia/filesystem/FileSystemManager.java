package ca.concordia.filesystem;

import ca.concordia.filesystem.datastructures.FEntry;

import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

public class FileSystemManager {

    private final int MAXFILES = 5;
    private final int MAXBLOCKS = 10;

    //Djessica
    private static FileSystemManager instance = null;//not final

    private final RandomAccessFile disk;
    private final ReentrantLock globalLock = new ReentrantLock();

    private static final int BLOCK_SIZE = 128; // Example block size

    private FEntry[] inodeTable; // Array of inodes
    private boolean[] freeBlockList; // Bitmap for free blocks


    //Djessica
    //Made the constructor private.
    //Removed the instance check — singleton is now handled in getInstance() (at the bottom)

    public FileSystemManager(String filename, int totalSize) {
        // // Initialize the file system manager with a file
        // if(instance == null) {
        //     //TODO Initialize the file system
        // } else {
        //     throw new IllegalStateException("FileSystemManager is already initialized.");
        // }

          this.disk = null; // TODO: initialize RandomAccessFile with filename
    this.inodeTable = new FEntry[MAXFILES];
    this.freeBlockList = new boolean[MAXBLOCKS];
    // TODO: initialize FS

    }

    public void createFile(String fileName) throws Exception {
        // TODO
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    public static synchronized FileSystemManager getInstance(String filename, int totalSize) {
    if (instance == null) {
        instance = new FileSystemManager(filename, totalSize);
    }
    return instance;
}

    // TODO: Add readFile, writeFile and other required methods,
}
