import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

public class Client {
    public static void main(String args[])throws SocketException, UnknownHostException, IOException{
        
        //Parse de Argumentos
        String host_name = args[0];
        int port_number = Integer.parseInt(args[1]);
        String oper = args[2];
        String plate_number = args[3];
        String owner_name;

        if (oper.equals("register"))
            owner_name = args[4];
        else owner_name = "";

        System.out.println(" > Host Name: "+ host_name + "\n" + " > Port Number: "+ port_number + "\n" + " > Oper: "+ oper + "\n" + " > Plate Number: "+ plate_number+ "\n" + " > Owner Name: " + owner_name);
        
        //Criação Datagram Socket
        DatagramSocket socket = new DatagramSocket();

        //Criação Datagram Packet
        InetAddress inetAddress = InetAddress.getByName(host_name);
        String messageString = oper + " " + plate_number + " " + owner_name;
        byte[] message = messageString.getBytes();
        DatagramPacket packet = new DatagramPacket(message, message.length, inetAddress, port_number);

        //Envio da mensagem
        socket.send(packet);

        //Receção da Resposta
        readResponse(socket);
    }

    public static void readResponse(DatagramSocket socket) throws IOException{
        byte[] message = new byte[255];
        DatagramPacket packet = new DatagramPacket(message, message.length);
        socket.receive(packet);
        String messageString = new String(message);

        System.out.println(" > Response: "+ messageString);
    }

}
