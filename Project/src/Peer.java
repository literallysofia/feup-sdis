import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.UnknownHostException;
import java.net.MulticastSocket;

public class Peer implements RMIRemote {

    private int id;
    private static Multicast MC;
    private static Multicast MDB;
    private static Multicast MDR;

    public Peer() {
        try{
            this.MC = new Multicast();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public void sendMessage() throws UnknownHostException, InterruptedException {
        MC.send();
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

        /*while(true){
            try{
                //criar varias threas para cada tipo de multicast 
                MC.receive();
            }
            catch (UnknownHostException e){
                e.printStackTrace();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            
        }*/

        Thread threadMC = new Thread(new ChannelListener(MC));
        threadMC.start();

        Thread threadMDB = new Thread(new ChannelListener(MDB));
        threadMDB.start();

        Thread threadMDR = new Thread(new ChannelListener(MDR));
        threadMDR.start();
    }
}
