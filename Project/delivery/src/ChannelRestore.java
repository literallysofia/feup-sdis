import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ChannelRestore implements Runnable {
    private final String INET_ADDR = "224.0.0.17";
    private int PORT = 8003;
    private InetAddress address;

    public ChannelRestore() {
        //Get the address that we are going to connect to.
        try {
            address = InetAddress.getByName(INET_ADDR);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }


    public void sendMessage(byte[] msg) {

        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket senderSocket = new DatagramSocket()) {

            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg, msg.length, address, PORT);
            senderSocket.send(msgPacket);

            //System.out.println("CHANNEL RESTORE Sent msg: " + msg);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {

        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[65000];

        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try {
            //Joint the Multicast group.

            MulticastSocket receiverSocket = new MulticastSocket(PORT);

            receiverSocket.joinGroup(address);

            while (true) {
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                receiverSocket.receive(msgPacket);

                byte[] bufferCopy = Arrays.copyOf(buf, msgPacket.getLength());
                Peer.getExec().execute(new ManageReceivedMessageThread(bufferCopy));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
