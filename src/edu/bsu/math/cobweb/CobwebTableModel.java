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

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * class to maintain the table of values with a custom table model.
 * 
 * @author Ben Dean
 */
final class CobwebTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3877845437884037968L;

	private String[] columnNames = { "n", "X_n", "Z_n" };

	private final List<Integer> nList;

	private final List<Double> xnList, znList;

	private boolean zColumnVisible = false;

	/**
	 * construct a {@link CobwebTableModel}
	 * 
	 * @param n
	 *            a {@link List} of {@link Integer} representing the iterations
	 * @param x
	 *            a {@link List} of {@link Integer} representing the
	 *            f(x<sub>n-1</sub>) value
	 * @param z
	 *            a {@link List} of {@link Integer} representing the
	 *            f<sup>k</sup>(z<sub>n-1</sub>) value
	 */
	public CobwebTableModel(List<Integer> n, List<Double> x, List<Double> z) {
		nList = n;
		xnList = x;
		znList = z;
	}

	/**
	 * method to set whether or not the Z column is visible
	 * 
	 * @param visible
	 *            boolean true if Z column should be visible, false if Z column
	 *            should not be visible
	 */
	public void setZColumnVisible(boolean visible) {
		zColumnVisible = visible;
	}

	/**
	 * @see AbstractTableModel#getColumnCount()
	 */
	public int getColumnCount() {
		if (zColumnVisible)
			return columnNames.length;
		else
			return columnNames.length - 1;
	}

	/**
	 * @see AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/**
	 * @see AbstractTableModel#getRowCount()
	 */
	public int getRowCount() {
		return nList.size();
	}

	/**
	 * @see AbstractTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		switch (column) {
		case 0:
			return nList.get(row);
		case 1:
			return xnList.get(row);
		case 2:
			return zColumnVisible ? znList.get(row) : null;
		default:
			return null;
		}
	}
}
