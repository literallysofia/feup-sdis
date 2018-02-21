import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

public class Server {
    public static void main(String args[]) throws SocketException, UnknownHostException, IOException {
        
        //Parse de Argumentos
        int port_number = Integer.parseInt(args[0]);
        System.out.println(" > Port number: "+port_number);

        //Criação Datagram Socket
        DatagramSocket socket = new DatagramSocket(port_number);

        //Espera um pedido
        waitRequest(socket);
    }

    public static void waitRequest(DatagramSocket socket) throws IOException {

        while(true){
            //Cronstrução do Packet
            byte[] message = new byte[255];
            DatagramPacket packet = new DatagramPacket(message, message.length);

            //Receção do Packet
            socket.receive(packet);
           
            //Processamento do pedido
            if(message.length != 0){
                String messageString = new String(message);
                System.out.println(" > Message Received: " + messageString);
                String[] messageArray = messageString.split(" ");

                if(messageArray[0].equals("register")){
                    processRegisterMessage(messageArray[1], messageArray[2]);
                }else if (messageArray[0]. equals("lookup")){
                    processLookupMessage(messageArray[1]);
                }

            }
        }

    }


    public static void processRegisterMessage(String plate_number, String owner_name){
         System.out.println(" REGISTER MESSAGE" + "\n" + " > Plate Number: "+ plate_number+ "\n" + " > Owner Name: " + owner_name);
    }

    public static void processLookupMessage(String plate_number){
         System.out.println(" LOOKUP MESSAGE" + "\n" + " > Plate Number: "+ plate_number);
    }
}
