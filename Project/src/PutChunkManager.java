import java.util.concurrent.TimeUnit;

public class PutChunkManager implements Runnable {
    private byte[] message;
    private int time;
    private String key;
    private int replicationDegree;
    private int counter;

    public PutChunkManager(byte[] message, int time, String fileID, int chunkNr, int replicationDegree) {
        this.message = message;
        this.time = time;
        this.key = fileID + '_' + chunkNr;
        this.replicationDegree = replicationDegree;
        this.counter = 1;
    }

    @Override
    public void run() {

        int occurrences = Peer.getStorage().getStoredOccurrences().get(this.key);

        if (occurrences < replicationDegree) {
            Peer.getMDB().sendMessage(message);
            System.out.println("Sent PUTCHUNK");
            this.time = 2 * this.time;
            this.counter++;

            if (this.counter < 5)
                Peer.getExec().schedule(this, this.time, TimeUnit.SECONDS);
        }

    }
}
