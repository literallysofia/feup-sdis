import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ManageReceivedMessageThread implements Runnable {

    private byte[] msgBytes;

    public ManageReceivedMessageThread(byte[] msgBytes) {
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
                managePutchunk();
                break;
            case "CHUNK":
                manageChunk();
                break;
        }
    }

    private synchronized void managePutchunk() {

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
        if (!Peer.getStorage().getStoredOccurrences().containsKey(key)) {
            Peer.getStorage().getStoredOccurrences().put(key, 0);
        }

        if (Peer.getId() != senderId) {
            Random random = new Random();
            System.out.println("Received PUTCHUNK Version: " + version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr + " replicationDegree: " + replicationDegree);
            Peer.getExec().schedule(new PutchunkReceivedThread(version, senderId, fileId, chunkNr, replicationDegree, body), random.nextInt(401), TimeUnit.MILLISECONDS);
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


        if (Peer.getId() != senderId) {
            Peer.getStorage().incStoredChunk(fileId, chunkNr);
            System.out.println("Received STORED Version: " + version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr);
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

        if (Peer.getId() != senderId) {
            Peer.getStorage().deleteStoredChunks(fileId, senderId);
            System.out.println("Received DELETE Version: " + version + " SenderId: " + senderId + " fileId: " + fileId);
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

        if (Peer.getId() != senderId) {
            Random random = new Random();
            System.out.println("Received GETCHUNK Version: " + version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr);
            Peer.getExec().schedule(new GetChunkReceivedThread(fileId, chunkNr), random.nextInt(401), TimeUnit.MILLISECONDS);
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

        if (Peer.getId() != senderId) {
            Chunk chunk = new Chunk(chunkNr, fileId, 0, 0);
            Peer.getStorage().getReceivedChunks().add(chunk);

            if (!Peer.getStorage().getWantedChunks().isEmpty()) {
                Peer.getStorage().addWantedChunkContent(fileId, chunkNr, body);
                System.out.println("ADDED WANTED!");
            }
            System.out.println("Received CHUNK Version: " + version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr);
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

        if (Peer.getId() != senderId) {
            Peer.getStorage().decStoredChunk(fileId, chunkNr);
            System.out.println("Received REMOVED Version: " + version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr);
            Random random = new Random();
            Peer.getExec().schedule(new RemovedReceivedMessageThread(version, senderId, fileId, chunkNr), random.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }
}
