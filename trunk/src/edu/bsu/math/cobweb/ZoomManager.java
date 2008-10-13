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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Stack;

/**
 * class to take care of the zooming with the mouse buttons, dragging, and
 * scrolling.
 * 
 * @author Ben Dean
 */
final class ZoomManager extends MouseAdapter implements MouseListener,
		MouseMotionListener, MouseWheelListener {
	private static final int SCROLL_ZOOM_SIZE = 75;

	private final Stack<ZoomLevel> zoomStack = new Stack<ZoomLevel>();

	/**
	 * construct the listener with the default zoom on the stack
	 * 
	 * @param fullZoom
	 *            the {@link ZoomLevel} with which to initialize the
	 *            {@link ZoomManager}
	 */
	public ZoomManager(ZoomLevel fullZoom) {
		setFullZoom(fullZoom);
	}

	private CoordinatePair current;
	private CoordinatePair start;
	private Point startPoint;

	private static final Rectangle NOT_ZOOMING_RECT = new Rectangle(0, 0, 0, 0);
	private Rectangle zoomRectangle = NOT_ZOOMING_RECT;

	/**
	 * handle events when the mouse moves
	 * 
	 * @param ev
	 *            the {@link MouseEvent}
	 */
	public void mouseMoved(MouseEvent ev) {
		Component obj = (Component) ev.getSource();
		while (!(obj instanceof CobwebPanel))
			obj = obj.getParent();
		CobwebPanel panel = (CobwebPanel) obj;

		current = panel.pointToCoordinatePair(ev.getPoint());
		panel.setPointString(current.toString());
	}

	/**
	 * when a mouse button is pressed, look for the starting point of the zoom
	 * rectangle
	 * 
	 * @param ev
	 *            the {@link MouseEvent}
	 */
	public void mousePressed(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON1 && startPoint == null) {
			startPoint = ev.getPoint();
			start = current;
		}
	}

	/**
	 * handle the mouse button being released. if a zoom rectangle was being
	 * dragged, zoom to the new rectangle. if the right button was clicked,
	 * reset the zoom to the default.
	 * 
	 * @param ev
	 *            the {@link MouseEvent}
	 */
	public void mouseReleased(MouseEvent ev) {
		Component obj = (Component) ev.getSource();
		while (!(obj instanceof CobwebPanel))
			obj = obj.getParent();
		CobwebPanel panel = (CobwebPanel) obj;

		if (ev.getButton() == MouseEvent.BUTTON1 && startPoint != null) {
			CoordinatePair end = panel.pointToCoordinatePair(ev.getPoint());
			if (start.equals(end))
				return;

			// figure out what the zoom level should be
			ZoomLevel zoom;
			double x1 = Math.min(start.x, end.x);
			double x2 = Math.max(start.x, end.x);
			double y1 = Math.min(start.y, end.y);
			double y2 = Math.max(start.y, end.y);
			if ((ev.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK) {
				double width = Math.max(x2 - x1, y2 - y1);
				zoom = new ZoomLevel(x1, x1 + width, y1, y1 + width);
			} else {
				zoom = new ZoomLevel(x1, x2, y1, y2);
			}

			zoomStack.push(zoom);
			panel.setGraphZoom(zoom);
			panel.setFullZoomOptionsEnabled(false);

			start = null;
			startPoint = null;
			zoomRectangle = NOT_ZOOMING_RECT;
		} else if (ev.getButton() == MouseEvent.BUTTON3 && startPoint == null) {
			ZoomLevel fullZoom = zoomStack.firstElement();
			setFullZoom(fullZoom);
			panel.setGraphZoom(fullZoom);
			panel.setFullZoomOptionsEnabled(true);
		}
	}

	/**
	 * when the mouse is dragged, create a rectangle
	 * 
	 * @param ev
	 *            the {@link MouseEvent}
	 */
	public void mouseDragged(MouseEvent ev) {
		if (startPoint == null)
			return;

		Component obj = (Component) ev.getSource();
		while (!(obj instanceof CobwebPanel))
			obj = obj.getParent();
		CobwebPanel panel = (CobwebPanel) obj;

		if ((ev.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != InputEvent.BUTTON1_DOWN_MASK)
			return;
		Point currentPoint = ev.getPoint();
		int width = currentPoint.x - startPoint.x;
		int height = currentPoint.y - startPoint.y;
		int x = startPoint.x;
		int y = startPoint.y;

		if ((ev.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK) {
			width = Math.max(Math.abs(width), Math.abs(height))
					* ((width > 0) ? 1 : -1);
			height = Math.max(Math.abs(width), Math.abs(height))
					* ((height > 0) ? 1 : -1);
		}

		if (width < 0) {
			width *= -1;
			x = x - width;
		}

		if (height < 0) {
			height *= -1;
			y = y - height;
		}

		zoomRectangle = new Rectangle(x, y, width, height);
		panel.repaint();
	}

	/**
	 * for scroll wheel zoom in and out.
	 * 
	 * @param ev
	 *            the {@link MouseEvent}
	 */
	public void mouseWheelMoved(MouseWheelEvent ev) {
		Component obj = (Component) ev.getSource();
		while (!(obj instanceof CobwebPanel))
			obj = obj.getParent();
		CobwebPanel panel = (CobwebPanel) obj;

		if (ev.getWheelRotation() == 1 && zoomStack.size() > 1) {
			// scroll wheel scrolling down, zoom out
			zoomStack.pop();
			if (zoomStack.size() == 1)
				panel.setFullZoomOptionsEnabled(true);
			panel.setGraphZoom(zoomStack.peek());
		} else if (ev.getWheelRotation() == -1) {
			// scroll wheel scrolling up, zoom in
			Point center = ev.getPoint();
			CoordinatePair topLeft = panel.pointToCoordinatePair(new Point(
					center.x - SCROLL_ZOOM_SIZE, center.y - SCROLL_ZOOM_SIZE));
			CoordinatePair bottomRight = panel.pointToCoordinatePair(new Point(
					center.x + SCROLL_ZOOM_SIZE, center.y + SCROLL_ZOOM_SIZE));
			ZoomLevel zoomIn = new ZoomLevel(topLeft.x, bottomRight.x,
					bottomRight.y, topLeft.y);
			zoomStack.push(zoomIn);
			panel.setGraphZoom(zoomIn);
			panel.setFullZoomOptionsEnabled(false);
		}
	}

	/**
	 * method to restore the zoom to fully zoomed out
	 * 
	 * @param fullZoom
	 *            the full {@link ZoomLevel} to use
	 */
	public void setFullZoom(ZoomLevel fullZoom) {
		zoomStack.clear();
		zoomStack.push(fullZoom);
	}

	/**
	 * @return the zoomRectangle to draw
	 */
	public Shape getRectangle() {
		return zoomRectangle;
	}
}