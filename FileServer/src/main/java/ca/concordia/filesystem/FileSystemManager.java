package ca.concordia.filesystem;

import ca.concordia.filesystem.datastructures.FEntry;

import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;


public class FileSystemManager {

    private final int MAXFILES = 5;
    private final int MAXBLOCKS = 10;
    private final static FileSystemManager instance;
    private final RandomAccessFile disk;
    private final ReentrantLock globalLock = new ReentrantLock();

    private static final int BLOCK_SIZE = 128; // Example block size

    private FEntry[] inodeTable; // Array of inodes
    private boolean[] freeBlockList; // Bitmap for free blocks

    public FileSystemManager(String filename, int totalSize) {
        // Initialize the file system manager with a file
        if(instance == null) {
            //TODO Initialize the file system
        	//PRATHIKSHA
        	try {
        		this.disk = new RandomAccessFile(filename, "rw");
        		
                this.inodeTable = new FEntry[MAXFILES];
                this.freeBlockList = new boolean[MAXBLOCKS];

                for (int i = 0; i < MAXBLOCKS; i++) {
                    freeBlockList[i] = true;
                }

                freeBlockList[0] = false;

            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize filesystem: " + e.getMessage());
            }

            instance = this;

        } else {
            throw new IllegalStateException("FileSystemManager is already initialized.");
        }

    }

    public void createFile(String fileName) throws Exception {
        // TODO
    	//PRATHIKSHA
    	globalLock.lock();
        try {
            if (fileName.length() > 11) {
                System.out.println("ERROR: Filename too long.");
                return;
            }

            for (FEntry entry : inodeTable) {
                if (entry != null && entry.getFilename().equals(fileName)) {
                    System.out.println("ERROR: File already exists.");
                    return;
                }
            }

            for (int i = 0; i < inodeTable.length; i++) {
                if (inodeTable[i] == null) {

                    inodeTable[i] = new FEntry(fileName, (short) 0, (short) -1);

                    System.out.println("SUCCESS: File '" + fileName + "' created.");
                    return;
                }
            }

            System.out.println("ERROR: Maximum file limit reached (" + MAXFILES + ")");

        } finally {
            globalLock.unlock();
        }
    }
    //PRATHIKSHA
    public void deleteFile(String fileName) throws Exception {
        
    	globalLock.lock();
    	
    	try {
    		for (int i = 0; i <inodeTable.length; i++) {
    			FEntry entry = inodeTable[i];
    			
    			if(entry != null && entry.getFilename().equals(fileName)) {
    				inodeTable[i] = null;
    				
    				System.out.println("SUCCESS: File '" + fileName + "'deleted");
    				return;
    			}
    		}
    		System.out.println("ERROR: File '" + fileName + "' not found");
    		
    	}
    		finally {
    			globalLock.unlock();
    		}
    	}
    	
    }
    // TODO: Add readFile, writeFile and other required methods,
