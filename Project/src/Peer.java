import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Peer implements RMIRemote {

    private int id;
    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ExecutorService exec;
    private static Storage storage;
    private static ArrayList<FileData> files;

    public Peer() {
        exec = Executors.newFixedThreadPool(50);
        MC = new ChannelControl();
        MDB = new ChannelBackup();
        MDR = new ChannelRestore();
        storage = new Storage();
        files = new ArrayList<>();
    }

    public static ExecutorService getExec() {
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

    public static ArrayList<FileData> getFiles() {
        return files;
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

        FileData file = new FileData(filepath);
        files.add(file);

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

                Thread.sleep(500);
                exec.execute(sendThread);
            } catch (UnsupportedEncodingException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("ANSWER: " + storage.getFiles().get(filepath));
    }

    public void restore(String filepath) throws RemoteException {

    }

    public void delete(String filepath) throws RemoteException {

    }

    public void reclaim(int diskSpaceToReclaim) throws RemoteException {

    }

    public void state() throws RemoteException {

    }
}