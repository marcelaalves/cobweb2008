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

import java.text.DecimalFormat;

/**
 * a class to keep track of a pair of coordinates as doubles
 * 
 * @author Ben Dean
 */
final class CoordinatePair {

	/**
	 * the x coordinate
	 */
	final double x;

	/**
	 * the y coordinate
	 */
	final double y;

	/**
	 * construct a new {@link CoordinatePair}
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public CoordinatePair(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * method to test if two {@link CoordinatePair} objects are equal
	 * 
	 * @param obj
	 *            the other {@link Object} to test
	 * @return boolean true if both objects are equal, false otherwise
	 */
	public boolean equals(Object obj) {
		try {
			return ((CoordinatePair) obj).x == this.x
					&& ((CoordinatePair) obj).y == this.y;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * @return {@link String} representation of the {@link CoordinatePair}.
	 */
	public String toString() {
		DecimalFormat formatter = new DecimalFormat("#0.000000");
		return "(" + formatter.format(x) + ", " + formatter.format(y) + ")";
	}
}