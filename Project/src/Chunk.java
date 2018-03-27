import java.io.UnsupportedEncodingException;

public class Chunk {
    private int nr;
    private byte[] content;
    private int desiredReplicationDegree;
    private int currReplicationDegree;
    private String contentString;

    public Chunk(int nr, byte[] content) {
        this.nr =nr;
        this.content=content;
        contentString = new String (content);
    }

    public int getNr() {
        return nr;
    }

    public byte[] getContent() {
        return content;
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
