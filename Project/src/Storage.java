import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

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

    public Storage() {
        this.files = new ArrayList<>();
        this.storedChunks = new ArrayList<>();
        this.storedOccurrences = new ConcurrentHashMap<>();
    }

    public ArrayList<FileData> getFiles() {
        return this.files;
    }

    public ArrayList<Chunk> getStoredChunks() {
        return this.storedChunks;
    }

    public ArrayList<Chunk> getReceivedChunks() {
        return this.receivedChunks;
    }

    public ConcurrentHashMap<String, Integer> getStoredOccurrences() {
        return this.storedOccurrences;
    }

    public void addFile(FileData f) {
        this.files.add(f);
    }

    public boolean addStoredChunk(Chunk chunk) {

        for (int i = 0; i < this.storedChunks.size(); i++) {
            if (this.storedChunks.get(i).getFileID().equals(chunk.getFileID()) && this.storedChunks.get(i).getNr() == chunk.getNr())
                return false;
        }
        this.storedChunks.add(chunk);
        return true;
    }

    public void addReceivedChunk(Chunk chunk) {
        this.receivedChunks.add(chunk);
    }

    public void deleteReceivedChunk(Chunk chunk) {
        for (int i = 0; i < this.receivedChunks.size(); i++) {
            if (this.receivedChunks.get(i).getFileID().equals(chunk.getFileID()) && this.storedChunks.get(i).getNr() == chunk.getNr())
                this.receivedChunks.remove(i);
        }
    }

    public void incStoredChunk(String fileID, int chunkNr) {
        String key = fileID + '_' + chunkNr;

        /*if (this.storedOccurrences.putIfAbsent(key, 1) != null) {
            int total = this.storedOccurrences.get(key);
            this.storedOccurrences.replace(key, total++);
        }*/

        //if(this.storedOccurrences.containsKey(key)) {
        int total = this.storedOccurrences.get(key) + 1;
        this.storedOccurrences.replace(key, total);
        //}
        //else{
        //  this.storedOccurrences.put(key, 1);
        //}
    }

    public void deleteStoredChunks(String fileID, int senderId) {
        for (int i = 0; i < this.storedChunks.size(); i++) {
            if (this.storedChunks.get(i).getFileID().equals(fileID)) {
                String filename = Peer.getId() + "/" + senderId + "_" + fileID + "_" + this.storedChunks.get(i).getNr();
                File file = new File(filename);
                file.delete();
                this.storedChunks.remove(i);
            }
        }
    }

}