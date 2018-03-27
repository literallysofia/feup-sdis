import java.io.IOException;
import java.net.*;

public class ChannelBackup implements Runnable {
    private final String INET_ADDR = "224.0.0.16";
    private int PORT = 8002;
    private InetAddress address;


    public ChannelBackup() {
        //Get the address that we are going to connect to.
        try{
            address = InetAddress.getByName(INET_ADDR);
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }

    }


    public void sendMessage(byte[] msg){

        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket senderSocket = new DatagramSocket()) {

            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg,msg.length, address, PORT);
            senderSocket.send(msgPacket);

            System.out.println("CHANNEL BACKUP Sent msg: " + msg);

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

            MulticastSocket receiverSocket = new MulticastSocket(PORT);
            receiverSocket.joinGroup(address);

            while (true) {
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                receiverSocket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("CHANNEL BACKUP Received msg: " + msg);

                ManageReceivedMessageThread manageMessage = new ManageReceivedMessageThread(msg);
                Peer.getExec().execute(manageMessage);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
