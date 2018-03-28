import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.RandomAccessFile;

public class FileInfo {
    private String path;
    private int id = 100;
    private List<Chunk> chunks;

    public FileInfo(String path){
        this.path = path;
        this.chunks = new ArrayList<Chunk>();
        split();
        generateId();
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public int getId() {
        return id;
    }

    public void split(){
        int chunkNr = 0;

        int sizeOfChunks = 64000;// 1MB
        byte[] buffer = new byte[sizeOfChunks];
        File f = new File(path);

        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                byte[] body = Arrays.copyOf(buffer, bytesAmount);

                chunkNr++;
                Chunk chunk = new Chunk(chunkNr, body);
                chunks.add(chunk);
                buffer = new byte[sizeOfChunks];
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateId(){

    }
}
