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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Dane
 */
public class BitmapFont {

	/**
	 * The horizontal flag center constant.
	 */
	public static final int CENTER_X = (1 << 0);

	/**
	 * The vertical flag center constant.
	 */
	public static final int CENTER_Y = (1 << 1);

	/**
	 * The shadow flag constant.
	 */
	public static final int SHADOW = (1 << 2);

	/**
	 * The minimum ASCII char index.
	 */
	public static final int MIN_CHAR = 0;

	/**
	 * The maximum ASCII char index.
	 */
	public static final int MAX_CHAR = 127;

	/**
	 * The ASCII char count.
	 */
	public static final int CHAR_COUNT = MAX_CHAR - MIN_CHAR;

	/**
	 * The character masks.
	 */
	public byte[][] charMask;

	/**
	 * The character widths.
	 */
	public int[] charWidth;

	/**
	 * The font height.
	 */
	public int height;

	/**
	 * Creates a new font from the provided input stream and derives our BitmapFont from it.
	 *
	 * @param in the input stream.
	 * @param size the font size.
	 * @throws FontFormatException if the <code>fontStream</code> data does not contain the required font tables for the
	 * specified format.
	 * @throws IOException if the <code>fontStream</code> cannot be completely read.
	 */
	public BitmapFont(InputStream in, int size) throws IOException, FontFormatException {
		this(Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.PLAIN, size));
	}

	/**
	 * Creates a new font from the provided input stream and derives our BitmapFont from it.
	 *
	 * @param in the input stream.
	 * @param style the font style.
	 * @param size the font size.
	 * @throws FontFormatException if the <code>fontStream</code> data does not contain the required font tables for the
	 * specified format.
	 * @throws IOException if the <code>fontStream</code> cannot be completely read.
	 */
	public BitmapFont(InputStream in, int style, int size) throws IOException, FontFormatException {
		this(Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(style, size));
	}

	/**
	 * Creates a new font from the provided input stream and derives our BitmapFont from it.
	 *
	 * @param f the font to derive from.
	 */
	public BitmapFont(Font f) {
		int size = (int) f.getSize2D() * 2;

		BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics g = i.getGraphics();
		g.setFont(f);
		g.setColor(Color.WHITE);

		charMask = new byte[CHAR_COUNT][];
		charWidth = new int[CHAR_COUNT];

		FontMetrics m = g.getFontMetrics(f);
		int ascent = m.getAscent();
		height = m.getHeight();

		for (int c = 0; c < CHAR_COUNT; c++) {
			charWidth[c] = m.charWidth(c);
			charMask[c] = new byte[charWidth[c] * height];

			// clear image
			g.clearRect(0, 0, size, size);

			// draw character
			g.drawString(String.valueOf((char) (c + MIN_CHAR)), 0, ascent);

			int j = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < charWidth[c]; x++) {
					charMask[c][j++] = (byte) i.getRGB(x, y);
				}
			}
		}

		// bye!
		g.dispose();
	}

	/**
	 * Returns the width of the string's characters.
	 *
	 * @param s the string.
	 * @return the width of all the characters.
	 */
	public final int stringWidth(final String s) {
		if (s == null) {
			return 0;
		}
		return stringWidth(s, 0, s.length());
	}

	/**
	 * Returns the width of the string's characters.
	 *
	 * @param s the string.
	 * @param start the first character.
	 * @param end the last character.
	 * @return the width of all the characters between the first and last characters.
	 */
	public final int stringWidth(final String s, final int start, final int end) {
		if (s == null) {
			return 0;
		}
		int width = 0;
		for (int n = start; n < end; n++) {
			int i = s.charAt(n);
			if (i >= MIN_CHAR && i <= MAX_CHAR) {
				width += charWidth[i - MIN_CHAR];
			}
		}
		return width;
	}

	/**
	 * Draws an object as a string.
	 *
	 * @param o the object.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 */
	public final void drawObject(final Object o, int x, int y, final int rgb) {
		BitmapFont.this.drawString(String.valueOf(o), x, y, rgb);
	}

	/**
	 * Draws a string.
	 *
	 * @param s the string.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 */
	public final void drawString(final String s, int x, int y, final int rgb) {
		BitmapFont.this.drawString(s, x, y, rgb, 0);
	}

	/**
	 * Draws an object as a string.
	 *
	 * @param o the object.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 * @param flags the flags. (Default is 0, which has no effect)
	 * @see #CENTER_X
	 * @see #CENTER_Y
	 * @see #SHADOW
	 */
	public final void drawObject(final Object o, int x, int y, final int rgb, final int flags) {
		BitmapFont.this.drawString(String.valueOf(o), x, y, rgb, flags);
	}

	/**
	 * Draws a string.
	 *
	 * @param s the string.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 * @param flags the flags. (Default is 0, which has no effect)
	 * @see #CENTER_X
	 * @see #CENTER_Y
	 * @see #SHADOW
	 */
	public final void drawString(final String s, final int x, final int y, final int rgb, final int flags) {
		drawString(s, 0, s.length(), x, y, rgb, flags);
	}

	/**
	 * Draws a string.
	 *
	 * @param s the string.
	 * @param start the first character index.
	 * @param end the last character index.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 * @param flags the flags. (Default is 0, which has no effect)
	 * @see #CENTER_X
	 * @see #CENTER_Y
	 * @see #SHADOW
	 */
	public final void drawString(final String s, final int start, final int end, int x, int y, int rgb, int flags) {
		if (s == null) {
			return;
		}

		if ((flags & CENTER_X) == CENTER_X) {
			x -= stringWidth(s, start, end) / 2;
		}

		if ((flags & CENTER_Y) == CENTER_Y) {
			y -= height / 2;
		}

		final boolean shadowed = (flags & SHADOW) == SHADOW;

		for (int n = start; n < end; n++) {
			char c = s.charAt(n);

			if (c >= MIN_CHAR && c <= MAX_CHAR) {
				c -= MIN_CHAR;

				if (shadowed) {
					drawCharacter(c, x + 1, y + 1, 0);
				}

				drawCharacter(c, x, y, rgb);
				x += charWidth[c];
			}
		}
	}

	/**
	 * Draws a character.
	 *
	 * @param c the char.
	 * @param x the x.
	 * @param y the y.
	 * @param rgb the color.
	 */
	public final void drawCharacter(final char c, final int x, final int y, final int rgb) {
		Graphics2D.drawPixelMask(x, y, charWidth[c], height, charMask[c], rgb);
	}
}
