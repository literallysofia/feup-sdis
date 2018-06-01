package Peer;

import Peer.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements java.io.Serializable{


    private volatile ConcurrentHashMap<String, TorrentInfo> filesSeeded; //<fileid, torrentInfo>
    private volatile ConcurrentHashMap<String, TorrentInfo> filesDownloaded; //<fileid, torrentInfo>
    private volatile ConcurrentHashMap<String, ArrayList<PeerInfo>> filePeers; //<fileid, peers>
    
    

    /**
     * Default construtor of the database
     */
    public Storage(){
        this.filesSeeded = new ConcurrentHashMap<>();
        this.filesDownloaded = new ConcurrentHashMap<>();
        this.filePeers = new ConcurrentHashMap<>();
    
    }

    /**
     * @return the filesSeeded
     */
    public  synchronized ConcurrentHashMap<String, TorrentInfo> getFilesSeeded() {
        return filesSeeded;
    }
    /**
     * @param filesSeeded the filesSeeded to set
     */
    public synchronized void setFilesSeeded(ConcurrentHashMap<String, TorrentInfo> filesSeeded) {
        this.filesSeeded = filesSeeded;
    }

    /**
     * @return the filesDownloaded
     */
    public synchronized ConcurrentHashMap<String, TorrentInfo> getFilesDownloaded() {
        return filesDownloaded;
    }

    /**
     * @param filesDownloaded the filesDownloaded to set
     */
    public void setFilesDownloaded(ConcurrentHashMap<String, TorrentInfo> filesDownloaded) {
        this.filesDownloaded = filesDownloaded;
    }

    /**
     * @return the filePeers
     */
    public synchronized ConcurrentHashMap<String,  ArrayList<PeerInfo>> getFilePeers() {
        return filePeers;
    }

    /**
     * @param filePeers the filePeers to set
     */
    public void setFilePeers(ConcurrentHashMap<String,  ArrayList<PeerInfo>> filePeers) {
        this.filePeers = filePeers;
    }


    public synchronized TorrentInfo getDownloadedFile(String fileId){
        return this.filesDownloaded.get(fileId);
    }

    public synchronized ArrayList<PeerInfo> getPeerInfosByIpPort(String address, int port){

        ArrayList<PeerInfo> peers = new ArrayList<>();

        for (String key : filePeers.keySet()) {
            ArrayList<PeerInfo> peerList = filePeers.get(key);
            
            for(int i = 0; i < peerList.size(); i++){
                if(peerList.get(i).getAddress().equals(address) && peerList.get(i).getPort()==port){
                    peers.add(peerList.get(i));
                }
            }
        }

        return peers;

    }
    

    /**
     * Write status variables to serializable
     * @param stream
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {

        stream.writeObject(filesDownloaded);
        stream.writeObject(filesSeeded);
        stream.writeObject(filePeers);        
        stream.writeObject(Peer.getPeerID());


    }

    /**
     * Read status variables to serializable
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {

        filesDownloaded = (ConcurrentHashMap<String, TorrentInfo>)stream.readObject();
        filesSeeded = (ConcurrentHashMap<String, TorrentInfo>)stream.readObject();
        filePeers = (ConcurrentHashMap<String, ArrayList<PeerInfo>>)stream.readObject();        
        Peer.setPeerID((String)stream.readObject());
    }


}
