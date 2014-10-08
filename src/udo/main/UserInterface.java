package udo.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import udo.util.shared.Constants.Keys;
import udo.util.shared.ItemData;
import udo.util.shared.OutputData;
import udo.util.ui.DayView;
import udo.util.ui.Feedback;
import udo.util.ui.ToDoView;
import udo.util.ui.uDoPopup;

public class UserInterface implements ActionListener {

	private JFrame mFrame = new JFrame("uDo");
	private JLayeredPane mLayer = new JLayeredPane();
	private JPanel mTextPanel = new JPanel(new GridBagLayout());
	private JPanel mTextArea = new JPanel();
	private DayView mTodayView = new DayView();
	private ToDoView mToDoView = new ToDoView();
	private JFormattedTextField mTextField = new JFormattedTextField();
	private uDoPopup mPopup = new uDoPopup();

	private static final int MAIN_HEIGHT = 600;
	private static final int MAIN_WIDTH = 400;
	// private static final Color UDO_BG = new Color(255,244,122);
	private static final Color UDO_BG = new Color(255, 255, 255);

	private Timer mTimer;
	private Timer mExistingTimer;
	private volatile boolean mWaiting;
	private String mUserInput;

	private Feedback fb;

	public UserInterface() {


		fb = new Feedback();
		initUI();
	}

	public void initUI() {
		/**
		 * Sets up font
		 */
		try {
		     GraphicsEnvironment ge = 
		         GraphicsEnvironment.getLocalGraphicsEnvironment();
		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("COPRGTL.TTF")));
		} catch (IOException|FontFormatException e) {
		     //Handle exception
		}
		
		/**
		 * Sets up layer
		 */
		mLayer.setPreferredSize(new Dimension(MAIN_WIDTH, MAIN_HEIGHT));

		/**
		 * Sets up textArea
		 */
		mTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		mTextArea.setBackground(UDO_BG);

		/**
		 * Sets up textField
		 */
		mTextField.setColumns(20);
		mTextField.addActionListener(this);
		mTextField.setBackground(UDO_BG);
		Font newFont = mTextField.getFont().deriveFont(Font.PLAIN, 16f);
		mTextField.setFont(newFont);
		mTextField.requestFocus();

		/**
		 * Sets up textPanel
		 */
		mTextPanel.setBounds(0, 0, MAIN_WIDTH, MAIN_HEIGHT);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;

		mTextPanel.add(mTextArea, c);

		c.gridy = 1;
		c.weighty = 0;

		mTextPanel.add(mTextField, c);
		mTextPanel.setBackground(UDO_BG);

		mLayer.add(mTextPanel, new Integer(0));

		/**
		 * Sets up popup
		 */

		mLayer.add(mPopup, new Integer(2));
		

		/**
		 * Sets up ToDoView
		 */
		
		mToDoView.init();
		mToDoView.setPreferredSize(new Dimension(370,550));

		/**
		 * Sets up todayView
		 */

		mTodayView.init();
		mTodayView.setPreferredSize(new Dimension(370,550));
		
		/**
		 * Sets up the frame
		 */
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// mFrame.setUndecorated(true);
		mFrame.add(mToDoView, BorderLayout.WEST);
		mFrame.add(mLayer, BorderLayout.CENTER);
		mFrame.add(mTodayView, BorderLayout.EAST);
		mFrame.pack();
		mFrame.setLocationRelativeTo(null);
		mFrame.setVisible(true);
	}

	@Override
	/**
	 * actionPerformed when user press enter on textField.
	 * Instead of the following, it should return input to main.java
	 */
	public void actionPerformed(ActionEvent arg0) {

		String text = mTextField.getText();
		mTextField.setText("");

		mUserInput = text;
		mWaiting = false;
	}

	/**
	 * getInput takes the user input and returns it to main
	 * 
	 * @return the String userInput
	 */
	public String getInput() {
		mWaiting = true;
		while (mWaiting) {
			
		}
		return mUserInput;
	}

	/**
	 * ui.show is to show the output sent by engine
	 */
	public void show(OutputData output) {
		fb.process(output);
		String outputString = fb.getCommand();
		
		if(! (mTextArea.equals(fb.getFinalView()))) {
			mLayer.remove(mTextArea);
			mTextArea = fb.getFinalView();	
			mLayer.add(mTextArea, new Integer(1));
		}
		/**
		 * testing side views
		 *
		mToDoView.removeAll();
		mToDoView.init((ArrayList<ItemData>) output.get(Keys.ITEMS));
		mTodayView.removeAll();
		mTodayView.init((ArrayList<ItemData>) output.get(Keys.ITEMS));
		/**
		 * testing ends
		 */

		showPopup(outputString);
	}

	/**
	 * show popup as feedback to user.
	 * 
	 * @param text
	 *            it is the text to be shown to user (from FeedBack class)
	 */
	public void showPopup(String text) {

		FontMetrics fm = mPopup.getFontMetrics(mPopup.getFont());
		int padding = 5;
		int height = fm.getHeight() + padding;
		int width = fm.stringWidth(text) + padding;
		int x = MAIN_WIDTH / 2 - width / 2;
		int y = MAIN_HEIGHT - mTextField.getHeight() - height - padding;
		mPopup.setText(text);
		mPopup.setHorizontalAlignment(SwingConstants.CENTER);
		mPopup.setBounds(x, y, width, height);
		fadePopup();
	}

	public void fadePopup() {
		if (mExistingTimer != null)
			mExistingTimer.stop();
		mTimer = new Timer(10, new ActionListener() {
			int fade = -1;

			@Override
			public void actionPerformed(ActionEvent e) {
				float alpha = mPopup.getAlpha();
				if (fade < 0) {
					alpha += 0.05f;
					if (alpha < 1) {
						mPopup.setAlpha(alpha);
					} else {
						fade++;
					}
				} else if (fade == 0) {
					mTimer.setDelay(1500);
					fade++;
				} else {
					mTimer.setDelay(10);
					alpha -= 0.05f;
					if (alpha > 0) {
						mPopup.setAlpha(alpha);
					} else {
						mExistingTimer = null;
						mTimer.stop();
					}
				}
			}

		});
		mExistingTimer = mTimer;
		mTimer.start();

	}

	/**
	 * The following main method is to test and see the UI. It will be deleted/
	 * commented out when the uDo main method is run.
	 */

	/*public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UserInterface newUI = new UserInterface();
			}
		});
	}*/

}
