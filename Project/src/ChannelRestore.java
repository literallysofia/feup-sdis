import java.io.IOException;
import java.net.*;

public class ChannelRestore implements Runnable{
    final static String INET_ADDR = "224.0.0.17";
    static int PORT = 8003;
    static InetAddress address;
    static MulticastSocket receiverSocket;


    public ChannelRestore() throws UnknownHostException {
        //Get the address that we are going to connect to.
        try{
            address = InetAddress.getByName(INET_ADDR);
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }

    }


    public static void sendMessage(String msg) throws UnknownHostException, InterruptedException{

        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket senderSocket = new DatagramSocket()) {

            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, address, PORT);
            senderSocket.send(msgPacket);

            System.out.println("Sent msg: " + msg);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run(){

        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[256];

        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try{
            //Joint the Multicast group.

            receiverSocket = new MulticastSocket(PORT);

            receiverSocket.joinGroup(address);

            while (true) {
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                receiverSocket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("Received msg: " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
