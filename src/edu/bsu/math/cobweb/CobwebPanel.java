/*
 * Cobweb Plot 2008: A function iteration and cobweb plot visualization tool
 * Copyright (C) 2008 Ball State University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.bsu.math.cobweb;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import EDU.emporia.mathbeans.MathGrapher;
import EDU.emporia.mathbeans.MathGrid;
import EDU.emporia.mathbeans.SymbolicFunction;
import EDU.emporia.mathtools.Graphable_error;
import EDU.emporia.mathtools.PolygonalCurve;

/**
 * JPanel for drawing the cobweb plot.
 * 
 * @author Ben Dean
 */
final class CobwebPanel extends JPanel {

	private static final long serialVersionUID = 6313838441935717346L;

	private static final String TABLE_INFO_MESSAGE = "For each nth iteration:"
			+ "\n\n"
			+ "X_n = f( X_{n-1} )\n"
			+ "Z_n = f^k( Z_{n-1} )"
			+ "\n\n"
			+ "Rows in the table can be selected using the <SHIFT> or <CTRL> keys, <CTRL>-A.\n"
			+ "Once selected, values can be copied with <CTRL>-C.\n"
			+ "These values can be pasted into a variety of applications (such as Notepad or Excel).";

	private static final String ZOOMING_HELP_MESSAGE = "Zooming Controls:\n"
			+ "Use the left mouse button to drag a box over an area to zoom in.\n"
			+ "Use the right mouse button to reset to the full zoom specified by xMin, xMax, yMin, yMax.\n"
			+ "Use the scroll wheel to zoom in and out centered on the mouse cursor.\n\n"
			+ "Holding <ALT> while dragging the mouse will cause the zooming area to be a square.\n"
			+ "This will maintain the aspect ratio of the previous zoom.\n"
			+ "If you want the x and y ranges to be the same (i.e. show the graph with an aspect ratio of 1),\n"
			+ "you should have xMin-xMax == yMin-yMax\n"
			+ "and make sure to zoom holding down <ALT> each time.";

	private static final double DEFAULT_MIN_X = 0.0;

	private static final double DEFAULT_MAX_X = 1.0;

	private static final double DEFAULT_MIN_Y = 0.0;

	private static final double DEFAULT_MAX_Y = 1.0;

	private ZoomLevel fullZoom = new ZoomLevel(DEFAULT_MIN_X, DEFAULT_MAX_X,
			DEFAULT_MIN_Y, DEFAULT_MAX_Y);

	private ZoomManager zoomManager = new ZoomManager(fullZoom);

	private static final Stroke ZOOM_STROKE = new BasicStroke(1,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 5f,
					5f }, 0);

	private final ClassLoader cl = Thread.currentThread()
			.getContextClassLoader();

	private int index = 0;

	private SymbolicFunction line;

	private SymbolicFunction func;

	private SymbolicFunction kFunc;

	private PolygonalCurve web;

	private PolygonalCurve kWeb;

	private MathGrapher graph;

	private CobwebTableModel tableModel;

	private final List<Integer> nList = new ArrayList<Integer>();

	private final List<Double> xnList = new ArrayList<Double>();

	private final List<Double> znList = new ArrayList<Double>();

	private JTextField fTextField;

	private JTextField seedTextField;

	private JTextField kTextField;

	private static final int DEFAULT_K_VALUE = 5;

	private static final String DEFAULT_FUNCTION = "2*x*(1-x)";

	private static final Double DEFAULT_SEED = 0.1;

	private static final String DEFAULT_CUSTOM_ITERATION = "1000";

	private JCheckBox kCheckBox = new JCheckBox();

	private Double seed = DEFAULT_SEED;

	private int kValue = DEFAULT_K_VALUE;

	private Double currentValue, kCurrentValue;

	private JLabel kLabel;

	private int iterationSize;

	private JTextField customTextField;

	private JButton resetButton;

	private AbstractAction kCheckBoxAction;

	private JButton iterateButton;

	private JCheckBox gridCheckBox;

	private JCheckBox funcCheckBox;

	private AbstractButton lineCheckBox;

	private JCheckBox webCheckBox;

	private JCheckBox kWebCheckBox;

	private JCheckBox kFuncCheckBox;

	private JPanel fullZoomPanel;

	private static final Border MARGIN = new EmptyBorder(new Insets(5, 5, 5, 5));

	private static final Dimension FIELD_SIZE = new Dimension(125, 20);

	/**
	 * method to initialize the applet.
	 * 
	 * places all the components in a grid. all the actions of buttons and
	 * textboxes and all the listeners are set up here making use of anonymous
	 * inner classes
	 */
	public CobwebPanel() {
		// add a mouse listener that takes care of grabbing the focus when the
		// user clicks on the panel
		this.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				CobwebPanel.this.grabFocus();
			}
		});

		// initialize the line and function
		line = new SymbolicFunction();
		func = new SymbolicFunction();
		kFunc = new SymbolicFunction();
		web = new PolygonalCurve();
		kWeb = new PolygonalCurve();
		try {
			line.setFormula("x");
			String f = DEFAULT_FUNCTION;
			func.setFormula(f);

			for (int i = 0; i < DEFAULT_K_VALUE - 1; i++) {
				f = f.replace("x", "(" + DEFAULT_FUNCTION + ")");
			}
			kFunc.setFormula(f);
		} catch (Graphable_error e) {
		}

		// initialize the graph
		graph = new MathGrapher() {

			private static final long serialVersionUID = -1686608942648653451L;

			/**
			 * extended paint method to draw the zoom rectangle
			 */
			public void paintComponent(Graphics g) {
				if (this.getTitle().equals(""))
					setTitle("(0.000000, 0.000000)");
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(ZOOM_STROKE);
				g2.setColor(Color.BLACK);
				g2.draw(zoomManager.getRectangle());
				g2.setStroke(oldStroke);
			}
		};
		graph.addGraph(line, Color.BLACK);
		graph.addGraph(func, Color.BLUE);
		graph.addGraph(web, Color.RED);
		graph.setXMin(0.0);
		graph.setXMax(1.0);
		graph.setYMin(0.0);
		graph.setYMax(1.0);
		graph.setZoomMode(MathGrapher.ZOOMOFF);
		graph.setTraceEnabled(false);
		graph.setFont(new Font("Serif", Font.PLAIN, 10));
		graph.setBackground(Color.LIGHT_GRAY);
		graph.setPreferredSize(new Dimension(300, 300));

		graph.addMouseWheelListener(zoomManager);
		graph.addMouseListener(zoomManager);
		graph.addMouseMotionListener(zoomManager);

		// set up the layout
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		// initialize the table
		tableModel = new CobwebTableModel(nList, xnList, znList);
		JTable table = new JTable(tableModel);

		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setPreferredSize(new Dimension(600, 300));

		JButton tableInfoButton = new JButton(new AbstractAction(
				"Table Information") {

			private static final long serialVersionUID = 3962757827516328355L;

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(CobwebPanel.this,
						TABLE_INFO_MESSAGE, "Table Information",
						JOptionPane.INFORMATION_MESSAGE, new ImageIcon(cl
								.getResource("Information24.gif")));
			}
		});
		tableInfoButton.setIcon(new ImageIcon(cl
				.getResource("Information16.gif")));
		JPanel tablePanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		tablePanel.add(scrollPane, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		tablePanel.add(tableInfoButton, c);
		tablePanel
				.setBorder(BorderFactory.createCompoundBorder(MARGIN, MARGIN));

		// add the title
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		JLabel title = new JLabel("Cobweb Plot");
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setFont(new Font("Dialog", Font.BOLD, 24));
		this.add(title, c);

		// add the graph
		final JPanel graphPanel = new JPanel(new GridBagLayout());
		graphPanel
				.setBorder(BorderFactory.createCompoundBorder(MARGIN, MARGIN));
		c.gridx = 0;
		c.gridy = 0;
		graphPanel.add(graph, c);

		JButton helpButton = new JButton(new AbstractAction("Zooming Help") {

			private static final long serialVersionUID = -959727892421262755L;

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(CobwebPanel.this,
						ZOOMING_HELP_MESSAGE, "Zooming Help",
						JOptionPane.INFORMATION_MESSAGE, new ImageIcon(cl
								.getResource("Help24.gif")));

			}
		});
		helpButton.setIcon(new ImageIcon(cl.getResource("Help16.gif")));
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		graphPanel.add(helpButton, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = c.gridheight = 1;
		this.add(graphPanel, c);

		// add the table
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		this.add(tablePanel, c);

		// create sub panel for functions
		final JPanel functionPanel = new JPanel(new GridBagLayout());
		functionPanel.setBorder(BorderFactory.createCompoundBorder(MARGIN,
				BorderFactory.createCompoundBorder(BorderFactory
						.createTitledBorder("Function"), MARGIN)));

		// add f(x) label to function panel
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		functionPanel.add(new JLabel("f(x) = "), c);

		// add f(x) field to function panel
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		fTextField = new JTextField(DEFAULT_FUNCTION);
		fTextField.setDisabledTextColor(Color.lightGray);
		fTextField.setPreferredSize(FIELD_SIZE);
		fTextField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (!e.isTemporary())
					try {
						String f = fTextField.getText();
						func.setFormula(f);

						String kF = f;
						for (int i = 0; i < kValue - 1; i++) {
							kF = kF.replace("x", "(" + f + ")");
						}
						kFunc.setFormula(kF);

						graph.updateGraph();
					} catch (Graphable_error e1) {
						JOptionPane.showMessageDialog(null, fTextField
								.getText()
								+ " is not a valid formula", "Syntax Error",
								JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								fTextField.grabFocus();
							}
						});
					}
			}
		});
		functionPanel.add(fTextField, c);

		// add seed label to function panel
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		functionPanel.add(new JLabel("Initial Value: "), c);

		// add seed field to function panel
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		seed = DEFAULT_SEED;
		seedTextField = new JTextField(seed.toString());
		seedTextField.setDisabledTextColor(Color.lightGray);
		seedTextField.setPreferredSize(FIELD_SIZE);
		seedTextField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (!e.isTemporary())
					try {
						seed = Double.parseDouble(seedTextField.getText());
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null,
								"Initial value must be a double precision"
										+ " floating point number",
								"Syntax Error", JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								seedTextField.grabFocus();
							}
						});
					}
			}
		});
		functionPanel.add(seedTextField, c);

		// add k checkbox to function panel
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		kCheckBoxAction = new AbstractAction("Enable kth iterate f^k(x)") {

			private static final long serialVersionUID = -8097232262544652590L;

			/**
			 * when checkbox is clicked, enable / disable kth iterate
			 */
			public void actionPerformed(ActionEvent e) {
				if (kCheckBox.isSelected()) {
					kTextField.setEditable(true);
					kLabel.setForeground(Color.BLACK);
					kFuncCheckBox.setEnabled(true);
					kWebCheckBox.setEnabled(true);
				} else {
					kTextField.setEditable(false);
					kLabel.setForeground(Color.GRAY);
					kFuncCheckBox.setEnabled(false);
					kWebCheckBox.setEnabled(false);
				}

				updateGraphOptions();
				tableModel.setZColumnVisible(kCheckBox.isSelected());
				tableModel.fireTableStructureChanged();
			}
		};
		kCheckBox = new JCheckBox(kCheckBoxAction);
		functionPanel.add(kCheckBox, c);

		// add k label to function panel
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		kLabel = new JLabel("k = ");
		kLabel.setForeground(Color.GRAY);
		functionPanel.add(kLabel, c);

		// add k field to function panel
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		kValue = DEFAULT_K_VALUE;
		kTextField = new JTextField("" + kValue);
		kTextField.setEditable(false);
		kTextField.setDisabledTextColor(Color.lightGray);
		kTextField.setPreferredSize(FIELD_SIZE);
		kTextField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (!e.isTemporary())
					try {
						kValue = Integer.parseInt(kTextField.getText());
						String f = fTextField.getText();
						String kF = f;
						for (int i = 0; i < kValue - 1; i++) {
							kF = kF.replace("x", "(" + f + ")");
						}
						kFunc.setFormula(kF);

						graph.updateGraph();
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null,
								"k value must be an integer", "Syntax Error",
								JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								kTextField.grabFocus();
							}
						});
					} catch (Graphable_error e1) {
					}
			}
		});
		functionPanel.add(kTextField, c);

		// add reset button to function panel
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.gridheight = 1;
		resetButton = new JButton(new AbstractAction("Reset") {

			private static final long serialVersionUID = -6144368548853840049L;

			public void actionPerformed(ActionEvent arg0) {
				currentValue = kCurrentValue = null;
				index = 0;
				web.removeAllPoints();
				kWeb.removeAllPoints();
				nList.clear();
				xnList.clear();
				znList.clear();
				tableModel.fireTableDataChanged();

				graph.updateGraph();

				fTextField.setEditable(true);
				seedTextField.setEditable(true);
				kCheckBox.setEnabled(true);
				kCheckBoxAction.actionPerformed(null);

				resetButton.setEnabled(false);
			}
		});
		resetButton.setEnabled(false);
		functionPanel.add(resetButton, c);

		// add the function panel to main panel
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		this.add(functionPanel, c);

		// create sub panel for iteration controls
		final JPanel iterationPanel = new JPanel(new GridBagLayout());
		iterationPanel.setBorder(BorderFactory.createCompoundBorder(MARGIN,
				BorderFactory.createCompoundBorder(BorderFactory
						.createTitledBorder("Iteration"), MARGIN)));

		iterationSize = 1;

		// add one radio button to iteration panel
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		JRadioButton oneButton = new JRadioButton(new AbstractAction("1") {

			private static final long serialVersionUID = -6144368548853840049L;

			public void actionPerformed(ActionEvent arg0) {
				customTextField.setEditable(false);
				iterationSize = 1;
			}

		});
		oneButton.setSelected(true);
		iterationPanel.add(oneButton, c);

		// add ten radio button to iteration panel
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		JRadioButton tenButton = new JRadioButton(new AbstractAction("10") {

			private static final long serialVersionUID = -7964215334216339593L;

			public void actionPerformed(ActionEvent arg0) {
				customTextField.setEditable(false);
				iterationSize = 10;
			}

		});
		iterationPanel.add(tenButton, c);

		// add hundred radio button to iteration panel
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		JRadioButton hundredButton = new JRadioButton(
				new AbstractAction("100") {

					private static final long serialVersionUID = -3406238601662855526L;

					public void actionPerformed(ActionEvent arg0) {
						customTextField.setEditable(false);
						iterationSize = 100;
					}

				});
		iterationPanel.add(hundredButton, c);

		// add custom iteration radio button to iteration panel
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		JRadioButton customButton = new JRadioButton(new AbstractAction("") {

			private static final long serialVersionUID = 7585854512606988147L;

			public void actionPerformed(ActionEvent arg0) {
				customTextField.setEditable(true);
				iterationSize = Integer.parseInt(customTextField.getText());
			}

		});
		iterationPanel.add(customButton, c);

		// add custom iteration field to function panel
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		customTextField = new JTextField(DEFAULT_CUSTOM_ITERATION);
		customTextField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (!e.isTemporary())
					try {
						iterationSize = Integer.parseInt(customTextField
								.getText());
					} catch (NumberFormatException e1) {
						JOptionPane
								.showMessageDialog(CobwebPanel.this,
										"Iterations must be integers",
										"Invalid Interation",
										JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								customTextField.grabFocus();
							}
						});
					}

			}

		});
		customTextField.setEditable(false);
		customTextField.setDisabledTextColor(Color.lightGray);
		customTextField.setPreferredSize(FIELD_SIZE);
		iterationPanel.add(customTextField, c);

		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 5, 0, 5);
		JButton customInfoButton = new JButton(new AbstractAction("") {

			private static final long serialVersionUID = -959727892421262755L;

			public void actionPerformed(ActionEvent arg0) {
				String helpMessage = "Iterating more than 1000 times at once "
						+ "may take a long time.\n\n"
						+ "Hit ESC to stop the iteration process";
				JOptionPane.showMessageDialog(CobwebPanel.this, helpMessage,
						"Iteration Information",
						JOptionPane.INFORMATION_MESSAGE, new ImageIcon(cl
								.getResource("Information24.gif")));
			}
		});
		customInfoButton.setIcon(new ImageIcon(cl
				.getResource("Information16.gif")));
		customInfoButton.setPreferredSize(new Dimension(20, 20));
		iterationPanel.add(customInfoButton, c);

		// create the button group
		ButtonGroup group = new ButtonGroup();
		group.add(oneButton);
		group.add(tenButton);
		group.add(hundredButton);
		group.add(customButton);

		// add the iterate button to the iteration panel
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		iterateButton = new JButton(new AbstractAction("Iterate") {

			private static final long serialVersionUID = 7729263454936606886L;

			private volatile boolean cancelIteration = false;

			private final KeyListener listener = new KeyAdapter() {

				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
						cancelIteration = true;
				}
			};

			private void addListenerTo(Component c) {
				c.addKeyListener(listener);
				if (c instanceof Container)
					for (Component child : ((Container) c).getComponents())
						addListenerTo(child);
			}

			private void removeListenerFrom(Component c) {
				c.removeKeyListener(listener);
				if (c instanceof Container)
					for (Component child : ((Container) c).getComponents())
						removeListenerFrom(child);
			}

			public void actionPerformed(ActionEvent arg0) {
				cancelIteration = false;

				if (currentValue == null) {
					currentValue = kCurrentValue = seed;
					nList.add(0);
					xnList.add(seed);
					znList.add(seed);

					fTextField.setEditable(false);
					seedTextField.setEditable(false);
					kCheckBox.setEnabled(false);
					kTextField.setEditable(false);
					resetButton.setEnabled(true);
				}

				iterateButton.setVisible(false);
				resetButton.setEnabled(false);
				final JProgressBar iterationProgress = new JProgressBar(0,
						iterationSize);
				iterationProgress.setStringPainted(true);
				iterationProgress.setPreferredSize(iterateButton.getSize());
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0;
				c.gridy = 4;
				c.gridwidth = 3;
				c.gridheight = 1;
				iterationPanel.add(iterationProgress, c);

				addListenerTo(CobwebPanel.this);

				new Thread() {

					public void run() {
						long startTime = System.currentTimeMillis();
						for (int i = 0; i < iterationSize; ++i) {
							iterate();
							if (i % 20 == 0) {
								graph.updateGraph();
								tableModel.fireTableDataChanged();
							}
							iterationProgress.setValue(i);
							Thread.yield();
							if (cancelIteration)
								break;
						}
						graph.updateGraph();
						tableModel.fireTableDataChanged();

						iterationPanel.remove(iterationProgress);
						iterateButton.setVisible(true);
						resetButton.setEnabled(true);

						long endTime = System.currentTimeMillis();
						long diffTime = endTime - startTime;
						System.out.println("Time for " + iterationSize
								+ " iterations: " + diffTime + " ms.");

						removeListenerFrom(CobwebPanel.this);
					}
				}.start();
			}
		});
		iterationPanel.add(iterateButton, c);

		// add the function panel to main panel
		c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		this.add(iterationPanel, c);

		// create sub panel for graph options
		final JPanel graphOptionPanel = new JPanel(new GridBagLayout());
		graphOptionPanel.setBorder(BorderFactory.createCompoundBorder(MARGIN,
				BorderFactory.createCompoundBorder(BorderFactory
						.createTitledBorder("Graph Options"), MARGIN)));

		// add grid lines checkbox to graph options panel
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		gridCheckBox = new JCheckBox(new AbstractAction("Show grid lines") {

			private static final long serialVersionUID = -8233930439066975785L;

			/**
			 * when checkbox is clicked, show / hide grid lines
			 */
			public void actionPerformed(ActionEvent e) {
				if (gridCheckBox.isSelected()) {
					graph.setGridLines(MathGrapher.GRIDNORMAL);
				} else {
					graph.setGridLines(MathGrapher.GRIDOFF);
				}
				graph.updateGraph();
			}
		});
		gridCheckBox.setSelected(true);
		graphOptionPanel.add(gridCheckBox, c);

		// add grid lines checkbox to graph options panel
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		lineCheckBox = new JCheckBox(new AbstractAction("Show y=x") {

			private static final long serialVersionUID = -727613881043904956L;

			/**
			 * when checkbox is clicked, show / hide grid lines
			 */
			public void actionPerformed(ActionEvent e) {
				updateGraphOptions();
			}

		});
		lineCheckBox.setSelected(true);
		graphOptionPanel.add(lineCheckBox, c);

		// add f(x) checkbox to graph options panel
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		funcCheckBox = new JCheckBox(new AbstractAction("Show f(x)") {

			private static final long serialVersionUID = -727613881043904956L;

			/**
			 * when checkbox is clicked, show / hide grid lines
			 */
			public void actionPerformed(ActionEvent e) {
				updateGraphOptions();
			}
		});
		funcCheckBox.setSelected(true);
		graphOptionPanel.add(funcCheckBox, c);

		// add f^k(x) checkbox to graph options panel
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		kFuncCheckBox = new JCheckBox(new AbstractAction("Show f^k(x)") {

			private static final long serialVersionUID = -4216011056166060631L;

			/**
			 * when checkbox is clicked, show / hide grid lines
			 */
			public void actionPerformed(ActionEvent e) {
				updateGraphOptions();
			}
		});
		kFuncCheckBox.setSelected(true);
		kFuncCheckBox.setEnabled(false);
		graphOptionPanel.add(kFuncCheckBox, c);

		// add cobweb f(x) checkbox to graph options panel
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		webCheckBox = new JCheckBox(new AbstractAction(
				"Show cobweb plot of f(x)") {

			private static final long serialVersionUID = -4216011056166060631L;

			/**
			 * when checkbox is clicked, show / hide grid lines
			 */
			public void actionPerformed(ActionEvent e) {
				updateGraphOptions();
			}
		});
		webCheckBox.setSelected(true);
		graphOptionPanel.add(webCheckBox, c);

		// add cobweb f^k(x) checkbox to graph options panel
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		kWebCheckBox = new JCheckBox(new AbstractAction(
				"Show cobweb plot of f^k(x)") {

			private static final long serialVersionUID = -3008309701996614999L;

			/**
			 * when checkbox is clicked, show / hide grid lines
			 */
			public void actionPerformed(ActionEvent e) {
				updateGraphOptions();
			}
		});
		kWebCheckBox.setSelected(true);
		kWebCheckBox.setEnabled(false);
		graphOptionPanel.add(kWebCheckBox, c);

		// add the graph option panel to main panel
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		this.add(graphOptionPanel, c);

		// create the panel for the graph full zoom
		fullZoomPanel = new JPanel(new GridBagLayout());
		fullZoomPanel.setBorder(BorderFactory.createCompoundBorder(MARGIN,
				BorderFactory.createCompoundBorder(BorderFactory
						.createTitledBorder("Graph dimensions when"
								+ " fully zoomed out"), MARGIN)));

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		fullZoomPanel.add(new JLabel("x min: "), c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		fullZoomPanel.add(new JLabel("x max: "), c);
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		fullZoomPanel.add(new JLabel("y min: "), c);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		fullZoomPanel.add(new JLabel("y max: "), c);

		final JTextField xMinField = new JTextField("" + DEFAULT_MIN_X);
		xMinField.setDisabledTextColor(Color.lightGray);
		xMinField.setPreferredSize(FIELD_SIZE);
		xMinField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent ev) {
				if (!ev.isTemporary())
					try {
						double xMin = Double.parseDouble(xMinField.getText());
						fullZoom = new ZoomLevel(xMin, fullZoom.xMax,
								fullZoom.yMin, fullZoom.yMax);
						zoomManager.setFullZoom(fullZoom);
						setGraphZoom(fullZoom);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,
								"x Min must be a double precision"
										+ " floating point number",
								"Syntax Error", JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								xMinField.grabFocus();
							}
						});
					}
			}
		});
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		fullZoomPanel.add(xMinField, c);

		final JTextField xMaxField = new JTextField("" + DEFAULT_MAX_X);
		xMaxField.setDisabledTextColor(Color.lightGray);
		xMaxField.setPreferredSize(FIELD_SIZE);
		xMaxField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent ev) {
				if (!ev.isTemporary())
					try {
						double xMax = Double.parseDouble(xMaxField.getText());
						fullZoom = new ZoomLevel(fullZoom.xMin, xMax,
								fullZoom.yMin, fullZoom.yMax);
						zoomManager.setFullZoom(fullZoom);
						setGraphZoom(fullZoom);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,
								"x Max must be a double precision"
										+ " floating point number",
								"Syntax Error", JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								xMaxField.grabFocus();
							}
						});
					}
			}
		});
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		fullZoomPanel.add(xMaxField, c);

		final JTextField yMinField = new JTextField("" + DEFAULT_MIN_Y);
		yMinField.setDisabledTextColor(Color.lightGray);
		yMinField.setPreferredSize(FIELD_SIZE);
		yMinField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent ev) {
				if (!ev.isTemporary())
					try {
						double yMin = Double.parseDouble(yMinField.getText());
						fullZoom = new ZoomLevel(fullZoom.xMin, fullZoom.xMax,
								yMin, fullZoom.yMax);
						zoomManager.setFullZoom(fullZoom);
						setGraphZoom(fullZoom);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,
								"y Min must be a double precision"
										+ " floating point number",
								"Syntax Error", JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								yMinField.grabFocus();
							}
						});
					}
			}
		});
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = c.gridheight = 1;
		fullZoomPanel.add(yMinField, c);

		final JTextField yMaxField = new JTextField("" + DEFAULT_MAX_Y);
		yMaxField.setDisabledTextColor(Color.lightGray);
		yMaxField.setPreferredSize(FIELD_SIZE);
		yMaxField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent ev) {
				if (!ev.isTemporary())
					try {
						double yMax = Double.parseDouble(yMaxField.getText());
						fullZoom = new ZoomLevel(fullZoom.xMin, fullZoom.xMax,
								fullZoom.yMin, yMax);

						zoomManager.setFullZoom(fullZoom);
						setGraphZoom(fullZoom);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,
								"y Max must be a double precision"
										+ " floating point number",
								"Syntax Error", JOptionPane.ERROR_MESSAGE);
						SwingUtilities.invokeLater(new Runnable() {

							public void run() {
								yMaxField.grabFocus();
							}
						});
					}
			}
		});
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = c.gridheight = 1;
		fullZoomPanel.add(yMaxField, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridheight = 1;
		this.add(fullZoomPanel, c);
	}

	/**
	 * method to update the graph based on which options are currently selected
	 * (which lines and functions to display)
	 */
	private void updateGraphOptions() {
		graph.removeGraph(line);
		graph.removeGraph(func);
		graph.removeGraph(web);
		graph.removeGraph(kFunc);
		graph.removeGraph(kWeb);

		line.setTitle("");
		func.setTitle("");
		web.setTitle("");
		kWeb.setTitle("");
		kFunc.setTitle("");

		if (lineCheckBox.isSelected())
			graph.addGraph(line, Color.BLACK);
		if (funcCheckBox.isSelected())
			graph.addGraph(func, Color.BLUE);
		if (webCheckBox.isSelected())
			graph.addGraph(web, Color.RED);
		if (kCheckBox.isSelected()) {
			if (kFuncCheckBox.isSelected())
				graph.addGraph(kFunc, Color.YELLOW);
			if (kWebCheckBox.isSelected())
				graph.addGraph(kWeb, Color.GREEN);
		}
	}

	/**
	 * method to iterate the functions one time (and also k times if kth iterate
	 * is enabled)
	 */
	private void iterate() {
		if (kCheckBox.isSelected()) {
			kWeb.addPoint(kCurrentValue, kCurrentValue);
			double kOldValue = kCurrentValue;
			for (int i = 0; i < kValue; ++i) {
				kCurrentValue = func.functionValue(kCurrentValue);
			}
			kWeb.addPoint(kOldValue, kCurrentValue);
			znList.add(kCurrentValue);
		}

		web.addPoint(currentValue, currentValue);
		double oldValue = currentValue;
		currentValue = func.functionValue(currentValue);
		web.addPoint(oldValue, currentValue);
		nList.add(++index);
		xnList.add(currentValue);
	}

	/**
	 * method to enable / disable the full zoom options
	 * 
	 * @param enabled
	 *            boolean to describe if full zoom options should be enabled
	 */
	public void setFullZoomOptionsEnabled(boolean enabled) {
		if (enabled)
			for (Component c : fullZoomPanel.getComponents()) {
				c.setForeground(Color.BLACK);
				c.setEnabled(true);
			}
		else
			for (Component c : fullZoomPanel.getComponents()) {
				c.setForeground(Color.LIGHT_GRAY);
				c.setEnabled(false);
			}
	}

	/**
	 * zoom the graph in to a given zoom level
	 * 
	 * @param zoomLevel
	 *            the new {@link ZoomLevel} to set for the graph
	 */
	public void setGraphZoom(ZoomLevel zoomLevel) {
		graph.setXMax(zoomLevel.xMax);
		graph.setXMin(zoomLevel.xMin);
		graph.setYMax(zoomLevel.yMax);
		graph.setYMin(zoomLevel.yMin);
	}

	/**
	 * method to use the {@link MathGrid#xPixelToMath(int)} and
	 * {@link MathGrid#yPixelToMath(int)} to convert screen coordinates to graph
	 * coordinates
	 * 
	 * @param p
	 *            the {@link Point} of the mouse location in screen coordinates
	 * @return a {@link CoordinatePair} of graph coordinates
	 */
	public CoordinatePair pointToCoordinatePair(Point p) {
		return new CoordinatePair(graph.xPixelToMath(p.x), graph
				.yPixelToMath(p.y));

	}

	/**
	 * method to set the title of the graph to show the current point at the
	 * mouse cursor
	 * 
	 * @param string
	 *            the {@link String} title to set for the graph
	 */
	public void setPointString(String string) {
		graph.setTitle(string);
	}

}
