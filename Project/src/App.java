import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class App {

    private App() {}

    public static void main(String[] args) {

        //String host = (args.length < 1) ? null : args[0];
        try {

            if(args.length > 4){
                System.out.println("ERROR: App format must be: App <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
                return;
            }

            String peerID = args[0];

            Registry registry = LocateRegistry.getRegistry(1099);
            RMIRemote peer = (RMIRemote) registry.lookup(peerID);

            String filePath;
            int diskSpaceToReclaim;
            int replicationDegree;

            if(args[1].equals("BACKUP")){

                if(args.length != 4){
                    System.out.println("ERROR: Backup format must be: > BACKUP <file_path> <replication_degree>");
                    return;
                }

                filePath = args[2];
                replicationDegree = Integer.parseInt(args[3]);
                peer.backup(filePath, replicationDegree);
            }
            else if(args[1].equals("RESTORE")){

                if(args.length != 2){
                    System.out.println("ERROR: Restore format must be: > RESTORE <file_path>");
                    return;
                }

                filePath = args[2];
                peer.restore(filePath);
            }
            else if(args[1].equals("DELETE")){

                if(args.length != 2){
                    System.out.println("ERROR: Delete format must be: > DELETE <file_path>");
                    return;
                }

                filePath = args[2];
                peer.delete(filePath);
            }
            else if(args[1].equals("RECLAIM")){

                if(args.length != 2){
                    System.out.println("ERROR: Reclaim format must be: > RECLAIM <disk_space_to_reclaim>");
                    return;
                }

                diskSpaceToReclaim = Integer.parseInt(args[2]);
                peer.reclaim(diskSpaceToReclaim);
            }
            else if(args[1].equals("STATE")){

                if(args.length != 1){
                    System.out.println("ERROR: State format must be: > STATE");
                    return;
                }

                peer.state();
            }
            
        } catch (Exception e) {
            System.out.println("App exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
