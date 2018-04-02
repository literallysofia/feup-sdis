import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RemovedReceivedMessageThread implements Runnable {

    private String fileId;
    private int chunkNr;

    public RemovedReceivedMessageThread(String fileId, int chunkNr) {
        this.fileId = fileId;
        this.chunkNr = chunkNr;
    }

    @Override
    public void run() {

        boolean hasChunk = false;
        int desiredReplicationDegree = 0;
        for (int i = 0; i < Peer.getStorage().getStoredChunks().size(); i++) {
            if (Peer.getStorage().getStoredChunks().get(i).getFileID().equals(fileId) && Peer.getStorage().getStoredChunks().get(i).getNr() == chunkNr) {
                hasChunk = true;
                desiredReplicationDegree = Peer.getStorage().getStoredChunks().get(i).getDesiredReplicationDegree();
                break;
            }
        }

        if (hasChunk) {
            String key = fileId + '_' + chunkNr;

                if (Peer.getStorage().getStoredOccurrences().get(key) < desiredReplicationDegree) {

                    int sizeOfChunks = 64000;
                    byte[] buffer = new byte[sizeOfChunks];
                    byte[] body = new byte[sizeOfChunks];

                    File file = new File(Peer.getId() + "/" + fileId + "_" + chunkNr);

                    try (FileInputStream fis = new FileInputStream(file);
                         BufferedInputStream bis = new BufferedInputStream(fis)) {

                        int bytesAmount;
                        while ((bytesAmount = bis.read(buffer)) > 0) {
                            body = Arrays.copyOf(buffer, bytesAmount);
                            buffer = new byte[sizeOfChunks];
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String header = "PUTCHUNK " + "1.0" + " " + Peer.getId() + " " + fileId + " " + chunkNr + " " + desiredReplicationDegree + "\r\n\r\n";
                    System.out.println("Sent PUTCHUNK " + chunkNr + " after REMOVED chunk ");

                    if (!Peer.getStorage().getStoredOccurrences().containsKey(key)) {
                        Peer.getStorage().getStoredOccurrences().put(key, 0);
                    }

                    byte[] asciiHeader;
                    try {
                        asciiHeader = header.getBytes("US-ASCII");
                        byte[] message = new byte[asciiHeader.length + body.length];
                        System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                        System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                        SendMessageThread sendThread = new SendMessageThread(message, "MDB");
                        Peer.getExec().execute(sendThread);

                        Peer.getExec().schedule(new PutChunkManager(message, 1, fileId, chunkNr, desiredReplicationDegree), 1, TimeUnit.SECONDS);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

        }

    }
}
