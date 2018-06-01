package Peer;

import Messages.*;
import java.util.AbstractMap.SimpleEntry;
import Sockets.*;
import java.util.*;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.net.InetAddress;
import java.util.concurrent.*;



/**
 * peer
 */
public class Peer extends Node{

    private static String peerID;
    private static String trackerIP;
    private static int trackerPort;

    private static ScheduledThreadPoolExecutor exec;
    private static Storage storage;

    private static ReceiverSocket controlReceiver;

    private static int serId;
       

    public Peer() {
    }

    public Peer(String trackerIP ,int port, int serId) throws IOException {
        peerID = UUID.randomUUID().toString();

        Peer.trackerIP = trackerIP;
        Peer.trackerPort = port;
        Peer.serId = serId;

        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(100000);

    
        //Serializable
        LogsManager logsManager = new LogsManager();
        storage = logsManager.LoadData();


        if(!setKeyPair())
            return;

        controlReceiver = new ReceiverSocket(0);
        controlReceiver.connect(peerID);

        /*if(!this.register()){
            return;
        }*/

    }

    public class OnlineMessagesThread implements Runnable {
        public OnlineMessagesThread() {}
        @Override
        public void run() {
            Message message = new OnlineMessage(peerID, true);
            try {
                Peer.sendMessageToTracker(message);                
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTracker(){


        for (String key : storage.getFilesSeeded().keySet()) {

        	File f = new File(storage.getFilesSeeded().get(key).getFilePath());
        	if(!f.exists() || f.isDirectory()) {
        		continue;
        	}
            storage.getFilePeers().clear();
            Message message = new HasFileMessage(peerID, key);
            try {
                sendMessageToTracker(message);                
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }  
        }

        for (String key : storage.getFilesDownloaded().keySet()) {
        	
        	if(storage.getFilesDownloaded().get(key).isCompleted()) {
        		storage.getFilesDownloaded().remove(key);
        	} else {
        		
        		Message message = new GetFileMessage(peerID, key);
                try {
                    sendMessageToTracker(message);                
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }  
                
        	}
        	
            
        }


        
    }

    public boolean setKeyPair(){
        File priFile = new File("./Peer/" + peerID + ".private");
        File pubFile = new File("./Peer/" + peerID + ".public");
        if((!priFile.exists() || priFile.isDirectory()) || (!pubFile.exists() || pubFile.isDirectory())){
            if(!this.generateKeyPair()){
                System.out.println("There was a problem generating the keys");
                return false;
            }
        }
        return true;
    }


    public static ScheduledExecutorService getExec() {
        return exec;
    }

    public static String getPeerID() {
        return peerID;
    }

    public static void setPeerID(String peerID) {
        Peer.peerID = peerID;
    }

    public synchronized static Storage getStorage() {
        return storage;
    }

    public static ReceiverSocket getControlReceiver() {
        return controlReceiver;
    }

    public static String getTrackerIP() {
        return trackerIP;
    }
   
    public static int getTrackerPort() {
        return trackerPort;
    }

    public static int getSerId() {
        return serId;
    }

    public static void setSerId(int serId) {
        Peer.serId = serId;
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.net.preferIPv4Stack", "true");

        Peer peer;
        if (args.length == 4) {
            peer = new Peer(args[0],Integer.parseInt(args[1]), Integer.parseInt(args[2]));

            if(args[3].equals("download")){
                peer.download("/home/julieta/Github/feup-sdis/src/bridge.jpeg.xml", "/home/julieta/Github/feup-sdis/src/bridge" + serId +".jpeg");
            }else if(args[3].equals("seed")){
                peer.seed("/home/julieta/Github/feup-sdis/src/bridge.jpeg", "/home/julieta/Github/feup-sdis/src");
            }

        } else {
            System.err.println("Error retrieving function arguments");
            return;
        }

    }

    public boolean generateKeyPair(){
        
        System.out.println("Generating Peer keys at " + System.getProperty("user.dir"));
        
        String peerName = this.peerID;
        String commandtoCreate = "keytool -genkey -alias " + peerName + "private -keystore " + peerName + ".private -storetype JKS -keyalg rsa -dname 'CN=Your Name, OU=Your Organizational Unit, O=Your Organization, L=Your City, S=Your State, C=Your Country' -storepass " + peerName + "pw -keypass "+ peerName + "pw";
        String commandtoExportPublic = "keytool -export -alias " + peerName + "private -keystore " + peerName + ".private -file temp.key -storepass " + peerName + "pw";
        String commandtoImportPublic = "keytool -import -noprompt -alias " +peerName + "public -keystore " + peerName + ".public -file temp.key -storepass public";

        try{
            String[] args = {"/bin/bash","-c","cd Peer;" + commandtoCreate + ";" + commandtoExportPublic + ";" + commandtoImportPublic + ";rm -f temp.key"};
            Process proc = new ProcessBuilder(args).start();
            proc.waitFor();
            System.out.println("Keys generated succesfully");
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
      
    }

    public byte[] readPublicKey() throws IOException{
        String pathname = "./Peer/"  + peerID + ".public";
        FileManager manager = new FileManager(pathname);
        return manager.readEntireFileData();
    }

    public boolean register() throws IOException{

        byte[] key = readPublicKey();
 
        String address = InetAddress.getLocalHost().getHostAddress();
        int port = controlReceiver.getServerSocket().getLocalPort();
        SenderSocket channelStarter = new SenderSocket(trackerPort, trackerIP);
        if(!channelStarter.connect(peerID, "tracker",null)){
            System.out.println("Tracker is offline");
            return false;
        }
        Message message = new RegisterMessage(this.peerID, address, port, key);
        channelStarter.getHandler().sendMessage(message);
        updateTracker();
        Runnable onlineMessagesThread = new OnlineMessagesThread();
        Peer.getExec().scheduleAtFixedRate(onlineMessagesThread, 30, 60, TimeUnit.SECONDS);
        return true;
    }

    public void download(String torrentPath, String filePath) throws IOException{
        
        FileManager manager = new FileManager(torrentPath);      
        SimpleEntry<String,TorrentInfo> torrentInfo = manager.parseDownloadFile();
        
        String namePath = filePath + "/" + torrentInfo.getValue().getName();
   
        torrentInfo.getValue().setFilePath(namePath);
        storage.getFilesDownloaded().put(torrentInfo.getKey(), torrentInfo.getValue());
        Message message = new GetFileMessage(peerID, torrentInfo.getKey());
        sendMessageToTracker(message);
        
    }

    public static void manageFileDownload(String fileId){
        
        
        long chunkLength = storage.getFilesDownloaded().get(fileId).getChunkLength();
        long fileLength = storage.getFilesDownloaded().get(fileId).getFileLength();

        /*int totalChunks = (int)Math.ceil(fileLength/chunkLength);
        if(totalChunks == 0)
            totalChunks = 1;*/

        long totalChunks = (fileLength + chunkLength - 1)/chunkLength;
        long totalPeers = storage.getFilePeers().get(fileId).size();

        int threadsLimit = 10;
        int size = (int) Math.min(totalChunks, totalPeers);
        int limit = Math.min(size, threadsLimit);

        for(int i = 0; i < limit; i++){

            //storage.getFilesDownloaded().get(fileId).updateSendedGetChunkMessages(i, true);
            //System.out.println("PEER: " + i);
            PeerInfo peerInfo= storage.getFilePeers().get(fileId).get(i);
            storage.getFilesDownloaded().get(fileId).updateSentGetChunk(i, true);
            Message message = new GetChunkMessage(fileId,i);
            try {
                if(!sendMessageToPeer(peerInfo.getAddress(), peerInfo.getPort(), peerInfo.getPublicKey(), message)){
                    storage.getFilesDownloaded().get(fileId).updateSentGetChunk(i, false);
                    peerInfo.setAvailable(false);
                }                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Runnable reconnect = new Reconnect();
        Peer.getExec().scheduleAtFixedRate(reconnect, 10, 5, TimeUnit.SECONDS);

        


    }

    public void seed(String filePath, String torrentPath) throws IOException{
        FileManager manager = new FileManager(filePath);
        InetAddress address = InetAddress.getByName(this.trackerIP);
        SimpleEntry<String,TorrentInfo> torrentInfo = manager.createDownloadFile(30000, this.trackerPort, address.getHostAddress(),torrentPath);
        storage.getFilesSeeded().put(torrentInfo.getKey(), torrentInfo.getValue());
        Message message = new HasFileMessage(peerID, torrentInfo.getKey());
        sendMessageToTracker(message);
    }

    public static boolean sendMessageToPeer(String address, int port, byte[] key, Message message) throws UnknownHostException {
        SenderSocket channelStarter = new SenderSocket(port, address);
        if(!channelStarter.connect(peerID, "peer" ,key)){
            System.out.println("The peer you trying to communicate is offline");
            return false;
        }
            
        if(!channelStarter.getHandler().sendMessage(message)) {
            return false;
        }

        return true;
    }

    public static boolean sendMessageToTracker(Message message) throws UnknownHostException {
        SenderSocket channelStarter = new SenderSocket(trackerPort, trackerIP);
        if(!channelStarter.connect(peerID, "tracker",null)){
            System.out.println("Tracker is offline");
            return false;
        } 
        channelStarter.getHandler().sendMessage(message);
        return true;
    }


    public static synchronized TorrentInfo getStorageFileDownloaded(String fileId){
        TorrentInfo torrentInfo = storage.getDownloadedFile(fileId);

        return torrentInfo;
    }

    public static synchronized ArrayList <PeerInfo> getPeerInfosByIpPort(String address, int port){
        return storage.getPeerInfosByIpPort(address, port);
    }
}