import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileInfo {
    private String path;
    private int id;
    private List<Chunk> chunks;
    private int replicationDegree;

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

        int sizeOfChunks = 65000;// 1MB
        byte[] buffer = new byte[sizeOfChunks];
        File f = new File(path);

        //String fileName = f.getName();

        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //bis.write(buffer, 0, bytesAmount);
                chunkNr++;
                Chunk chunk = new Chunk(chunkNr, buffer);
                chunks.add(chunk);


                /*String filePartName = String.format("%s-%03d.jpg", fileName, chunkNr++);
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }*/
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateId(){

    }

    public void setReplicationDegree(int replicationDegree){
        this.replicationDegree = replicationDegree;
    }

}
