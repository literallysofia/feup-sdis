import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedMessagesManagerThread implements Runnable {

    private byte[] msgBytes;

    public ReceivedMessagesManagerThread(byte[] msgBytes) {
        this.msgBytes = msgBytes;
    }

    public void run() {
        String msg = new String(this.msgBytes, 0, this.msgBytes.length);
        String trimmedMsg = msg.trim();
        String[] msgArray = trimmedMsg.split(" ");

        switch (msgArray[0]) {
            case "STORED":
                manageStored();
                break;
            case "GETCHUNK":
                manageGetChunk();
                break;
            case "DELETE":
                manageDelete();
                break;
            case "REMOVED":
                manageRemoved();
                break;
            case "PUTCHUNK":
                managePutChunk();
                break;
            case "CHUNK":
                manageChunk();
                break;
        }
    }

    private synchronized void managePutChunk() {

        List<byte[]> headerAndBody = getHeaderAndBody();
        byte[] header = headerAndBody.get(0);
        byte[] body = headerAndBody.get(1);

        String headerStr = new String(header);
        String trimmedStr = headerStr.trim();
        String[] headerArray = trimmedStr.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId = Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());
        int replicationDegree = Integer.parseInt(headerArray[5].trim());

        String key = fileId + "_" + chunkNr;
        if (!Peer.getStorage().getStoredOccurrences().containsKey(key)) { //if this chunk ins's in the current replication degrees table
            Peer.getStorage().getStoredOccurrences().put(key, 0); //the chunk is added to that table
        }

        if (Peer.getId() != senderId) { //if the peer who sent the message isnt the one who is receiving it
            Random random = new Random();
            System.out.println("Received PUTCHUNK " + version + " " + senderId + " " + fileId + " " + chunkNr + " " + replicationDegree);
            Peer.getExec().schedule(new ReceivedPutChunkThread(version, fileId, chunkNr, replicationDegree, body), random.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void manageStored() {

        List<byte[]> headerAndBody = getHeaderAndBody();
        byte[] header = headerAndBody.get(0);

        String headerStr = new String(header);
        String trimmedStr = headerStr.trim();
        String[] headerArray = trimmedStr.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId = Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());


        if (Peer.getId() != senderId) { //if the peer who sent the message isnt the one who is receiving it
            Peer.getStorage().incStoredOccurrences(fileId, chunkNr); //increments the number of stored occurances of this chunk
            System.out.println("Received STORED " + version + " " + senderId + " " + fileId + " " + chunkNr);
        }
    }

    private void manageDelete() {

        List<byte[]> headerAndBody = getHeaderAndBody();
        byte[] header = headerAndBody.get(0);

        String headerStr = new String(header);
        String trimmedStr = headerStr.trim();
        String[] headerArray = trimmedStr.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId = Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();

        if (Peer.getId() != senderId) { //if the peer who sent the message isnt the one who is receiving it
            Peer.getStorage().deleteStoredChunks(fileId);
            System.out.println("Received DELETE " + version + " " + senderId + " " + fileId);
        }
    }

    private void manageGetChunk() {

        List<byte[]> headerAndBody = getHeaderAndBody();
        byte[] header = headerAndBody.get(0);

        String headerStr = new String(header);
        String trimmedStr = headerStr.trim();
        String[] headerArray = trimmedStr.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId = Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());

        if (Peer.getId() != senderId) { //if the peer who sent the message isnt the one who is receiving it
            Random random = new Random();
            System.out.println("Received GETCHUNK " + version + " " + senderId + " " + fileId + " " + chunkNr);
            Peer.getExec().schedule(new ReceivedGetChunkThread(fileId, chunkNr), random.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }

    private void manageChunk() {

        List<byte[]> headerAndBody = getHeaderAndBody();
        byte[] header = headerAndBody.get(0);
        byte[] body = headerAndBody.get(1);

        String headerStr = new String(header);
        String trimmedStr = headerStr.trim();
        String[] headerArray = trimmedStr.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId = Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());

        if (Peer.getId() != senderId) { //if the peer who sent the message isnt the one who is receiving it
            Chunk chunk = new Chunk(chunkNr, fileId, 0, 0);
            Peer.getStorage().getReceivedChunks().add(chunk);

            if (!Peer.getStorage().getWantedChunks().isEmpty()) {
                Peer.getStorage().setWantedChunkReceived(fileId, chunkNr);
                storeRestoredChunks(fileId, chunkNr, body);
            }
            System.out.println("Received CHUNK " + version + " " + senderId + " " + fileId + " " + chunkNr);
        }
    }

    private void manageRemoved() {

        List<byte[]> headerAndBody = getHeaderAndBody();
        byte[] header = headerAndBody.get(0);

        String headerStr = new String(header);
        String trimmedStr = headerStr.trim();
        String[] headerArray = trimmedStr.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId = Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());

        if (Peer.getId() != senderId) {  //if the peer who sent the message isnt the one who is receiving it
            Peer.getStorage().decStoredOccurrences(fileId, chunkNr); //decrements de current replication degree of this chunk
            System.out.println("Received REMOVED " + version + " " + senderId + " " + fileId + " " + chunkNr);
            Random random = new Random();
            Peer.getExec().schedule(new ReceivedRemovedMessageThread(fileId, chunkNr), random.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }

    private void storeRestoredChunks(String fileId, int chunkNr, byte[] body) {

        try {
            String filename = Peer.getId() + "/" + fileId + "_" + chunkNr;

            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            try (FileOutputStream fos = new FileOutputStream(filename)) {
                fos.write(body);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<byte[]> getHeaderAndBody() {

        int i;
        for (i = 0; i < this.msgBytes.length - 4; i++) {
            if (this.msgBytes[i] == 0xD && this.msgBytes[i + 1] == 0xA && this.msgBytes[i + 2] == 0xD && this.msgBytes[i + 3] == 0xA) {
                break;
            }
        }
        byte[] header = Arrays.copyOfRange(this.msgBytes, 0, i);
        byte[] body = Arrays.copyOfRange(this.msgBytes, i + 4, this.msgBytes.length);

        List<byte[]> headerAndBody = new ArrayList<>();

        headerAndBody.add(header);
        headerAndBody.add(body);

        return headerAndBody;
    }
}
