package Messages;

import Peer.*;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class HasFileMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String senderId;
    private String fileId;  

    public HasFileMessage(String header){

        super();
        String[] headerWords = header.split(" ");
        this.senderId = headerWords[1];
        this.fileId = headerWords[2];
        
    }

    public HasFileMessage(String senderId, String fileId) {

        super();
        this.senderId = senderId;
        this.fileId = fileId;
    
    }


    public byte[] getFullMessage() {
        String header = "HASFILE " + this.senderId + " " + this.fileId + " " + this.CRLFCRLF;
        System.out.println("Sent: " +  "HASFILE " + this.senderId + " " + this.fileId);                
        byte[] headerBytes = header.getBytes();
        return headerBytes;

    }

    public int action(DataOutputStream writer) {

        int res = Tracker.addPeerToFile(this.senderId, this.fileId);

        if(res == -1){
            String header = "ERROR" + " " + this.CRLFCRLF;
            byte[] headerBytes = header.getBytes();
            //TODO: send error
        
        }else if(res == 0){
            String header = "CLOSE " + this.senderId + " " + this.CRLFCRLF;
            byte[] headerBytes = header.getBytes();
            try {
                writer.write(headerBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return 0;
        }

        return -1;
        
    }
}