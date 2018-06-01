package Peer;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class ChunkStatus implements java.io.Serializable{

    boolean sentGetChunk = false;
    boolean receivedChunk = false;
    long getChunkTime;
     
    public ChunkStatus(){

       
    }

    /**
     * @return the getChunkTime
     */
    public synchronized long getGetChunkTime() {
        return getChunkTime;
    }

    /**
     * @param getChunkTime the getChunkTime to set
     */
    public synchronized void setGetChunkTime(long getChunkTime) {
        this.getChunkTime = getChunkTime;
    }

    public synchronized boolean getSentGetChunk(){
        return this.sentGetChunk;
    }

    public synchronized void updateSentGetChunk(boolean bool){
        this.sentGetChunk = bool;
        this.getChunkTime = System.currentTimeMillis();
    }

    public synchronized boolean getReceivedChunk(){
        return receivedChunk;
    }

    public synchronized void updateReceivedChunk(boolean bool){
        this.receivedChunk = bool;
    }

    public synchronized boolean toResend() {
        
        if(sentGetChunk && !receivedChunk) {
            if((System.currentTimeMillis()-getChunkTime)>15000) {
                return true;
            }
        }
        return false;
    }

    
}