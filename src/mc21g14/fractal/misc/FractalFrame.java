package mc21g14.fractal.misc;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

/**
 * The main Fractal Frame
 * 
 * @author Matthew Consterdine
 */
public class FractalFrame extends JFrame {
	private final FractalExplorer fractalExplorer;
	private static final long serialVersionUID = 1L;

	public FractalFrame(FractalExplorer fe) {
		this.fractalExplorer = fe;
		// Create
		setTitle("Fractal Explorer");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setBackground(Colors.background);
		getContentPane().setBackground(Colors.background);
		
		// Icon
		try {
			try {
				setIconImage(ImageIO.read(getClass().getResourceAsStream("/fractal.png")));
			} catch(IllegalArgumentException f) {
				setIconImage(ImageIO.read(new File("fractal.png")));
			}
		} catch (IOException e) {
			System.err.println("Couldn't read icon, using Java default");
		}
		
		// Split pane lets us resize it nicely
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setBorder(null);
		splitPane.setResizeWeight(1);
		// Style the divider
		BasicSplitPaneDivider divider = (BasicSplitPaneDivider) splitPane.getComponent(0);
		divider.setBackground(Colors.foreground);
		divider.setBorder(null);
		
		// Side panel
		JPanel sidePanel = new JPanel(new GridLayout(2, 1, 0, fractalExplorer.padding));
		sidePanel.setBackground(Colors.background);
		sidePanel.setBorder(new EmptyBorder(fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding));
		
		JPanel topPanel = new JPanel(new BorderLayout(0, fractalExplorer.padding));
		topPanel.setBackground(Colors.background);
		
		JPanel optionsPanel = new JPanel(new GridLayout(0, 4, fractalExplorer.padding, fractalExplorer.padding));
		optionsPanel.setOpaque(false);
		
		// Buttons
		JButton favouriteFractalButton = new JButton("+ Fractal");
		JButton favouriteJuliaButton   = new JButton("+ Julia");
		JButton exportButton           = new JButton("Export");
		JButton settingsButton         = new JButton("Settings");
		
		// Change the colors on hover listener
		MouseAdapter buttonListener = new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) {
				((JButton) e.getSource()).setBackground(Colors.highlight);
				((JButton) e.getSource()).setForeground(Colors.foreground);
			}
			@Override public void mouseExited (MouseEvent e) {
				((JButton) e.getSource()).setBackground(Colors.foreground);
				((JButton) e.getSource()).setForeground(Colors.background);
			}
		};
		
		// Style all the buttons at once
		for(JButton button : new JButton[]{
			favouriteFractalButton, favouriteJuliaButton, exportButton, settingsButton
		}) {
			button.setBackground(Colors.foreground);
			button.setContentAreaFilled(false);
			button.setOpaque(true);
			button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			button.addMouseListener(buttonListener);
			optionsPanel.add(button);
		}
		
		topPanel.add(fractalExplorer.julia, BorderLayout.CENTER);
		topPanel.add(optionsPanel, BorderLayout.PAGE_END);
		
		// Favourites
		final JPanel favouritesPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(favouritesPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(false);
		scrollPane.validate();
		scrollPane.updateUI();
		scrollPane.setBorder(null);
		favouritesPanel.setOpaque(false);
		favouritesPanel.add(fractalExplorer.createFavoritesPanel());
		
		sidePanel.add(topPanel);
		sidePanel.add(scrollPane);

		splitPane.setLeftComponent(fractalExplorer.fractal);
		splitPane.setRightComponent(sidePanel);
		add(splitPane);
		
		// Listeners
		
		// Resize the scroll pane so the left takes most of the room
		addComponentListener(new ComponentAdapter() {
			boolean firstResize = true;
			
			@Override
			public void componentResized(ComponentEvent e) {
				if(firstResize) {
					splitPane.setDividerLocation(getWidth() - 400);
					firstResize = false;
				}
			}
		});
		
		// Favourite
		favouriteFractalButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalState temp = FractalFrame.this.fractalExplorer.fractal.getState();
				temp.coloring = temp.coloring.clone();
				FractalFrame.this.fractalExplorer.favourites.add(temp);
				favouritesPanel.removeAll();
				favouritesPanel.add(FractalFrame.this.fractalExplorer.createFavoritesPanel());
				favouritesPanel.updateUI();
			}
		});
		
		favouriteJuliaButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalState temp = FractalFrame.this.fractalExplorer.julia.getState();
				temp.coloring = temp.coloring.clone();
				FractalFrame.this.fractalExplorer.favourites.add(temp);
				favouritesPanel.removeAll();
				favouritesPanel.add(FractalFrame.this.fractalExplorer.createFavoritesPanel());
				favouritesPanel.updateUI();
			}
		});
		
		// Save
		exportButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {FractalFrame.this.fractalExplorer.saveImage(FractalFrame.this.fractalExplorer.fractal, true);}});
		

		// Toggle settings Frame
		settingsButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalFrame.this.fractalExplorer.settingsFrame.setVisible(!FractalFrame.this.fractalExplorer.settingsFrame.isVisible());
				//centerFrames();
		}});
	}
}