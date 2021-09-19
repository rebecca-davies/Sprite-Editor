package image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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
/**
 *
 * @author Dane
 */
public class Sprite {

	private static final Logger logger = Logger.getLogger(Sprite.class.toString());

	/**
	 * Loads an image using ImageIO and casts it to a Sprite.
	 *
	 * @param f the file.
	 * @return the sprite.
	 * @throws IOException if an error occurs during reading.
	 */
	public static final Sprite load(File f) throws IOException {
		BufferedImage image = ImageIO.read(f);
		Sprite b = new Sprite(image.getWidth(), image.getHeight());
		image.getRGB(0, 0, b.width, b.height, b.pixels, 0, b.width);
		for (int i = 0; i < b.pixels.length; i++) {
			b.pixels[i] &= ~(0xFF000000);
		}
		return b;
	}

	/**
	 * The information for each pixel.
	 */
	protected int[] pixels;

	/**
	 * The width.
	 */
	protected int width;

	/**
	 * The height.
	 */
	protected int height;

	/**
	 * Constructs a blank sprite.
	 *
	 * @param width the width.
	 * @param height the height.
	 */
	public Sprite(int width, int height) {
		this.pixels = new int[width * height];
		this.width = width;
		this.height = height;
	}

	/**
	 * Binds this sprite as the target to the {@link Graphics2D} class.
	 *
	 * @see Graphics2D
	 */
	public void bind() {
		Graphics2D.setTarget(this.pixels, this.width, this.height);
	}

	/**
	 * Replaces the color A with the color B.
	 *
	 * @param a the rgb A.
	 * @param b the rgb B.
	 */
	public void replaceRGB(int a, int b) {
		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] == a) {
				this.pixels[i] = b;
			}
		}
	}

	/**
	 * Returns the data of the sprite.
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
	 * Draws the sprite opaquely.
	 *
	 * @param x the draw x.
	 * @param y the draw y.
	 */
	public void drawOpaque(int x, int y) {
		Graphics2D.drawPixels(x, y, this.width, this.height, this.pixels);
	}

	/**
	 * Draws the sprite. (ignores black pixels)
	 *
	 * @param x the draw x.
	 * @param y the draw y.
	 */
	public void draw(int x, int y) {
		Graphics2D.drawPixels(x, y, this.width, this.height, this.pixels);
	}

	/**
	 * Draws the sprite translucently. (ignores black pixels)
	 *
	 * @param x the draw x.
	 * @param y the draw y.
	 * @param alpha the alpha.
	 */
	public void draw(int x, int y, int alpha) {
		Graphics2D.drawPixels(x, y, this.width, this.height, this.pixels, alpha);
	}

	/**
	 * Draws the sprite.
	 *
	 * @param x the draw x.
	 * @param y the draw y.
	 * @param w the draw width.
	 * @param h the draw height.
	 */
	public void draw(int x, int y, int w, int h) {
		Graphics2D.drawSprite(this, x, y, w, h);
		drawGrid(x, y, w, h);
	}

	public void drawGrid(int x, int y, int w, int h) {
	    for(int col = 0; col <= h; col += 8) {
	        Graphics2D.drawVerticalLine(x + col, y, h, 0x000000);
        }
        for(int row = 0; row <= w; row += 8) {
            Graphics2D.drawHorizontalLine(x, y + row, w, 0x000000);
        }
    }

	/**
	 * Draws the sprite rotated around a point.
	 *
	 * @param x the draw x.
	 * @param y the draw y.
	 * @param w the sprite width.
	 * @param h the sprite height.
	 * @param pivotX the pivot x.
	 * @param pivotY the pivot y.
	 * @param angle the angle.
	 * @param horizontalOffsets the horizontal offset array.
	 * @param rowWidth the pixel row width array.
	 */
	public void draw(int x, int y, int w, int h, int pivotX, int pivotY, int angle, int[] horizontalOffsets, int[] rowWidth) {
		try {
			int cx = -w / 2;
			int cy = -h / 2;

			int sin = (int) (Math.sin((double) angle / 326.11) * 65536.0);
			int cos = (int) (Math.cos((double) angle / 326.11) * 65536.0);

			int offX = (pivotX << 16) + (cy * sin + cx * cos);
			int offY = (pivotY << 16) + (cy * cos - cx * sin);
			int baseOffset = x + (y * Graphics2D.targetWidth);

			for (y = 0; y < h; y++) {
				int start = horizontalOffsets[y];
				int off = baseOffset + start;
				int srcX = offX + cos * start;
				int srcY = offY - sin * start;
				for (x = 0; x < rowWidth[y]; x++) {
					Graphics2D.target[off++] = this.pixels[(srcX >> 16) + (srcY >> 16) * this.width];
					srcX += cos;
					srcY -= sin;
				}
				offX += sin;
				offY += cos;
				baseOffset += Graphics2D.targetWidth;
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Draws the sprite rotated around a point.
	 *
	 * @param x the draw x.
	 * @param y the draw y.
	 * @param w the width.
	 * @param h the height.
	 * @param pivotX the pivot x.
	 * @param pivotY the pivot y.
	 * @param angle the angle.
	 */
	public void draw(int x, int y, int w, int h, int pivotX, int pivotY, int angle) {
		try {
			int cx = -w / 2;
			int cy = -h / 2;

			int sin = (int) (Math.sin((double) angle / 326.11) * 65536.0);
			int cos = (int) (Math.cos((double) angle / 326.11) * 65536.0);

			int offX = (pivotX << 16) + (cy * sin + cx * cos);
			int offY = (pivotY << 16) + (cy * cos - cx * sin);
			int baseOffset = x + (y * Graphics2D.targetWidth);

			for (y = 0; y < h; y++) {
				int off = baseOffset;
				int dstX = offX + cos;
				int dstY = offY - sin;

				for (x = 0; x < w; x++) {
					int rgb = pixels[(dstX >> 16) + (dstY >> 16) * width];

					if (rgb != 0) {
						Graphics2D.target[off++] = rgb;
					} else {
						off++;
					}
					dstX += cos;
					dstY -= sin;
				}

				offX += sin;
				offY += cos;
				baseOffset += Graphics2D.targetWidth;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error drawing rotated bitmap", e);
		}
	}

	/**
	 * Flips the sprite horizontally.
	 */
	public void flipHorizontally() {
		int[] flipped = new int[width * height];
		int off = 0;
		for (int y = 0; y < height; y++) {
			for (int x = width - 1; x >= 0; x--) {
				flipped[off++] = pixels[x + (y * width)];
			}
		}
		pixels = flipped;
	}

	/**
	 * Flips the sprite vertically.
	 */
	public void flipVertically() {
		int[] flipped = new int[width * height];
		int off = 0;
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				flipped[off++] = pixels[x + (y * width)];
			}
		}
		pixels = flipped;
	}

}
