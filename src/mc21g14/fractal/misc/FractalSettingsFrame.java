package mc21g14.fractal.misc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import mc21g14.fractal.calculable.Calculable;
import mc21g14.fractal.calculable.JavaCalculator;
import mc21g14.fractal.calculable.OpenCLCalculator;
import mc21g14.fractal.colorable.Colorable;

/**
 * The Settings Frame
 * 
 * @author Matthew Consterdine
 */
public class FractalSettingsFrame extends JFrame {
	private final FractalExplorer fractalExplorer;

	private static final long serialVersionUID = 1L;

	protected final JPanel               coloringPanel;
	protected final JComboBox<Colorable> colorableCombo;
	
	public FractalSettingsFrame(FractalExplorer fe) {
		this.fractalExplorer = fe;
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
		settingsPanel.setBorder(new EmptyBorder(fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding));
		
		// We want the headings to stand out, hence this font
		Font headingFont = new JLabel().getFont().deriveFont(20f);
		
		JPanel calculatorPanel   = new JPanel(new GridLayout(1, 2));
		calculatorPanel.setBorder(new EmptyBorder(fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding));
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
		final JTextField fractalSettingStart        = new JTextField(String.format("%.7f", fractalExplorer.fractal.getStart()));
		final JTextField fractalSettingEnd          = new JTextField(String.format("%.7f", fractalExplorer.fractal.getEnd()));
		final JTextField fractalSettingTop          = new JTextField(String.format("%.7f", fractalExplorer.fractal.getTop()));
		final JTextField fractalSettingBottom       = new JTextField(String.format("%.7f", fractalExplorer.fractal.getBottom()));
		final JTextField fractalSettingIterations   = new JTextField(Integer.toString(fractalExplorer.fractal.getIterations()));
		final JTextField fractalSettingThreshold    = new JTextField(String.format("%.7f", fractalExplorer.fractal.getThreshold()));
		final JTextField fractalSettingOrder        = new JTextField(Integer.toString(fractalExplorer.fractal.getOrder()));
		final JCheckBox  fractalSettingInverse      = new JCheckBox("", fractalExplorer.fractal.isInverse());
		final JCheckBox  fractalSettingBuddha       = new JCheckBox("", fractalExplorer.fractal.isBuddha());
		final JCheckBox  fractalSettingSmooth       = new JCheckBox("", fractalExplorer.fractal.isSmooth());
		final JCheckBox  fractalSettingInvert       = new JCheckBox("", fractalExplorer.fractal.isInvert());
		final JCheckBox  liveJulia                  = new JCheckBox("", fractalExplorer.liveJuliaUpdates);
		
		final JComboBox<String> fractalSettingOrbitTraps  = new JComboBox<String>();
		final JComboBox<String> fractalSettingRegionSplit = new JComboBox<String>();
		
		for(String s : new String[] {"None", "Cross", "Dots"}) fractalSettingOrbitTraps.addItem(s);
		for(String s : new String[] {"None", "Iterations", "Axis"}) fractalSettingRegionSplit.addItem(s);
		
		fractalSettingOrbitTraps.setSelectedItem(fractalExplorer.fractal.getOrbitTraps());
		fractalSettingRegionSplit.setSelectedItem(fractalExplorer.fractal.getRegionSplits());

		fractalSettingOrbitTraps.setEnabled(!fractalSettingBuddha.isSelected());
		fractalSettingRegionSplit.setEnabled(fractalSettingOrbitTraps.getSelectedIndex() != 0 && !fractalSettingBuddha.isSelected());

		JLabel fractalSettingInverseLabel = new JLabel("Inverse Base");
		fractalSettingInverseLabel.setToolTipText("Inverse the inital seed/base value to prduce an inverse image.");
		JLabel fractalSettingBuddhaLabel  = new JLabel("Buddha");
		fractalSettingBuddhaLabel.setToolTipText("Due to how this is calculated, zooming won't work as you imagine.");
		JLabel fractalSettingSmoothLabel  = new JLabel("Smoothing");
		fractalSettingSmoothLabel.setToolTipText("We pretend the difference between two iterations is linear to produce a pretty image.");
		
		gridSettings.setBorder(new EmptyBorder(fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding));
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
		coloringPanel.setBorder(new EmptyBorder(fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding, fractalExplorer.padding));
		coloringPanel.setBackground(Colors.foreground);
		if(fractalExplorer.fractal.getColoring() instanceof UserConfigurable)
			coloringPanel.add(((UserConfigurable) fractalExplorer.fractal.getColoring()).getSettingsPanel());
		
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
		JLabel userLabel = new JLabel("consto.uk");
		userLabel.setForeground(Colors.foreground);
		aboutPanel.add(aboutLabel, BorderLayout.CENTER);
		aboutPanel.add(userLabel, BorderLayout.LINE_END);
		
		// Add everything to our frame
		settingsPanel.add(fractalSettingsPanel);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
		settingsPanel.add(calculatorPanel);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
		settingsPanel.add(fractalsCombo);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
		settingsPanel.add(gridSettings);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
		settingsPanel.add(coloringSettingsPanel);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
		settingsPanel.add(colorableCombo);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
		settingsPanel.add(coloringPanel);
		settingsPanel.add(Box.createVerticalStrut(fractalExplorer.padding));
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
				fractalsCombo.setSelectedItem(FractalSettingsFrame.this.fractalExplorer.fractal.getFractal());
				
				// Regenerate fractals
				FractalSettingsFrame.this.fractalExplorer.fractal.regenerate();
				FractalSettingsFrame.this.fractalExplorer.julia.regenerate();
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
				fractalsCombo.setSelectedItem(FractalSettingsFrame.this.fractalExplorer.fractal.getFractal());
				
				// Regenerate fractals
				FractalSettingsFrame.this.fractalExplorer.fractal.regenerate();
				FractalSettingsFrame.this.fractalExplorer.julia.regenerate();
			}
		});
		
		fractalsCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FractalSettingsFrame.this.fractalExplorer.fractal.setFractal((String) fractalsCombo.getSelectedItem());
			}
		});
		
		// Color picker
		colorableCombo.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalSettingsFrame.this.fractalExplorer.fractal.setColoring((Colorable) colorableCombo.getSelectedItem());
				FractalSettingsFrame.this.fractalExplorer.julia.setColoring((Colorable) colorableCombo.getSelectedItem());
				
				// Update color panel
				coloringPanel.removeAll();
				if(FractalSettingsFrame.this.fractalExplorer.fractal.getColoring() instanceof UserConfigurable) {
					coloringPanel.add(
						((UserConfigurable) FractalSettingsFrame.this.fractalExplorer.fractal.getColoring()).getSettingsPanel()
					);
				}
				coloringPanel.updateUI();
			}
		});
		
		// Add listerns to update the fractal
		fractalSettingStart.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setStart(Double.parseDouble(fractalSettingStart.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingEnd.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setEnd(Double.parseDouble(fractalSettingEnd.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingTop.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setTop(Double.parseDouble(fractalSettingTop.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingBottom.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setBottom(Double.parseDouble(fractalSettingBottom.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingIterations.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setIterations(Integer.parseInt(fractalSettingIterations.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingThreshold.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setThreshold(Double.parseDouble(fractalSettingThreshold.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingOrder.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					FractalSettingsFrame.this.fractalExplorer.fractal.setOrder(Integer.parseInt(fractalSettingOrder.getText()));
				} catch (NumberFormatException ignored) {}
			}
		});
		fractalSettingInverse.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalSettingsFrame.this.fractalExplorer.fractal.setInverse(fractalSettingInverse.isSelected());
			}
		});
		fractalSettingSmooth.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalSettingsFrame.this.fractalExplorer.fractal.setSmooth(fractalSettingSmooth.isSelected());
			}
		});
		fractalSettingInvert.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalSettingsFrame.this.fractalExplorer.fractal.setInvert(fractalSettingInvert.isSelected());
			}
		});
		fractalSettingBuddha.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				FractalSettingsFrame.this.fractalExplorer.fractal.setBuddha(fractalSettingBuddha.isSelected());
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
				fractalSettingBuddha.setEnabled(FractalSettingsFrame.this.fractalExplorer.fractal.getOrbitTraps().equals("None"));
				if(!FractalSettingsFrame.this.fractalExplorer.fractal.getOrbitTraps().equals(
					(String) fractalSettingOrbitTraps.getSelectedItem())
				) {
					FractalSettingsFrame.this.fractalExplorer.fractal.setOrbitTraps((String) fractalSettingOrbitTraps.getSelectedItem());
				}
			}
		});
		
		fractalSettingRegionSplit.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				fractalSettingBuddha.setEnabled(FractalSettingsFrame.this.fractalExplorer.fractal.getOrbitTraps().equals("None"));
				if(!FractalSettingsFrame.this.fractalExplorer.fractal.getRegionSplits().equals(
					(String) fractalSettingRegionSplit.getSelectedItem())
				) {
					FractalSettingsFrame.this.fractalExplorer.fractal.setRegionSplits((String) fractalSettingRegionSplit.getSelectedItem());
				}
			}
		});
		
		// And to update the settings menu
		fractalExplorer.fractal.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				fractalSettingStart.setText(String.format("%.7f", FractalSettingsFrame.this.fractalExplorer.fractal.getStart()));
				fractalSettingEnd.setText(String.format("%.7f", FractalSettingsFrame.this.fractalExplorer.fractal.getEnd()));
				fractalSettingTop.setText(String.format("%.7f", FractalSettingsFrame.this.fractalExplorer.fractal.getTop()));
				fractalSettingBottom.setText(String.format("%.7f", FractalSettingsFrame.this.fractalExplorer.fractal.getBottom()));
				fractalSettingIterations.setText(Integer.toString(FractalSettingsFrame.this.fractalExplorer.fractal.getIterations()));
				fractalSettingThreshold.setText(String.format("%.7f", FractalSettingsFrame.this.fractalExplorer.fractal.getThreshold()));
				fractalSettingOrder.setText(Integer.toString(FractalSettingsFrame.this.fractalExplorer.fractal.getOrder()));
				fractalSettingBuddha.setSelected(FractalSettingsFrame.this.fractalExplorer.fractal.isBuddha());
				fractalSettingBuddha.setEnabled(FractalSettingsFrame.this.fractalExplorer.fractal.getOrbitTraps().equals("None"));
				fractalSettingInverse.setSelected(FractalSettingsFrame.this.fractalExplorer.fractal.isInverse());
				fractalSettingSmooth.setSelected(FractalSettingsFrame.this.fractalExplorer.fractal.isSmooth());
				fractalSettingInvert.setSelected(FractalSettingsFrame.this.fractalExplorer.fractal.isInvert());
				fractalSettingOrbitTraps.setSelectedItem(FractalSettingsFrame.this.fractalExplorer.fractal.getOrbitTraps());
				fractalSettingRegionSplit.setSelectedItem(FractalSettingsFrame.this.fractalExplorer.fractal.getRegionSplits());
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
				FractalSettingsFrame.this.fractalExplorer.liveJuliaUpdates = liveJulia.isSelected();
			}
		});
	}

	/**
	 * Update the color settings panel
	 */
	public void updateColorSettings() {
		coloringPanel.removeAll();
		if(fractalExplorer.fractal.getColoring() instanceof UserConfigurable)
			coloringPanel.add(((UserConfigurable) fractalExplorer.fractal.getColoring()).getSettingsPanel());

		// Probably not the nicest way to do this, but it works
		Colorable[] colorables = Colorable.getImplementations();
		for(int i = 0; i < colorables.length; i++) {
			if(colorables[i].getClass().getName().equals(fractalExplorer.fractal.getColoring().getClass().getName())) {
				colorableCombo.setSelectedIndex(i);
				break;
			}
		}
		
		coloringPanel.updateUI();
	}
}