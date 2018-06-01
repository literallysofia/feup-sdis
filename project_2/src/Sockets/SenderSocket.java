package Sockets;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.net.UnknownHostException;
import java.nio.charset.*;
import java.lang.ProcessBuilder;
import java.security.cert.X509Certificate;
import Peer.Peer;
import Peer.MessageHandler;
import Peer.MessageHandler.Transition;
/**
 * DRSocket
 */
public class SenderSocket extends SecureSocket{

    protected String host;
    protected InetAddress address;
    protected TrustManager[] trustAllCerts;
    protected MessageHandler handler;

    public SenderSocket(int port,String host)throws UnknownHostException{
        super();
        this.port = port;
        this.host = host;
        this.address = InetAddress.getByName(this.host);
        trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
   

    }

    /**
     * @return the handler
     */
    public MessageHandler getHandler() {
        return handler;
    }


    public boolean connect(String connectFrom,String connectTo,byte[] key){
        
        try{
            setupSocketKeyStore(connectFrom);
            if(connectTo.equals("tracker")){
                setupPublicKeyStore(connectTo);
            }else{
                setupP2PPublicKeyStore(key);
            }

            setupSSLContext();

            SSLSocketFactory sf = sslContext.getSocketFactory();
            SSLSocket socket;
            try{
                socket = (SSLSocket) sf.createSocket(this.host,this.port);
            }catch (IOException e) {
                return false;
            }
            

            handler = new MessageHandler(socket,this.host, this.port);
            handler.updateState(Transition.SENDER);

            Peer.getExec().execute(handler);

            return true;

        }catch(GeneralSecurityException gse){
            gse.printStackTrace();
            return false;
        }catch(IOException e){
            e.printStackTrace();
            return false;
            //System.out.println("Error connecting to the host");
            //return false;
        }
    }     

}