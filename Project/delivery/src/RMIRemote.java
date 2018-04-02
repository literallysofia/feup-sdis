import java.rmi.Remote;
import java.rmi.RemoteException;

interface RMIRemote extends Remote {
    void backup(String filepath, int replicationDegree) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String filepath) throws RemoteException;
    void reclaim(int diskSpaceToReclaim) throws RemoteException;
    void state() throws RemoteException;
}
