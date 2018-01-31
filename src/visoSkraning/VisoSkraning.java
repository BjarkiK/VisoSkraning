/*
 * Bjarki Kjartansson
 * 30/01/18
 * Iceland
 */


package visoSkraning;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class VisoSkraning {
	
	private WebDriver driver = null;
	private List<WebElement> checkBoxes;
	private DynamicGetWebsite continuing;
	private List<JCheckBox> availableEvents;
	private List<JCheckBox> eventQuestions;
	private String selectedEvent;
	
	
	class DynamicGetWebsite extends SwingWorker<Object, Object>{
		
		private String credentials;
	
		DynamicGetWebsite(String credentials){
			this.credentials = credentials;
		}

		@Override
		protected Object doInBackground() throws Exception {
			driver.get("https://" + this.credentials + "@myschool.ru.is/myschool/");
			return null;
		}
	
	}

	public void start(JFrame frame, String credentials, JButton button, JTextField username, JPasswordField password, JLabel errorLabel, JLabel usernameLabel, JLabel passwLabel, JLabel timeLabel) throws InterruptedException, AWTException{		
		driver = initialiceDriver();

		continuing = new DynamicGetWebsite(credentials);
		continuing.execute();
		String errorLabelValue = errorLabel.getText();
		List<WebElement> events = getEvents(errorLabelValue);
		if(events == null) {
			loginError(button, username, password, errorLabel);
			return;
		}
		if(events.size() == 0) {
			System.out.println("No events");
			quitDriver();
			return;
		}
		
		removeEmptyListing(events);		
		setVisibilityFalse(username, password, usernameLabel, passwLabel, button);
		handleEventCheckboxes(events, button, frame);
		
		fetchAndDisplayQuestionCheckBoxes(timeLabel, button, frame);
	}
	
	public void startCountdown(JLabel timeLabel, JFrame frame) {
		stalkButton(timeLabel, frame);
	}
	
	private void stalkButton(JLabel timeLabel, JFrame frame) {
		for(int i = 0; i < eventQuestions.size(); i++) {
			if(eventQuestions.get(i).isSelected()) {
				checkBoxes.get(i).click();
			}
		}
		hideCheckBoxes(eventQuestions);

		String timeLeft = "";
		String timeLeftChecker = "";
		List<JLabel> labels = creatFinalLabels(frame);
		frame.repaint();
		System.out.println(labels.size());

		while(true) {
//			System.out.println("Running");
			boolean countdownVisible = driver.findElements(By.id("TheCountDown")).size() > 0;
			if(countdownVisible == false) {
				timeLeftChecker = "Skráning er hafin";
			}
			else {
				timeLeftChecker = driver.findElement(By.id("TheCountDown")).getText();
			}
			
			if(timeLeftChecker.equals("Skráning er hafin")) {
				clickButton(driver);
				System.out.println("Skrá");
				hideLabels(labels);
				String successString = successStringMaking(driver);
				timeLabel.setText(successString);
				timeLabel.setBounds(30, 65, 368, 150);
				frame.repaint();
				quitDriver();
				return;
			}
			timeLeft = setTimeLabel(timeLeftChecker, timeLeft, timeLabel);
//			System.out.println(formateTime(timeLeft));
		}
	}
	
	private WebDriver initialiceDriver(){
		ChromeOptions chromeOptions = new ChromeOptions();
	    chromeOptions.addArguments("--headless");
		System.setProperty("webdriver.chrome.driver", "driver\\chromedriver.exe");
		return new ChromeDriver(chromeOptions);
	}
	
	/*
	 * This function waits for myschool front page to load (Waits for html tag)
	 * When loaded all table elements with class = ruTable are stored.
	 * If any of the table elements text equals "NÆSTU ATBURÐIR" then there is a event
	 * If "NÆSTU ATBURÐIR" element is found, its children are returned
	 * If "NÆSTU ATBURÐIR" element is not found a empyy list is found. 
	 * If the html element is not found within 3/13 sek then null is returned
	 * If errorLabelValue == "" timeToWait is 3 sek, 13 sek else
	 */
	private List<WebElement> getEvents(String errorLabelValue){
		int timeToWait = setTimeout(errorLabelValue);
		
		List<WebElement> events = new ArrayList<WebElement>();
		long startTime = new Date().getTime();
		while(true) {
			
			if(numberOfElements("/html") > 0) {
				List<WebElement> allTables = getElements("//table[(@class='ruTable')]/tbody/tr");
				int tablsSize = allTables.size();
				
				for(int i = 0; i < tablsSize; i++) {
					if(allTables.get(i).getText().equals("NÆSTU ATBURÐIR")) {
						System.out.println("Took " + (new Date().getTime() - startTime) + " ms" );
						return allTables.get(i).findElements(By.xpath("following-sibling::*/td[1]"));
					}
					
					//target element not found;
					if(i == tablsSize-1) {
						return events;
					}
				}
			}
			
			long endTime = new Date().getTime();
			if(endTime - startTime > timeToWait) {
				return null;
			}
		}
	}
	
	private void loginError(JButton button, JTextField username, JPasswordField password, JLabel errorLabel) {
		System.out.println("Unable to login");
		button.setEnabled(true);
		button.setText("Reyna aftur");
		username.setEnabled(true);
		password.setEnabled(true);
		errorLabel.setText("Innskráning tókst ekki. Reynið aftur");
		errorLabel.setVisible(true);
		quitDriver();
	}
	
	private void removeEmptyListing(List<WebElement> events) {
		for(int i = 0; i < events.size(); i++) {
			if(events.get(i).getText().length() == 0) {
				events.remove(i);
			}
		}
	}
	
	private void setVisibilityFalse(JTextField username, JPasswordField password, JLabel usernameLabel, JLabel passwLabel, JButton button){
		username.setVisible(false);
		password.setVisible(false);
		usernameLabel.setVisible(false);
		passwLabel.setVisible(false);
		button.setBounds(260, 110, 150, 170);
	}
	
	private void handleEventCheckboxes(List<WebElement> events, JButton button, JFrame frame) {

		if(events.size() == 1) {
			selectedEvent = events.get(0).getAttribute("title");
			events.get(0).click();
		}
		else {
			availableEvents = generateCheckBoxes(events, frame, 1);
			button.setText("Veldu Viðburð");
		}
	}
	
	private void fetchAndDisplayQuestionCheckBoxes(JLabel timeLabel, JButton button, JFrame frame) {
		while(true) {
			if(numberOfElements("//span[(@class='ruContent')]") > 0) {
				System.out.println("Element found");
				checkBoxes = getElements("//input[(@type='checkbox')]");
				List<WebElement> checkBoxeNames = getElements("//input[(@type='checkbox')]/../..");
				
				if(checkBoxeNames.size() == 0) {
					stalkButton(timeLabel, frame);
				}

				eventQuestions = generateCheckBoxes(checkBoxeNames, frame, 2);

				button.setText("Halda áfram");
				button.setEnabled(true);
				break;
			}
		}
	}   
	
	private List<JCheckBox> generateCheckBoxes(List<WebElement> elements, JFrame frame, int nrOfList) {
		int height = 110;
		List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
		for(int i = 0; i < elements.size(); i++) {
			JCheckBox checkBox = assembleCheckbox(nrOfList, i, height, elements, frame);
			checkBoxes.add(checkBox);
			height = height + 26;
		}
		
		return checkBoxes;
	}
	
	private JCheckBox assembleCheckbox(int nrOfList, int index, int height, List<WebElement> elements, JFrame frame) {
		JCheckBox checkBox = createCheckBox(50, height, 200, 30, frame);
		String text = "";
		if(nrOfList == 1) {
			text = elements.get(index).getAttribute("title");
			checkBox.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					clickSelectedEvent(elements);
				}
			});
		}
		else {
			text = elements.get(index).getText().replaceAll(":", "");
		}
		checkBox.setText(text);
		checkBox.setToolTipText(text);
		
		return checkBox;
	}

	private JCheckBox createCheckBox(int x, int y, int width, int height, JFrame frame) {
		JCheckBox checkBox = new JCheckBox("");
		setSettingsForCheckBox(checkBox);
		checkBox.setBounds(x, y, width, height);
		checkBox.setFont(new Font("Cambria", Font.PLAIN, 13));
		frame.getContentPane().add(checkBox);
		
		return checkBox;
	}
	
	private void setSettingsForCheckBox(JCheckBox element){
		element.setOpaque(false);
		element.setContentAreaFilled(false);
		element.setBorderPainted(false);
		element.setFocusPainted(false);
	}

	private String fixString(String faultyString) {
		int begIndex = 0;
		int endIndex = faultyString.length();
		char tmpChar = '-';
		boolean hasSeenNoSpace = false;
		for(int i = 0; i < faultyString.length(); i++) {
			if(faultyString.charAt(i) != ' ' && begIndex == 0) {
				System.out.println("BegIndex " + i);
				begIndex = i;
				hasSeenNoSpace = true;
				if(i == faultyString.length()-1) {
					return "";
				}
			}
			if(faultyString.charAt(i) == ' ' && tmpChar == ' ' && hasSeenNoSpace == true) {
				endIndex = i - 1;
				System.out.println("EndIndex " + endIndex);
				break;
			}
			tmpChar = faultyString.charAt(i);
		}
		return faultyString.substring(begIndex, endIndex);
	}
	
	private List<JLabel> creatFinalLabels(JFrame frame){
		List<JLabel> labels = new ArrayList<JLabel>();
		labels.add(createLabel("Viðburður: " + selectedEvent, 35, 182, 200, 25, frame));
		
		if(eventQuestions.size() > 0) {
			int height = 200;

			labels.add(createLabel("Valið: ", 35, 208, 200, 14, frame));
			for(int i = 0; i < eventQuestions.size(); i++) {
				if(eventQuestions.get(i).isSelected()) {
					height = height + 22;
					String text = fixString(eventQuestions.get(i).getText());
					labels.add(createLabel(text, 35, height, 200, 18, frame));
				}
			}
		}
		if(labels.size() == 2) {
			labels.remove(1);
		}
		return labels;
	}
	
	private JLabel createLabel(String text, int x, int y, int width, int height, JFrame frame) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setFont(new Font("Cambria", Font.PLAIN, 15));
		label.setBounds(x, y, width, height);
		frame.getContentPane().add(label);
		
		return label;
	}

	private void clickSelectedEvent(List<WebElement> elements) {
		for(int i = 0; i < availableEvents.size(); i++) {
			if(availableEvents.get(i).isSelected()) {
				selectedEvent = elements.get(i).getText();
				elements.get(i).click();
				break;
			}
		}
		hideCheckBoxes(availableEvents);
	}
	
	private String formateTime(String time) {
		String[] spaceSplitted = time.split(" ");
		for(int i = 0; i < spaceSplitted.length-1; i=i+2) {
			if(spaceSplitted[i].length() < 2) {
				spaceSplitted[i] = "0" + spaceSplitted[i];
			}
		}
		String formatedTime = "";
		for(int i = 0; i < spaceSplitted.length-1; i=i+2) {
			if(i == spaceSplitted.length-2) {
				formatedTime = formatedTime + spaceSplitted[i];
			}
			else {
				formatedTime = formatedTime + spaceSplitted[i] + ":";
			}
		}
		while(formatedTime.length() != 8) {
			formatedTime = "00:" + formatedTime;
		}
		return formatedTime;
	}
	
	private String setTimeLabel(String timeLeftChecker, String timeLeft, JLabel timeLabel) {
		if(!timeLeftChecker.equals(timeLeft)) {
			timeLeft = timeLeftChecker;
			
			timeLabel.setText(formateTime(timeLeft));
		}
		return timeLeft;
	}
	
	private void clickButton(WebDriver driver) {
		boolean buttonVisible = driver.findElements(By.xpath("//input[(@type='button') and (@value='Skrá') and (@name='ruButton')]")).size() > 0;
		while(buttonVisible == true) {
			System.out.println("Click");
			driver.findElement(By.xpath("//input[(@type='button') and (@value='Skrá') and (@name='ruButton')]")).click();
			buttonVisible = driver.findElements(By.xpath("//input[(@type='button') and (@value='Skrá') and (@name='ruButton')]")).size() > 0;
		}
	}
	
	private String successStringMaking(WebDriver driver) {
		String successstring = "<html>"
				  + "<p style=\"font-size:25px; text-align:center\">Skráning tókst fyrir:</p>"
				  + "<p style=\"font-size:30px; text-align:center\">" + driver.findElement(By.xpath("//*[@id='register']/table/tbody/tr[1]/th[2]")).getText() + "</p>"
			 + "</html>";
		return successstring;
	}
	
	private void hideCheckBoxes(List<JCheckBox> checkBoxes) {
		for(int i = 0; i < checkBoxes.size(); i++) {
			checkBoxes.get(i).setVisible(false);
		}
	}
	
	private void hideLabels(List<JLabel> label) {
		for(int i = 0; i < checkBoxes.size(); i++) {
			label.get(i).setVisible(false);
		}
	}
	
	private int setTimeout(String errorLabelValue) {
		if(!errorLabelValue.equals("")) {
			return 13000;
		}
		return 3000;
	}
	
	private int numberOfElements(String xpath) {
		return driver.findElements(By.xpath(xpath)).size();
	}
	
	private List<WebElement> getElements(String xpath){
		return driver.findElements(By.xpath(xpath));
	}

	private void quitDriver(){
		driver.close();
		driver.quit();
	}

}
