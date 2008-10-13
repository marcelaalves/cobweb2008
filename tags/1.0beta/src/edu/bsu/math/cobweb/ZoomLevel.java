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

/**
 * class to keep track of the min and max x and y coordinates for zooming
 * 
 * @author Ben Dean
 */
final class ZoomLevel {

	/**
	 * the minimum x coordinate of the zoom level
	 */
	final double xMin;

	/**
	 * the maximum x coordinate of the zoom level
	 */
	final double xMax;

	/**
	 * the minimum y coordinate of the zoom level
	 */
	final double yMin;

	/**
	 * the maximum y coordinate of the zoom level
	 */
	final double yMax;

	/**
	 * Construct a new {@link ZoomLevel}
	 * 
	 * @param x1
	 *            the minimum x coordinate of the zoom level
	 * @param x2
	 *            the maximum x coordinate of the zoom level
	 * @param y1
	 *            the minimum y coordinate of the zoom level
	 * @param y2
	 *            the maximum y coordinate of the zoom level
	 */
	public ZoomLevel(double x1, double x2, double y1, double y2) {
		xMin = x1;
		xMax = x2;
		yMin = y1;
		yMax = y2;
	}
}