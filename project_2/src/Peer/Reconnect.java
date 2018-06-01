package Peer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Messages.*;
import Peer.*;



/**
 * Reconnect
 */
public class Reconnect implements Runnable{

    public Reconnect(){}

    @Override
    public synchronized void run(){
        ConcurrentHashMap<String,ArrayList<PeerInfo>> peers =  Peer.getStorage().getFilePeers();
        for(String key: peers.keySet()){
            boolean hasPeer=false;
            ArrayList<PeerInfo> peersList = peers.get(key);
            for(PeerInfo info : peersList){
                if(info.isAvailable()) {
                    hasPeer = true;
                    break;
                }
            }
            if(!hasPeer) {
                Message message = new GetFileMessage(Peer.getPeerID(), key);
                try {
					Peer.sendMessageToTracker(message);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
            }
        }


        /*for(String key: peers.keySet()){
            ArrayList<PeerInfo> peersList = peers.get(key);
            for(PeerInfo info : peersList){
                if(!info.isAvailable()){
                    TorrentInfo torrent = Peer.getStorage().getFilesDownloaded().get(key);
                    int chunknr;
                    if(((chunknr = torrent.getNextChunkToSend()) != -1) || ((chunknr = torrent.toResend()) != -1)){
                        GetChunkMessage message = new GetChunkMessage(key,chunknr);
                        try{
                            if(Peer.sendMessageToPeer(info.getAddress(), info.getPort(), info.getPublicKey(), message)){
                                info.setAvailable(true);
                            }else{
                                info.setAvailable(false);
                            }
                        }catch(UnknownHostException e){
                            e.printStackTrace();
                        }
                        
                    }

                }
            }
        }*/
        }
    
}