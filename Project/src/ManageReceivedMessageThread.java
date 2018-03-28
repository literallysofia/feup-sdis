import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ManageReceivedMessageThread implements Runnable {

    public byte[] msgBytes;

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

    public void managePutchunk() {

        byte[] CRLFCRLF = ("\r\n\r\n").getBytes();

        List<byte[]> headerBody = tokens(msgBytes, CRLFCRLF);
        System.out.println("MESSAGE: " + msgBytes);
        System.out.println("HEADER: " + headerBody.get(0));
        System.out.println("BODY: " + headerBody.get(1));

        String headerStr = new String(headerBody.get(0));
        String trimmedMsg = headerStr.trim();
        String[] headerArray = trimmedMsg.split(" ");

        Double version = Double.parseDouble(headerArray[1].trim());
        int senderId= Integer.parseInt(headerArray[2].trim());
        String fileId = headerArray[3].trim();
        int chunkNr = Integer.parseInt(headerArray[4].trim());
        int replicationDegree = Integer.parseInt(headerArray[5].trim());

        System.out.println("Received PUTCHUNK Version: "+ version + " SenderId: " + senderId + " fileId: " + fileId + " chunkNr: " + chunkNr + " replicationDegree: " +  replicationDegree);
    }

    public static List<byte[]> tokens(byte[] array, byte[] delimiter) {
        List<byte[]> byteArrays = new LinkedList<>();
        if (delimiter.length == 0) {
            return byteArrays;
        }
        int begin = 0;

        outer:
        for (int i = 0; i < array.length - delimiter.length + 1; i++) {
            for (int j = 0; j < delimiter.length; j++) {
                if (array[i + j] != delimiter[j]) {
                    continue outer;
                }
            }
            byteArrays.add(Arrays.copyOfRange(array, begin, i));
            begin = i + delimiter.length;
        }
        byteArrays.add(Arrays.copyOfRange(array, begin, array.length));
        return byteArrays;
    }
}
