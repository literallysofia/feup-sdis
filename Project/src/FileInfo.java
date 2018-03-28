import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class FileInfo {
    private String id;
    private String path; //delete
    private File file;
    private List<Chunk> chunks;

    public FileInfo(String path) {
        this.path = path; //delete
        this.file = new File(path);
        this.chunks = new ArrayList<Chunk>();
        split();
        generateId();
        Peer.getStorage().addFile(this.file.getPath(), this.id);
    }

    public String getId() {
        return this.id;
    }

    public File getFile() {
        return this.file;
    }

    public List<Chunk> getChunks() {
        return this.chunks;
    }

    public void split() {
        int chunkNr = 0;

        int sizeOfChunks = 64000;// 1MB
        byte[] buffer = new byte[sizeOfChunks];
        File f = new File(path);

        String filename = f.getName();

        try (FileInputStream fis = new FileInputStream(f); BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //bis.write(buffer, 0, bytesAmount);

                chunkNr++;
                Chunk chunk = new Chunk(chunkNr, buffer);
                this.chunks.add(chunk);
                buffer = new byte[sizeOfChunks];

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

    public void generateId() {
        String fileName = this.file.getName();
        String dateModified = String.valueOf(this.file.lastModified());
        String owner = this.file.getParent();

        String fileID = fileName + '-' + dateModified + '-' + owner;

        this.id = sha256(fileID);
    }

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
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
