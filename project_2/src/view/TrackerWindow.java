package view;
import Peer.*;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import Peer.*;
import java.awt.BorderLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.IOException;
import java.awt.Label;

public class TrackerWindow {

	private Tracker tracker;
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TrackerWindow window = new TrackerWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException */
	
	public TrackerWindow() throws IOException {
		initialize();
		this.tracker = new Tracker();
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		TextArea console = new TextArea();
		console.setEditable(false); 
		frame.getContentPane().add(console, BorderLayout.CENTER);
		
		Label label = new Label("TRACKER");
		frame.getContentPane().add(label, BorderLayout.NORTH);
		
	}

}
