package Peer;

import Messages.*;
import Peer.*;
import java.util.AbstractMap.SimpleEntry;
import Sockets.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * Tracker
 */
public class Tracker extends Node{

    private static volatile ConcurrentHashMap<String,PeerInfo> onlinePeers = new ConcurrentHashMap<>();
    private static volatile ConcurrentHashMap<String,ArrayList<String>> availableFiles = new ConcurrentHashMap<>();

    private static ScheduledThreadPoolExecutor exec;
    private static ReceiverSocket sslServerSocket;
   

    public Tracker() throws IOException{

        this.exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(100000);
        this.sslServerSocket = new ReceiverSocket(5555);
        this.sslServerSocket.connect("tracker");


        Runnable verifyOnlinePeersThread = new VerifyOnlinePeersThread();
        Tracker.getExec().scheduleAtFixedRate(verifyOnlinePeersThread, 60, 120, TimeUnit.SECONDS);

    }

    public class VerifyOnlinePeersThread implements Runnable {
        public VerifyOnlinePeersThread() {}
        @Override
        public void run() {
            System.out.println("TRACKER - Verifying online peers...");                        
            for (String key : onlinePeers.keySet()) {
                PeerInfo peer = onlinePeers.get(key);

                long deltaTime = 120000;
                long currTime = System.currentTimeMillis();
                long lastTimeOnline = peer.getLastTimeOnline();

                if(currTime - lastTimeOnline > deltaTime){
                    onlinePeers.remove(key);
                    System.out.println("TRACKER - Peer " + key + " is no longer online.");
                }

            }

        }
    }

    public static ScheduledExecutorService getExec() {
        return exec;
    }

    public static ConcurrentHashMap<String,PeerInfo> getOnlinePeers(){
        return Tracker.onlinePeers;
    }

    public static ConcurrentHashMap<String,ArrayList<String>> getAvailableFiles(){
        return Tracker.availableFiles;
    }


    public static void main(String[] args) throws IOException {
        
        System.setProperty("java.net.preferIPv4Stack", "true");
        Tracker tracker = new Tracker();
    }
    
    public static int addOnlinePeer(String peerId, String address, int port,byte[] key){

        long time = System.currentTimeMillis();
        PeerInfo peerInfo = new PeerInfo(address, port, time,key);
        
        if(onlinePeers.get(peerId)==null){
            Tracker.onlinePeers.put(peerId, peerInfo); 
            System.out.println("TRACKER - Peer added to the system.");
            return 0;
        }
        else{
            PeerInfo oldInfo = onlinePeers.get(peerId);

            if(oldInfo.getAddress().equals(peerInfo.getAddress())){
                if(oldInfo.getPort() != peerInfo.getPort()){
                    oldInfo.setPort(port);
                    System.out.println("TRACKER - Peer updated with new port.");
                    return 0;
                }
            }
            else{
                System.out.println("TRACKER ERROR - The id and ip address do not match.");
                return -1;
            }
        }
        
        return -1;
        
    }

    public static int refreshOnlinePeer(String senderId){
        if(onlinePeers.get(senderId)==null){
            System.out.println("TRACKER ERROR - You are not registered in the system.");
            return - 1;  
        }

        PeerInfo peerInfo = onlinePeers.get(senderId);
        long time = System.currentTimeMillis();
        peerInfo.setLastTimeOnline(time);

        return 0;
    }

    public static int addPeerToFile(String senderId, String fileId){

        if(onlinePeers.get(senderId)==null){
            System.out.println("TRACKER ERROR - You are not registered in the system.");
            return - 1;  
        }

        if(availableFiles.get(fileId) == null){
            ArrayList<String> peersIds = new ArrayList(); 
            peersIds.add(senderId);
            availableFiles.put(fileId, peersIds);

            System.out.println("TRACKER - File added.");
            return 0;
        }
        else{
            if(!availableFiles.get(fileId).contains(senderId)){
                availableFiles.get(fileId).add(senderId);
                System.out.println("TRACKER - You were added to the peers with this file.");
                return 0;
            }
        }

        return -1;

    }

    public static int removePeerOfFile(String senderId, String fileId){

        if(onlinePeers.get(senderId)==null){
            System.out.println("TRACKER ERROR - You are not registered in the system.");
            return - 1;  
        }

        if(availableFiles.get(fileId) != null){

            availableFiles.get(fileId).remove(senderId);

            if(availableFiles.get(fileId).isEmpty()){
                availableFiles.remove(fileId);
            }

            System.out.println("TRACKER - File removed.");
            return 0;
        }

        return -1;

    }

    public static ArrayList<PeerInfo> getAvailableFile(String senderId, String fileId){


        if(onlinePeers.get(senderId)==null){
            System.out.println("TRACKER ERROR - You are not registered in the system.");
            return null;  
        }

        ArrayList<PeerInfo> filePeers = new ArrayList<>();

        ArrayList<String> peersIds = availableFiles.get(fileId);
        if(peersIds!=null){
            for(int i = 0; i < peersIds.size(); i++){
                if(onlinePeers.get(peersIds.get(i)) != null){
                    filePeers.add(onlinePeers.get(peersIds.get(i)));   
                }
                else{
                    peersIds.remove(i);
                }
            }
        }
        else{
            System.out.println("TRACKER ERROR - That file is not available");
            return null;
        }
        

        return filePeers;
    }
    

    

    

}