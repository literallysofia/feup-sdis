package Peer;

import Peer.Peer;
import Peer.TorrentInfo;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.AbstractMap.SimpleEntry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.text.StyledEditorKit.BoldAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FileManager {

    private static final int CHUNKSSIZE = 64000;

    private String pathname;

    public FileManager(String pathname) {
        this.pathname = pathname;
    }

    public FileManager() {

    }

    /*
     * public List<ChunkData> splitFile() throws IOException{
     * 
     * byte[] buffer = new byte[CHUNKSSIZE]; List<ChunkData> chunksArray = new
     * ArrayList<ChunkData>();
     * 
     * 
     * //try-with-resources to ensure closing stream try ( FileInputStream fis = new
     * FileInputStream(pathname); BufferedInputStream bis = new
     * BufferedInputStream(fis) ) {
     * 
     * int chunkNmb = 0; int size; while ((size = bis.read(buffer)) > 0) {
     * 
     * ChunkData chunk = new ChunkData(chunkNmb); chunk.setData(size, buffer);
     * chunkNmb += 1; chunksArray.add(chunk); buffer = new byte[CHUNKSSIZE]; }
     * 
     * File file = new File(pathname); if(file.length()%CHUNKSSIZE == 0) { ChunkData
     * chunk = new ChunkData(chunkNmb); chunksArray.add(chunk); } } return
     * chunksArray;
     * 
     * }
     */

    /**
     * Merges chunk files into one
     */
    /*
     * public void mergeFile(File[] files) throws IOException{
     * 
     * File file = new File(pathname); if(!file.exists()) {
     * file.getParentFile().mkdirs(); file.createNewFile(); }
     * 
     * try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream
     * mergingStream = new BufferedOutputStream(fos)) { for (File f : files) {
     * Files.copy(f.toPath(), mergingStream); } } }
     */
    /**
     * Randomly generates an unique file id
     */
    public String generateFileID() {

        File f = new File(pathname);
        Path path = Paths.get(pathname);
        if (!f.exists() || f.isDirectory())
            return null;
        String bitString = null;
        try {
            bitString = f.getName() + Long.toString(f.lastModified()) + Boolean.toString(f.canWrite())
                    + Boolean.toString(f.canRead()) + f.getPath() + Files.getOwner(path).getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sha256(bitString);
    }

    /**
     * Encrypts a String with sha256 without "prohibited characters"
     */
    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte[] readEntireFileData() throws IOException {

        Path path = Paths.get(pathname);

        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(CHUNKSSIZE);
        long position = 0;

        Future<Integer> operation = channel.read(buffer, position);

        while (!operation.isDone())
            ;

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        buffer.clear();

        return data;

    }

    /**
     * Retrieves and deletes all the file data from the file specified
     * 
     * @param pathname name of the file to delete
     * @return return deleted data or null if where's nothing to delete
     */
    public byte[] deleteFile(String pathname) {
        Path path = Paths.get(pathname);
        File fileParent = new File(pathname).getParentFile();
        try {
            byte[] return_data = Files.readAllBytes(path);
            Files.delete(path);
            if (fileParent.isDirectory() && fileParent.list().length == 0)
                Files.delete(fileParent.toPath());
            return return_data;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Retrieves the data bytes of the file specified
     */
    public byte[] getFileData(String pathname) {
        try {
            return Files.readAllBytes(new File(pathname).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeToFileAsync(byte[] data, long offset) throws IOException {

        try {
            Path path = Paths.get(pathname);
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.SYNC);

        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data);
        buffer.flip();

        fileChannel.write(buffer, offset, buffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                try {
                    fileChannel.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    fileChannel.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }

        });
        
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
    }

    public boolean createFileDir(String folderName) {
        File dir = new File(folderName);
        return dir.mkdir();
    }

    public byte[] readFileAsync(long offset, int dataSize) throws IOException {
    	
    	File f = new File(pathname);
    	if(!f.exists() || f.isDirectory()) {
    		return null;
    	}
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(pathname), StandardOpenOption.READ,
                StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(dataSize);

        Future<Integer> operation = channel.read(buffer, offset);

        try {
            operation.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }
    
        byte[] returnChunk = buffer.array();
        int remaining = buffer.remaining();

        byte[] chunk = new byte[returnChunk.length-remaining];

        System.arraycopy(returnChunk, 0, chunk,0, returnChunk.length-remaining);

        buffer.clear();

        channel.close();

        return chunk;

    }

    public SimpleEntry<String,TorrentInfo> createDownloadFile(long chunkSize, int port, String address,String toStore) {

        File file = new File(pathname);
        long size = 0;
        try{
            size = Files.size(file.toPath());
        }catch(IOException e ){
            e.printStackTrace();
        }

        if (!file.exists()) {
            System.err.println("Can't create download file from non existing files");
            return null;
        }

        String fileName = file.getName();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // root element
            Element rootElement = doc.createElement("torrent");
            doc.appendChild(rootElement);

            Element trackerElement = doc.createElement("tracker");
            rootElement.appendChild(trackerElement);

            // setting attributes to tracker
            Attr ipAttr = doc.createAttribute("IP");
            ipAttr.setValue(address);
            trackerElement.setAttributeNode(ipAttr);

            Attr portAttr = doc.createAttribute("Port");
            portAttr.setValue(Integer.toString(port));
            trackerElement.setAttributeNode(portAttr);

            Element chunkElement = doc.createElement("Chunk");
            rootElement.appendChild(chunkElement);

            Attr sizeAttr = doc.createAttribute("length");
            sizeAttr.setValue(Long.toString(chunkSize));
            chunkElement.setAttributeNode(sizeAttr);

            Element fileElement = doc.createElement("File");
            rootElement.appendChild(fileElement);

            Attr fileID = doc.createAttribute("ID");
            String id = this.generateFileID();
            fileID.setValue(id);
            fileElement.setAttributeNode(fileID);

            Attr fileNameAttr = doc.createAttribute("name");
            fileNameAttr.setValue(fileName);
            fileElement.setAttributeNode(fileNameAttr);

            Attr fileLengthAttr = doc.createAttribute("length");
            //System.out.println("FILE YOLO: "+size);
            fileLengthAttr.setValue(Long.toString(size));
            fileElement.setAttributeNode(fileLengthAttr);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(toStore +"/" + fileName + ".xml"));
            transformer.transform(source, result);

            return new SimpleEntry<>(id,new TorrentInfo(address, port, chunkSize, file.length(), this.pathname));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public SimpleEntry<String,TorrentInfo> parseDownloadFile() {
        File toParse = new File(this.pathname);

        if (!toParse.exists()) {
            System.err.println("File does not exist");
            return null;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(toParse);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("tracker");
            String ip = null;
            int port = 0;
            for (int i = 0; i < nList.getLength(); i++) {
                
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    ip = eElement.getAttribute("IP");
                    port = Integer.parseInt(eElement.getAttribute("Port"));


                }
            }

            nList = doc.getElementsByTagName("Chunk");
            long length = -1;
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    length = Long.parseLong(eElement.getAttribute("length"));

                }
            }

            nList = doc.getElementsByTagName("File");
            String fileID = null;
            long fileLength = -1;
            String name = null;

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    fileID = eElement.getAttribute("ID");
                    fileLength = Long.parseLong(eElement.getAttribute("length"));
                    name = eElement.getAttribute("name");


                }
            }
            TorrentInfo torrentInfo =  new TorrentInfo(ip, port, length, fileLength, pathname);
            torrentInfo.setName(name);
            return new SimpleEntry<>(fileID,torrentInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}