package Messages;

import Peer.*;
public class NoFileMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String senderId;
    private String fileId;  

    public NoFileMessage(String header){

        String[] headerWords = header.split(" ");
        this.senderId = headerWords[1];
        this.fileId = headerWords[2];
        
    }

    public NoFileMessage(String senderId, String fileId) {

        this.senderId = senderId;
        this.fileId = fileId;
    
    }


    public byte[] getFullMessage() {
        String header = "NOFILE " + this.senderId + " " + this.fileId + " " + this.CRLFCRLF;
        System.out.println("Sent: " + "NOFILE " + this.senderId + " " + this.fileId);                
        byte[] headerBytes = header.getBytes();
        return headerBytes;

    }

    public int action() {

        int res = Tracker.removePeerOfFile(this.senderId, this.fileId);

        if(res == -1){
            String header = "ERROR" + " " + this.CRLFCRLF;
            byte[] headerBytes = header.getBytes();
            //TODO: send error
        
        }else if(res == 0){
            String header = "SUCCESS" + " " + this.CRLFCRLF;
            byte[] headerBytes = header.getBytes();
            //TODO: send success
        }

        return 0;
        
    }
}