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

import javax.swing.JFrame;

/**
 * driver class to create a window and add a panel to it.
 * 
 * @author Ben Dean
 */
public class CobwebPlot2008 {

	/**
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Cobweb Plot 2008");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new CobwebPanel());
		frame.pack();
		frame.setVisible(true);
	}

}
