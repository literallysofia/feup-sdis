import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
            //CONTROL
            case "STORED":
                manageStored();
                break;
            case "GETCHUNK":
                break;
            case "DELETE":
                break;
            case "REMOVED":
                break;
            //BACKUP
            case "PUTCHUNK":
                managePutchunk();
                break;
            //RESTORE
            case "RESTORE":
                break;
            case "CHUNK":
                break;
        }
    }

    private void managePutchunk() {

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

        if (Peer.getId() != senderId) {
            Random random = new Random();
            System.out.println("Received PUTCHUNK Version: " + version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr + " replicationDegree: " + replicationDegree);
            Peer.getExec().schedule(new PutchunkReceivedThread(version, senderId, fileId, chunkNr, replicationDegree), random.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }

    private void manageStored() {

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
