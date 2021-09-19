package image;

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
import media.Model;

import java.awt.Font;
import java.util.Arrays;

/**
 *
 * @author Dane
 */
public class Graphics2D {

	/**
	 * The array being modified by the operations in this class.
	 */
	public static int[] target;

	/**
	 * The font being used to draw strings.
	 */
	public static BitmapFont font;

	/**
	 * The dimensions of the destintaion.
	 */
	public static int targetWidth, targetHeight;

	/**
	 * The clipped drawing boundaries.
	 */
	public static int left, top, right, bottom;

	/**
	 * The center of the destination.
	 */
	public static int halfWidth, halfHeight;

	/**
	 * The rightmost horizontal position in our destination.
	 */
	public static int rightX;

	/**
	 * Used for drawing ovals.
	 */
	private static final int[] ovalPointX = new int[1024], ovalPointY = new int[1024];

	/**
	 * Offsets for drawing pixels
	 */
	private static int targetOffset, srcOffset;

	/**
	 * Stride length for drawing pixels
	 */
	private static int targetStep, srcStep;

	/**
	 * Dimension for drawing pixels
	 */
	private static int drawWidth, drawHeight;

	static {
		// I just wanted a default font to play with.
		try {
			font = new BitmapFont(new Font("Helvetica", Font.PLAIN, 12));
		} catch (Exception e) {
		}
	}

	/**
	 * Fills our target with 0's.
	 */
	public static void clear() {
		clear(0);
	}

	/**
	 * Fills our target with the provided color.
	 *
	 * @param rgb the clear color. (INT24_RGB)
	 */
	public static void clear(int rgb) {
		Arrays.fill(target, rgb);
	}

	/**
	 * Sets our destination and resets the boundaries to accomodate.
	 *
	 * @param pixels the pixels.
	 * @param width the width.
	 * @param height the height.
	 */
	public static void setTarget(int[] pixels, int width, int height) {
		Graphics2D.target = pixels;
		Graphics2D.targetWidth = width;
		Graphics2D.targetHeight = height;
		setBounds(0, 0, width, height);
	}

	/**
	 * Resets the boundaries to fit the destintaion.
	 */
	public static void resetBounds() {
		left = 0;
		top = 0;
		right = 0;
		bottom = 0;
		rightX = right - 1;
		halfWidth = right / 2;
		halfHeight = bottom / 2;
	}

	/**
	 * Sets the area which we allow ourselves to draw into.
	 *
	 * @param left the leftmost horizontal pixel.
	 * @param top the topmost vertical pixel.
	 * @param right the rightmost horizontal pixel.
	 * @param bottom the bottommost vertical pixel.
	 */
	public static void setBounds(int left, int top, int right, int bottom) {
		if (left < 0) {
			left = 0;
		}

		if (right > targetWidth) {
			right = targetWidth;
		}

		if (top < 0) {
			top = 0;
		}

		if (bottom > targetHeight) {
			bottom = targetHeight;
		}

		Graphics2D.left = left;
		Graphics2D.top = top;
		Graphics2D.right = right;
		Graphics2D.bottom = bottom;
		Graphics2D.rightX = right - 1;
		Graphics2D.halfWidth = right / 2;
		Graphics2D.halfHeight = bottom / 2;
	}

	/**
	 * Fills an opaque oval.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param rgb the color. (INT24_RGB)
	 * @param points the segment count.
	 * @param angle the starting angle.
	 */
	public static void fillOval(int x, int y, int w, int h, int rgb, int points, int angle) {
		if (points < 3) {
			return;
		}

		int hw = w / 2;
		int hh = h / 2;

		x += hw;
		y += hh;

		for (int i = 0; i < points; i++) {
			int a = angle + ((i << 11) / points);

			a %= 2047; // keep it within the 0-2047 range

			ovalPointX[i] = x + ((hw * Model.cos[a]) >> 16);
			ovalPointY[i] = y + ((hh * Model.sin[a]) >> 16);
		}

		int cx = x;
		int cy = y;

		for (int i = 1; i < points; i++) {
			x = ovalPointX[i - 1];
			y = ovalPointY[i - 1];

			Graphics3D.fillTriangleDepth(cx, cy, 0, x, y, 0, ovalPointX[i], ovalPointY[i], 0, rgb);
		}

		// fill from last point to first point (last triangle)
		Graphics3D.fillTriangleDepth(cx, cy, 0, ovalPointX[0], ovalPointY[0], 0, ovalPointX[points - 1], ovalPointY[points - 1], 0, rgb);
	}

	/**
	 * Fills an opaque oval.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param rgb the color. (INT24_RGB)
	 * @param segments the segment count.
	 * @param alpha the alpha. (0-FF)
	 * @param angle the starting angle.
	 */
	public static void fillOval(int x, int y, int w, int h, int rgb, int segments, int angle, int alpha) {
		Graphics3D.alpha = alpha;
		fillOval(x, y, w, h, rgb, segments, angle);
	}

	/**
	 * Fills an opaque circle.
	 *
	 * @param x the center x of the circle.
	 * @param y the center y of the circle.
	 * @param radius the radius of the circle.
	 * @param color the color. (INT24_RGB)
	 */
	public static void fillCircle(int x, int y, int radius, int color) {
		int radius2 = radius * radius; // used to avoid Math.sqrt

		for (int xA = x - radius; xA < x + radius; xA++) {
			if (xA < left || xA >= right) {
				continue;
			}

			for (int yA = y - radius; yA < y + radius; yA++) {
				if (yA < top || yA >= bottom) {
					continue;
				}

				int xD = xA - x;
				int yD = yA - y;
				int distance2 = xD * xD + yD * yD;

				if (distance2 < radius2) { // hey look! no sqrt
					target[xA + (yA * targetWidth)] = color;
				}
			}
		}
	}

	/**
	 * Fills an opaque circle.
	 *
	 * @param x the center x of the circle.
	 * @param y the center y of the circle.
	 * @param radius the radius of the circle.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void fillCircle(int x, int y, int radius, int color, int alpha) {
		int radius2 = radius * radius;

		color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);

		int alphaB = 256 - alpha;

		for (int xA = x - radius; xA < x + radius; xA++) {
			if (xA < left || xA > right) {
				continue;
			}

			for (int yA = y - radius; yA < y + radius; yA++) {
				if (yA < top || yA > bottom) {
					continue;
				}

				int xD = (xA - x);
				int yD = (yA - y);
				int distance2 = (xD * xD + yD * yD);

				if (distance2 < radius2) {
					int pos = xA + (yA * targetWidth);
					int old = target[pos];
					old = ((old & 0xFF00FF) * alphaB >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaB >> 8 & 0xFF00);
					target[pos] = color + old;
				}
			}
		}
	}

	/**
	 * Draws an opaque rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 */
	public static void drawRect(int x, int y, int w, int h, int color) {
		drawHorizontalLine(x, y, w, color);
		drawHorizontalLine(x, y + h - 1, w, color);
		drawVerticalLine(x, y, h, color);
		drawVerticalLine(x + w - 1, y, h, color);
	}

	/**
	 * Draws an opaque rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void drawRect(int x, int y, int w, int h, int color, int alpha) {
		drawHorizontalLine(x, y, w, color, alpha);
		drawHorizontalLine(x, y + h - 1, w, color, alpha);
		if (h > 2) {
			drawVerticalLine(x, y + 1, h - 2, color, alpha);
			drawVerticalLine(x + w - 1, y + 1, h - 2, color, alpha);
		}
	}

	/**
	 * Fills an opaque rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 */
	public static void fillRect(int x, int y, int w, int h, int color) {
		if (x < left) {
			w -= left - x;
			x = left;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (x + w > right) {
			w = right - x;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		int step = targetWidth - w;
		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				target[pos++] = color;
			}
			pos += step;
		}
	}

	/**
	 * Fills a translucent rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void fillRect(int x, int y, int w, int h, int color, int alpha) {
		if (x < left) {
			w -= left - x;
			x = left;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (x + w > right) {
			w = right - x;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);
		int alphaInverted = 256 - alpha;
		int step = targetWidth - w;
		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int old = target[pos];
				target[pos++] = color + ((old & 0xFF00FF) * alphaInverted >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaInverted >> 8 & 0xFF00);
			}
			pos += step;
		}
	}

	/**
	 * Draws a horizontal line.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the line width.
	 * @param color the color. (INT24_RGB)
	 */
	public static void drawHorizontalLine(int x, int y, int w, int color) {
		if (y >= top && y < bottom) {
			if (x < left) {
				w -= left - x;
				x = left;
			}

			if (x + w > right) {
				w = right - x;
			}

			int pos = x + y * targetWidth;

			for (int i = 0; i < w; i++) {
				target[pos++] = color;
			}
		}
	}

	/**
	 * Draws a vertical line.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param h the line height.
	 * @param color the color. (INT24_RGB)
	 */
	public static void drawVerticalLine(int x, int y, int h, int color) {
		if (x < left || x >= right) {
			return;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			target[pos] = color;
			pos += targetWidth;
		}
	}

	/**
	 * Draws a horizontal line with an alpha channel.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the line width.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-0xFF)
	 */
	public static void drawHorizontalLine(int x, int y, int w, int color, int alpha) {
		if (y >= top && y < bottom) {
			if (x < left) {
				w -= left - x;
				x = left;
			}

			if (x + w > right) {
				w = right - x;
			}

			color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);
			int alphaInverted = 256 - alpha;
			int pos = x + y * targetWidth;

			for (int i = 0; i < w; i++) {
				int old = target[pos];
				old = ((old & 0xFF00FF) * alphaInverted >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaInverted >> 8 & 0xFF00);
				target[pos++] = color + old;
			}
		}
	}

	/**
	 * Draws a vertical line.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param h the line height.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void drawVerticalLine(int x, int y, int h, int color, int alpha) {
		if (x < left || x >= right) {
			return;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);
		int alphaInverted = 256 - alpha;
		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			int old = target[pos];
			old = ((old & 0xFF00FF) * alphaInverted >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaInverted >> 8 & 0xFF00);
			target[pos] = color + old;
			pos += targetWidth;
		}
	}

	/**
	 * A helper method used to clip bounds and return whether the boundary is larger than one pixel or naw.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @return w > 0 && h > 0
	 */
	public static final boolean testBounds(int x, int y, int w, int h) {
		targetOffset = x + (y * targetWidth);
		srcOffset = 0;
		targetStep = (int) (targetWidth - w);
		srcStep = 0;

		// clip the top
		if (y < top) {
			int cut = (int) (top - y);
			h -= cut;
			y = top;
			srcOffset += cut * w;
			targetOffset += cut * targetWidth;
		}

		// clip the bottom
		if (y + h > bottom) {
			h -= (y + h) - bottom;
		}

		// clip the left
		if (x < left) {
			int cut = (int) (left - x);
			w -= cut;
			x = left;

			srcOffset += cut;
			targetOffset += cut;
			srcStep += cut;
			targetStep += cut;
		}

		// clip the right
		if (x + w > right) {
			int cut = (int) ((x + w) - right);
			w -= cut;
			srcStep += cut;
			targetStep += cut;
		}

		drawWidth = (int) w;
		drawHeight = (int) h;

		return w > 0 && h > 0;
	}

	/**
	 * Fills in the mask with the provided color on the target.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param mask the mask.
	 * @param rgb the fill color.
	 */
	public static void drawPixelMask(int x, int y, int w, int h, byte[] mask, int rgb) {
		if (testBounds(x, y, w, h)) {
			for (y = 0; y < drawHeight; y++) {
				for (x = 0; x < drawWidth; x++) {
					byte b = mask[srcOffset++];
					if (b != 0) {
						target[targetOffset++] = rgb;
					} else {
						targetOffset++;
					}
				}
				targetOffset += targetStep;
				srcOffset += srcStep;
			}
		}
	}

	/**
	 * Draws the provided pixels to the target.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the pixel array width.
	 * @param h the pixel array height.
	 * @param pixels the pixel array.
	 */
	public static void drawPixels(int x, int y, int w, int h, int[] pixels) {
		if (testBounds(x, y, w, h)) {
			for (y = 0; y < drawHeight; y++) {
				for (x = 0; x < drawWidth; x++) {
					int rgb = pixels[srcOffset++];
					if (rgb != 0) {
						target[targetOffset++] = rgb;
					} else {
						targetOffset++;
					}
				}
				targetOffset += targetStep;
				srcOffset += srcStep;
			}
		}
	}

	/**
	 * draws the provided pixels to the target translucently.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the pixel array width.
	 * @param h the pixel array height.
	 * @param pixels the pixel array.
	 * @param alpha the alpha.
	 */
	public static void drawPixels(int x, int y, int w, int h, int[] pixels, int alpha) {
		int alphaInverted = 256 - alpha;
		if (testBounds(x, y, w, h)) {
			for (y = 0; y < drawHeight; y++) {
				for (x = 0; x < drawWidth; x++) {
					int src = pixels[srcOffset++];

					src = ((src & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((src & 0xFF00) * alpha >> 8 & 0xFF00);

					if (src != 0) {
						int dst = target[targetOffset];
						target[targetOffset++] = ((((src & 0xff00ff) * alpha + (dst & 0xff00ff) * alphaInverted) & ~0xff00ff) + (((src & 0xff00) * alpha + (dst & 0xff00) * alphaInverted) & 0xff0000)) >> 8;
					} else {
						targetOffset++;
					}
				}
				targetOffset += targetStep;
				srcOffset += srcStep;
			}
		}
	}

	/**
	 * Draws a sprite to the target.
	 *
	 * @param s the sprite.
	 * @param x the x.
	 * @param y the y.
	 * @param w the draw width.
	 * @param h the draw height.
	 */
	public static void drawSprite(Sprite s, int x, int y, int w, int h) {
		if (w <= 1 || h <= 1) {
			return;
		}

		targetOffset = x + (y * targetWidth);
		targetStep = targetWidth - w;

		// our texture coordinates as 24.8 fixed points
		int u = 0, v = 0;
		int uStep = (s.width << 8) / w;
		int vStep = (s.height << 8) / h;

		// clip the top
		if (y < top) {
			int cut = top - y;
			h -= cut;
			y = top;

			v += vStep * cut;
			targetOffset += cut * targetWidth;
		}

		// clip the bottom
		if (y + h > bottom) {
			h -= (y + h) - bottom;
		}

		// clip the left
		if (x < left) {
			int cut = left - x;
			w -= cut;
			x = left;

			u += (cut << 8);
			targetOffset += cut;
			targetStep += cut;
		}

		// clip the right
		if (x + w > right) {
			int cut = (x + w) - right;
			w -= cut;
			targetStep += cut;
		}

		// if our image is too small then just don't draw it.
		if (w <= 1 || h <= 1) {
			return;
		}

		// local reference
		int[] pixels = s.pixels;

		// we'll be coming back to this.
		int startU = u;

		for (y = 0; y < h; y++) {

			// this only needs to be calculated once per row
			int vOffset = (v >> 8) * s.width;

			// loop through row
			for (x = 0; x < w; x++) {
				// apply values
                int rgb = pixels[(u >> 8) + vOffset];
                if (rgb != 0) {
                    target[targetOffset++] = rgb;
                } else {
                    targetOffset++;
                }
                u += uStep;
			}

			// step down a row of pixels on the destination
			targetOffset += Graphics2D.targetWidth - w;

			// step down vertically
			v += vStep;

			// reset back to left
			u = startU;

		}
	}

	/**
	 * Draws a string to the target with the current set font.
	 *
	 * @param s the string.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 */
	public static void drawString(String s, int x, int y, int rgb) {
		drawString(s, x, y, rgb, 0);
	}

	/**
	 * Draws a string to the target with the current set font.
	 *
	 * @param s the string.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 * @param flags the flags.
	 * @see BitmapFont#CENTER_X
	 */
	public static void drawString(String s, int x, int y, int rgb, int flags) {
		// we don't have a font to draw with :(
		if (font == null) {
			return;
		}

		font.drawString(s, x, y, rgb, flags);
	}

	private Graphics2D() {
	}
}
