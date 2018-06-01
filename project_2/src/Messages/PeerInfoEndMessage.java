package Messages;

import Peer.*;
import Sockets.SenderSocket;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class PeerInfoEndMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String fileId;

    public PeerInfoEndMessage(String header){

        String[] headerWords = header.split(" ");
        this.fileId = headerWords[1];
               
    }

    public PeerInfoEndMessage(String fileId, boolean toSend){

        this.fileId = fileId;
    
    }

    public byte[] getFullMessage() {
        String header = "PEERINFOEND " + this.fileId + " " +this.CRLFCRLF;
        System.out.println("Sent: " +  "PEERINFOEND " + this.fileId);                
        byte[] headerBytes = header.getBytes();
        return headerBytes;

    }

    public int action() {
        try {
            Peer.manageFileDownload(this.fileId);
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
      
        return 0;


    }
}