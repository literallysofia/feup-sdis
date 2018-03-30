import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    /*
     * Array that contains all files and data
     */

    private ArrayList<FileData> files;

    /*
     * Array that contains all chunks stored in a peer
     */
    private ArrayList<Chunk> chunks;

    /*
     * key = <fileID>_<ChunkNo>
     * value = number of times the chunk is stored
     */
    private ConcurrentHashMap<String, Integer> storedOccurrences;

    public Storage() {
        this.files = new ArrayList<>();
        this.chunks = new ArrayList<>();
        this.storedOccurrences = new ConcurrentHashMap<>();
    }

    public ArrayList<FileData> getFiles() {
        return this.files;
    }

    public ArrayList<Chunk> getChunks() {
        return this.chunks;
    }

    public ConcurrentHashMap<String, Integer> getStoredOccurrences() {
        return this.storedOccurrences;
    }

    public void addFile(FileData f) {
        this.files.add(f);
    }

    public boolean addChunk(Chunk chunk) {

        for (int i = 0; i < this.chunks.size(); i++) {
            if (this.chunks.get(i).getFileID().equals(chunk.getFileID()) && this.chunks.get(i).getNr() == chunk.getNr())
                return false;
        }
        this.chunks.add(chunk);
        return true;
    }

    public void incStoredChunk(String fileID, int chuckNr) {
        String key = fileID + '_' + chuckNr;
        int total = this.storedOccurrences.get(key) + 1;
        this.storedOccurrences.replace(key, total);

    }

    public void deleteChunks(String fileID, int senderId) {
        for (int i = 0; i < this.chunks.size(); i++) {
            if (this.chunks.get(i).getFileID().equals(fileID)) {
                String filename = Peer.getId() + "/" + senderId + "_" + fileID + "_" + this.chunks.get(i).getNr();
                File file = new File(filename);
                file.delete();
                this.chunks.remove(i);
            }

        }

    }

    public void fillCurrRDChunks(){
        for (int i = 0; i < this.chunks.size(); i++) {
            String key = this.chunks.get(i).getFileID()+"_"+this.chunks.get(i).getNr();
            this.chunks.get(i).setCurrReplicationDegree(this.storedOccurrences.get(key));
        }
    }
}