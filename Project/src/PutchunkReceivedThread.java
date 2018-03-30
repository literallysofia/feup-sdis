import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class PutchunkReceivedThread implements Runnable {

    Double version;
    private int senderId;
    private String fileId;
    private int chunkNr;
    private int replicationDegree;
    private byte[] content;

    public PutchunkReceivedThread(Double version, int senderId, String fileId, int chunkNr, int replicationDegree, byte [] content) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNr = chunkNr;
        this.replicationDegree = replicationDegree;
        this.content=content;
    }

    @Override
    public void run() {
        String key = fileId + "_" + chunkNr;
        if (Peer.getStorage().getStoredOccurrences().get(key) < replicationDegree) {

            Chunk chunk = new Chunk(chunkNr, fileId, replicationDegree, content.length, senderId);

            if (!Peer.getStorage().addChunk(chunk))
                return;

            try {
                String filename = Peer.getId() + "/" + senderId + "_" + fileId + "_" + chunkNr;

                File file = new File(filename);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                try (FileOutputStream fos = new FileOutputStream(filename)) {
                    fos.write(content);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            Peer.getStorage().incStoredChunk(fileId, chunkNr);
            String header = "STORED " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr + "\r\n\r\n";
            System.out.println("Sent " + header);
            Peer.getMC().sendMessage(header.getBytes());
        }


    }
}
