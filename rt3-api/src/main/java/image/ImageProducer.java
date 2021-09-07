/*
 * Copyright (C) 2015 Dane.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * An encapsulation of a buffered image with shortcut methods and direct access to raster data.
 *
 * @author Dane
 */
public class ImageProducer {

	/**
	 * A shortcut to creating a TYPE_INT buffered image with or without an alpha channel.
	 *
	 * @param width the width.
	 * @param height the height.
	 * @param hasAlpha whether the image will have an alpha channel.
	 * @return the buffered image.
	 */
	private static BufferedImage createBufferedImage(int width, int height, boolean hasAlpha) {
		return new BufferedImage(width, height, hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * A minified method to getting the data of a raster.
	 *
	 * @param image the image.
	 * @return the data.
	 */
	private static int[] getPixels(BufferedImage image) {
		return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	}

	private BufferedImage image;
	private int[] pixels;
	private int width, height;

	/**
	 * Constructs a new opaque image producer with the provided width and height.
	 *
	 * @param width the width.
	 * @param height the height.
	 */
	public ImageProducer(int width, int height) {
		this(width, height, false);
	}

	/**
	 * Constructs a new image producer with the provided width, height, and alpha channel if specified.
	 *
	 * @param width the width.
	 * @param height the height.
	 * @param hasAlpha whether the producer will have an alpha channel or not.
	 */
	public ImageProducer(int width, int height, boolean hasAlpha) {
		this.image = createBufferedImage(width, height, hasAlpha);
		this.pixels = getPixels(this.image);
		this.width = width;
		this.height = height;
	}

	/**
	 * Binds the backing image as the target to the {@link Graphics2D} class.
	 *
	 * @see Graphics2D
	 */
	public void bind() {
		Graphics2D.setTarget(this.pixels, this.width, this.height);
	}

	/**
	 * Returns the {@code BufferedImage} this class is backed by.
	 *
	 * @return the buffered image.
	 */
	public BufferedImage getBufferedImage() {
		return this.image;
	}

	/**
	 * Creates a <code>Graphics2D</code>, which can be used to draw into the backing <code>BufferedImage</code>.
	 *
	 * @return a <code>Graphics2D</code>, used for drawing into the backed image.
	 */
	public java.awt.Graphics2D getGraphics2D() {
		return this.image.createGraphics();
	}

	/**
	 * Returns the data of the raster.
	 *
	 * @return the data.
	 */
	public int[] getPixels() {
		return this.pixels;
	}

	/**
	 * Returns the width of the image.
	 *
	 * @return the width.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height of the image.
	 *
	 * @return the height.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Draws the backing image onto the graphics context provided.
	 *
	 * @param g the graphics.
	 * @param x the x.
	 * @param y the y.
	 */
	public void draw(Graphics g, int x, int y) {
		g.drawImage(this.image, x, y, null);
	}

	public void kill() {
	    this.pixels = null;
    }

}
