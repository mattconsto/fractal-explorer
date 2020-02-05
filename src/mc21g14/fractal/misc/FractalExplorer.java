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

import uk.co.camick.wraplayout.WrapLayout;
import mc21g14.fractal.calculable.*;

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
	protected FractalSettingsFrame settingsFrame;
	
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
									"Try allocating more memory to java using '-Xmx???m' where ??? is the ammount of memory in MB.",
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
				julia.state        = fractal.state.clone();
				julia.state.seed   = fractal.getSelected();
				julia.state.start  = start;
				julia.state.end    = end;
				julia.state.top    = top;
				julia.state.bottom = bottom;
				julia.regenerate();
			}
		});
		
		// Load our favourites
		favourites = loadFavourites(file);
		
		// Create our frames
		fractalFrame  = new FractalFrame(this);
		settingsFrame = new FractalSettingsFrame(this);
		
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
}
