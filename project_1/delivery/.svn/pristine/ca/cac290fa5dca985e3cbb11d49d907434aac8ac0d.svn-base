import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements java.io.Serializable{

    /*
     * Array that contains all files and data
     */

    private ArrayList<FileData> files;

    /*
     * Array that contains all chunks stored in a peer
     */
    private ArrayList<Chunk> storedChunks;

    /*
     * Array that contains all chunks received in a peer
     */
    private ArrayList<Chunk> receivedChunks;

    /*
     * key = <fileID>_<ChunkNo>
     * value = number of times the chunk is stored
     */
    private ConcurrentHashMap<String, Integer> storedOccurrences;

    /*
     * key = <fileID>_<ChunkNo>
     * hasReceived = if received (true) if not (false)
     */
    private ConcurrentHashMap<String, String> wantedChunks;

    private int spaceAvailable;

    public Storage() {
        this.files = new ArrayList<>();
        this.storedChunks = new ArrayList<>();
        this.receivedChunks = new ArrayList<>();
        this.storedOccurrences = new ConcurrentHashMap<>();
        this.wantedChunks = new ConcurrentHashMap<>();
        this.spaceAvailable = 1000000000;
    }

    public ArrayList<FileData> getFiles() {
        return this.files;
    }

    public synchronized ArrayList<Chunk> getStoredChunks() {
        return this.storedChunks;
    }

    public ArrayList<Chunk> getReceivedChunks() {
        return this.receivedChunks;
    }

    public synchronized ConcurrentHashMap<String, Integer> getStoredOccurrences() {
        return this.storedOccurrences;
    }

    public ConcurrentHashMap<String, String> getWantedChunks() {
        return this.wantedChunks;
    }

    public void addFile(FileData f) {
        this.files.add(f);
    }

    public synchronized boolean addStoredChunk(Chunk chunk) {

        for (Chunk storedChunk : this.storedChunks) {
            if (storedChunk.getFileID().equals(chunk.getFileID()) && storedChunk.getNr() == chunk.getNr())
                return false;
        }
        this.storedChunks.add(chunk);
        return true;
    }

    public void deleteReceivedChunk(Chunk chunk) {
        for (int i = 0; i < this.receivedChunks.size(); i++) {
            if (this.receivedChunks.get(i).getFileID().equals(chunk.getFileID()) && this.storedChunks.get(i).getNr() == chunk.getNr())
                this.receivedChunks.remove(i);
        }
    }

    public synchronized void incStoredChunk(String fileID, int chunkNr) {

        String key = fileID + '_' + chunkNr;

        if (!Peer.getStorage().getStoredOccurrences().containsKey(key)) {
            Peer.getStorage().getStoredOccurrences().put(key, 1);
        } else {
            int total = this.storedOccurrences.get(key) + 1;
            this.storedOccurrences.replace(key, total);
        }

    }

    public synchronized void decStoredChunk(String fileID, int chunkNr) {
        String key = fileID + '_' + chunkNr;
        int total = this.storedOccurrences.get(key) - 1;
        this.storedOccurrences.replace(key, total);
    }

    public synchronized void removeStoredOccurrencesEntry(String fileID, int chunkNr){
        String key = fileID + '_' + chunkNr;
        this.storedOccurrences.remove(key);
    }

    public void addWantedChunk(String fileID, int chunkNr) {
        String key = fileID + '_' + chunkNr;
        this.wantedChunks.put(key, "false");
    }

    public void setWantedChunkReceived(String fileID, int chunkNr) {
        String key = fileID + '_' + chunkNr;
        this.wantedChunks.replace(key, "true");
    }

    public void deleteStoredChunks(String fileID) {
        for (Iterator<Chunk> iter = this.storedChunks.iterator(); iter.hasNext(); ) {
            Chunk chunk = iter.next();
            if (chunk.getFileID().equals(fileID)) {
                String filename = Peer.getId() + "/" + fileID + "_" + chunk.getNr();
                File file = new File(filename);
                file.delete();
                removeStoredOccurrencesEntry(fileID, chunk.getNr());
                incSpaceAvailable(fileID, chunk.getNr());
                iter.remove();
            }
        }
    }

    public void fillCurrRDChunks() {
        for (Chunk storedChunk : this.storedChunks) {
            String key = storedChunk.getFileID() + "_" + storedChunk.getNr();
            storedChunk.setCurrReplicationDegree(this.storedOccurrences.get(key));
        }
    }

    public synchronized int getSpaceAvailable() {
        return this.spaceAvailable;
    }

    public synchronized void setSpaceAvailable(int spaceAvailable) {
        System.out.println("OLD SPACE AVAILABLE: " + this.spaceAvailable);
        this.spaceAvailable = spaceAvailable;
        System.out.println("NEW SPACE AVAILABLE: " + this.spaceAvailable);
    }

    public synchronized void decSpaceAvailable(int chunkSize){
        spaceAvailable = spaceAvailable - chunkSize;
    }

    public synchronized void incSpaceAvailable(String fileId, int chunkNr){

        for (Chunk storedChunk : this.storedChunks) {
            if (storedChunk.getFileID().equals(fileId) && storedChunk.getNr() == chunkNr)
                this.spaceAvailable = this.spaceAvailable + storedChunk.getSize();
        }

    }

    public synchronized int getOccupiedSpace(){
        int occupiedSpace = 0;
        for (Chunk storedChunk : this.storedChunks) {
            occupiedSpace = occupiedSpace + storedChunk.getSize();
        }
        return occupiedSpace;
    }

    public synchronized void removeStoredChunk(Chunk chunk){
        for (int i = 0; i < this.storedChunks.size(); i++) {
            if (this.storedChunks.get(i).getFileID().equals(chunk.getFileID()) && this.storedChunks.get(i).getNr() == chunk.getNr()){
                this.storedChunks.remove(i);
                break;
            }
        }
    }

}