import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedGetChunkThread implements Runnable {

    private String fileId;
    private int chunkNr;

    public ReceivedGetChunkThread(String fileId, int chunkNr) {
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

                    File file = new File(chunkPath);
                    byte[] body = new byte[(int) file.length()];
                    FileInputStream in = new FileInputStream(file);
                    in.read(body);

                    byte[] message = new byte[asciiHeader.length + body.length];
                    System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                    System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                    SendMessageThread sendThread = new SendMessageThread(message, "MDR");
                    System.out.println("Sent" + "CHUNK " + "1.0" + " " + Peer.getId() + " " + this.fileId + " " + this.chunkNr);
                    Random random = new Random();
                    Peer.getExec().schedule(sendThread, random.nextInt(401), TimeUnit.MILLISECONDS);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private boolean isSameChunk(String fileId, int chunkNr) {
        return fileId.equals(this.fileId) && chunkNr == this.chunkNr;
    }

    private boolean isAbortSend() {
        for (int i = 0; i < Peer.getStorage().getReceivedChunks().size(); i++) {
            if (isSameChunk(Peer.getStorage().getReceivedChunks().get(i).getFileID(), Peer.getStorage().getReceivedChunks().get(i).getNr()))
                return true;
        }
        return false;
    }
}
