package Messages;

import Messages.*;
import Peer.*;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String senderId;
    private String address;
    private int port;
    private byte[] key;
    

    public RegisterMessage(String header,byte[] body){
        super();
        String[] headerWords = header.split(" ");
        this.senderId = headerWords[1];
        this.address = headerWords[2];
        this.port = Integer.parseInt(headerWords[3]);
        this.key = body;       
    }

    public RegisterMessage(String senderId, String address, int port,byte[] body){
        super();
        this.senderId = senderId;
        this.address = address;
        this.port = port;
        this.key = body;
    
    }

    public byte[] getFullMessage() {
        String header = "REGISTER " + this.senderId + " " + this.address + " " + this.port + " " + this.CRLFCRLF;
        System.out.println("Sent: " + "REGISTER " + this.senderId + " " + this.address + " " + this.port);                
        byte[] headerBytes = header.getBytes();
        byte[] finalArray = new byte[headerBytes.length+key.length];

        System.arraycopy(headerBytes, 0, finalArray, 0, headerBytes.length);
        System.arraycopy(key, 0, finalArray, headerBytes.length,key.length);
        return finalArray;

    }

    public int action(DataOutputStream writer) {

        int res = Tracker.addOnlinePeer(this.senderId, this.address, this.port,this.key);
        
        if(res == -1){
            String header = "ERROR " + " " + this.CRLFCRLF;
            byte[] headerBytes = header.getBytes();
            //TODO: send error
            return -1;
        
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