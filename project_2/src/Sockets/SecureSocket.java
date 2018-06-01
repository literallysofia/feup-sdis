package Sockets;

import javax.net.ssl.*;
import java.io.*;
import java.util.*;
import java.security.*;

import Peer.Peer;

/**
 * SecureSocket
 */
public abstract class SecureSocket{

    protected int port;
    

    protected static final int PACKET_SIZE = 65536;

    protected DataInputStream din;
    protected DataOutputStream dout;


    protected KeyStore socketKeyStore;
    protected KeyStore publicKeyStore;

    protected SSLContext sslContext;


    static protected String passphrase;
    
    /**
     * A source of secure random numbers
    */
    static protected SecureRandom secureRandom;


    SecureSocket(){createSecureRandom();setPassphrase(Peer.getPeerID());}



    public void setPassphrase(String peerName){
        this.passphrase = peerName + "pw";
    }

    public boolean generatePublicKey(String peerName){
        setPassphrase(peerName);
        System.out.println("Generating " + peerName + " public and private keys");
        String commandtoCreate = "keytool -genkey -alias " + peerName + "private -keystore " + peerName + ".private -storetype JKS -keyalg rsa -dname 'CN=Your Name, OU=Your Organizational Unit, O=Your Organization, L=Your City, S=Your State, C=Your Country' -storepass " + peerName + "pw -keypass "+ peerName + "pw";
        String commandtoExportPublic = "keytool -export -alias " + peerName + "private -keystore " + peerName + ".private -file temp.key -storepass " + peerName + "pw";
        String commandtoImportPublic = "keytool -import -noprompt -alias " +peerName + "public -keystore " + peerName + ".public -file temp.key -storepass public";

        try{
            String[] args = {"/bin/bash","-c",commandtoCreate + ";" + commandtoExportPublic + ";" + commandtoImportPublic + ";rm -f temp.key"};
            Process proc = new ProcessBuilder(args).start();
            proc.waitFor();
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Keys generated succesfully");
        return true;
    }

    public void setupSocketKeyStore(String peerName) throws GeneralSecurityException, IOException{
        this.socketKeyStore = KeyStore.getInstance("JKS");
        String filename;
        if(peerName.equals("tracker")){
            filename = "Peer/"+peerName+".private";
            this.setPassphrase("tracker");
        } else
            filename = "Peer/"+peerName+".private";
        
        this.socketKeyStore.load(new FileInputStream(filename),
                                    this.passphrase.toCharArray());
    }

    public void setupPublicKeyStore(String peerName) throws GeneralSecurityException, IOException{
        this.publicKeyStore = KeyStore.getInstance("JKS");
        String publicpw = "public";
        this.publicKeyStore.load(new FileInputStream("Peer/"+peerName + ".public"),publicpw.toCharArray());
    }

    public void setupP2PPublicKeyStore(byte[] key)throws GeneralSecurityException, IOException{
        this.publicKeyStore = KeyStore.getInstance("JKS");
        String publicpw = "public";
        this.publicKeyStore.load(new ByteArrayInputStream(key),publicpw.toCharArray());
   
    }

    public void setupSSLContext() throws GeneralSecurityException, IOException{
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(publicKeyStore);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(socketKeyStore,passphrase.toCharArray());

        this.sslContext = SSLContext.getInstance("TLS");
        this.sslContext.init(kmf.getKeyManagers(),
                                tmf.getTrustManagers(),
                                secureRandom);
    }

    public void createSecureRandom(){
        System.out.println( "Wait while secure random numbers are initialized...." );
        secureRandom = new SecureRandom();
        secureRandom.nextInt();
        System.out.println( "Done." );
    }

}