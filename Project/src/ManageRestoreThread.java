import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageRestoreThread implements Runnable {

    private String fileName;

    public ManageRestoreThread(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void run() {

        if (!Peer.getStorage().getWantedChunks().containsValue("false")) {
            if (restoreFile())
                System.out.println("> File restored!\n");
            else System.out.println("ERROR: File not restored.\n");
        } else System.out.println("ERROR: File not restored, chunks missing.\n");


    }

    private boolean restoreFile() {
        String filePath = Peer.getId() + "/" + this.fileName;
        File file = new File(filePath);
        byte[] body = null;

        try {
            FileOutputStream fos = new FileOutputStream(file, true);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            List<String> sortedChunkKeys = new ArrayList<>(Peer.getStorage().getWantedChunks().keySet());
            Collections.sort(sortedChunkKeys);

            for (String key : sortedChunkKeys) {
                String chunkPath = Peer.getId() + "/" + key;

                File chunkFile = new File(chunkPath);

                if (!chunkFile.exists()) {
                    return false;
                }

                body = new byte[(int) chunkFile.length()];
                FileInputStream in = new FileInputStream(chunkFile);

                in.read(body);
                fos.write(body);

                chunkFile.delete();
            }

            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
