import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetChunkReceivedThread implements Runnable {

    private String fileId;
    private int chunkNr;

    public GetChunkReceivedThread(String fileId, int chunkNr) {
        this.fileId = fileId;
        this.chunkNr = chunkNr;
    }

    @Override
    public void run() {
        for (int i = 0; i < Peer.getStorage().getStoredChunks().size(); i++) {
            if (isSameChunk(Peer.getStorage().getStoredChunks().get(i).getFileID(), Peer.getStorage().getStoredChunks().get(i).getNr()) && !isAbortSend()) {
                String header = "CHUNK " + "1.0" + " " + Peer.getId() + " " + this.fileId + " " + this.chunkNr + "\r\n\r\n";

                try {
                    byte[] asciiHeader = header.getBytes("US-ASCII");

                    String chunkPath = Peer.getId() + "/" + fileId + "_" + chunkNr;

                    Path fileLocation = Paths.get(chunkPath);
                    byte[] body = Files.readAllBytes(fileLocation);

                    byte[] message = new byte[asciiHeader.length + body.length];
                    System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                    System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                    SendMessageThread sendThread = new SendMessageThread(message, "MDR");
                    System.out.println("Sent CHUNK");
                    Peer.getExec().execute(sendThread);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private boolean isSameChunk(String fileId, int chunkNr) {
        if (fileId.equals(this.fileId) && chunkNr == this.chunkNr)
            return true;
        else return false;
    }

    private boolean isAbortSend() {
        for (int i = 0; i < Peer.getStorage().getReceivedChunks().size(); i++) {
            if (isSameChunk(Peer.getStorage().getReceivedChunks().get(i).getFileID(), Peer.getStorage().getReceivedChunks().get(i).getNr()))
                return true;
        }
        return false;
    }
}
