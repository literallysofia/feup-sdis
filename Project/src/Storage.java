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

    /*
     * key = <fileID>_<ChunkNo>
     * content = chunk's content (null if not received)
     */
    private ConcurrentHashMap<String, Object> wantedChunks;

    public Storage() {
        this.files = new ArrayList<>();
        this.storedChunks = new ArrayList<>();
        this.receivedChunks = new ArrayList<>();
        this.storedOccurrences = new ConcurrentHashMap<>();
        this.wantedChunks = new ConcurrentHashMap<>();
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

    public ConcurrentHashMap<String, Object> getWantedChunks() {
        return this.wantedChunks;
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

    public void deleteReceivedChunk(Chunk chunk) {
        for (int i = 0; i < this.receivedChunks.size(); i++) {
            if (this.receivedChunks.get(i).getFileID().equals(chunk.getFileID()) && this.storedChunks.get(i).getNr() == chunk.getNr())
                this.receivedChunks.remove(i);
        }
    }

    public void incStoredChunk(String fileID, int chunkNr) {
        String key = fileID + '_' + chunkNr;
        int total = this.storedOccurrences.get(key) + 1;
        this.storedOccurrences.replace(key, total);
    }

    public void addWantedChunk(String fileID, int chunkNr) {
        String key = fileID + '_' + chunkNr;
        this.wantedChunks.put(key, "no content");
    }

    public void addWantedChunkContent(String fileID, int chunkNr, byte[] content) {
        String key = fileID + '_' + chunkNr;
        this.wantedChunks.replace(key, content);
    }

    public void deleteStoredChunks(String fileID, int senderId) {
        for (int i = 0; i < this.storedChunks.size(); i++) {
            if (this.storedChunks.get(i).getFileID().equals(fileID)) {
                String filename = Peer.getId() + "/" + fileID + "_" + this.storedChunks.get(i).getNr();
                File file = new File(filename);
                file.delete();
                this.storedChunks.remove(i);
            }
        }
    }

    public void fillCurrRDChunks() {
        for (int i = 0; i < this.storedChunks.size(); i++) {
            String key = this.storedChunks.get(i).getFileID() + "_" + this.storedChunks.get(i).getNr();
            this.storedChunks.get(i).setCurrReplicationDegree(this.storedOccurrences.get(key));
        }
    }
}