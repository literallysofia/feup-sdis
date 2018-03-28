import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    /*
     * key = filePath
     * value = fileID
     */
    private ConcurrentHashMap<String, String> files;

    /*
     * key = <fileID>_<ChunkNo>
     * value = number of times the chunk is stored
     */
    private ConcurrentHashMap<String, Integer> chunks;

    public Storage() {
        this.files = new ConcurrentHashMap<>();
        this.chunks = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap getFiles() {
        return this.files;
    }

    public ConcurrentHashMap<String, Integer> getChunks() {
        return this.chunks;
    }

    public void addFile(String filePath, String fileID) {
        this.files.put(filePath, fileID);
    }

    public void deleteFile(String filePath) {
        this.files.remove(filePath);
    }

    public void addChunk(String fileID, int chuckNr) {
        String key = fileID + '_' + chuckNr;

        if (this.chunks.putIfAbsent(key, 1) == null) {
            int total = this.chunks.get(key);
            this.chunks.replace(key, total++);
        }
    }

    public void deleteChunk(String fileID, int chuckNr) {
        String key = fileID + '_' + chuckNr;
        this.files.remove(key);
    }

}