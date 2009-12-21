package net.srcz.android.screencast.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.srcz.android.screencast.api.injector.ConstEvtKey;
import net.srcz.android.screencast.api.injector.ConstEvtMotion;
import net.srcz.android.screencast.api.injector.Injector;
import net.srcz.android.screencast.api.injector.KeyCodeConverter;
import net.srcz.android.screencast.api.injector.ScreenCaptureThread.ScreenCaptureListener;
import net.srcz.android.screencast.ui.explorer.JFrameExplorer;

import com.android.ddmlib.IDevice;

public class JFrameMain extends JFrame {

	private JPanelScreen jp = new JPanelScreen();
	private JToolBar jtb = new JToolBar();
	private JToolBar jtbHardkeys = new JToolBar();
	private JToggleButton jtbRecord = new JToggleButton("Record");
	private JButton jbExplorer = new JButton("Explore");
	private JButton jbKbHome = new JButton("Home");
	private JButton jbKbMenu = new JButton("Menu");
	private JButton jbKbBack = new JButton("Back");
	private JButton jbKbSearch = new JButton("Search");
	private JButton jbKbPhoneOn = new JButton("Call");
	private JButton jbKbPhoneOff = new JButton("End call");

	public class KbActionListener implements ActionListener {

		int key;
		
		public KbActionListener(int key) {
			this.key = key;
		}
		
		public void actionPerformed(ActionEvent e) {
			if(injector == null)
				return;
			try {
				injector.injectKeycode(ConstEvtKey.ACTION_DOWN,key);
				injector.injectKeycode(ConstEvtKey.ACTION_UP,key);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
		
	}
	
	private IDevice device;
	private Injector injector;
	
	public void setInjector(Injector injector) {
		this.injector = injector;
		injector.screencapture.setListener(new ScreenCaptureListener() {
			
			public void handleNewImage(Dimension size, BufferedImage image,
					boolean landscape) {
				jp.handleNewImage(size, image, landscape);
			}
		});
	}

	public JFrameMain(IDevice device) throws IOException {
		this.device = device;
		initialize();
	}
	
	public void initialize() throws IOException {
		jtb.setFocusable(false);
		jbExplorer.setFocusable(false);
		jtbRecord.setFocusable(false);
		
		jbKbHome.addActionListener(new KbActionListener(ConstEvtKey.KEYCODE_HOME));
		jbKbMenu.addActionListener(new KbActionListener(ConstEvtKey.KEYCODE_MENU));
		jbKbBack.addActionListener(new KbActionListener(ConstEvtKey.KEYCODE_BACK));
		jbKbSearch.addActionListener(new KbActionListener(ConstEvtKey.KEYCODE_SEARCH));
		jbKbPhoneOn.addActionListener(new KbActionListener(ConstEvtKey.KEYCODE_CALL));
		jbKbPhoneOff.addActionListener(new KbActionListener(ConstEvtKey.KEYCODE_ENDCALL));

		jtbHardkeys.add(jbKbHome);
		jtbHardkeys.add(jbKbMenu);
		jtbHardkeys.add(jbKbBack);
		jtbHardkeys.add(jbKbSearch);
		jtbHardkeys.add(jbKbPhoneOn);
		jtbHardkeys.add(jbKbPhoneOff);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
		setDefaultCloseOperation(3);
		setLayout(new BorderLayout());
		add(jtb,BorderLayout.NORTH);
		add(jtbHardkeys,BorderLayout.SOUTH);
		JScrollPane jsp = new JScrollPane(jp);
		add(jsp,BorderLayout.CENTER);
		jsp.setPreferredSize(new Dimension(336*1, 512*1));
		pack();
		setLocationRelativeTo(null);
		jp.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(injector == null)
					return;
				try {
					int code = KeyCodeConverter.getKeyCode(e);
					injector.injectKeycode(ConstEvtKey.ACTION_DOWN,code);
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if(injector == null)
					return;
				try {
					int code = KeyCodeConverter.getKeyCode(e);
					injector.injectKeycode(ConstEvtKey.ACTION_UP,code);
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
			
			
		});
		
		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent arg0) {
				if(injector == null)
					return;
				try {
					Point p2 = jp.getRawPoint(arg0.getPoint());
					injector.injectMouse(ConstEvtMotion.ACTION_MOVE, p2.x, p2.y);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				if(injector == null)
					return;
				try {
					Point p2 = jp.getRawPoint(arg0.getPoint());
					injector.injectMouse(ConstEvtMotion.ACTION_DOWN, p2.x, p2.y);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(injector == null)
					return;
				try {
					if(arg0.getButton() == MouseEvent.BUTTON3) {
						injector.screencapture.toogleOrientation();
						arg0.consume();
						return;
					}
					Point p2 = jp.getRawPoint(arg0.getPoint());
					injector.injectMouse(ConstEvtMotion.ACTION_UP, p2.x, p2.y);
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				if(injector == null)
					return;
				try {
					//injector.injectKeycode(ConstEvtKey.ACTION_DOWN,code);
					//injector.injectKeycode(ConstEvtKey.ACTION_UP,code);
					injector.injectTrackball(arg0.getWheelRotation() < 0 ? -1f : 1f);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		jp.addMouseMotionListener(ma);
		jp.addMouseListener(ma);
		jp.addMouseWheelListener(ma);
		
		jtbRecord.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if(jtbRecord.isSelected()) {
					startRecording();
				} else {
					stopRecording();
				}
			}
			
		});
		jtb.add(jtbRecord);
		
		jbExplorer.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				JFrameExplorer jf = new JFrameExplorer(device);
				jf.setIconImage(getIconImage());
				jf.setVisible(true);
			}
		});
		jtb.add(jbExplorer);
		
	}
	
	private void startRecording() {
		JFileChooser jFileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Vidéo file","mov");
		jFileChooser.setFileFilter(filter);
		int returnVal = jFileChooser.showSaveDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			injector.screencapture.startRecording(jFileChooser.getSelectedFile());
		}
	}
	
	private void stopRecording() {
		injector.screencapture.stopRecording();
	}
}
