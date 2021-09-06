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
package util;

/**
 *
 * @author Dane
 */
public class Colors {

	public static final int WHITE = 0xFFFFFF;
	public static final int BLACK = 0x000000;
	public static final int RED = 0xFF0000;
	public static final int GREEN = 0x00FF00;
	public static final int BLUE = 0x0000FF;
	public static final int YELLOW = 0xFFFF00;
	public static final int PINK = 0xFF00FF;
	public static final int CYAN = 0x00FFFF;
	public static final int ORANGE = 0xFF7F00;
	public static final int PURPLE = 0x7F00FF;
	public static final int HOTPINK = 0xFF007F;
	public static final int SKYBLUE = 0x87CEEB;

	/**
	 * Converts an INT24_RGB value to INT16_HSL.
	 *
	 * @param rgb the rgb.
	 * @return the HSL.
	 */
	public static int rgbToHSL16(int rgb) {
		return hsl24To16(rgbToHSL24(rgb));
	}

	/**
	 * Converts an INT24_RGB value to INT16_HSL.
	 *
	 * @param r the red channel.
	 * @param g the green channel.
	 * @param b the blue channel.
	 * @return the HSL.
	 */
	public static int rgbToHSL16(int r, int g, int b) {
		return hsl24To16(rgbToHSL24((r << 16) | (g << 8) | b));
	}

	/**
	 * Converts the red, green, and blue values to INT24_HSL.
	 *
	 * @param r the red.
	 * @param g the green.
	 * @param b the blue.
	 * @return the hsl.
	 */
	public static int rgbToHSL24(int r, int g, int b) {
		return Colors.rgbToHSL24((r << 16) | (g << 8) | b);
	}

	/**
	 * Converts an INT24_RGB value to INT24_HSL.
	 *
	 * @param rgb the rgb.
	 * @return the HSL.
	 */
	public static int rgbToHSL24(int rgb) {
		double r = (double) ((rgb >> 16) & 0xFF) / 255.0;
		double g = (double) ((rgb >> 8) & 0xFF) / 255.0;
		double b = (double) (rgb & 0xFF) / 255.0;

		return Colors.rgbToHSL24(r, g, b);
	}

	/**
	 * Converts an INT24_RGB value to INT24_HSL.
	 *
	 * @param r the red channel.
	 * @param g the green channel.
	 * @param b the blue channel.
	 * @return the HSL.
	 */
	public static int rgbToHSL24(double r, double g, double b) {
		double min = Math.min(Math.min(r, g), b);
		double max = Math.max(Math.max(r, g), b);

		double hue = 0.0;
		double saturation = 0.0;
		double lightness = (min + max) / 2.0;

		if (min != max) {
			if (lightness < 0.5) {
				saturation = (max - min) / (max + min);
			}
			if (lightness >= 0.5) {
				saturation = (max - min) / (2.0 - max - min);
			}

			if (r == max) {
				hue = (g - b) / (max - min);
			} else if (g == max) {
				hue = 2.0 + (b - r) / (max - min);
			} else if (b == max) {
				hue = 4.0 + (r - g) / (max - min);
			}
		}

		hue /= 6.0;

		return ((int) (hue * 255.0) << 16) | ((int) (saturation * 255.0) << 8) | (int) (lightness * 255.0);
	}

	/**
	 * Converts INT24_HSL to INT16_HSL. A format usually used with the palette generated in RuneTek 3 engines. (Lossy)
	 *
	 * @param hsl
	 * @return the hsl16.
	 */
	public static int hsl24To16(int hsl) {
		int hue = (hsl >> 16) & 0xFF;
		int saturation = (hsl >> 8) & 0xFF;
		int lightness = hsl & 0xFF;

		return ((hue / 4) << 10) | ((saturation / 32) << 7) | (lightness >> 1);
	}

	private Colors() {
	}
}
