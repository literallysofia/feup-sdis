import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    private ConcurrentHashMap<String, String> files;
    private ArrayList<Chunk> chunks;

    public Storage() {
        this.files = new ConcurrentHashMap<String, String>();
        this.chunks = new ArrayList<Chunk>();
    }

    public ConcurrentHashMap getFiles() {
        return this.files;
    }

    public ArrayList<Chunk> getChunks() {
        return this.chunks;
    }

    public void addFile(String filePath, String fileID) {
        this.files.put(filePath, fileID);
    }

    public void deleteFile(String filePath) {
        this.files.remove(filePath);
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public void deleteChunk(Chunk chunk) {
        for (int i = 0; i < this.chunks.size(); i++) {
            if (chunk.getNr() == this.chunks.get(i).getNr()
                    && Arrays.equals(chunk.getContent(), this.chunks.get(i).getContent()))
                this.chunks.remove(i);
        }
    }

}