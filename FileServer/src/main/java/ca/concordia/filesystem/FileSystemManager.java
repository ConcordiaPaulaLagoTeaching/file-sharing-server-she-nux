package ca.concordia.filesystem;

import ca.concordia.filesystem.datastructures.FEntry;

import ca.concordia.filesystem.datastructures.FNode;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class FileSystemManager {
private final int MAXFILES = 5;
    private final int MAXBLOCKS = 10;
    private static FileSystemManager instance = null;
    private final RandomAccessFile disk;
    private final ReentrantLock globalLock = new ReentrantLock();

    private static final int BLOCK_SIZE = 128; // Example block size

    private FEntry[] inodeTable;    // Array of file entries
    private FNode[] fnodeTable;     // Array of FNODES
    private boolean[] freeBlockList; // Bitmap for free blocks


    public FileSystemManager(String filename, int totalSize) {

        // Initialize the file system manager with a file
        //TODO Initialize the file system
        	
        	//PRATHIKSHA
        	//FileSystemManager()

        if (instance == null) {
            try {
                this.disk = new RandomAccessFile(filename, "rw");
                this.inodeTable = new FEntry[MAXFILES];
                this.fnodeTable = new FNode[MAXBLOCKS];
                this.freeBlockList = new boolean[MAXBLOCKS];

                for (int i = 0; i < MAXBLOCKS; i++) {
                    freeBlockList[i] = true; // mark all free initially
                    fnodeTable[i] = new FNode(-1);
                }

                freeBlockList[0] = false; // reserve block 0 for metadata

                if (disk.length() > 0) {
                    loadMetadata();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize filesystem: " + e.getMessage());
            }
            instance = this;
        } else {
            throw new IllegalStateException("FileSystemManager is already initialized.");
        }
    }
    

         //Djessica
    private void loadMetadata() {
        try {
            disk.seek(0);

            for (int i = 0; i < MAXFILES; i++) {
                int nameLength = disk.readByte();
                if (nameLength > 0) {
                    byte[] nameBytes = new byte[nameLength];
                    disk.readFully(nameBytes);
                    String filename = new String(nameBytes);
                    short filesize = disk.readShort();
                    short firstBlock = disk.readShort();
                    inodeTable[i] = new FEntry(filename, filesize, firstBlock);
                    if (firstBlock >= 0) freeBlockList[firstBlock] = false;
                } else {
                    inodeTable[i] = null;
                }
            }

            for (int i = 0; i < MAXBLOCKS; i++) {
                int blockIndex = disk.readInt();
                int next = disk.readInt();
                fnodeTable[i] = new FNode(blockIndex);
                fnodeTable[i].setNext(next);
                if (blockIndex >= 0) freeBlockList[blockIndex] = false;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load metadata: " + e.getMessage());
        }
    }
    

    
    // Djessica 
  // Save metadata back to disk
    private void saveMetadata() {
        globalLock.lock();
        try {
            disk.seek(0);

            for (FEntry entry : inodeTable) {
                if (entry != null) {
                    byte[] nameBytes = entry.getFilename().getBytes();
                    disk.writeByte(nameBytes.length);
                    disk.write(nameBytes);
                    disk.writeShort(entry.getFilesize());
                    disk.writeShort(entry.getFirstBlock());
                } else {
                    disk.writeByte(0);
                }
            }

            for (FNode node : fnodeTable) {
                disk.writeInt(node.getBlockIndex());
                disk.writeInt(node.getNext());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save metadata: " + e.getMessage());
        } finally {
            globalLock.unlock();
        }
    }

    public static FileSystemManager getInstance(String filename, int totalsize) {
        if (instance == null) {
            instance = new FileSystemManager(filename, totalsize);
        }
        return instance;
    }


        // TODO
    	//PRATHIKSHA
    	//createFile()
    	public void createFile(String fileName) throws Exception {
        globalLock.lock();
        try {
            if (fileName.length() > 11) {
            throw new IllegalArgumentException("Filename cannot be longer than 11 characters.");
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
                    saveMetadata();
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
    //deleteFile()
   public void deleteFile(String fileName) throws Exception {
        globalLock.lock();
        try {
            for (int i = 0; i < inodeTable.length; i++) {
                FEntry entry = inodeTable[i];
                if (entry != null && entry.getFilename().equals(fileName)) {

                    int fnodeIndex = entry.getFirstBlock();
                    while (fnodeIndex >= 0) {
                        int next = fnodeTable[fnodeIndex].getNext();
                        freeBlockList[fnodeTable[fnodeIndex].getBlockIndex()] = true;
                        fnodeTable[fnodeIndex].setBlockIndex(-1);
                        fnodeTable[fnodeIndex].setNext(-1);
                        fnodeIndex = next;
                    }

                    inodeTable[i] = null;
                    saveMetadata();
                    System.out.println("SUCCESS: File '" + fileName + "' deleted");
                    return;
                }
            }
            System.out.println("ERROR: File '" + fileName + "' not found");
        } finally {
            globalLock.unlock();
        }
    }


    
    //PRATHIKSHA
    //readFile()

    // READ file (multiblock)
    public byte[] readFile(String fileName) throws Exception {
        globalLock.lock();
        try {
            FEntry entry = null;
            for (FEntry e : inodeTable) {
                if (e != null && e.getFilename().equals(fileName)) {
                    entry = e;
                    break;
                }
            }

            if (entry == null) {
                System.out.println("ERROR: File '" + fileName + "' not found.");
                return null;
            }

            if (entry.getFirstBlock() == -1 || entry.getFilesize() == 0) {
                return new byte[0];
            }

            byte[] data = new byte[entry.getFilesize()];
            int offset = 0;
            int fnodeIndex = entry.getFirstBlock();
            while (fnodeIndex >= 0) {
                int bytesToRead = Math.min(BLOCK_SIZE, entry.getFilesize() - offset);
                disk.seek(fnodeTable[fnodeIndex].getBlockIndex() * BLOCK_SIZE);
                disk.readFully(data, offset, bytesToRead);
                offset += bytesToRead;
                fnodeIndex = fnodeTable[fnodeIndex].getNext();
            }

            System.out.println("SUCCESS: File '" + fileName + "' read.");
            return data;
        } finally {
            globalLock.unlock();
        }
    }

    
 // PRATHIKSHA
    //listFile()
   public String[] listFiles() {
        globalLock.lock();
        try {
            List<String> files = new ArrayList<>();
            for (FEntry entry : inodeTable) {
                if (entry != null) files.add(entry.getFilename());
            }
            return files.toArray(new String[0]);
        } finally {
            globalLock.unlock();
        }
    }

    
    // TODO: Add readFile, writeFile and other required methods,


    //Djessica
    //write File

   // WRITE file (multiblock)
    public void writeFile(String fileName, byte[] data) throws Exception {
        globalLock.lock();
        try {
            FEntry entry = null;
            for (FEntry e : inodeTable) {
                if (e != null && e.getFilename().equals(fileName)) {
                    entry = e;
                    break;
                }
            }

            if (entry == null) {
                System.out.println("ERROR: File '" + fileName + "' not found.");
                return;
            }

            // Free old blocks
            int fnodeIndex = entry.getFirstBlock();
            while (fnodeIndex >= 0) {
                int next = fnodeTable[fnodeIndex].getNext();
                freeBlockList[fnodeTable[fnodeIndex].getBlockIndex()] = true;
                fnodeTable[fnodeIndex].setBlockIndex(-1);
                fnodeTable[fnodeIndex].setNext(-1);
                fnodeIndex = next;
            }

            // Allocate new blocks
            int blocksNeeded = (int) Math.ceil((double) data.length / BLOCK_SIZE);
            if (blocksNeeded > MAXBLOCKS) {
                System.out.println("ERROR: File too large");
                return;
            }

            int prevFNode = -1;
            int offset = 0;
            int firstFNodeIndex = -1;

            for (int i = 0; i < MAXBLOCKS; i++) {
                if (freeBlockList[i]) {
                    freeBlockList[i] = false;
                    int sizeToWrite = Math.min(BLOCK_SIZE, data.length - offset);
                    disk.seek(i * BLOCK_SIZE);
                    disk.write(data, offset, sizeToWrite);

                    fnodeTable[i].setBlockIndex(i);
                    fnodeTable[i].setNext(-1);

                    if (prevFNode >= 0) {
                        fnodeTable[prevFNode].setNext(i);
                    } else {
                        firstFNodeIndex = i;
                    }

                    prevFNode = i;
                    offset += sizeToWrite;

                    if (offset >= data.length) break;
                }
            }

            if (offset < data.length) {
                System.out.println("ERROR: Not enough free blocks");
                return;
            }

            entry.setFilesize((short) data.length);
            entry.setFirstBlock((short) firstFNodeIndex);
            saveMetadata();
            System.out.println("SUCCESS: File '" + fileName + "' written (" + data.length + " bytes)");
        } finally {
            globalLock.unlock();
        }
    }

}