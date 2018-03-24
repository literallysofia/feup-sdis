import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

public class Multicast {

    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;
    static InetAddress address;

    public Multicast() throws UnknownHostException{
        //Get the address that we are going to connect to.
       try{
           address = InetAddress.getByName(INET_ADDR);
       }
       catch(UnknownHostException e){
           e.printStackTrace();
       }
    }

    public static void send() throws UnknownHostException, InterruptedException{
     
        // Open a new DatagramSocket, which will be used to send the data.
        try (MulticastSocket serverSocket = new MulticastSocket(PORT)) {
            for (int i = 0; i < 5; i++) {
                String msg = "MESSAGE " + i;

                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, address, PORT);
                serverSocket.send(msgPacket);
     
                System.out.println("Sent msg: " + msg);
                Thread.sleep(500);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void receive() throws UnknownHostException, InterruptedException{
        
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[256];
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
            //Joint the Multicast group.
            clientSocket.joinGroup(address);
     
            while (true) {
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("Received msg: " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
    