import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
        storage = new Storage();
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

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);
    }


    public void backup(String filepath, int replicationDegree) throws RemoteException {

        FileData file = new FileData(filepath, replicationDegree);
        storage.addFile(file);

        for (int i = 0; i < file.getChunks().size(); i++) {
            Chunk chunk = file.getChunks().get(i);
            chunk.setDesiredReplicationDegree(replicationDegree);

            String header = "PUTCHUNK " + "1.0" + " " + this.id + " " + file.getId() + " " + chunk.getNr() + " " + chunk.getDesiredReplicationDegree() + "\r\n\r\n";

            try {
                byte[] asciiHeader = header.getBytes("US-ASCII");
                byte[] body = chunk.getContent();
                byte[] message = new byte[asciiHeader.length + body.length];
                System.arraycopy(asciiHeader, 0, message, 0, asciiHeader.length);
                System.arraycopy(body, 0, message, asciiHeader.length, body.length);

                SendMessageThread sendThread = new SendMessageThread(message, "MDB");

                exec.execute(sendThread);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void restore(String filepath) throws RemoteException {

    }

    public void delete(String filepath) throws RemoteException {

    }

    public void reclaim(int diskSpaceToReclaim) throws RemoteException {

    }

    public void state() throws RemoteException {
        //Each file whose backup it has initiated
        for (int i = 0; i < storage.getFiles().size(); i++) {
            String fileID = storage.getFiles().get(i).getId();

            System.out.println("FILE PATHNAME: " + storage.getFiles().get(i).getFile().getPath() + "\n");
            System.out.println("FILE ID: " + fileID + "\n");
            System.out.println("FILE REPLICATION DEGREE: " + storage.getFiles().get(i).getReplicationDegree() + "\n");

            for (int j = 0; j < storage.getFiles().get(i).getChunks().size(); j++) {
                int chunkNr = storage.getFiles().get(i).getChunks().get(j).getNr();
                String key = fileID + '_' + chunkNr;

                System.out.println("CHUNK ID: " + chunkNr + "\n");
                System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + storage.getStoredOccurrences().get(key) + "\n");
            }
        }

        //Each chunk it stores
        for (int i = 0; i < storage.getChunks().size(); i++) {
            int chunkNr = storage.getChunks().get(i).getNr();
            String key = storage.getChunks().get(i).getFileID() + '_' + chunkNr;
            System.out.println("CHUNK ID: " + chunkNr + "\n");
            System.out.println("CHUNK PERCEIVED REPLICATION DEGREE: " + storage.getStoredOccurrences().get(key) + "\n");
        }

    }
}