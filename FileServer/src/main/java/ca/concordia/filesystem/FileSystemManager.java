package ca.concordia.filesystem;

import ca.concordia.filesystem.datastructures.FEntry;

import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;


public class FileSystemManager {

    private final int MAXFILES = 5;
    private final int MAXBLOCKS = 10;
    private static FileSystemManager instance = null;
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
        	//FileSystemManager()
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
    	//createFile()
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

        } 
    	finally {
            globalLock.unlock();
        }
    }
    
    //PRATHIKSHA
    //deleteFile()
    public void deleteFile(String fileName) throws Exception {
        
    	globalLock.lock();
    	
    	try {
    		for (int i = 0; i <inodeTable.length; i++) {
    			FEntry entry = inodeTable[i];
    			
    			if(entry != null && entry.getFilename().equals(fileName)) {
    				inodeTable[i] = null;
    				
    				System.out.println("SUCCESS: File '" + fileName + "' deleted");
    				return;
    			}
    		}
    		System.out.println("ERROR: File '" + fileName + "' not found");
    		
    	}
    		finally {
    			globalLock.unlock();
    		}
    	
    }

    
    //PRATHIKSHA
    //readFile()

    public byte[] readFile(String fileName) throws Exception {
        globalLock.lock();
        try {
            for (FEntry entry : inodeTable) {
                if (entry != null && entry.getFilename().equals(fileName)) {
                    if (entry.getFirstBlock() == -1 || entry.getFilesize() == 0) {
                        return new byte[0]; // empty file
                    }
                    
                    byte[] data = new byte[entry.getFilesize()];
                    disk.seek(entry.getFirstBlock() * BLOCK_SIZE);
                    disk.readFully(data);
                    System.out.println("SUCCESS: File '" + fileName + "' read.");
                    return data;
                }
            }
            System.out.println("ERROR: File '" + fileName + "' not found.");
            return null;
        } finally {
            globalLock.unlock();
        }
    }

    
 // PRATHIKSHA
    //listFile()
    public String listFiles() {

        globalLock.lock();
        try {
        	
            StringBuilder result = new StringBuilder();
            boolean found = false;

            for (FEntry entry : inodeTable) {
                if (entry != null) {
                    result.append(entry.getFilename()).append("\n");
                    found = true;
                }
            }

            if (!found) {
                return "No files found.";
            }

            return result.toString();
        }
        
        finally {
            globalLock.unlock();
        }
    }

    //Djessica
   public static FileSystemManager getInstance(String filename, int totalsize){
        if (instance == null){
            instance = new FileSystemManager(filename, totalsize);
        }
        return instance;
    }

    
    // TODO: Add readFile, writeFile and other required methods,


    //Djessica
    //write File

    public void writeFile(String fileName, byte[] data) throws Exception {

        globalLock.lock();
        try{
            // Find file entry
            FEntry entry = null;
            for (FEntry e : inodeTable){
                if (e != null && e.getFilename().equals(fileName)){
                    entry = e;
                    break;
                }
            }

            if (entry == null){
                System.out.println("ERROR: File '" + fileName + "' not found.");
            return;
            }

            // File size must fit in one 128-byte block
            if (data.length > BLOCK_SIZE) {
            System.out.println("ERROR: File size exceeds block size of " + BLOCK_SIZE + " bytes.");
            return;
            }

            // Free existing blocks in the files if any
            if (entry.getFirstBlock() != -1) {
            freeBlockList[entry.getFirstBlock()] = true;
            }

            // Find a free block
            int freeBlock = -1;
            for (int i = 0; i < MAXBLOCKS; i++) {
                if (freeBlockList[i]) {
                    freeBlock = i;
                    break;
                }
            }

            if (freeBlock == -1) {
                System.out.println("ERROR: No free blocks available.");
                return;
            }

            // Mark block as used
            freeBlockList[freeBlock] = false;

            //write data to disk
            disk.seek(freeBlock * BLOCK_SIZE);
            disk.write(data);

            // Update inode entry
            entry.setFilesize((short) data.length);
            entry.setFirstBlock((short) freeBlock);

            System.out.println("SUCCESS: File '" + fileName + "' written (" + data.length + " bytes).");

        } finally {
            globalLock.unlock();

        }

    }

}