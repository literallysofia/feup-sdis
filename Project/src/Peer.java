import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.UnknownHostException;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Peer implements RMIRemote {

    private int id;
    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ExecutorService exec;

    public Peer() {
        exec = Executors.newFixedThreadPool(5);
        try{
            MC = new ChannelControl();
            MDB = new ChannelBackup();
            MDR = new ChannelRestore();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws UnknownHostException{

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
        try{
            byte[] chunks = {(byte)100, (byte)200,(byte)300,(byte)400};

            for(int i = 0; i < chunks.length; i++){
                String message = "PUTCHUNK " + "1.0 " + " 3 " + this.id + " " + i + " " + chunks[i];
                MDB.sendMessage(message);
            }
        }catch (UnknownHostException | InterruptedException e){
            e.printStackTrace();
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

    public void manageReceivedControlMessages(String msg){

    }

    public void manageReceivedBackupMessages(String msg){

    }

    public void manageReceivedRestoreMessages(String msg){

    }

}