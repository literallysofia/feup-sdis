package Messages;
import Peer.*;
import Peer.FileManager;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetChunkMessage extends Message {

    private String CRLFCRLF = "\r\n\r\n";

    private String fileId;
    private int chunkNro;

    public GetChunkMessage(String header) {

        super();
        String[] headerWords = header.split(" ");
        this.fileId = headerWords[1];
        this.chunkNro = Integer.parseInt(headerWords[2]);
    }

    public GetChunkMessage(String fileId, int chunkNro) {

        super();
        this.fileId = fileId;
        this.chunkNro = chunkNro;
    }

    public byte[] getFullMessage() {
        String header = "GETCHUNK " + this.fileId + " " + this.chunkNro + " " + this.CRLFCRLF;
        System.out.println("Sent: " + "GETCHUNK " + this.fileId + " " + this.chunkNro);
        byte[] headerBytes = header.getBytes();
        return headerBytes;

    }

    public synchronized int action(DataOutputStream writer) {

        TorrentInfo torrentInfo = Peer.getStorage().getFilesSeeded().get(fileId);

        if (torrentInfo != null) {
            FileManager manager = new FileManager(torrentInfo.getFilePath());

            try {
                byte[] data = manager.readFileAsync(chunkNro * torrentInfo.getChunkLength(),
                        (int) torrentInfo.getChunkLength());
                if(data == null)
                	return -1;
                ChunkMessage chunkMessage = new ChunkMessage(this.fileId, this.chunkNro, data);
                writer.write(chunkMessage.getFullMessage());
            } catch (IOException e) {
                System.out.println("Cannot write getchunk");
                e.printStackTrace();
            }

            return 0;

        }

        return -1;

    }
}