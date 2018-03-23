import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class App {

    private App() {}

    public static void main(String[] args) {

        //String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            RMIRemote stub = (RMIRemote) registry.lookup(args[0]);
            stub.sendMessage();
        } catch (Exception e) {
            System.err.println("App exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
