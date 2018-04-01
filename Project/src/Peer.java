import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements RMIRemote {

    private static int id;
    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ScheduledThreadPoolExecutor exec;
    private static Storage storage;

    public Peer() {
        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(50);
        MC = new ChannelControl();
        MDB = new ChannelBackup();
        MDR = new ChannelRestore();
    }

    public static int getId() {
        return id;
    }

    public static ScheduledThreadPoolExecutor getExec() {
        return exec;
    }

    public static ChannelControl getMC() {
        return MC;
    }

    public static ChannelBackup getMDB() {
        return MDB;
    }

    public static ChannelRestore getMDR() {
        return MDR;
    }

    public static Storage getStorage() {
        return storage;
    }

    public static void main(String args[]) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            Peer obj = new Peer();
            obj.id = Integer.parseInt(args[0]);
            RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);

            System.err.println("Peer ready");
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }

        deserializeStorage();

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> serializeStorage()));
    }


    public synchronized void backup(String filepath, int replicationDegree) {

        FileData file = new FileData(filepath, replicationDegree);
        storage.addFile(file);

        for (int i = 0; i < file.getChunks().size(); i++) {
            Chunk chunk = file.getChunks().get(i);
            chunk.setDesiredReplicationDegree(replicationDegree);

            String header = "PUTCHUNK " + "1.0" + " " + this.id + " " + file.getId() + " " + chunk.getNr() + " " + chunk.getDesiredReplicationDegree() + "\r\n\r\n";
            System.out.println("Sent PUTCHUNK chunk size: " + chunk.getSize());

            String key = file.getId() + "_" + chunk.getNr();
            if (!storage.getStoredOccurrences().containsKey(key)) {
                Peer.getStorage().getStoredOccurrences().put(key, 0);
            }

            try {
                byte[] asciiHeader = header.getBytes("US-ASCII");
                byte[] body = chunk.getContent();
                byte[] message = new byte[asciiHeader.length + body.length];
                System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                SendMessageThread sendThread = new SendMessageThread(message, "MDB");
                exec.execute(sendThread);

                Peer.getExec().schedule(new PutChunkManager(message, 1, file.getId(), chunk.getNr(), replicationDegree), 1, TimeUnit.SECONDS);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void restore(String filepath) {
        String fileName = null;

        for (int i = 0; i < storage.getFiles().size(); i++) {
            if (storage.getFiles().get(i).getFile().getPath().equals(filepath)) { //if file exists
                for (int j = 0; j < storage.getFiles().get(i).getChunks().size(); j++) { //if file has backedup chunks

                    String header = "GETCHUNK " + "1.0" + " " + this.id + " " + storage.getFiles().get(i).getId() + " " + storage.getFiles().get(i).getChunks().get(j).getNr() + "\r\n\r\n";
                    System.out.println("Sent GETCHUNK");

                    storage.addWantedChunk(storage.getFiles().get(i).getId(), storage.getFiles().get(i).getChunks().get(j).getNr());
                    fileName = storage.getFiles().get(i).getFile().getName();

                    try {
                        SendMessageThread sendThread = new SendMessageThread(header.getBytes("US-ASCII"), "MC");

                        exec.execute(sendThread);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Peer.getExec().schedule(new ManageRestoreThread(fileName), 10, TimeUnit.SECONDS);
            } else System.out.println("ERROR: File was never backed up.");
        }
    }

    public void delete(String filepath) {

        for (int i = 0; i < storage.getFiles().size(); i++) {
            if (storage.getFiles().get(i).getFile().getPath().equals(filepath)) {

                for (int j = 0; j < 5; j++) {
                    String header = "DELETE " + "1.0" + " " + this.id + " " + storage.getFiles().get(i).getId() + "\r\n\r\n";
                    System.out.println("Send DELETE " + "1.0" + " " + this.id + " " + storage.getFiles().get(i).getId());
                    try {
                        SendMessageThread sendThread = new SendMessageThread(header.getBytes("US-ASCII"), "MC");
                        exec.execute(sendThread);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                for (int j = 0; j < this.storage.getFiles().get(i).getChunks().size(); j++) {
                    this.storage.removeStoredOccurrencesEntry(this.storage.getFiles().get(i).getId(), this.storage.getFiles().get(i).getChunks().get(j).getNr());
                }

                storage.getFiles().remove(i);

                break;
            }
        }

        return;
    }

    public void reclaim(int newSpaceAvailable) {

        storage.setSpaceAvailable(newSpaceAvailable);

        int spaceToFree = storage.getOccupiedSpace() - newSpaceAvailable;

        if (spaceToFree > 0) {
            storage.fillCurrRDChunks();
            storage.getStoredChunks().sort(Collections.reverseOrder());

            ArrayList<Integer> indexsToDelete = new ArrayList<>();

            int toDelete = 0;
            for (int i = 0; i < storage.getStoredChunks().size(); i++) {
                if (toDelete < spaceToFree) {
                    toDelete = toDelete + storage.getStoredChunks().get(i).getSize();
                    indexsToDelete.add(i);
                } else {
                    break;
                }
            }

            for (int j = 0; j < indexsToDelete.size(); j++) {
                Chunk chunk = storage.getStoredChunks().get(indexsToDelete.get(j));

                String header = "REMOVED " + "1.0" + " " + id + " " + chunk.getFileID() + " " + chunk.getNr() + "\r\n\r\n";
                System.out.println("Sent REMOVED " + chunk.getFileID() + " " + chunk.getNr() + " size: " + chunk.getSize() + " RD: " + chunk.getCurrReplicationDegree());
                try {
                    byte[] asciiHeader = header.getBytes("US-ASCII");
                    SendMessageThread sendThread = new SendMessageThread(asciiHeader, "MC");
                    exec.execute(sendThread);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String filename = Peer.getId() + "/" + chunk.getFileID() + "_" + chunk.getNr();
                File file = new File(filename);
                file.delete();
                Peer.getStorage().decStoredChunk(chunk.getFileID(), chunk.getNr());
                storage.removeStoredChunk(chunk);

            }
        }

    }

    public void state() {
        //Each file whose backup it has initiated
        System.out.println("\n> For each file whose backup it has initiated!");
        for (int i = 0; i < storage.getFiles().size(); i++) {
            String fileID = storage.getFiles().get(i).getId();

            System.out.println("FILE PATHNAME: " + storage.getFiles().get(i).getFile().getPath());
            System.out.println("FILE ID: " + fileID);
            System.out.println("FILE REPLICATION DEGREE: " + storage.getFiles().get(i).getReplicationDegree() + "\n");

            for (int j = 0; j < storage.getFiles().get(i).getChunks().size(); j++) {
                int chunkNr = storage.getFiles().get(i).getChunks().get(j).getNr();
                String key = fileID + '_' + chunkNr;

                System.out.println("CHUNK ID: " + chunkNr);
                System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + storage.getStoredOccurrences().get(key) + "\n");
            }
        }

        //Each chunk it stores
        System.out.println("\n> For each chunk it stores!");
        for (int i = 0; i < storage.getStoredChunks().size(); i++) {
            int chunkNr = storage.getStoredChunks().get(i).getNr();
            String key = storage.getStoredChunks().get(i).getFileID() + '_' + chunkNr;
            System.out.println("CHUNK ID: " + chunkNr);
            System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + storage.getStoredOccurrences().get(key) + "\n");
        }
    }

    public static void serializeStorage() {
        try {
            String filename = Peer.getId() + "/storage.ser";

            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(storage);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void deserializeStorage() {
        try {
            String filename = Peer.getId() + "/storage.ser";

            File file = new File(filename);
            if (!file.exists()) {
                storage = new Storage();
                return;
            }

            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            storage = (Storage) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Storage class not found");
            c.printStackTrace();
            return;
        }
    }

}