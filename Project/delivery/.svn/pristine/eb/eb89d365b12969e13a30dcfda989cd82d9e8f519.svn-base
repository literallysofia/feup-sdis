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

        for(int i =0; i < Peer.getStorage().getFiles().size(); i++){
            if(Peer.getStorage().getFiles().get(i).getId().equals(fileId)) //if this peer is the owner of the chunk
                return;
        }

        if (version == 2.0) {
            if (Peer.getStorage().getStoredOccurrences().get(key) >= replicationDegree) //if the current replication degree of the chunk is already the desired one
                return; //the peer doesn't save the chunk
        }

        if (Peer.getStorage().getSpaceAvailable() >= content.length ) { //if the peer has enough space for that chunk
            Chunk chunk = new Chunk(chunkNr, fileId, replicationDegree, content.length);

            if (!Peer.getStorage().addStoredChunk(chunk)) { //if the peer already has that chunk
                return;
            }

            Peer.getStorage().decSpaceAvailable(content.length); //decrements the space available on storage

            //creates the file and saves the chunk
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

            Peer.getStorage().incStoredOccurrences(fileId, chunkNr); //increments the number of times this chunk has been stored
            String header = "STORED " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr + "\r\n\r\n";
            System.out.println("Sent " + "STORED " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr);
            Peer.getMC().sendMessage(header.getBytes());
        } else {
            System.out.println("THIS PEER DOESN'T HAVE SPACE TO STORE CHUNK " + fileId + "_" + chunkNr);
        }

    }
}
