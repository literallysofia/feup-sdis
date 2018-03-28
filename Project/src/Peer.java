import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Peer implements RMIRemote {

    private int id;
    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ExecutorService exec;

    public Peer() {
        exec = Executors.newFixedThreadPool(50);
        MC = new ChannelControl();
        MDB = new ChannelBackup();
        MDR = new ChannelRestore();
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

    public static void main(String args[]){

        try {
            Peer obj = new Peer();
            obj.id = Integer.parseInt(args[0]);
            RMIRemote stub = (RMIRemote)UnicastRemoteObject.exportObject(obj, 0);

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


    public void backup(String filepath, int replicationDegree) throws RemoteException{

        FileInfo file = new FileInfo(filepath);

        for(int i = 0; i < file.getChunks().size(); i++){
            Chunk chunk = file.getChunks().get(i);
            chunk.setDesiredReplicationDegree(replicationDegree);

            String header = "PUTCHUNK " +  "1.0" + " " + this.id + " " + file.getId() + " " + chunk.getNr() + " " + chunk.getDesiredReplicationDegree() + "\r\n\r\n";

            try {
                byte[] asciiHeader = header.getBytes("US-ASCII");
                byte[] body = chunk.getContent();
                byte[] message = new byte[asciiHeader.length + body.length];
                System.arraycopy(asciiHeader,0,message,0,asciiHeader.length);
                System.arraycopy(body,0,message,asciiHeader.length,body.length);

                SendMessageThread sendThread = new SendMessageThread(message, "MDB");
                exec.execute(sendThread);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void restore(String filepath) throws RemoteException{

    }

    public void delete(String filepath) throws RemoteException{

    }

    public void reclaim(int diskSpaceToReclaim) throws RemoteException{

    }

    public void state() throws RemoteException {

    }
}