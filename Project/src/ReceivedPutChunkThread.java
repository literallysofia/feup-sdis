import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceivedPutChunkThread implements Runnable {

    private Double version;
    private String fileId;
    private int chunkNr;
    private int replicationDegree;
    private byte[] content;

    public ReceivedPutChunkThread(double version, String fileId, int chunkNr, int replicationDegree, byte[] content) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNr = chunkNr;
        this.replicationDegree = replicationDegree;
        this.content = content;
    }

    @Override
    public void run() {
        String key = fileId + "_" + chunkNr;

        if (version == 2.0) {
            if (Peer.getStorage().getStoredOccurrences().get(key) >= replicationDegree)
                return;
        }

        if (Peer.getStorage().getSpaceAvailable() - content.length >= 0) {
            Chunk chunk = new Chunk(chunkNr, fileId, replicationDegree, content.length);

            if (!Peer.getStorage().addStoredChunk(chunk)) {
                return;
            }

            Peer.getStorage().decSpaceAvailable(content.length);

            try {
                String filename = Peer.getId() + "/" + fileId + "_" + chunkNr;

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
            Peer.getStorage().incStoredOccurrences(fileId, chunkNr);
            String header = "STORED " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr + "\r\n\r\n";
            System.out.println("Sent " + "STORED " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr);
            Peer.getMC().sendMessage(header.getBytes());
        } else {
            System.out.println("THIS PEER DOESN'T HAVE SPACE TO STORE CHUNK " + fileId + "_" + chunkNr);
        }

    }
}
