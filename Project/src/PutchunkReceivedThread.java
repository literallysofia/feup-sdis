import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class PutchunkReceivedThread implements Runnable {

    Double version;
    private int senderId;
    private String fileId;
    private int chunkNr;
    private int replicationDegree;

    public PutchunkReceivedThread(Double version, int senderId, String fileId, int chunkNr, int replicationDegree) {
        this.version=version;
        this.senderId=senderId;
        this.fileId=fileId;
        this.chunkNr=chunkNr;
        this.replicationDegree=replicationDegree;
    }

    @Override
    public void run() {
        if(Peer.getId() != senderId){

            Chunk chunk = new Chunk(chunkNr, fileId, replicationDegree);
            Peer.getStorage().getChunks().add(chunk);

            try {
                String filename = Peer.getId() + "/" +senderId + "_" + fileId+"_"+chunkNr;

                File file = new File(filename);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String header = "STORED " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr + "\r\n\r\n";

            SendMessageThread sendThread = null;
            try {
                sendThread = new SendMessageThread(header.getBytes("US-ASCII"), "MC");
                Peer.getExec().execute(sendThread);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
