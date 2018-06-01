
package Peer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerInfo implements java.io.Serializable{
    
    private String address;
    private int port;
    private long lastTimeOnline; //in milliseconds
    private byte[] publicKey;
    private boolean available = true;

    public PeerInfo(String address, int port, long lastTimeOnline,byte[] key) {
        this.address=address;
        this.port = port;
        this.lastTimeOnline = lastTimeOnline;
        this.publicKey = key;
        this.available = true;
    }

    public synchronized String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address=address;
    }

    public synchronized int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port=port;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
    
    public long getLastTimeOnline() {
        return this.lastTimeOnline;
    }

    public void setLastTimeOnline(long lastTimeOnline) {
        this.lastTimeOnline=lastTimeOnline;
    }

    public synchronized boolean isAvailable(){
        return this.available;
    }

    public synchronized void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object object)
    {

        if (object != null && object instanceof PeerInfo)
        {
            PeerInfo info = ((PeerInfo) object);
            return this.address.equals(getAddress()) && this.port == info.getPort();
        }

        return false;
    }


}