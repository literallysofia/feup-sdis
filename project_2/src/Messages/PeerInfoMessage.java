package Messages;

import Peer.*;
import Sockets.SenderSocket;
import java.util.ArrayList;

public class PeerInfoMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String fileId;
    private String address;
    private int port; 
    private byte[] key;

    public PeerInfoMessage(String header, byte[] body){

        String[] headerWords = header.split(" ");
        this.fileId = headerWords[1];
        this.address = headerWords[2];
        this.port = Integer.parseInt(headerWords[3]);
        this.key = body; 
               
    }

    public PeerInfoMessage(String fileId, String address, int port, byte[] key){

        this.fileId = fileId;
        this.address = address;
        this.port = port;
        this.key = key;
    
    }

    public byte[] getFullMessage() {
        String header = "PEERINFO " + this.fileId + " " + this.address + " " + this.port + " " +this.CRLFCRLF;
        System.out.println("Sent: " +  "PEERINFO " + this.fileId + " " +this.address + " " + this.port);                
        byte[] headerBytes = header.getBytes();
        byte[] finalByteArray = new byte[headerBytes.length+this.key.length];
        System.arraycopy( headerBytes, 0, finalByteArray, 0, headerBytes.length);
        System.arraycopy( key, 0, finalByteArray, headerBytes.length, key.length );        
        return finalByteArray;

    }

    public int action() {

        PeerInfo peerinfo = new PeerInfo(this.address, this.port, 0, this.key);
        
        if(Peer.getStorage().getFilePeers()==null){
            //System.out.println("RIP");
        }

        if(Peer.getStorage().getFilePeers().containsKey(this.fileId)){
            if(!Peer.getStorage().getFilePeers().get(this.fileId).contains(peerinfo)) {
                Peer.getStorage().getFilePeers().get(this.fileId).add(peerinfo);
            }
        } else {
            ArrayList<PeerInfo> peers = new ArrayList<>();
            peers.add(peerinfo);
            Peer.getStorage().getFilePeers().put(this.fileId, peers);
        }
    
         
        /*try{
            SenderSocket channelStarter = new SenderSocket(this.port,this.address);
            channelStarter.connect(Peer.getPeerID(),"peer",key);
            channelStarter.getHandler().getWriter().write(new String("Ola").getBytes());
        }catch(IOException e){

        }*/
      
        return 0;


    }
}