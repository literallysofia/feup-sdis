package Messages;

import Peer.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class OnlineMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String senderId;    

    public OnlineMessage(String header){

        String[] headerWords = header.split(" ");
        this.senderId = headerWords[1];        
    }

    public OnlineMessage(String senderId, boolean toSend){

        this.senderId = senderId;    
    }

    public byte[] getFullMessage() {
        String header = "ONLINE " + this.senderId + " " + this.CRLFCRLF;
        System.out.println("Sent: " +  "ONLINE " + this.senderId );                
        byte[] headerBytes = header.getBytes();
        return headerBytes;
    }

    public int action(DataOutputStream writer) {

        int res = Tracker.refreshOnlinePeer(this.senderId);
        
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
        }

        return 0;
        
    }
}