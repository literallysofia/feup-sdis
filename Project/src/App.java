import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
class App {

    private App() {
    }

    public static void main(String[] args) {

        try {


            if (args.length > 4) {
                System.out.println("ERROR: App format must be: App <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
                return;
            }

            String[] ipPeerID = args[0].split("/");
            String host = ipPeerID[0];
            String peerID = ipPeerID[1];

            Registry registry = LocateRegistry.getRegistry(host);
            RMIRemote peer = (RMIRemote) registry.lookup(peerID);

            String filePath;
            int diskSpaceToReclaim;
            int replicationDegree;

            switch (args[1]) {
                case "BACKUP":

                    if (args.length != 4) {
                        System.out.println("ERROR: Backup format must be: > BACKUP <file_path> <replication_degree>");
                        return;
                    }

                    filePath = args[2];
                    replicationDegree = Integer.parseInt(args[3]);
                    peer.backup(filePath, replicationDegree);
                    break;
                case "RESTORE":

                    if (args.length != 3) {
                        System.out.println("ERROR: Restore format must be: > RESTORE <file_path>");
                        return;
                    }

                    filePath = args[2];
                    peer.restore(filePath);
                    break;
                case "DELETE":

                    if (args.length != 3) {
                        System.out.println("ERROR: Delete format must be: > DELETE <file_path>");
                        return;
                    }

                    filePath = args[2];
                    peer.delete(filePath);
                    break;
                case "RECLAIM":

                    if (args.length != 3) {
                        System.out.println("ERROR: Reclaim format must be: > RECLAIM <disk_space_to_reclaim>");
                        return;
                    }

                    diskSpaceToReclaim = Integer.parseInt(args[2]);
                    peer.reclaim(diskSpaceToReclaim);

                    break;
                case "STATE":

                    if (args.length != 2) {
                        System.out.println("ERROR: State format must be: > STATE");
                        return;
                    }

                    peer.state();
                    break;
            }

        } catch (Exception e) {
            System.out.println("App exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
