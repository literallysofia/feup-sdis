import java.rmi.Remote;
import java.rmi.RemoteException;
import java.net.UnknownHostException;

public interface RMIRemote extends Remote {
    public void backup(String filepath, int replicationDegree) throws RemoteException;        
    public void restore(String filepath) throws RemoteException;
    public void delete(String filepath) throws RemoteException;
    public void reclaim(int diskSpaceToReclaim) throws RemoteException;
    public void state() throws RemoteException;
}
