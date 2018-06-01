import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ChannelControl implements Runnable {
    private String INET_ADDR;
    private int PORT;
    private InetAddress address;


    public ChannelControl(String INETaddress, int port) {
        //Get the address that we are going to connect to.
        try {
            INET_ADDR = INETaddress;
            PORT = port;
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
                Peer.getExec().execute(new ReceivedMessagesManagerThread(bufferCopy));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
