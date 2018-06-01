package Messages;

import Peer.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ChunkMessage extends Message{

    private String CRLFCRLF = "\r\n\r\n";

    private String fileId;
    private int chunkNr; 
    private byte[] body;

    public ChunkMessage(String header, byte[] body){

        String[] headerWords = header.split(" ");
        this.fileId = headerWords[1];
        this.chunkNr = Integer.parseInt(headerWords[2]);
        this.body = body; 
               
    }

    public ChunkMessage(String fileId, int chunkNr, byte[] body){

        this.fileId = fileId;
        this.chunkNr = chunkNr;
        //this.body = new String("aaa").getBytes();
        this.body = body;
    
    }

    public byte[] getFullMessage() {
        String header = "CHUNK " + this.fileId + " " + this.chunkNr + " " +this.CRLFCRLF;
        System.out.println("Sent: " + "CHUNK " + this.fileId + " " + this.chunkNr );        
        byte[] headerBytes = header.getBytes();
        byte[] finalByteArray = new byte[headerBytes.length+this.body.length];
        System.arraycopy( headerBytes, 0, finalByteArray, 0, headerBytes.length);
        System.arraycopy( this.body, 0, finalByteArray, headerBytes.length, this.body.length );       
        return finalByteArray;
    }

    public synchronized void setPeersUnavailable(ArrayList<PeerInfo> peerInfos){

        if(peerInfos == null)
            return;
        for (int i = 0; i<peerInfos.size(); i++) {
            if(peerInfos.get(i) != null)
            {
                //System.out.println("UNAVAILABLE");
                peerInfos.get(i).setAvailable(false);
            }
        }
    }


    public synchronized int action(DataOutputStream outputStream, ArrayList<PeerInfo> peersInfo) {
        
       

        //TorrentInfo torrentInfo = Peer.getStorage().getFilesDownloaded().get(fileId);
        TorrentInfo torrentInfo = Peer.getStorageFileDownloaded(fileId);
        if(torrentInfo.isCompleted()){
            return 1;
        }


       
        if(torrentInfo != null && !torrentInfo.isCompleted()){

            torrentInfo.updateReceivedChunk(chunkNr, true);

            FileManager manager = new FileManager(torrentInfo.getFilePath());

            try {
                manager.writeToFileAsync(body,chunkNr*torrentInfo.getChunkLength());           
                
                //int nextChunkNro = torrentInfo.getNextFalseSendedGetChunks();
                int nextChunkNro = torrentInfo.getNextChunkToSend();
                if((nextChunkNro != -1) || ((nextChunkNro = torrentInfo.toResend()) != -1)){

                    //torrentInfo.updateSendedGetChunkMessages(nextChunkNro, true);
                    torrentInfo.updateSentGetChunk(nextChunkNro, true);
                    

                    Message getchunk = new GetChunkMessage(fileId,nextChunkNro);
                    try {
                        outputStream.write(getchunk.getFullMessage());
                    } catch (IOException e) {
                        //torrentInfo.updateSendedGetChunkMessages(nextChunkNro, false);
                        setPeersUnavailable(peersInfo);
                        System.out.println("Cannot write chunk");
                        torrentInfo.updateSentGetChunk(nextChunkNro, false);
                        return 1;
                    }

                    return 0;

                }
                else{
                    //torrentInfo.setSendedGetChunkMessages(null);
                    //torrentInfo.clearChunksStatus();           
                    Peer.getStorage().getFilesSeeded().put(fileId, torrentInfo);
                    //Peer.getStorage().getFilesDownloaded().remove(fileId);
                    if(torrentInfo.isCompleted()){
                        return 1;
                    }
                    torrentInfo.setCompleted(true);
                    Message message = new HasFileMessage(Peer.getPeerID(), fileId);
                    Peer.sendMessageToTracker(message);
                    return 1;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Torrent info is null");
        }
        return 0;
    }
}