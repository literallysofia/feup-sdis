public class Chunk {
    private int nr;
    private byte[] content;
    private String fileID;
    private int desiredReplicationDegree;
    private int currReplicationDegree = 0;

    public Chunk(int nr, byte[] content) {
        this.nr = nr;
        this.content = content;
    }

    public Chunk(int nr, String fileID, int desiredReplicationDegree) {
        this.nr = nr;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.fileID = fileID;
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

}
