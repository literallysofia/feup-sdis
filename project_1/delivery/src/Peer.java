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
    private static double version;

    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ScheduledThreadPoolExecutor exec;
    private static Storage storage;

    private Peer(String MCAddress, int MCPort, String MDBAddress, int MDBPort, String MDRAddress, int MDRPort) {
        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);
        MC = new ChannelControl(MCAddress, MCPort);
        MDB = new ChannelBackup(MDBAddress, MDBPort);
        MDR = new ChannelRestore(MDRAddress, MDRPort);
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

            if(args.length != 9){
                System.out.println("ERROR: Peer format must be: Peer <version> <server id> <access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>");
                return;
            }

            version = Double.parseDouble(args[0]);
            id = Integer.parseInt(args[1]);
            String accessP = args[2];
            String MCAddress = args[3];
            int MCPort = Integer.parseInt(args[4]);
            String MDBAddress = args[5];
            int MDBPort = Integer.parseInt(args[6]);
            String MDRAddress = args[7];
            int MDRPort = Integer.parseInt(args[8]);

            Peer obj = new Peer(MCAddress,MCPort, MDBAddress,MDBPort, MDRAddress, MDRPort);
            RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(accessP, stub);

            System.err.println("Peer ready");
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }

        deserializeStorage(); //loads storage

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);

        Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage)); //if CTRL-C is pressed when a Peer is running, it saves his storage so it can be loaded next time it runs
    }


    public synchronized void backup(String filepath, int replicationDegree) {

        FileData file = new FileData(filepath, replicationDegree);
        storage.addFile(file); //adds file to my storage so the peer know what files he asked for backup

        for (int i = 0; i < file.getChunks().size(); i++) { //for every chunk of that file
            Chunk chunk = file.getChunks().get(i);
            chunk.setDesiredReplicationDegree(replicationDegree);

            String header = "PUTCHUNK " + version + " " + id + " " + file.getId() + " " + chunk.getNr() + " " + chunk.getDesiredReplicationDegree() + "\r\n\r\n";
            System.out.println("Sent " + "PUTCHUNK " + version + " " + id + " " + file.getId() + " " + chunk.getNr() + " " + chunk.getDesiredReplicationDegree());

            String key = file.getId() + "_" + chunk.getNr();
            if (!storage.getStoredOccurrences().containsKey(key)) {
                Peer.getStorage().getStoredOccurrences().put(key, 0); //ads the chunk to the stored occurrences table with value 0
            }

            try {
                byte[] asciiHeader = header.getBytes("US-ASCII");
                byte[] body = chunk.getContent();
                byte[] message = new byte[asciiHeader.length + body.length];
                System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                SendMessageThread sendThread = new SendMessageThread(message, "MDB");
                exec.execute(sendThread);
                Thread.sleep(500);
                Peer.getExec().schedule(new ManagePutChunkThread(message, 1, file.getId(), chunk.getNr(), replicationDegree), 1, TimeUnit.SECONDS);

            } catch (UnsupportedEncodingException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void restore(String filepath) {
        String fileName = null;

        for (int i = 0; i < storage.getFiles().size(); i++) {
            if (storage.getFiles().get(i).getFile().getPath().equals(filepath)) {
                for (int j = 0; j < storage.getFiles().get(i).getChunks().size(); j++) {

                    String header = "GETCHUNK " + version + " " + id + " " + storage.getFiles().get(i).getId() + " " + storage.getFiles().get(i).getChunks().get(j).getNr() + "\r\n\r\n";
                    System.out.println("Sent "+ "GETCHUNK " + version + " " + id + " " + storage.getFiles().get(i).getId() + " " + storage.getFiles().get(i).getChunks().get(j).getNr());

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
                    String header = "DELETE " + version + " " + id + " " + storage.getFiles().get(i).getId() + "\r\n\r\n";
                    System.out.println("Send DELETE " + version + " " + id + " " + storage.getFiles().get(i).getId());
                    try {
                        SendMessageThread sendThread = new SendMessageThread(header.getBytes("US-ASCII"), "MC");
                        exec.execute(sendThread);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                for (int j = 0; j < storage.getFiles().get(i).getChunks().size(); j++) {
                    storage.removeStoredOccurrencesEntry(storage.getFiles().get(i).getId(), storage.getFiles().get(i).getChunks().get(j).getNr());
                }

                storage.getFiles().remove(i);

                break;
            }
        }

    }

    public void reclaim(int newSpaceAvailable) {

        System.out.println("RECLAIM: old space - " + storage.getSpaceAvailable());

        int spaceToFree = storage.getOccupiedSpace() - newSpaceAvailable; //calculates the space he has to free eliminating chunks

        if (spaceToFree > 0) { //if it has to remove chunks
            storage.fillCurrRDChunks();
            storage.getStoredChunks().sort(Collections.reverseOrder());

            int deletedChunksSpace = 0; //space occupied by the deleted chunks

            for (Iterator<Chunk> iter = storage.getStoredChunks().iterator(); iter.hasNext(); ) {
                Chunk chunk = iter.next();
                if (deletedChunksSpace < spaceToFree) { //if the space occupied by the deleted chunks isn't enough
                    deletedChunksSpace = deletedChunksSpace + chunk.getSize();

                    String header = "REMOVED " + version + " " + id + " " + chunk.getFileID() + " " + chunk.getNr() + "\r\n\r\n";
                    System.out.println("Sent " + "REMOVED " + version + " " + id + " " + chunk.getFileID() + " " + chunk.getNr());
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
                    Peer.getStorage().decStoredOccurrences(chunk.getFileID(), chunk.getNr()); //decrements the stored occurrences of this chunk
                    iter.remove();
                } else {
                    break;
                }
            }

            storage.setSpaceAvailable(newSpaceAvailable - storage.getOccupiedSpace()); //updates the available space to the new value minus the space occupied by the chunks that weren't deleted
            System.out.println("RECLAIM: new space - " + storage.getSpaceAvailable());
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

    //saves this peer storage in a file called storage.ser
    private static void serializeStorage() {
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

    //loads this peer storage from a file called storage.ser if it exists
    private static void deserializeStorage() {
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
        } catch (ClassNotFoundException c) {
            System.out.println("Storage class not found");
            c.printStackTrace();
        }
    }

}