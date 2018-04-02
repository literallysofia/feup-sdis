import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

public class FileData implements java.io.Serializable{

    private String id;
    private File file;
    private int replicationDegree;
    private ArrayList<Chunk> chunks;

    public FileData(String path, int replicationDegree) {
        this.file = new File(path);
        this.replicationDegree = replicationDegree;
        this.chunks = new ArrayList<>();
        splitFile();
        generateId();
    }

    public String getId() {
        return this.id;
    }

    public File getFile() {
        return this.file;
    }

    public int getReplicationDegree() {
        return this.replicationDegree;
    }

    public ArrayList<Chunk> getChunks() {
        return this.chunks;
    }

    private void splitFile() {
        int chunkNr = 0;

        int sizeOfChunks = 64000;
        byte[] buffer = new byte[sizeOfChunks];

        try (FileInputStream fis = new FileInputStream(this.file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                byte[] body = Arrays.copyOf(buffer, bytesAmount);

                chunkNr++;
                Chunk chunk = new Chunk(chunkNr, body, bytesAmount);
                this.chunks.add(chunk);
                buffer = new byte[sizeOfChunks];
            }

            if (this.file.length() % 64000 == 0) {
                Chunk chunk = new Chunk(chunkNr, null, 0);
                this.chunks.add(chunk);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateId() {
        String fileName = this.file.getName();
        String dateModified = String.valueOf(this.file.lastModified());
        String owner = this.file.getParent();

        String fileID = fileName + '-' + dateModified + '-' + owner;

        this.id = sha256(fileID);
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
