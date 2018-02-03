package mc21g14.fractal.misc;

import java.awt.*;
import java.awt.Window.Type;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import uk.co.camick.wraplayout.WrapLayout;
import mc21g14.fractal.calculable.*;
import mc21g14.fractal.colorable.*;

/**
 * The Fractal Explorer GUI
 * 
 * @author Matthew Consterdine
 */
public class FractalExplorer {
	protected int  padding = 10;
	protected File file    = new File("favourites.list");

	protected Fractal       fractal;
	protected Fractal       julia;
	protected FractalFrame  fractalFrame;
	protected SettingsFrame settingsFrame;
	
	protected ArrayList<FractalState> favourites;
	
	protected boolean liveJuliaUpdates = false;
	
	/**
	 * Load favourites from file
	 * @param file The file
	 * @return List of favourites
	 */
	@SuppressWarnings("unchecked")
	protected ArrayList<FractalState> loadFavourites(File file) {
		ArrayList<FractalState> favourites = new ArrayList<FractalState>();
		
		// Object(Input/Output)Stream is great and so easy to use
		try {
			FileInputStream stream = new FileInputStream(file);
			favourites = (ArrayList<FractalState>) new ObjectInputStream(
				stream).readObject(); // Unchecked cast
			stream.close();
		} catch(IOException | ClassNotFoundException e){
			System.err.println("Error loading favourites");
		}
		
		return favourites;
	}

	/**
	 * Save favourites to file
	 * @param file The file
	 */
	protected void saveFavourites(File file) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			new ObjectOutputStream(stream).writeObject(favourites);
			stream.close();
		} catch(IOException e){
			System.err.println("Error saving favourites");
		}
	}
	
	/**
	 * Create the favourites panel
	 * @return A JPanel
	 */
	protected JPanel createFavoritesPanel() {
		final JPanel panel = new JPanel(new WrapLayout(WrapLayout.CENTER, padding, padding));
		panel.setBackground(Colors.foreground);
		
		for(final FractalState fav : favourites) {
			// Create a fractal
			final Fractal temp = new Fractal(fav);
			temp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			// Set the listeners we want
			temp.removeMouseListener(temp);
			temp.removeMouseMotionListener(temp);
			temp.removeMouseWheelListener(temp);
			temp.removeKeyListener(temp);
			temp.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1) {
						// Left click, load the fractal
						fractal.setState(temp.getState());
						fractal.setColoring(fractal.getColoring().clone());
						settingsFrame.updateColorSettings();
						fractal.getColoring().removeAllListeners();
						fractal.getColoring().addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								fractal.repaint();
							}
						});
					} else if(e.getButton() == MouseEvent.BUTTON3) {
						// Right click, delete the fractal
						panel.remove(temp);
						panel.updateUI();
						favourites.remove(fav);
					}
				};
			});
			panel.add(temp);
		}
		
		// It is done
		panel.updateUI();
		return panel;
	}
	
	/**
	 * Save the fractal to an image
	 * @param fractal The fractal
	 * @param open Do we want to open the image after saving
	 */
	protected void saveImage(final Fractal fractal, final boolean open) {
		// Create a GUI that asks for the size, default to 1080p 16:9
		final JFrame sizeFrame = new JFrame("Pick a size");
		sizeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		sizeFrame.setResizable(false);
		sizeFrame.setType(Type.UTILITY);
		sizeFrame.setAlwaysOnTop(true);
		JPanel sizePanel = new JPanel(new GridLayout(3, 2, padding, padding));
		sizePanel.setBorder(new EmptyBorder(padding, padding, padding, padding));
		
		// Buttons
		final JTextField sizeX  = new JTextField("4096");
		final JTextField sizeY  = new JTextField("2160");
		JButton    cancel = new JButton("Cancel");
		JButton    ok     = new JButton("Ok");
		
		// Select field contents on select
		sizeX.addFocusListener(new FocusAdapter() {@Override public void focusGained(FocusEvent e) {
			sizeX.selectAll();
		}});
		sizeY.addFocusListener(new FocusAdapter() {@Override public void focusGained(FocusEvent e) {
			sizeY.selectAll();
		}});
		
		// Close the frame on cancel
		cancel.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				sizeFrame.setVisible(false);
				sizeFrame.dispose();
			}
		});
		
		// Save image listener
		ActionListener listener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				sizeFrame.setVisible(false);
				
				// Ask the user where they want to store the image
				final JFileChooser exportChooser = new JFileChooser();
				exportChooser.setAcceptAllFileFilterUsed(false);
				exportChooser.setFileFilter(
					new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes())
				);
				//exportChooser.setCurrentDirectory(new File("./exported"));
				
				// Only want to save if the user says yes
				if(exportChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					new Thread(new Runnable() {
						@Override public void run() {
							try {
								// Get the chosen size
								Dimension size;
								try {
									size = new Dimension(
										Integer.parseInt(sizeX.getText()),
										Integer.parseInt(sizeY.getText())
									);
								} catch (NumberFormatException ex) {
									size = fractal.getSize();
								}
							
								// Storage
								BufferedImage image = new BufferedImage(
									(int) size.getWidth(),
									(int) size.getHeight(),
									BufferedImage.TYPE_INT_RGB
								);
								
								// Paint the image
								Fractal temp = new Fractal(fractal.getState());
								temp.setSize(size);
								temp.regenerate();
								temp.paintComponent(image.createGraphics());
								
								// Need to ensure that the file we are saving has an extension.
								String file = exportChooser.getSelectedFile().getPath();
								if(!file.contains(".")) file += ".png";
								
								try {
									ImageIO.write(
										image,
										file.substring(file.lastIndexOf(".") + 1),
										new File(file)
									);
									// Open the saved image or display a popup.
									if(open) {
										Desktop.getDesktop().open(new File(file));
									} else {
										JOptionPane.showMessageDialog(
											null,
											"Image saved to " + file,
											"Saved",
											JOptionPane.INFORMATION_MESSAGE
										);
									}
								} catch (IOException ioe) {
									JOptionPane.showMessageDialog(
										null,
										"Error writing image to " + file,
										"Error",
										JOptionPane.ERROR_MESSAGE
									);
								}
							} catch (OutOfMemoryError e) {
								JOptionPane.showMessageDialog(
									null,
									"Try allocating more memory to java using '-Xmx????m' where ???? is the ammount of memory in MB.",
									"Out of memory",
									JOptionPane.ERROR_MESSAGE
								);
							}
						}
					}).start();
				}
				
				// sizeFrame has solved it's purpose
				sizeFrame.dispose();
			}
		};
		
		// Add listeners
		sizeX.addActionListener(listener);
		sizeY.addActionListener(listener);
		ok.addActionListener(listener);
		
		// Add components
		sizePanel.add(new JLabel("Width"));
		sizePanel.add(sizeX);
		sizePanel.add(new JLabel("Height"));
		sizePanel.add(sizeY);
		sizePanel.add(cancel);
		sizePanel.add(ok);
		
		// Finish it
		sizeFrame.add(sizePanel);
		sizeFrame.pack();
		sizeFrame.setLocationRelativeTo(null);
		sizeFrame.setVisible(true);
		
		// We want sizeX to be selected initially
		sizeX.requestFocus();
	}
	
	public void centerFrames() {
		// Position it all nicely. Need to take into account system insets
		Insets in = Toolkit.getDefaultToolkit().getScreenInsets(fractalFrame.getGraphicsConfiguration());
		Dimension all = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension use = new Dimension(
			(int) (all.getWidth()  - in.left - in.right),
			(int) (all.getHeight() - in.top  - in.bottom)
		);
		
		settingsFrame.pack();
		settingsFrame.setLocation(in.left +  use.width  - settingsFrame.getWidth() - padding,
		                          in.top  + (use.height - settingsFrame.getHeight()) / 2);
		
		// Only want to take the width of the settingsFrame into account if it's visible
		if(settingsFrame.isVisible()) {
			fractalFrame.setSize(use.width - settingsFrame.getWidth() - padding * 3, settingsFrame.getHeight());
			fractalFrame.setLocation(in.left + padding, in.top + (use.height - fractalFrame.getHeight()) / 2);
		} else {
			fractalFrame.setSize(use.width - padding * 2, settingsFrame.getHeight());
			fractalFrame.setLocation(in.left + padding, in.top  + (use.height - fractalFrame.getHeight()) / 2);
		}
	}
	
	public FractalExplorer() {
		// Pick our backend
		try {
			Calculable.set(OpenCLCalculator.class);
		} catch (Exception oh) {
			oh.printStackTrace();
			System.err.println("GPU unavaliable, falling back to a slower CPU implementation");
			try {
				Calculable.set(JavaCalculator.class);
			} catch (Exception ohno) {
				System.err.println("Oh dear, that didn't work. May be unable to recover from this");
				ohno.printStackTrace();
			}
		}
		
		// Setup our fractals
		fractal = new Fractal();
		fractal.setForeground(Colors.foreground);
		fractal.setBackground(Colors.background);
		fractal.selected = new Complex(0, 0);

		julia = new Fractal();
		julia.setForeground(Colors.foreground);
		julia.setBackground(Colors.background);
		julia.setSeed(new Complex(0, 0));
		julia.setColoring(fractal.getColoring());

		// Listeners
		fractal.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point position = fractal.getMousePosition();
				
				// Only want to change once, without the ready check we will call this a lot.
				if(position != null && liveJuliaUpdates) {
					julia.setSeed(Fractal.getComplexFromPoint(position, fractal));
					julia.setInverse(fractal.isInverse());
				}
			}
		});
		
		fractal.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if(liveJuliaUpdates)
					julia.setSeed(fractal.getSelected());
			}
		});
		
		fractal.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.out.println("Julia for " + fractal.getSelected().toString());
				// Clone the fractal state, preserving a few important fields
				double  start      = julia.getStart();
				double  end        = julia.getEnd();
				double  top        = julia.getTop();
				double  bottom     = julia.getBottom();
				julia.state           = julia.state.setSeed(fractal.getSelected());
				julia.location.start  = start;
				julia.location.end    = end;
				julia.location.top    = top;
				julia.location.bottom = bottom;
				julia.regenerate();
			}
		});
		
		// Load our favourites
		favourites = loadFavourites(file);
		
		// Create our frames
		fractalFrame  = new FractalFrame();
		settingsFrame = new SettingsFrame();
		
		// Close settings on close
		fractalFrame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				saveFavourites(file);
				settingsFrame.dispose();
			}
		});
		
		// Show
		settingsFrame.setVisible(true);
		fractalFrame.setVisible(true);
		
		centerFrames();
	}
	
	public class FractalFrame extends JFrame {
		public FractalFrame() {
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
			JPanel sidePanel = new JPanel(new GridLayout(2, 1, 0, padding));
			sidePanel.setBackground(Colors.background);
			sidePanel.setBorder(new EmptyBorder(padding, padding, padding, padding));
			
			JPanel topPanel = new JPanel(new BorderLayout(0, padding));
			topPanel.setBackground(Colors.background);
			
			JPanel optionsPanel = new JPanel(new GridLayout(0, 4, padding, padding));
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
			
			topPanel.add(julia, BorderLayout.CENTER);
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
			favouritesPanel.add(createFavoritesPanel());
			
			sidePanel.add(topPanel);
			sidePanel.add(scrollPane);

			splitPane.setLeftComponent(fractal);
			splitPane.setRightComponent(sidePanel);
			add(splitPane);
			
			// Listeners
			
			// Resize the scrollpane so the left takes most of the room
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
			
			// Favorite
			favouriteFractalButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					favourites.add(fractal.getState().setColoring(fractal.getState().coloring.clone()));
					favouritesPanel.removeAll();
					favouritesPanel.add(createFavoritesPanel());
					favouritesPanel.updateUI();
				}
			});
			
			favouriteJuliaButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					favourites.add(fractal.getState().setColoring(fractal.getState().coloring.clone()));
					favouritesPanel.removeAll();
					favouritesPanel.add(createFavoritesPanel());
					favouritesPanel.updateUI();
				}
			});
			
			// Save
			exportButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {saveImage(fractal, true);}});
			

			// Toggle settings Frame
			settingsButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					settingsFrame.setVisible(!settingsFrame.isVisible());
					//centerFrames();
			}});
		}
	}
	
	public class SettingsFrame extends JFrame {
		protected final JPanel               coloringPanel;
		protected final JComboBox<Colorable> colorableCombo;
		
		public SettingsFrame() {
			setTitle("Settings");
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
			
			// Create the panel
			JPanel settingsPanel = new JPanel();
			settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
			settingsPanel.setBackground(Colors.background);
			settingsPanel.setForeground(Colors.foreground);
			settingsPanel.setBorder(new EmptyBorder(padding, padding, padding, padding));
			
			// We want the headings to stand out, hence this font
			Font headingFont = new JLabel().getFont().deriveFont(20f);
			
			JPanel calculatorPanel   = new JPanel(new GridLayout(1, 2));
			calculatorPanel.setBorder(new EmptyBorder(padding, padding, padding, padding));
			calculatorPanel.setBackground(Colors.foreground);
			ButtonGroup  calculator  = new ButtonGroup();
			final JRadioButton javaRadio   = new JRadioButton("CPU", Calculable.get() == JavaCalculator.class);
			final JRadioButton openclRadio = new JRadioButton("GPU", Calculable.get() == OpenCLCalculator.class);
			javaRadio.setToolTipText("Usually slower than using the GPU, especially on a desktop");
			openclRadio.setHorizontalAlignment(JRadioButton.RIGHT);
			openclRadio.setHorizontalTextPosition(JLabel.LEFT);
			javaRadio.setOpaque(false);
			openclRadio.setOpaque(false);
			
			calculator.add(javaRadio);
			calculator.add(openclRadio);
			
			calculatorPanel.add(javaRadio);
			calculatorPanel.add(openclRadio);
			
			// Fill the list of fractals
			final JComboBox<String> fractalsCombo = new JComboBox<String>();
			fractalsCombo.setBorder(new EmptyBorder(5, 5, 5, 5));
			for(String s : Calculable.getImplemented()) fractalsCombo.addItem(s);
			
			// Fill the list of colorings
			colorableCombo = new JComboBox<Colorable>();
			colorableCombo.setBackground(Colors.foreground);
			colorableCombo.setBorder(new EmptyBorder(5, 5, 5, 5));
			for(Colorable c : Colorable.getImplementations()) colorableCombo.addItem(c);
			colorableCombo.setSelectedIndex(Math.min(3, Colorable.getImplementations().length));
			
			// Long settings list
			JPanel           gridSettings               = new JPanel(new GridLayout(0, 2, 50, 2));
			final JTextField fractalSettingStart        = new JTextField(String.format("%.5f", fractal.getStart()));
			final JTextField fractalSettingEnd          = new JTextField(String.format("%.5f", fractal.getEnd()));
			final JTextField fractalSettingTop          = new JTextField(String.format("%.5f", fractal.getTop()));
			final JTextField fractalSettingBottom       = new JTextField(String.format("%.5f", fractal.getBottom()));
			final JTextField fractalSettingIterations   = new JTextField(Integer.toString(fractal.getIterations()));
			final JTextField fractalSettingThreshold    = new JTextField(String.format("%.5f", fractal.getThreshold()));
			final JTextField fractalSettingOrder        = new JTextField(Integer.toString(fractal.getOrder()));
			final JCheckBox  fractalSettingInverse      = new JCheckBox("", fractal.isInverse());
			final JCheckBox  fractalSettingBuddha       = new JCheckBox("", fractal.isBuddha());
			final JCheckBox  fractalSettingSmooth       = new JCheckBox("", fractal.isSmooth());
			final JCheckBox  fractalSettingInvert       = new JCheckBox("", fractal.isInvert());
			final JCheckBox  liveJulia                  = new JCheckBox("", liveJuliaUpdates);
			
			final JComboBox<String> fractalSettingOrbitTraps  = new JComboBox<String>();
			final JComboBox<String> fractalSettingRegionSplit = new JComboBox<String>();
			
			for(String s : new String[] {"None", "Cross", "Dots"}) fractalSettingOrbitTraps.addItem(s);
			for(String s : new String[] {"None", "Iterations", "Axis"}) fractalSettingRegionSplit.addItem(s);
			
			fractalSettingOrbitTraps.setSelectedItem(fractal.getOrbitTraps());
			fractalSettingRegionSplit.setSelectedItem(fractal.getRegionSplits());

			fractalSettingOrbitTraps.setEnabled(!fractalSettingBuddha.isSelected());
			fractalSettingRegionSplit.setEnabled(fractalSettingOrbitTraps.getSelectedIndex() != 0 && !fractalSettingBuddha.isSelected());

			JLabel fractalSettingInverseLabel = new JLabel("Inverse Base");
			fractalSettingInverseLabel.setToolTipText("Inverse the inital seed/base value to prduce an inverse image.");
			JLabel fractalSettingBuddhaLabel  = new JLabel("Buddha");
			fractalSettingBuddhaLabel.setToolTipText("Due to how this is calculated, zooming won't work as you imagine.");
			JLabel fractalSettingSmoothLabel  = new JLabel("Smoothing");
			fractalSettingSmoothLabel.setToolTipText("We pretend the difference between two iterations is linear to produce a pretty image.");
			
			gridSettings.setBorder(new EmptyBorder(padding, padding, padding, padding));
			gridSettings.setBackground(Colors.foreground);
			
			// Add everything at once
			gridSettings.add(new JLabel("Fractal Start"));
			gridSettings.add(fractalSettingStart);
			gridSettings.add(new JLabel("Fractal End"));
			gridSettings.add(fractalSettingEnd);
			gridSettings.add(new JLabel("Fractal Top"));
			gridSettings.add(fractalSettingTop);
			gridSettings.add(new JLabel("Fractal Bottom"));
			gridSettings.add(fractalSettingBottom);
			gridSettings.add(new JLabel("Iterations"));
			gridSettings.add(fractalSettingIterations);
			gridSettings.add(new JLabel("Cutoff Threshold"));
			gridSettings.add(fractalSettingThreshold);
			gridSettings.add(new JLabel("Order"));
			gridSettings.add(fractalSettingOrder);
			gridSettings.add(new JLabel("Orbit Traps"));
			gridSettings.add(fractalSettingOrbitTraps);
			gridSettings.add(new JLabel("Region Splits"));
			gridSettings.add(fractalSettingRegionSplit);
			gridSettings.add(fractalSettingInverseLabel);
			gridSettings.add(fractalSettingInverse);
			gridSettings.add(fractalSettingBuddhaLabel);
			gridSettings.add(fractalSettingBuddha);
			gridSettings.add(fractalSettingSmoothLabel);
			gridSettings.add(fractalSettingSmooth);
			gridSettings.add(new JLabel("Invert Image"));
			gridSettings.add(fractalSettingInvert);
			gridSettings.add(new JLabel("Live Julia"));
			gridSettings.add(liveJulia);

			// Style our long menu
			for(Component c : gridSettings.getComponents()) {
				JComponent jc = (JComponent) c;
				if(jc instanceof JCheckBox) {
					jc.setOpaque(false);
					((JCheckBox)  jc).setHorizontalAlignment(JCheckBox.RIGHT);
				}
				if(jc instanceof JTextField) {
					((JTextField) jc).setHorizontalAlignment(JTextField.RIGHT);
				} else {
					jc.setBackground(Colors.foreground);
				}
			}

			coloringPanel = new JPanel(new BorderLayout());
			coloringPanel.setBorder(new EmptyBorder(padding, padding, padding, padding));
			coloringPanel.setBackground(Colors.foreground);
			if(fractal.getColoring() instanceof UserConfigurable)
				coloringPanel.add(((UserConfigurable) fractal.getColoring()).getSettingsPanel());
			
			JPanel fractalSettingsPanel = new JPanel(new BorderLayout());
			fractalSettingsPanel.setOpaque(false);
			JLabel fractalSettingsLabel = new JLabel("Fractal Settings");
			fractalSettingsLabel.setFont(headingFont);
			fractalSettingsLabel.setForeground(Colors.foreground);
			fractalSettingsPanel.add(fractalSettingsLabel);
			
			JPanel coloringSettingsPanel = new JPanel(new BorderLayout());
			coloringSettingsPanel.setOpaque(false);
			JLabel coloringSettingsLabel = new JLabel("Coloring Settings");
			coloringSettingsLabel.setFont(headingFont);
			coloringSettingsLabel.setForeground(Colors.foreground);
			coloringSettingsPanel.add(coloringSettingsLabel);
			
			JPanel aboutPanel = new JPanel(new BorderLayout());
			aboutPanel.setOpaque(false);
			JLabel aboutLabel = new JLabel("Created by Matthew Consterdine");
			aboutLabel.setForeground(Colors.foreground);
			JLabel userLabel = new JLabel("mc21g14");
			userLabel.setForeground(Colors.foreground);
			aboutPanel.add(aboutLabel, BorderLayout.CENTER);
			aboutPanel.add(userLabel, BorderLayout.LINE_END);
			
			// Add everything to our frame
			settingsPanel.add(fractalSettingsPanel);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(calculatorPanel);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(fractalsCombo);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(gridSettings);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(coloringSettingsPanel);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(colorableCombo);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(coloringPanel);
			settingsPanel.add(Box.createVerticalStrut(padding));
			settingsPanel.add(aboutPanel);
			
			add(settingsPanel);
			
			// Listeners
			
			// Calculators
			javaRadio.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						Calculable.set(JavaCalculator.class);
					} catch (Exception e1) {
						System.err.println("Error setting Java Calculator");
					}
					
					// Update fractals list
					fractalsCombo.removeAllItems();
					for(String s : Calculable.getImplemented()) fractalsCombo.addItem(s);
					fractalsCombo.setSelectedItem(fractal.getFractal());
					
					// Regenerate fractals
					fractal.regenerate();
					julia.regenerate();
				}
			});
			openclRadio.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						Calculable.set(OpenCLCalculator.class);
					} catch (Exception oh) {
						oh.printStackTrace();
						System.err.println("GPU unavaliable, falling back to CPU implementation");
						try {
							Calculable.set(JavaCalculator.class);
						} catch (Exception ohno) {
							System.err.println("That didn't work. May be unable to recover");
							ohno.printStackTrace();
						}
						
						// Disable radios so this can't happen again
						javaRadio.setSelected(true);
						javaRadio.setEnabled(false);
						openclRadio.setEnabled(false);
					}
					
					// Update fractals list
					fractalsCombo.removeAllItems();
					for(String s : Calculable.getImplemented()) fractalsCombo.addItem(s);
					fractalsCombo.setSelectedItem(fractal.getFractal());
					
					// Regenerate fractals
					fractal.regenerate();
					julia.regenerate();
				}
			});
			
			fractalsCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fractal.setFractal((String) fractalsCombo.getSelectedItem());
				}
			});
			
			// Color picker
			colorableCombo.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractal.setColoring((Colorable) colorableCombo.getSelectedItem());
					julia.setColoring((Colorable) colorableCombo.getSelectedItem());
					
					// Update color panel
					coloringPanel.removeAll();
					if(fractal.getColoring() instanceof UserConfigurable) {
						coloringPanel.add(
							((UserConfigurable) fractal.getColoring()).getSettingsPanel()
						);
					}
					coloringPanel.updateUI();
					//centerFrames();
				}
			});
			
			// Add listerns to update the fractal
			fractalSettingStart.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setStart(Double.parseDouble(fractalSettingStart.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingEnd.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setEnd(Double.parseDouble(fractalSettingEnd.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingTop.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setTop(Double.parseDouble(fractalSettingTop.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingBottom.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setBottom(Double.parseDouble(fractalSettingBottom.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingIterations.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setIterations(Integer.parseInt(fractalSettingIterations.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingThreshold.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setThreshold(Double.parseDouble(fractalSettingThreshold.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingOrder.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						fractal.setOrder(Integer.parseInt(fractalSettingOrder.getText()));
					} catch (NumberFormatException ignored) {}
				}
			});
			fractalSettingInverse.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractal.setInverse(fractalSettingInverse.isSelected());
				}
			});
			fractalSettingSmooth.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractal.setSmooth(fractalSettingSmooth.isSelected());
				}
			});
			fractalSettingInvert.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractal.setInvert(fractalSettingInvert.isSelected());
				}
			});
			fractalSettingBuddha.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractal.setBuddha(fractalSettingBuddha.isSelected());
					fractalSettingOrbitTraps.setEnabled(!fractalSettingBuddha.isSelected());
					fractalSettingRegionSplit.setEnabled(
						fractalSettingOrbitTraps.getSelectedIndex() != 0 &&
						!fractalSettingBuddha.isSelected()
					);
				}
			});
			fractalSettingOrbitTraps.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractalSettingRegionSplit.setEnabled(
						fractalSettingOrbitTraps.getSelectedIndex() != 0
					);
					fractalSettingBuddha.setEnabled(fractal.getOrbitTraps().equals("None"));
					if(!fractal.getOrbitTraps().equals(
						(String) fractalSettingOrbitTraps.getSelectedItem())
					) {
						fractal.setOrbitTraps((String) fractalSettingOrbitTraps.getSelectedItem());
					}
				}
			});
			
			fractalSettingRegionSplit.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractalSettingBuddha.setEnabled(fractal.getOrbitTraps().equals("None"));
					if(!fractal.getRegionSplits().equals(
						(String) fractalSettingRegionSplit.getSelectedItem())
					) {
						fractal.setRegionSplits((String) fractalSettingRegionSplit.getSelectedItem());
					}
				}
			});
			
			// And to update the settings menu
			fractal.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					fractalSettingStart.setText(String.format("%.5f", fractal.getStart()));
					fractalSettingEnd.setText(String.format("%.5f", fractal.getEnd()));
					fractalSettingTop.setText(String.format("%.5f", fractal.getTop()));
					fractalSettingBottom.setText(String.format("%.5f", fractal.getBottom()));
					fractalSettingIterations.setText(Integer.toString(fractal.getIterations()));
					fractalSettingThreshold.setText(String.format("%.5f", fractal.getThreshold()));
					fractalSettingOrder.setText(Integer.toString(fractal.getOrder()));
					fractalSettingBuddha.setSelected(fractal.isBuddha());
					fractalSettingBuddha.setEnabled(fractal.getOrbitTraps().equals("None"));
					fractalSettingInverse.setSelected(fractal.isInverse());
					fractalSettingSmooth.setSelected(fractal.isSmooth());
					fractalSettingInvert.setSelected(fractal.isInvert());
					fractalSettingOrbitTraps.setSelectedItem(fractal.getOrbitTraps());
					fractalSettingRegionSplit.setSelectedItem(fractal.getRegionSplits());
					fractalSettingOrbitTraps.setEnabled(!fractalSettingBuddha.isSelected());
					fractalSettingRegionSplit.setEnabled(
						fractalSettingOrbitTraps.getSelectedIndex() != 0 &&
						!fractalSettingBuddha.isSelected()
					);
				}
			});
			
			liveJulia.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					liveJuliaUpdates = liveJulia.isSelected();
				}
			});
		}
	
		/**
		 * Update the color settings panel
		 */
		public void updateColorSettings() {
			coloringPanel.removeAll();
			if(fractal.getColoring() instanceof UserConfigurable)
				coloringPanel.add(((UserConfigurable) fractal.getColoring()).getSettingsPanel());
			colorableCombo.setSelectedItem(fractal.getColoring());
			coloringPanel.updateUI();
			//centerFrames();
		}
	}
}
