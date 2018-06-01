package view;

import Peer.*;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.border.Border;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import java.awt.Button;
import java.awt.Container;

public class PeerWindow {
	
	private static Peer peer;
	private JFrame frame;
	private JTextField localIdField;
	private JTextField trackerIpField;
	private JTextField trackerPortField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PeerWindow window = new PeerWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public PeerWindow() throws IOException {
		initialize();
	}
	
	public void seed() {
		
		 String filePath ="";
		 String torrentPath="";
		
		//CHOOSE FILE TO SEED
		JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        	File file = fileChooser.getSelectedFile();
            if (file == null) {
                return;
            }

            filePath = fileChooser.getSelectedFile().getAbsolutePath();
            //System.out.println("FILE: " + filePath);
        }
        
      //CHOOSE WHERE TO SAVE TORRENT
        JFileChooser folderChooser = new JFileChooser(); 
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setAcceptAllFileFilterUsed(false);
           
        if (folderChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { 
        	File file =  folderChooser.getCurrentDirectory();
        	 if (file == null) {
                 return;
             }
        	torrentPath = folderChooser.getSelectedFile().getAbsolutePath();
        	//System.out.println("TORRENT: " + torrentPath);
        }else {
        	//System.out.println("No Selection ");
         }
        
        
		try {
			this.peer.seed(filePath, torrentPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void download() {
		
		String torrentPath ="";
		String filePath = "";
		//CHOOSE FILE TO SEED
		JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        	File file = fileChooser.getSelectedFile();
            if (file == null) {
                return;
            }

            torrentPath = fileChooser.getSelectedFile().getAbsolutePath();
            //System.out.println("TORRENT: " + torrentPath);
        }
        
        
      //CHOOSE WHERE TO SAVE TORRENT
        JFileChooser folderChooser = new JFileChooser(); 
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setAcceptAllFileFilterUsed(false);
           
        if (folderChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { 
        	File file =  folderChooser.getCurrentDirectory();
        	 if (file == null) {
                 return;
             }
        	filePath = folderChooser.getSelectedFile().getAbsolutePath();
        	//System.out.println("FILE: " + filePath);
        }else {
        	//System.out.println("No Selection ");
         }
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setValue(25);
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Reading...");
        progressBar.setBorder(border);
        
        frame.getContentPane().add(progressBar);
        frame.setSize(300, 100);
        frame.setVisible(true);
      
		try {
			this.peer.download(torrentPath, filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void register() {
		
		String trackerIp = trackerIpField.getText();
		int trackerPort = Integer.parseInt(trackerPortField.getText());
		int localId = Integer.parseInt(localIdField.getText());
		try {
			this.peer = new Peer(trackerIp, trackerPort, localId);
			this.peer.register();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 465, 383);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnRegister = new JButton("Register");
		btnRegister.setBounds(296, 171, 91, 25);
		btnRegister.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				register();
			}
		});
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(btnRegister);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(70, 245, 317, 25);
		btnDownload.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				download();
			}
		});
		frame.getContentPane().add(btnDownload);
		
		JButton btnSeed = new JButton("Seed");
		btnSeed.setBounds(68, 208, 319, 25);
		btnSeed.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				seed();
			}
		});
		frame.getContentPane().add(btnSeed);
		
		localIdField = new JTextField();
		localIdField.setBounds(177, 70, 210, 19);
		frame.getContentPane().add(localIdField);
		localIdField.setColumns(10);
		
		JLabel lblLocalId = new JLabel("Local Id:");
		lblLocalId.setBounds(70, 72, 66, 15);
		frame.getContentPane().add(lblLocalId);
		
		trackerIpField = new JTextField();
		trackerIpField.setBounds(177, 104, 210, 19);
		frame.getContentPane().add(trackerIpField);
		trackerIpField.setColumns(10);
		
		JLabel lblTrackerIp = new JLabel("Tracker Ip:");
		lblTrackerIp.setBounds(68, 106, 91, 15);
		frame.getContentPane().add(lblTrackerIp);
		
		trackerPortField = new JTextField();
		trackerPortField.setColumns(10);
		trackerPortField.setBounds(177, 135, 210, 19);
		frame.getContentPane().add(trackerPortField);
		
		JLabel lblTrackerPort = new JLabel("Tracker Port:");
		lblTrackerPort.setBounds(68, 137, 91, 15);
		frame.getContentPane().add(lblTrackerPort);
	}
}
