/*
 * Bjarki Kjartansson
 * 30/01/18
 * Iceland
 */

package visoSkraning;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import java.awt.Toolkit;

public class Frame {
	

	private JFrame frame;
	private VisoSkraning vs = new VisoSkraning();
	private DynamicStartCountdown startCountdown;
	private DynamicStarting starting;
	private JButton button;
	
	
	class DynamicStartCountdown extends SwingWorker<Object, Object>{
		private JLabel timeLabel, lblDescription1, lblDescription2;
		private JButton button;
		
		DynamicStartCountdown(JButton button, JLabel timeLabel, JLabel lblDescription1, JLabel lblDescription2 , JLabel lblHeader){
			this.timeLabel = timeLabel;
			this.lblDescription1 = lblDescription1;
			this.lblDescription2 = lblDescription2;
			this.button = button;
		}

		@Override
		protected Object doInBackground() throws Exception {
			button.setVisible(false);
			lblDescription1.setText("");
			lblDescription2.setText("");
			vs.startCountdown(timeLabel, frame);
			return null;
		}
	}
	
	class DynamicStarting extends SwingWorker<Object, Object>{
		private JTextField username;
		private JPasswordField password;
		private JButton button;
		private JLabel errorLabel, lblUsername, lblPassword, timeLabel;
		
		
		DynamicStarting(JButton button, JTextField username, JPasswordField password, JLabel errorLabel, JLabel lblUsername, JLabel lblPassword, JLabel timeLabel){
			this.button = button;
			this.username = username;
			this.password = password;
			this.errorLabel = errorLabel;
			this.lblUsername = lblUsername;
			this.lblPassword = lblPassword;
			this.timeLabel = timeLabel;
		}
		

		@Override
		protected Object doInBackground() throws Exception {
			System.out.println("Starting");

			
			String username = this.username.getText();
			char[] password = this.password.getPassword();
			String credentials = username + ":" + String.valueOf(password);
			vs.start(frame, credentials, button, this.username, this.password, errorLabel, lblUsername, lblPassword, timeLabel);
			return null;
		}
	}
	

	/**
	 * Launch the application.
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame window = new Frame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("driver//HR_logo_hringur_hires.jpg"));
		frame.setResizable(false);
		frame.setBounds(1000, 100, 450, 345);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Myschool rimm maskína");

		
		JLabel lblHeader = createLabel("Einfalt forrit til að skrá sig í viðburði á myshcool", 0, 11, 434, 56);
		lblHeader.setFont(new Font("Cambria", Font.BOLD, 18));
		
		JLabel lblDescription1 = createLabel("Sláðu inn notandanafn og lykilorð fyrir mySchool og ýttu á Byrja.", 0, 55, 434, 25);
		
		JLabel lblDescription2 = createLabel("Veldu réttan viðburð og rútu ef það er í boði.", 0, 74, 434, 25);
		
		JLabel timeLabel = createLabel("", 30, 94, 368, 56);
		timeLabel.setFont(new Font("Cambria", Font.BOLD, 70));
		
		JLabel errorLabel = createLabel("", 20, 281, 276, 20);
		errorLabel.setHorizontalAlignment(SwingConstants.LEFT);
		errorLabel.setVisible(false);
		errorLabel.setForeground(Color.RED);
		
		JTextField username = createTextField(145, 110, 227, 25, 10);
		
		JPasswordField password = createPasswordField(145, 137, 227, 25);
		
		JLabel lblUsername = createLabel("Notandanafn:", 60, 110, 130, 25);
		lblUsername.setHorizontalAlignment(SwingConstants.LEFT);
		lblUsername.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				username.requestFocus();
			}
		});
		
		JLabel lblPassword = createLabel("Lykilorð:", 60, 137, 130, 25);
		lblPassword.setHorizontalAlignment(SwingConstants.LEFT);
		lblPassword.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				password.requestFocus();
			}
		});
		
		button = createButton("Byrja", 54, 171, 324, 109);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(button.getText().equals("Byrja") || button.getText().equals("Reyna aftur")) {
					if(username.getText().length() < 3) {
						errorLabel.setText("Notandanafnið er ekki rétt slegið inn");
						errorLabel.setVisible(true);
					}
					else if(password.getPassword().length < 6) {
						errorLabel.setText("Lykilorðið er of stutt");
						errorLabel.setVisible(true);
					}
					else {
						button.setText("Sæki gögn...");
						button.setEnabled(false);
						password.setEnabled(false);
						username.setEnabled(false);
						errorLabel.setVisible(false);
						starting = new DynamicStarting(button, username, password, errorLabel, lblUsername, lblPassword, timeLabel);
						starting.execute();
					}
				}
				else if(button.getText().equals("Halda áfram") && button.isEnabled()) {
					lblHeader.setText("Tími þar til það opnar fyrir skráningu:");
					lblHeader.setFont(new Font("Cambria", Font.BOLD, 22));
					startCountdown = new DynamicStartCountdown(button, timeLabel, lblDescription1, lblDescription2 , lblHeader);
					startCountdown.execute();
				}
			}
		});
	
	}
	
	private JLabel createLabel(String text, int x, int y, int width, int height) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Cambria", Font.PLAIN, 13));
		label.setBounds(x, y, width, height);
		frame.getContentPane().add(label);
		
		return label;
	}

	
	private JPasswordField createPasswordField(int x, int y, int width, int height) {
		JPasswordField passwordField = new JPasswordField();
		passwordField.setBounds(x, y, width, height);
		passwordField.setFont(new Font("Cambria", Font.PLAIN, 13));
		frame.getContentPane().add(passwordField);
		
		return passwordField;
	}
	
	private JTextField createTextField(int x, int y, int width, int height, int columnNr) {
		JTextField textField = new JTextField();
		textField.setBounds(x, y, width, height);
		textField.setFont(new Font("Cambria", Font.PLAIN, 13));
		frame.getContentPane().add(textField);
		textField.setColumns(columnNr);
		
		return textField;
	}
	
	private JButton createButton(String text, int x, int y, int width, int height) {
		JButton button = new JButton(text);
		button.setBounds(x, y, width, height);
		button.setFont(new Font("Cambria", Font.PLAIN, 13));
		frame.getContentPane().add(button);
		
		return button;
	}

}
