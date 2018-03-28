import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    /*
     * Array that contains all files and data
     */

    private ArrayList<FileData> files;

    /*
     * Array that contains all chunks
     */
    private ArrayList<Chunk> chunks;

    /*
     * key = <fileID>_<ChunkNo>
     * value = number of times the chunk is stored
     */
    private ConcurrentHashMap<String, Integer> storedOccurrences;

    public Storage() {
        this.files = new ArrayList<>();
        this.chunks =  new ArrayList<>();
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

    public void incStoredChunk(String fileID, int chuckNr) {
        String key = fileID + '_' + chuckNr;

        if (this.storedOccurrences.putIfAbsent(key, 1) == null) {
            int total = this.storedOccurrences.get(key);
            this.storedOccurrences.replace(key, total++);
        }
    }

}