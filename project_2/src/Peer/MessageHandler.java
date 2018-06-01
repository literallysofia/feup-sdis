package Peer;

import Messages.*;
import Peer.Peer;

import static Peer.MessageHandler.Transition.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocket;


import java.util.AbstractMap.SimpleEntry;

/**
 * MessageHandler implements Runnable
 */
public class MessageHandler implements Runnable {

    private static final byte CR = 0xD;
    private static final byte LF = 0xA;
    private static final int MAX_SIZE = 31000;

    private SSLSocket connectedSocket;
    ArrayList<PeerInfo> peerInfos;
    
    private DataInputStream reader;
    private DataOutputStream writer;
    private String header;
    private byte[] body;
    private State fsmState = State.START;

    private volatile boolean running;

    public static enum Transition {
        SENDER, RECEIVER, WROTE, READ, QUIT;
    }

    public static enum State {
        START {
            @Override
            public State next(Transition transition) {
                if(transition == SENDER){
                    return WRITE;
                }else if(transition == RECEIVER){
                    return RECEIVE;

                }
                return START;
            }
        },
        WRITE {
            @Override
            public State next(Transition transition) {
                if (transition == WROTE) {
                    return RECEIVE;
                }
                return WRITE;
            }
        },
        RECEIVE {
            @Override
            public State next(Transition transition) {
                if (transition == READ) {
                    return WRITE;
                } else if (transition == QUIT) {
                    return CLOSE;
                }
                return RECEIVE;
            }
        },
        CLOSE {
            @Override
            public State next(Transition transition) {
                return CLOSE;
            }
        };

        public State next(Transition transition) {
            return null;
        }

    }

    public void updateState(Transition transition){
        fsmState = fsmState.next(transition);
    }

    @Override
    public void run() {
        running = true;
        try{
            while (running) {

                /*if(this.connectedSocket.isClosed()){
                    Node.decReceiverCounter();
                    break;                           
                }*/
            
                switch (fsmState) {
                case RECEIVE:
                    try {
                        checkMessage();
                    } catch (IOException e) {
                       // e.printStackTrace();
                    }
                    break;
                case CLOSE:
                 try {
                        this.connectedSocket.close();
                        Node.decReceiverCounter();
                                    
                    } catch (IOException e) {
                      e.printStackTrace();
                   }
                    running = false;
                    break;
                default:
                    break;
                }
            }
        }catch(Throwable o){
            o.printStackTrace();
        }

    }

    public MessageHandler(SSLSocket socket) throws IOException{
        this.connectedSocket = socket;

        this.peerInfos = null;

        socket.setSoTimeout(15000);

        InputStream in = this.connectedSocket.getInputStream();
        OutputStream out = this.connectedSocket.getOutputStream();

        reader = new DataInputStream(in);
        writer = new DataOutputStream(out);

    }
    
    public MessageHandler(SSLSocket socket, String address, int port) throws IOException{
        this.connectedSocket = socket;

        this.peerInfos = Peer.getPeerInfosByIpPort(address, port);

        socket.setSoTimeout(15000);

        InputStream in = this.connectedSocket.getInputStream();
        OutputStream out = this.connectedSocket.getOutputStream();

        reader = new DataInputStream(in);
        writer = new DataOutputStream(out);

    }

    public synchronized void setPeersUnavailable(){

        if(this.peerInfos == null)
            return;
        for (int i = 0; i<peerInfos.size(); i++) {
            if(peerInfos.get(i) != null)
            {
                System.out.println("UNAVAILABLE");
                peerInfos.get(i).setAvailable(false);
            }
        }
    }

    public DataOutputStream getWriter() {
        return writer;
    }

    public synchronized boolean sendMessage(Message msg) {
        if (writer != null && fsmState == State.WRITE){
            byte[] textMessage = msg.getFullMessage();
            try {             
                writer.write(textMessage);             
                fsmState = fsmState.next(WROTE);
                return true;
            } catch (IOException e) {
                setPeersUnavailable();
                return false;
            }

        } else {
            System.err.println("Error: cant send message if connection hasnt been established or you  are not the one to send the message");
            return false;
        }

    }

    public void separateMessage(int size, byte[] data) {
        int i = 0;
        for (; i < size; i++) {
            if (i <= size - 5) {
                if (data[i] == CR && data[i + 1] == LF && data[i + 2] == CR && data[i + 3] == LF) {
                    break;
                }
            }
        }
        byte[] headerByte = new byte[i];
        System.arraycopy(data, 0, headerByte, 0, i - 1);
        this.header = new String(headerByte);
        this.header = this.header.trim();

        if (size > i + 3) {
            this.body = new byte[size - i - 4];
            System.arraycopy(data, i + 4, this.body, 0, size - i - 4);

        } else {
            this.body = null;
        }

        System.out.println("Received: " + this.header);
        
        
    }

    public void checkMessage() throws IOException{
        
        byte[] buffer = new byte[MAX_SIZE];
        int readsize = 0;
        try {
            System.out.println("Reading");
            readsize = reader.read(buffer);
        } catch (SocketTimeoutException e) {
        	setPeersUnavailable();
            System.out.println("Socket timeout");
            Node.decReceiverCounter();            
            running=false;
            return;
        } catch(SocketException a) {
            setPeersUnavailable();
            System.out.println("Socket exception");
            Node.decReceiverCounter();            
            running=false;
            return;
        }catch(Exception j) {
            setPeersUnavailable();
            System.out.println("Socket exception");
            Node.decReceiverCounter();            
            running=false;
            return;
        }catch(Throwable th){
        	setPeersUnavailable();
            System.out.println("Socket exception");
            Node.decReceiverCounter();            
            running=false;
            return;
        }
        
        System.out.println("ReadSize: " + readsize);
        
        if(readsize == 0)
            return;
        if(readsize == -1)
        {
            /*setPeersUnavailable();
            Node.decReceiverCounter();            
            running=false;*/
            this.updateState(READ);
            return;
        }
        byte[] response = Arrays.copyOfRange(buffer, 0, readsize);
        buffer = new byte[MAX_SIZE];
        if(readsize > 16000){
            try {
        
                readsize = reader.read(buffer);
            } catch (SocketTimeoutException e) {
            	setPeersUnavailable();
                System.out.println("Socket timeout");
                Node.decReceiverCounter();            
                running=false;
                return;
            } catch(SocketException a) {
                setPeersUnavailable();
                System.out.println("Socket exception");
                Node.decReceiverCounter();            
                running=false;
                return;
            } catch(Exception j) {
                setPeersUnavailable();
                System.out.println("Socket exception");
                Node.decReceiverCounter();            
                running=false;
                return;
            }catch(Throwable th){
            	setPeersUnavailable();
                System.out.println("Socket exception");
                Node.decReceiverCounter();            
                running=false;
                return;
            }
            
                
            if(readsize == -1) {
                this.updateState(READ);
                return;
            }
            if(readsize == 0){
                return;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] messagePart = Arrays.copyOfRange(buffer, 0, readsize);
            outputStream.write(response);
            outputStream.write(messagePart);
            response = outputStream.toByteArray();
            buffer = new byte[MAX_SIZE];
            
        }

       //byte[] response = new byte[MAX_SIZE];
        //readsize = readInputStreamWithTimeout(response,6000);

        
        this.separateMessage(response.length, response);
        String messageType = this.header.substring(0,this.header.indexOf(" "));
        
        switch (messageType) {
            case "ERROR": {
                fsmState=fsmState.next(QUIT); 
                break;
            }
            case "CLOSE": {                        
                fsmState=fsmState.next(QUIT);              
                break;
            }
            case "REGISTER": {
                RegisterMessage register = new RegisterMessage(header,body);
                register.action(writer);

                running=false;
                break;
            }
            case "ONLINE": {
                OnlineMessage online = new OnlineMessage(header);
                online.action(writer);
                Node.decReceiverCounter();
                running=false;                
                break;
            }
            case "HASFILE": {                      
                HasFileMessage hasfile = new HasFileMessage(header);
                hasfile.action(writer);
                Node.decReceiverCounter();
                running=false;
                break;
            }
            case "NOFILE": {                      
                NoFileMessage nofile = new NoFileMessage(header);
                nofile.action();
                break;
            }
            case "GETFILE": {                      
                GetFileMessage getfile = new GetFileMessage(header);
                getfile.action(writer);
                Node.decReceiverCounter();
                running=false;
                break;
            }
            case "PEERINFO": {                      
                PeerInfoMessage peerinfo = new PeerInfoMessage(header, body);
                peerinfo.action();
                break;
            }
            case "PEERINFOEND": {                      
                PeerInfoEndMessage peerinfoend = new PeerInfoEndMessage(header);
                peerinfoend.action();
                fsmState=fsmState.next(QUIT);  
                break;
            }
            case "GETCHUNK": {                      
                GetChunkMessage getChunkMessage = new GetChunkMessage(header);
                if(getChunkMessage.action(writer) == -1) {
                	  Node.decReceiverCounter();
                      running=false;
                }
                break;
            }
            case "CHUNK": {                      
                ChunkMessage chunkMessage = new ChunkMessage(header, body);
                int res = chunkMessage.action(writer, this.peerInfos);
                if(res == 1){
                    fsmState=fsmState.next(QUIT);              
                }
                break;
            }  
            default:
                break;
        }

    }

    public  int readInputStreamWithTimeout(byte[] buffer,int timeoutMillis) throws IOException{
        int bufferOffset = 0;
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while(System.currentTimeMillis() < maxTimeMillis && bufferOffset < buffer.length){
            int readLength = buffer.length-bufferOffset;
            int readResult = reader.read(buffer,bufferOffset,readLength);
            if(readResult == -1) break;
            bufferOffset += readResult;
        }
        return bufferOffset;   
    }

}