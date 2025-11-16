package ca.concordia.filesystem.datastructures;

public class FNode {

    private int blockIndex; // Index of the block storing file data
    private int next;       // Index of next FNode in chain (-1 if none)

    public FNode(int blockIndex) {
        this.blockIndex = blockIndex;
        this.next = -1;
    }

    // Getters and Setters
    public int getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
}
