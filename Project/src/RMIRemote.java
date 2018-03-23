import java.rmi.Remote;
import java.rmi.RemoteException;
import java.net.UnknownHostException;

public interface RMIRemote extends Remote {
    void sendMessage() throws RemoteException, UnknownHostException, InterruptedException;
}
