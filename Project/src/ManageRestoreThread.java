import java.io.*;
import java.util.*;

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

        Peer.getStorage().getWantedChunks().clear();
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
            /*Collections.sort(sortedChunkKeys);

            List<String> test = new ArrayList<>(Peer.getStorage().getWantedChunks().keySet());*/
            Collections.sort(sortedChunkKeys, (o1, o2) -> {
                int chunkNr1 = Integer.valueOf(o1.split("_")[1]);
                int chunkNr2 = Integer.valueOf(o2.split("_")[1]);
                return Integer.compare(chunkNr1, chunkNr2);
            });

            for (String key : sortedChunkKeys) {
                String chunkPath = Peer.getId() + "/" + key;

                System.out.println("MERGE: " + key);

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
