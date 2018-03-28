import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
                break;
            case "GETCHUNK":
                break;
            case "DELETE":
                break;
            case "REMOVED":
                break;
            case "STATUS":
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

        int i=0;
        for (; i < this.msgBytes.length-6; i++){
            if(this.msgBytes[i]==0xD && this.msgBytes[i+1] == 0xA && this.msgBytes[i+2]==0xD && this.msgBytes[i+3]==0xA){
                break;
            }
        }
        byte[] header = Arrays.copyOfRange(this.msgBytes, 0, i);
        byte[] body = Arrays.copyOfRange(this.msgBytes, i+4, this.msgBytes.length);

        String headerStr = new String(header);
        String trimmedMsg = headerStr.trim();

        System.out.println(trimmedMsg);

        String[] headerArray = trimmedMsg.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId= Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());
        int replicationDegree = Integer.parseInt(headerArray[5].trim());

        System.out.println("Received PUTCHUNK Version: "+ version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr + " replicationDegree: " +  replicationDegree);

        System.out.println(new String(body));

        try (FileOutputStream fos = new FileOutputStream("./test.jpg")) {
            fos.write(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
