import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.HashMap;

public class Server {

    static HashMap<String, String> platesHashMap = new HashMap<String,String>();

    public static void main(String args[]) throws SocketException, UnknownHostException, IOException {
        
        //Parse de Argumentos
        int port_number = Integer.parseInt(args[0]);
        System.out.println(" > Port number: "+port_number);        

        //Criação Datagram Socket
        DatagramSocket socket = new DatagramSocket(port_number);

        //Espera um pedido
          while(true){
            //Cronstrução do Packet
            byte[] message = new byte[255];
            DatagramPacket packet = new DatagramPacket(message, message.length);
        
            //Receção do Packet
            socket.receive(packet);
            InetAddress client_address = packet.getAddress();
            int client_port = packet.getPort();

            //Processamento do pedido
            if(message.length != 0){
                String messageString = new String(message);
                //System.out.println(" > Message Received: " + messageString);
                String[] messageArray = messageString.split(" ");

                if(messageArray[0].equals("register")){
                    processRegisterMessage(socket, client_port, client_address, messageArray[1], messageArray[2]);
                }else if (messageArray[0]. equals("lookup")){
                    processLookupMessage(socket, client_port, client_address, messageArray[1]);
                }

            }
        }
    }


    public static void processRegisterMessage(DatagramSocket socket, int client_port, InetAddress client_address, String plate_number, String owner_name) throws IOException{
        System.out.println(" REGISTER MESSAGE" + "\n" + " > Plate Number: "+ plate_number+ "\n" + " > Owner Name: " + owner_name);
        
        int response;

        if(platesHashMap.containsKey(plate_number)){
            response=-1;
        }else{
            platesHashMap.put(plate_number, owner_name);
            response=platesHashMap.size();
        }

        System.out.println(" > Response: " + response);

        //Mandar a resposta
        String responseString = String.valueOf(response);
        byte[] responseByte = responseString.getBytes();
        DatagramPacket packet = new DatagramPacket(responseByte, responseByte.length, client_address, client_port);
        socket.send(packet);
    }

    public static void processLookupMessage(DatagramSocket socket,  int client_port, InetAddress client_address, String plate_number) throws IOException{
        System.out.println(" LOOKUP MESSAGE" + "\n" + " > Plate Number: "+ plate_number);


        String response;

        if(platesHashMap.containsKey(plate_number)){
            response = platesHashMap.get(plate_number);
        }else{
            response = "NOT_FOUND";
        }

        System.out.println(" > Response: " + response);
        
        //Mandar o resposta
        byte[] responseByte = response.getBytes();

        DatagramPacket packet = new DatagramPacket(responseByte, responseByte.length, client_address, client_port);
        socket.send(packet);
        
         
    }
}
