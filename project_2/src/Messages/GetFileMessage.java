package Messages;

import Peer.*;
import java.util.*;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetFileMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String senderId;
    private String fileId;    

    public GetFileMessage(String header){

        super();
        String[] headerWords = header.split(" ");
        this.senderId = headerWords[1];
        this.fileId = headerWords[2];        
    }

    public GetFileMessage(String senderId, String fileId) {

        super();
        this.senderId = senderId;
        this.fileId = fileId;
    
    }


    public byte[] getFullMessage() {
        String header = "GETFILE " + this.senderId + " " + this.fileId + " " + this.CRLFCRLF;
        System.out.println("Sent: " +  "GETFILE " + this.senderId + " " + this.fileId );                
        byte[] headerBytes = header.getBytes();
        return headerBytes;

    }

    public int action(DataOutputStream writer) {

        ArrayList<PeerInfo> filePeers = Tracker.getAvailableFile(this.senderId, this.fileId);

        if(filePeers == null){
            String header = "ERROR" + " " + this.CRLFCRLF;
            byte[] headerBytes = header.getBytes();
            //TODO: send error
        }
        else{
            for(int i = 0; i < filePeers.size(); i++){
                System.out.println("TRACKER - Peer: " + filePeers.get(i).getAddress() + " " +  filePeers.get(i).getPort());
                PeerInfoMessage peerinfo = new PeerInfoMessage(this.fileId, filePeers.get(i).getAddress(), filePeers.get(i).getPort(), filePeers.get(i).getPublicKey());
                try {
                    writer.write(peerinfo.getFullMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
            PeerInfoEndMessage peerinfoend = new PeerInfoEndMessage(this.fileId, true);
            try {
                writer.write(peerinfoend.getFullMessage());
                
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
       
        return 0;
        
    }
}