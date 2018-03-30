public class Chunk implements Comparable {
    private int nr;
    private byte[] content;
    private String fileID;
    private int desiredReplicationDegree;
    private int currReplicationDegree = 0;
    private int size;
    private int owner;

    public Chunk(int nr, byte[] content, int size) {
        this.nr = nr;
        this.content = content;
        this.size = size;
    }

    public Chunk(int nr, String fileID, int desiredReplicationDegree, int size, int owner) {
        this.nr = nr;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.fileID = fileID;
        this.size = size;
        this.owner = owner;
    }

    public int getNr() {
        return nr;
    }

    public byte[] getContent() {
        return content;
    }

    public String getFileID() {
        return this.fileID;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public void setDesiredReplicationDegree(int desiredReplicationDegree) {
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public int getCurrReplicationDegree() {
        return currReplicationDegree;
    }

    public void setCurrReplicationDegree(int currReplicationDegree) {
        this.currReplicationDegree = currReplicationDegree;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int compareTo(Object c2) {
        return this.getCurrReplicationDegree()-((Chunk)c2).getCurrReplicationDegree();
    }

    public int getOwner() {
        return owner;
    }
}
