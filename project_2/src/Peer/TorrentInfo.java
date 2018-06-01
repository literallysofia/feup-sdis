package Peer;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class TorrentInfo implements java.io.Serializable{
    
    private String trackerAddress;
    private int trackerPort;
    private long chunkLength;
    private long fileLength;
    private String filePath;
    private String name;
    //private volatile List<Boolean> sendedGetChunkMessages;
    //private volatile List<Boolean> receivedChunkMessages;
    public volatile List<ChunkStatus> chunksStatus;
    public boolean completed = false;
     

    public TorrentInfo(String trackerAddress, int trackerPort, long chunkLength, long fileLength, String filePath){

        this.trackerAddress = trackerAddress;
        this.trackerPort = trackerPort;
        this.chunkLength = chunkLength;
        this.fileLength = fileLength;
        this.filePath = filePath;

        long totalChunks = (fileLength + chunkLength - 1)/chunkLength;
        /*System.out.println("FILE LENGTH: "+ fileLength);
        System.out.println("CHUNKLENGTH: "+ chunkLength);
        System.out.println("TORRENT INFO AAASAA: " + totalChunks);*/
        
        /*this.sendedGetChunkMessages = Collections.synchronizedList(new ArrayList<>((int)totalChunks)) ;
        this.receivedChunkMessages = Collections.synchronizedList(new ArrayList<>((int) totalChunks));   
        for(int i = 0; i < totalChunks; i++){
            this.sendedGetChunkMessages.add(false);
            this.receivedChunkMessages.add(false);
        }*/

        this.chunksStatus = Collections.synchronizedList(new ArrayList <> ((int)totalChunks));

        for(int i = 0; i < totalChunks; i++){
            ChunkStatus chunkStatus = new ChunkStatus();
            this.chunksStatus.add(chunkStatus);
        }

    }
    
    public String getName(){
    	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    /**
     * @return the chunkLength
     */
    public synchronized long getChunkLength() {
        return chunkLength;
    }

    /**
     * @param chunkLength the chunkLength to set
     */
    public void setChunkLength(long chunkLength) {
        this.chunkLength = chunkLength;
    }

    
    /*public synchronized List<Boolean> getSendedGetChunkMessages() {
        return sendedGetChunkMessages;
    }

    
    public synchronized void setSendedGetChunkMessages(List<Boolean>sendedGetChunkMessages) {
        this.sendedGetChunkMessages = sendedGetChunkMessages;
    }

    public synchronized void updateSendedGetChunkMessages(int index, boolean bool){
        sendedGetChunkMessages.remove(index);                        
        sendedGetChunkMessages.add(index, bool);
    }

    public synchronized int getNextFalseSendedGetChunks(){
        return sendedGetChunkMessages.indexOf(false);
    }*/

    /**
     * @return the fileLength
     */
    public synchronized long getFileLength() {
        return fileLength;
    }

    /**
     * @param fileLength the fileLength to set
     */
    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    /**
     * @return the path
     */
    public synchronized String getFilePath() {
        return filePath;
    }

    /**
     * @param path the path to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the trackerAddress
     */
    public synchronized String getTrackerAddress() {
        return trackerAddress;
    }
    /**
     * @param trackerAddress the trackerAddress to set
     */
    public void setTrackerAddress(String trackerAddress) {
        this.trackerAddress = trackerAddress;
    }

    /**
     * @return the trackerPort
     */
    public synchronized int getTrackerPort() {
        return trackerPort;
    }

    /**
     * @param trackerPort the trackerPort to set
     */
    public void setTrackerPort(int trackerPort) {
        this.trackerPort = trackerPort;
    }

    public synchronized boolean isCompleted(){
        return this.completed;
    }

    public synchronized void setCompleted(boolean completed){
        this.completed = completed;
    }
    
    
    
    /*public synchronized List<Boolean> getReceivedChunkMessages() {
        return receivedChunkMessages;
    }

    public synchronized void setReceivedChunkMessages(List<Boolean> receivedChunkMessages) {
        this.receivedChunkMessages = receivedChunkMessages;
    }

    public synchronized void updateChunkMessages(int index, boolean bool){
        receivedChunkMessages.remove(index);                        
        receivedChunkMessages.add(index, bool);
       
    }*/

    public synchronized int getNextChunkToSend(){
        for(int i = 0; i < this.chunksStatus.size(); i++){
            if(this.chunksStatus.get(i).getSentGetChunk() == false){
                return i;
            }
        }

        return -1;
    }

    public synchronized void updateSentGetChunk(int index, boolean bool ){
        //System.out.println("TORRENT INFO: " +index);
        this.chunksStatus.get(index).updateSentGetChunk(bool);
    }

    public synchronized void updateReceivedChunk(int index, boolean bool){
        this.chunksStatus.get(index).updateReceivedChunk(bool);        
    }

    public synchronized void clearChunksStatus(){
        this.chunksStatus.clear();
    }

    public synchronized int toResend() {
        for(int i=0; i<this.chunksStatus.size(); i++) {
            if(this.chunksStatus.get(i).toResend())
                return i;
        }
        return -1;
    }


    
}