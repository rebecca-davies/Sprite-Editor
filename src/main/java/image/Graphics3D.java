package image;

import image.Graphics2D;
import media.Model;
import java.util.Arrays;

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
public class Graphics3D {

	/**
	 * Determines whether to use choppy looking shading
	 */
	public static boolean texturedShading = true;

	/**
	 * The center of our 3d target
	 */
	public static int centerX, centerY;

	/**
	 * A lookup table for 17.15 fixed point fractions.
	 */
	public static int[] oneOverFixed1715 = new int[512];

	/**
	 * A lookup table for 16.16 fixed point fractions.
	 */
	public static int[] oneOverFixed1616 = new int[2048];

	/**
	 * A sine lookup table. PI = 1024 (180 Degrees)
	 */
	public static int[] sin = new int[2048];

	/**
	 * A cosine lookup table. PI = 1024 (180 Degrees)
	 */
	public static int[] cos = new int[2048];

	/**
	 * Whether to test x boundaries when drawing scanlines
	 */
	public static boolean testX;

	/**
	 * The alpha component for drawing triangles
	 */
	public static int alpha;

	/**
	 * When set to true, the renderer ignores blending the alpha component.
	 */
	public static boolean opaque;

	/**
	 * A vertical pixel offset lookup table
	 */
	public static int[] offsets;

	/**
	 * Stores RGB values that can be looked up with an HSL value
	 */
	public static int[] palette;

	/**
	 * Stores an array of zbuffer values.
	 */
	public static int[] depthBuffer;

	static {
		for (int i = 1; i < 512; i++) {
			oneOverFixed1715[i] = (1 << 15) / i;
		}

		for (int i = 1; i < 2048; i++) {
			oneOverFixed1616[i] = (1 << 16) / i;
		}

		for (int i = 0; i < 2048; i++) {
			sin[i] = (int) (65536.0 * Math.sin((double) i * 0.0030679615));
			cos[i] = (int) (65536.0 * Math.cos((double) i * 0.0030679615));
		}

		palette = new int[128 * 512];
	}

	/**
	 * Generates the vertical pixel offsets using the width and height set in {@link Graphics2D}.
	 *
	 * @return the int[] of y offsets.
	 */
	public static final int[] setOffsets() {
		offsets = new int[Graphics2D.targetHeight];
		for (int y = 0; y < Graphics2D.targetHeight; y++) {
			offsets[y] = Graphics2D.targetWidth * y;
		}
		centerX = Graphics2D.targetWidth / 2;
		centerY = Graphics2D.targetHeight / 2;
		return offsets;
	}

	/**
	 * Generates the vertical pixel offsets.
	 *
	 * @param w the width.
	 * @param h the height.
	 * @return the int[] of y offsets.
	 */
	public static final int[] setOffsets(int w, int h) {
		offsets = new int[h];
		for (int y = 0; y < h; y++) {
			offsets[y] = w * y;
		}
		centerX = w / 2;
		centerY = h / 2;
		return offsets;
	}

	/**
	 * Resets the zbuffer.
	 */
	public static final void clearDepthBuffer() {
		Arrays.fill(depthBuffer, Model.FAR_Z_1616);
	}

	/**
	 * Creates the new zbuffer for the current target.
	 *
	 * @return the int[] zbuffer.
	 */
	public static final int[] setDepthBuffer() {
		return setDepthBuffer(Graphics2D.targetWidth, Graphics2D.targetHeight);
	}

	/**
	 * Creates the new zbuffer.
	 *
	 * @param w the width.
	 * @param h the height.
	 * @return the int[] zbuffer.
	 */
	public static final int[] setDepthBuffer(int w, int h) {
		depthBuffer = new int[w * h];
		clearDepthBuffer();
		return depthBuffer;
	}

	/**
	 * Adjusts the input RGB's brightness.
	 *
	 * @param rgb the color. (INT24_RGB)
	 * @param exponent the exponent.
	 * @return rgb^exponent.
	 */
	public static int setBrightness(int rgb, double exponent) {
		double r = (double) (rgb >> 16) / 256.0;
		double g = (double) (rgb >> 8 & 0xff) / 256.0;
		double b = (double) (rgb & 0xff) / 256.0;
		r = Math.pow(r, exponent);
		g = Math.pow(g, exponent);
		b = Math.pow(b, exponent);
		return ((int) (r * 256.0) << 16) + ((int) (g * 256.0) << 8) + (int) (b * 256.0);
	}

	/**
	 * Generates an HSL to RGB lookup table, also known as <i>palette</i>.
	 *
	 * @param exponent the brightness on a 0.0 to 1.0 scale.
	 */
	public static final void createPalette(double exponent) {
		int off = 0;

		for (int y = 0; y < 512; y++) {
			double hue = (double) (y / 8) / 64.0 + 0.0078125;
			double saturation = (double) (y & 0x7) / 8.0 + 0.0625;

			for (int x = 0; x < 128; x++) {
				double lightness = (double) x / 128.0;
				double r = lightness;
				double g = lightness;
				double b = lightness;

				if (saturation != 0.0) {
					double d_36_;

					if (lightness < 0.5) {
						d_36_ = lightness * (1.0 + saturation);
					} else {
						d_36_ = lightness + saturation - lightness * saturation;
					}

					double d_37_ = 2.0 * lightness - d_36_;
					double d_38_ = hue + 0.3333333333333333;

					if (d_38_ > 1.0) {
						d_38_--;
					}

					double d_39_ = hue;
					double d_40_ = hue - 0.3333333333333333;

					if (d_40_ < 0.0) {
						d_40_++;
					}

					if (6.0 * d_38_ < 1.0) {
						r = d_37_ + (d_36_ - d_37_) * 6.0 * d_38_;
					} else if (2.0 * d_38_ < 1.0) {
						r = d_36_;
					} else if (3.0 * d_38_ < 2.0) {
						r = d_37_ + (d_36_ - d_37_) * (0.6666666666666666 - d_38_) * 6.0;
					} else {
						r = d_37_;
					}

					if (6.0 * d_39_ < 1.0) {
						g = d_37_ + (d_36_ - d_37_) * 6.0 * d_39_;
					} else if (2.0 * d_39_ < 1.0) {
						g = d_36_;
					} else if (3.0 * d_39_ < 2.0) {
						g = d_37_ + (d_36_ - d_37_) * (0.6666666666666666 - d_39_) * 6.0;
					} else {
						g = d_37_;
					}

					if (6.0 * d_40_ < 1.0) {
						b = d_37_ + (d_36_ - d_37_) * 6.0 * d_40_;
					} else if (2.0 * d_40_ < 1.0) {
						b = d_36_;
					} else if (3.0 * d_40_ < 2.0) {
						b = d_37_ + (d_36_ - d_37_) * (0.6666666666666666 - d_40_) * 6.0;
					} else {
						b = d_37_;
					}
				}

				int rgb = ((int) (r * 256.0) << 16) + ((int) (g * 256.0) << 8) + (int) (b * 256.0);
				rgb = setBrightness(rgb, exponent);
				palette[off++] = rgb;
			}
		}
	}

	/**
	 * Fills a triangle.
	 *
	 * @param xA first point x
	 * @param yA first point y
	 * @param xB second point x
	 * @param yB second point y
	 * @param xC third point x
	 * @param yC third point y
	 * @param color the color of the triangle. (in INT24_RGB format)
	 */
	public static final void fillTriangleDepth(int xA, int yA, int zA, int xB, int yB, int zB, int xC, int yC, int zC, int color) {
		int slopeAB = 0;
		int slopeBC = 0;
		int slopeCA = 0;

		int zSlopeAB = 0;
		int zSlopeBC = 0;
		int zSlopeCA = 0;

		if (yB != yA) {
			slopeAB = (xB - xA << 16) / (yB - yA);
			zSlopeAB = (zB - zA << 16) / (yB - yA);
		}

		if (yC != yB) {
			slopeBC = (xC - xB << 16) / (yC - yB);
			zSlopeBC = (zC - zB << 16) / (yC - yB);
		}

		if (yC != yA) {
			slopeCA = (xA - xC << 16) / (yA - yC);
			zSlopeCA = (zA - zC << 16) / (yA - yC);
		}

		// A is above B and C
		if (yA <= yB && yA <= yC) {

			// A is below the bottom of our drawing area.
			if (yA >= Graphics2D.bottom) {
				return;
			}

			// Clamp B's Y
			if (yB > Graphics2D.bottom) {
				yB = Graphics2D.bottom;
			}

			// Clamp C's Y
			if (yC > Graphics2D.bottom) {
				yC = Graphics2D.bottom;
			}

			// B is above C
			if (yB < yC) {
				// xC is now xA and they are both 16.16
				xC = xA <<= 16;
				zC = zA <<= 16;

				// A is above our drawing area
				if (yA < 0) {
					xC -= slopeCA * yA;
					zC -= zSlopeCA * yA;

					xA -= slopeAB * yA;
					zA -= zSlopeAB * yA;

					yA = 0;
				}

				// 32.0 -> 16.16
				xB <<= 16;
				zB <<= 16;

				// B is above our drawing area
				if (yB < 0) {
					xB -= slopeBC * yB;
					zB -= zSlopeBC * yB;

					yB = 0;
				}

				//
				// If A isn't in parallel horizontally with B and the slope from C to A is lower than the slope from A to B.
				// Or, if A and B are in parallel horizontally and the slope from C to A is greater than the slope from B to C.
				//
				// if statement:
				//
				// yA != yB && slopeCA < slopeAB:
				// A (3, 4)
				// |\
				// |  \
				// |    \B (8, 8)
				// |    /
				// |  /
				// |/
				// C (3, 12)
				// slopeAB = (8 - 3) / (8 - 4) = 5 / 4 = 1.25
				// slopeBC = (3 - 8) / (12 - 8) = -5 / 4 = -1.25
				// slopeCA = 0
				//
				//
				// if statement:
				//
				// yA == yB && slopeCA > slopeBC
				// A (3, 4)___B (8, 4)
				// |         /
				// |      /
				// |   /
				// |/
				// C (3, 8)
				//
				// slopeAB = (8 - 3) / (4 - 4) = 0
				// slopeBC = (3 - 8) / (8 - 4) = -5 / 4 = -1.25
				// slopeCA = (3 - 3) / (4 - 8) = 0 / -4 = 0
				//
				//
				// slopeAB = (xB - xA) / (yB - yA)
				// slopeBC = (xC - xB) / (yC - yB)
				// slopeCA = (xA - xC) / (yA - yC)
				if (yA != yB && slopeCA < slopeAB || yA == yB && slopeCA > slopeBC) {
					// yC is now the distance from yB to yC in pixels
					yC -= yB;

					// yB is now the distance from yA to yB in pixels
					yB -= yA;

					// yA is now the offset for our current Y position.
					yA = offsets[yA];

					// While we still have a vertical space between A and B
					while (--yB >= 0) {
						// Draw our scanline from xC (start) to xA (end) starting at the offset provided by yA
						drawScanlineDepth(Graphics2D.target, yA, 0, xC >> 16, xA >> 16, zC, zA, color);

						// approach xC to xA
						xC += slopeCA;
						zC += zSlopeCA;

						// approach xA to xB
						xA += slopeAB;
						zA += zSlopeAB;

						// Go down a line
						yA += Graphics2D.targetWidth;
					}

					// While we still have a vertical space between B and C
					while (--yC >= 0) {
						// Draw our scanline from xC (start) to xB (end) starting at the offset provided by yA
						drawScanlineDepth(Graphics2D.target, yA, 0, xC >> 16, xB >> 16, zC, zB, color);

						// Approach C to A horizontally
						xC += slopeCA;
						zC += zSlopeCA;

						// Approach B to C horizontally
						xB += slopeBC;
						zB += zSlopeBC;

						// Move down a line
						yA += Graphics2D.targetWidth;
					}
				} else {
					// yC is now the distance from yB to yC in pixels
					yC -= yB;

					// yB is now the distance from yA to yB in pixels
					yB -= yA;

					// yA is now the offset for our current Y position.
					yA = offsets[yA];

					// While we still have a vertical space between A and B
					while (--yB >= 0) {
						// Draw our scanline from xC (start) to xC (end) starting at the offset provided by yA
						drawScanlineDepth(Graphics2D.target, yA, 0, xA >> 16, xC >> 16, zA, zC, color);

						// Approach C to A horizontally
						xC += slopeCA;
						zC += zSlopeCA;

						// Aproach A to B horizontally
						xA += slopeAB;
						zA += zSlopeAB;

						// Move down a line
						yA += Graphics2D.targetWidth;
					}

					// While we still have a vertical space between B and C
					while (--yC >= 0) {
						// Draw our scanline from xB (start) to xC (end) starting at the offset provided by yA
						drawScanlineDepth(Graphics2D.target, yA, 0, xB >> 16, xC >> 16, zB, zC, color);

						// Approach C to A horizontally
						xC += slopeCA;
						zC += zSlopeCA;

						// Approach B to C horizontally
						xB += slopeBC;
						zB += zSlopeBC;

						// Move down a line
						yA += Graphics2D.targetWidth;
					}
				}
			} else {
				xB = xA <<= 16;
				zB = zA <<= 16;

				if (yA < 0) {
					xB -= slopeCA * yA;
					zB -= zSlopeCA * yA;

					xA -= slopeAB * yA;
					zA -= zSlopeAB * yA;

					yA = 0;
				}

				xC <<= 16;
				zC <<= 16;

				if (yC < 0) {
					xC -= slopeBC * yC;
					zC -= zSlopeBC * yC;
					yC = 0;
				}

				if (yA != yC && slopeCA < slopeAB || yA == yC && slopeBC > slopeAB) {
					yB -= yC;
					yC -= yA;

					yA = offsets[yA];

					while (--yC >= 0) {
						drawScanlineDepth(Graphics2D.target, yA, 0, xB >> 16, xA >> 16, zB, zA, color);

						xB += slopeCA;
						zB += zSlopeCA;

						xA += slopeAB;
						zA += zSlopeAB;

						yA += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawScanlineDepth(Graphics2D.target, yA, 0, xC >> 16, xA >> 16, zC, zA, color);

						xC += slopeBC;
						zC += zSlopeBC;

						xA += slopeAB;
						zA += zSlopeAB;

						yA += Graphics2D.targetWidth;
					}
				} else {
					yB -= yC;
					yC -= yA;
					yA = offsets[yA];

					while (--yC >= 0) {
						drawScanlineDepth(Graphics2D.target, yA, 0, xA >> 16, xB >> 16, zA, zB, color);

						xB += slopeCA;
						zB += zSlopeCA;

						xA += slopeAB;
						zA += zSlopeAB;

						yA += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawScanlineDepth(Graphics2D.target, yA, 0, xA >> 16, xC >> 16, zA, zC, color);

						xC += slopeBC;
						zC += zSlopeBC;

						xA += slopeAB;
						zA += zSlopeAB;

						yA += Graphics2D.targetWidth;
					}
				}
			}
			// else A is below B or C, and B is above C.
		} else if (yB <= yC) {
			if (yB < Graphics2D.bottom) {
				if (yC > Graphics2D.bottom) {
					yC = Graphics2D.bottom;
				}

				if (yA > Graphics2D.bottom) {
					yA = Graphics2D.bottom;
				}

				if (yC < yA) {
					xA = xB <<= 16;
					zA = zB <<= 16;

					if (yB < 0) {
						xA -= slopeAB * yB;
						zA -= zSlopeAB * yB;

						xB -= slopeBC * yB;
						zB -= zSlopeBC * yB;

						yB = 0;
					}

					xC <<= 16;
					zC <<= 16;

					if (yC < 0) {
						xC -= slopeCA * yC;
						zC -= zSlopeCA * yC;

						yC = 0;
					}

					if (yB != yC && slopeAB < slopeBC || yB == yC && slopeAB > slopeCA) {
						yA -= yC;
						yC -= yB;

						yB = offsets[yB];

						while (--yC >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xA >> 16, xB >> 16, zA, zB, color);

							xA += slopeAB;
							zA += zSlopeAB;

							xB += slopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yA >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xA >> 16, xC >> 16, zA, zC, color);

							xA += slopeAB;
							zA += zSlopeAB;

							xC += slopeCA;
							zC += zSlopeCA;

							yB += Graphics2D.targetWidth;
						}
					} else {
						yA -= yC;
						yC -= yB;

						yB = offsets[yB];

						while (--yC >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xB >> 16, xA >> 16, zB, zA, color);

							xA += slopeAB;
							zA += zSlopeAB;

							xB += slopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yA >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xC >> 16, xA >> 16, zC, zA, color);

							xA += slopeAB;
							zA += zSlopeAB;

							xC += slopeCA;
							zC += zSlopeCA;

							yB += Graphics2D.targetWidth;
						}
					}
				} else {
					xC = xB <<= 16;
					zC = zB <<= 16;

					if (yB < 0) {
						xC -= slopeAB * yB;
						zC -= zSlopeAB * yB;

						xB -= slopeBC * yB;
						zB -= zSlopeBC * yB;

						yB = 0;
					}

					xA <<= 16;
					zA <<= 16;

					if (yA < 0) {
						xA -= slopeCA * yA;
						zA -= zSlopeCA * yA;

						yA = 0;
					}

					if (slopeAB < slopeBC) {
						yC -= yA;
						yA -= yB;
						yB = offsets[yB];

						while (--yA >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xC >> 16, xB >> 16, zC, zB, color);

							xC += slopeAB;
							zC += zSlopeAB;

							xB += slopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yC >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xA >> 16, xB >> 16, zA, zB, color);

							xA += slopeCA;
							zA += zSlopeCA;

							xB += slopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}
					} else {
						yC -= yA;
						yA -= yB;

						yB = offsets[yB];

						while (--yA >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xB >> 16, xC >> 16, zB, zC, color);

							xC += slopeAB;
							zC += zSlopeAB;

							xB += slopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yC >= 0) {
							drawScanlineDepth(Graphics2D.target, yB, 0, xB >> 16, xA >> 16, zB, zA, color);

							xA += slopeCA;
							zA += zSlopeCA;

							xB += slopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}
					}
				}
			}
		} else if (yC < Graphics2D.bottom) {
			if (yA > Graphics2D.bottom) {
				yA = Graphics2D.bottom;
			}

			if (yB > Graphics2D.bottom) {
				yB = Graphics2D.bottom;
			}

			if (yA < yB) {
				xB = xC <<= 16;
				zB = zC <<= 16;

				if (yC < 0) {
					xB -= slopeBC * yC;
					zB -= zSlopeBC * yC;

					xC -= slopeCA * yC;
					zC -= zSlopeCA * yC;

					yC = 0;
				}

				xA <<= 16;
				zA <<= 16;

				if (yA < 0) {
					xA -= slopeAB * yA;
					zA -= zSlopeAB * yA;

					yA = 0;
				}

				if (slopeBC < slopeCA) {
					yB -= yA;
					yA -= yC;

					yC = offsets[yC];

					while (--yA >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xB >> 16, xC >> 16, zB, zC, color);

						xB += slopeBC;
						zB += zSlopeBC;

						xC += slopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xB >> 16, xA >> 16, zB, zA, color);

						xB += slopeBC;
						zB += zSlopeBC;

						xA += slopeAB;
						zA += zSlopeAB;

						yC += Graphics2D.targetWidth;
					}
				} else {
					yB -= yA;
					yA -= yC;

					yC = offsets[yC];

					while (--yA >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xC >> 16, xB >> 16, zC, zB, color);

						xB += slopeBC;
						zB += zSlopeBC;

						xC += slopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xA >> 16, xB >> 16, zA, zB, color);

						xB += slopeBC;
						zB += zSlopeBC;

						xA += slopeAB;
						zA += zSlopeAB;

						yC += Graphics2D.targetWidth;
					}
				}
			} else {
				xA = xC <<= 16;
				zA = zC <<= 16;

				if (yC < 0) {
					xA -= slopeBC * yC;
					zA -= zSlopeBC * yC;

					xC -= slopeCA * yC;
					zC -= zSlopeCA * yC;

					yC = 0;
				}

				xB <<= 16;
				zB <<= 16;

				if (yB < 0) {
					xB -= slopeAB * yB;
					zB -= zSlopeAB * yB;

					yB = 0;
				}

				if (slopeBC < slopeCA) {
					yA -= yB;
					yB -= yC;

					yC = offsets[yC];

					while (--yB >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xA >> 16, xC >> 16, zA, zC, color);

						xA += slopeBC;
						zA += zSlopeBC;

						xC += slopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}

					while (--yA >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xB >> 16, xC >> 16, zB, zC, color);

						xB += slopeAB;
						zB += zSlopeAB;

						xC += slopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}
				} else {
					yA -= yB;
					yB -= yC;
					yC = offsets[yC];

					while (--yB >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xC >> 16, xA >> 16, zC, zA, color);

						xA += slopeBC;
						zA += zSlopeBC;

						xC += slopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}

					while (--yA >= 0) {
						drawScanlineDepth(Graphics2D.target, yC, 0, xC >> 16, xB >> 16, zC, zB, color);

						xB += slopeAB;
						zB += zSlopeAB;

						xC += slopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}
				}
			}
		}
	}

	/**
	 * Draws a scanline.
	 *
	 * @param dst the destination.
	 * @param off the initial offset.
	 * @param rgb the color.
	 * @param length the length.
	 * @param xA the start x.
	 * @param xB the end x.
	 */
	public static final void drawScanlineDepth(int[] dst, int off, int length, int xA, int xB, int zA, int zB, int rgb) {
		if (xA >= xB) {
			return;
		}

		int zSlope = (zB - zA) / (xB - xA);

		if (testX) {
			if (xB > Graphics2D.rightX) {
				xB = Graphics2D.rightX;
			}

			if (xA < 0) {
				zA -= xA * zSlope;
				xA = 0;
			}

			if (xA >= xB) {
				return;
			}
		}

		length = xB - xA;

		off += xA;

		if (alpha == 0) {
			while (--length >= 0) {
				if (zA <= depthBuffer[off]) {
					depthBuffer[off] = zA;
					dst[off++] = rgb;
				} else {
					off++;
				}
				zA += zSlope;
			}

			zA += zSlope;
		} else {
			int alphaA = alpha;
			int alphaB = 256 - alpha;
			rgb = (((rgb & 0xFF00FF) * alphaB >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * alphaB >> 8 & 0xFF00));

			while (--length >= 0) {
				if (zA <= depthBuffer[off]) {
					depthBuffer[off] = zA;
					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * alphaA >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * alphaA >> 8 & 0xFF00));
					off++;
				} else {
					off++;
				}
				zA += zSlope;
			}
		}
	}

	/**
	 * Fills a triangle using the gouraud shading technique.<p>
	 * <b>Warning:</b>
	 * Only interpolates the <i>lightness</i> channel of the provided colors for each point. That means you cannot
	 * select a different hue or saturation between points!</p>
	 *
	 * @param xA first point x.
	 * @param yA first point y.
	 * @param xB second point x.
	 * @param yB second point y.
	 * @param xC third point x.
	 * @param yC third point y.
	 * @param colorA first point color in HSL format.
	 * @param colorB second point color in HSL format.
	 * @param colorC third point color in HSL format.
	 */
	public static final void fillShadedTriangle(int xA, int yA, int xB, int yB, int xC, int yC, int colorA, int colorB, int colorC) {
		// All slopes are 16.16 fixed points
		// All light slopes are 17.15 fixed points
		int slopeAB = 0;
		int lightSlopeAB = 0;

		// What's going on here:
		// The slopes are being transformed into 16.16 or 17.15 fixed points.
		if (yB != yA) {
			slopeAB = ((xB - xA) << 16) / (yB - yA);
			lightSlopeAB = (colorB - colorA << 15) / (yB - yA);
		}

		int slopeBC = 0;
		int lightSlopeBC = 0;

		if (yC != yB) {
			slopeBC = ((xC - xB) << 16) / (yC - yB);
			lightSlopeBC = (colorC - colorB << 15) / (yC - yB);
		}

		int slopeCA = 0;
		int lightSlopeCA = 0;

		if (yC != yA) {
			slopeCA = ((xA - xC) << 16) / (yA - yC);
			lightSlopeCA = (colorA - colorC << 15) / (yA - yC);
		}

		if (yA <= yB && yA <= yC) {
			if (yA >= Graphics2D.bottom) {
				return;
			}

			if (yB > Graphics2D.bottom) {
				yB = Graphics2D.bottom;
			}

			if (yC > Graphics2D.bottom) {
				yC = Graphics2D.bottom;
			}

			if (yB < yC) {
				// transform into 16.16 fixed point
				xC = xA <<= 16;

				// transform into 17.15 fixed point
				colorC = colorA <<= 15;

				if (yA < 0) {
					xC -= slopeCA * yA;
					xA -= slopeAB * yA;
					colorC -= lightSlopeCA * yA;
					colorA -= lightSlopeAB * yA;
					yA = 0;
				}

				// transform into 16.16 fixed point
				xB <<= 16;

				// transform into 17.15 fixed point
				colorB <<= 15;

				if (yB < 0) {
					xB -= slopeBC * yB;
					colorB -= lightSlopeBC * yB;
					yB = 0;
				}

				if (yA != yB && slopeCA < slopeAB || yA == yB && slopeCA > slopeBC) {
					// yC is now the difference between B and C vertically
					yC -= yB;

					// yB is now the difference between A and B vertically
					yB -= yA;

					// yA is now our vertical offset.
					yA = offsets[yA];

					// while we have a vertical gap between A and B
					while (--yB >= 0) {
						// Notice the right shifts of 7
						// Those are transforming the 17.15 fixed points to 24.8! How exciting!
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xC >> 16, xA >> 16, colorC >> 7, colorA >> 7);

						// approach xC to xA
						xC += slopeCA;
						colorC += lightSlopeCA;

						// approach xA to xB
						xA += slopeAB;
						colorA += lightSlopeAB;

						// move yA down a row of pixels.
						yA += Graphics2D.targetWidth;
					}

					// while we have a vertical gap between B and C
					while (--yC >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xC >> 16, xB >> 16, colorC >> 7, colorB >> 7);

						xC += slopeCA;
						colorC += lightSlopeCA;

						xB += slopeBC;
						colorB += lightSlopeBC;

						yA += Graphics2D.targetWidth;
					}
				} else {
					yC -= yB;
					yB -= yA;
					yA = offsets[yA];

					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xA >> 16, xC >> 16, colorA >> 7, colorC >> 7);
						xC += slopeCA;
						xA += slopeAB;
						colorC += lightSlopeCA;
						colorA += lightSlopeAB;
						yA += Graphics2D.targetWidth;
					}

					while (--yC >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xB >> 16, xC >> 16, colorB >> 7, colorC >> 7);
						xC += slopeCA;
						xB += slopeBC;
						colorC += lightSlopeCA;
						colorB += lightSlopeBC;
						yA += Graphics2D.targetWidth;
					}
				}
			} else {
				xB = xA <<= 16;
				colorB = colorA <<= 15;

				if (yA < 0) {
					xB -= slopeCA * yA;
					xA -= slopeAB * yA;
					colorB -= lightSlopeCA * yA;
					colorA -= lightSlopeAB * yA;
					yA = 0;
				}

				xC <<= 16;
				colorC <<= 15;

				if (yC < 0) {
					xC -= slopeBC * yC;
					colorC -= lightSlopeBC * yC;
					yC = 0;
				}

				if (yA != yC && slopeCA < slopeAB || yA == yC && slopeBC > slopeAB) {
					yB -= yC;
					yC -= yA;
					yA = offsets[yA];

					while (--yC >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xB >> 16, xA >> 16, colorB >> 7, colorA >> 7);
						xB += slopeCA;
						xA += slopeAB;
						colorB += lightSlopeCA;
						colorA += lightSlopeAB;
						yA += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xC >> 16, xA >> 16, colorC >> 7, colorA >> 7);
						xC += slopeBC;
						xA += slopeAB;
						colorC += lightSlopeBC;
						colorA += lightSlopeAB;
						yA += Graphics2D.targetWidth;
					}
				} else {
					yB -= yC;
					yC -= yA;
					yA = offsets[yA];

					while (--yC >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xA >> 16, xB >> 16, colorA >> 7, colorB >> 7);
						xB += slopeCA;
						xA += slopeAB;
						colorB += lightSlopeCA;
						colorA += lightSlopeAB;
						yA += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yA, 0, 0, xA >> 16, xC >> 16, colorA >> 7, colorC >> 7);
						xC += slopeBC;
						xA += slopeAB;
						colorC += lightSlopeBC;
						colorA += lightSlopeAB;
						yA += Graphics2D.targetWidth;
					}
				}
			}
		} else if (yB <= yC) {
			if (yB < Graphics2D.bottom) {
				if (yC > Graphics2D.bottom) {
					yC = Graphics2D.bottom;
				}

				if (yA > Graphics2D.bottom) {
					yA = Graphics2D.bottom;
				}

				if (yC < yA) {
					xA = xB <<= 16;
					colorA = colorB <<= 15;
					if (yB < 0) {
						xA -= slopeAB * yB;
						xB -= slopeBC * yB;
						colorA -= lightSlopeAB * yB;
						colorB -= lightSlopeBC * yB;
						yB = 0;
					}
					xC <<= 16;
					colorC <<= 15;
					if (yC < 0) {
						xC -= slopeCA * yC;
						colorC -= lightSlopeCA * yC;
						yC = 0;
					}
					if (yB != yC && slopeAB < slopeBC || yB == yC && slopeAB > slopeCA) {
						yA -= yC;
						yC -= yB;
						yB = offsets[yB];
						while (--yC >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xA >> 16, xB >> 16, colorA >> 7, colorB >> 7);
							xA += slopeAB;
							xB += slopeBC;
							colorA += lightSlopeAB;
							colorB += lightSlopeBC;
							yB += Graphics2D.targetWidth;
						}
						while (--yA >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xA >> 16, xC >> 16, colorA >> 7, colorC >> 7);
							xA += slopeAB;
							xC += slopeCA;
							colorA += lightSlopeAB;
							colorC += lightSlopeCA;
							yB += Graphics2D.targetWidth;
						}
					} else {
						yA -= yC;
						yC -= yB;
						yB = offsets[yB];
						while (--yC >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xB >> 16, xA >> 16, colorB >> 7, colorA >> 7);
							xA += slopeAB;
							xB += slopeBC;
							colorA += lightSlopeAB;
							colorB += lightSlopeBC;
							yB += Graphics2D.targetWidth;
						}
						while (--yA >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xC >> 16, xA >> 16, colorC >> 7, colorA >> 7);
							xA += slopeAB;
							xC += slopeCA;
							colorA += lightSlopeAB;
							colorC += lightSlopeCA;
							yB += Graphics2D.targetWidth;
						}
					}
				} else {
					xC = xB <<= 16;
					colorC = colorB <<= 15;
					if (yB < 0) {
						xC -= slopeAB * yB;
						xB -= slopeBC * yB;
						colorC -= lightSlopeAB * yB;
						colorB -= lightSlopeBC * yB;
						yB = 0;
					}
					xA <<= 16;
					colorA <<= 15;
					if (yA < 0) {
						xA -= slopeCA * yA;
						colorA -= lightSlopeCA * yA;
						yA = 0;
					}
					if (slopeAB < slopeBC) {
						yC -= yA;
						yA -= yB;
						yB = offsets[yB];
						while (--yA >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xC >> 16, xB >> 16, colorC >> 7, colorB >> 7);
							xC += slopeAB;
							xB += slopeBC;
							colorC += lightSlopeAB;
							colorB += lightSlopeBC;
							yB += Graphics2D.targetWidth;
						}
						while (--yC >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xA >> 16, xB >> 16, colorA >> 7, colorB >> 7);
							xA += slopeCA;
							xB += slopeBC;
							colorA += lightSlopeCA;
							colorB += lightSlopeBC;
							yB += Graphics2D.targetWidth;
						}
					} else {
						yC -= yA;
						yA -= yB;
						yB = offsets[yB];
						while (--yA >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xB >> 16, xC >> 16, colorB >> 7, colorC >> 7);
							xC += slopeAB;
							xB += slopeBC;
							colorC += lightSlopeAB;
							colorB += lightSlopeBC;
							yB += Graphics2D.targetWidth;
						}
						while (--yC >= 0) {
							drawShadedScanline(Graphics2D.target, yB, 0, 0, xB >> 16, xA >> 16, colorB >> 7, colorA >> 7);
							xA += slopeCA;
							xB += slopeBC;
							colorA += lightSlopeCA;
							colorB += lightSlopeBC;
							yB += Graphics2D.targetWidth;
						}
					}
				}
			}
		} else if (yC < Graphics2D.bottom) {
			if (yA > Graphics2D.bottom) {
				yA = Graphics2D.bottom;
			}
			if (yB > Graphics2D.bottom) {
				yB = Graphics2D.bottom;
			}
			if (yA < yB) {
				xB = xC <<= 16;
				colorB = colorC <<= 15;
				if (yC < 0) {
					xB -= slopeBC * yC;
					xC -= slopeCA * yC;
					colorB -= lightSlopeBC * yC;
					colorC -= lightSlopeCA * yC;
					yC = 0;
				}
				xA <<= 16;
				colorA <<= 15;
				if (yA < 0) {
					xA -= slopeAB * yA;
					colorA -= lightSlopeAB * yA;
					yA = 0;
				}
				if (slopeBC < slopeCA) {
					yB -= yA;
					yA -= yC;
					yC = offsets[yC];
					while (--yA >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xB >> 16, xC >> 16, colorB >> 7, colorC >> 7);
						xB += slopeBC;
						xC += slopeCA;
						colorB += lightSlopeBC;
						colorC += lightSlopeCA;
						yC += Graphics2D.targetWidth;
					}
					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xB >> 16, xA >> 16, colorB >> 7, colorA >> 7);
						xB += slopeBC;
						xA += slopeAB;
						colorB += lightSlopeBC;
						colorA += lightSlopeAB;
						yC += Graphics2D.targetWidth;
					}
				} else {
					yB -= yA;
					yA -= yC;
					yC = offsets[yC];
					while (--yA >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xC >> 16, xB >> 16, colorC >> 7, colorB >> 7);
						xB += slopeBC;
						xC += slopeCA;
						colorB += lightSlopeBC;
						colorC += lightSlopeCA;
						yC += Graphics2D.targetWidth;
					}
					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xA >> 16, xB >> 16, colorA >> 7, colorB >> 7);
						xB += slopeBC;
						xA += slopeAB;
						colorB += lightSlopeBC;
						colorA += lightSlopeAB;
						yC += Graphics2D.targetWidth;
					}
				}
			} else {
				xA = xC <<= 16;
				colorA = colorC <<= 15;
				if (yC < 0) {
					xA -= slopeBC * yC;
					xC -= slopeCA * yC;
					colorA -= lightSlopeBC * yC;
					colorC -= lightSlopeCA * yC;
					yC = 0;
				}
				xB <<= 16;
				colorB <<= 15;
				if (yB < 0) {
					xB -= slopeAB * yB;
					colorB -= lightSlopeAB * yB;
					yB = 0;
				}
				if (slopeBC < slopeCA) {
					yA -= yB;
					yB -= yC;
					yC = offsets[yC];
					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xA >> 16, xC >> 16, colorA >> 7, colorC >> 7);
						xA += slopeBC;
						xC += slopeCA;
						colorA += lightSlopeBC;
						colorC += lightSlopeCA;
						yC += Graphics2D.targetWidth;
					}
					while (--yA >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xB >> 16, xC >> 16, colorB >> 7, colorC >> 7);
						xB += slopeAB;
						xC += slopeCA;
						colorB += lightSlopeAB;
						colorC += lightSlopeCA;
						yC += Graphics2D.targetWidth;
					}
				} else {
					yA -= yB;
					yB -= yC;
					yC = offsets[yC];
					while (--yB >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xC >> 16, xA >> 16, colorC >> 7, colorA >> 7);
						xA += slopeBC;
						xC += slopeCA;
						colorA += lightSlopeBC;
						colorC += lightSlopeCA;
						yC += Graphics2D.targetWidth;
					}
					while (--yA >= 0) {
						drawShadedScanline(Graphics2D.target, yC, 0, 0, xC >> 16, xB >> 16, colorC >> 7, colorB >> 7);
						xB += slopeAB;
						xC += slopeCA;
						colorB += lightSlopeAB;
						colorC += lightSlopeCA;
						yC += Graphics2D.targetWidth;
					}
				}
			}
		}
	}

	/**
	 * Draws a scanline and linearly translates the lightness.
	 *
	 * @param dst the destination.
	 * @param off the initial offset.
	 * @param rgb the INT24_RGB.
	 * @param length the length.
	 * @param xA the start x.
	 * @param xB the end x.
	 * @param colorA the start color. (24.8)
	 * @param colorB the end color. (24.8)
	 */
	public static final void drawShadedScanline(int[] dst, int off, int rgb, int length, int xA, int xB, int colorA, int colorB) {
		if (texturedShading) {
			int lightnessSlope;

			if (testX) {
				if (xB - xA > 3) {
					// notice no fixed point transformations here?
					// that's because they're still fixed points!
					// At this point, colorA and colorB are 24.8 fixed points. :)
					lightnessSlope = (colorB - colorA) / (xB - xA);
				} else {
					lightnessSlope = 0;
				}

				if (xB > Graphics2D.rightX) {
					xB = Graphics2D.rightX;
				}

				// clip off screen part and recalculate initial color
				if (xA < 0) {
					colorA -= xA * lightnessSlope;
					xA = 0;
				}

				// if we start ahead of our end point, don't do anything.
				if (xA >= xB) {
					return;
				}

				off += xA;
				length = xB - xA >> 2;
				lightnessSlope <<= 2;
			} else {
				if (xA >= xB) {
					return;
				}

				off += xA;
				length = xB - xA >> 2;

				if (length > 0) {
					lightnessSlope = (colorB - colorA) * oneOverFixed1715[length] >> 15;
				} else {
					lightnessSlope = 0;
				}
			}

			if (alpha == 0) {
				while (--length >= 0) {
					rgb = palette[colorA >> 8];
					colorA += lightnessSlope;
					dst[off++] = rgb;
					dst[off++] = rgb;
					dst[off++] = rgb;
					dst[off++] = rgb;
				}

				length = xB - xA & 0x3;

				if (length > 0) {
					rgb = palette[colorA >> 8];
					do {
						dst[off++] = rgb;
					} while (--length > 0);
				}
			} else {
				int a0 = alpha;
				int a1 = 256 - alpha;

				while (--length >= 0) {
					rgb = palette[colorA >> 8];
					colorA += lightnessSlope;
					rgb = (((rgb & 0xFF00FF) * a1 >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * a1 >> 8 & 0xFF00));
					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;
					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;
					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;
					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;
				}

				length = xB - xA & 0x3;

				if (length > 0) {
					rgb = palette[colorA >> 8];
					rgb = (((rgb & 0xFF00FF) * a1 >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * a1 >> 8 & 0xFF00));
					do {
						dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
						off++;
					} while (--length > 0);
				}
			}
		} else if (xA < xB) {
			int lightnessSlope = (colorB - colorA) / (xB - xA);

			if (testX) {
				if (xB > Graphics2D.rightX) {
					xB = Graphics2D.rightX;
				}

				if (xA < 0) {
					colorA -= xA * lightnessSlope;
					xA = 0;
				}

				if (xA >= xB) {
					return;
				}
			}

			off += xA;
			length = xB - xA;

			if (alpha == 0) {
				do {
					dst[off++] = palette[colorA >> 8];
					colorA += lightnessSlope;
				} while (--length > 0);
			} else {
				int a0 = alpha;
				int a1 = 256 - alpha;
				do {
					rgb = palette[colorA >> 8];
					colorA += lightnessSlope;
					rgb = (((rgb & 0xFF00FF) * a1 >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * a1 >> 8 & 0xFF00));
					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;
				} while (--length > 0);
			}
		}
	}

	/**
	 * Fills a triangle using the gouraud shading technique.<p>
	 * <b>Warning:</b>
	 * Only interpolates the <i>lightness</i> channel of the provided colors for each point. That means you cannot
	 * select a different hue or saturation between points!</p>
	 *
	 * @param xA first point x
	 * @param yA first point y
	 * @param zA first point depth
	 * @param xB second point x
	 * @param yB second point y
	 * @param zB second point depth
	 * @param xC third point x
	 * @param yC third point y
	 * @param zC third point depth
	 * @param colorA first point color in HSL format.
	 * @param colorB second point color in HSL format.
	 * @param colorC third point color in HSL format.
	 */
	public static final void fillShadedTriangleDepth(int xA, int yA, int zA, int xB, int yB, int zB, int xC, int yC, int zC, int colorA, int colorB, int colorC) {
		int slopeAB = 0;
		int slopeBC = 0;
		int slopeCA = 0;

		int lightSlopeAB = 0;
		int lightSlopeBC = 0;
		int lightSlopeCA = 0;

		int zSlopeAB = yB - yA; // temporary value
		int zSlopeBC = yC - yB; // temporary value
		int zSlopeCA = yA - yC; // temporary value

		if (yB != yA) {
			slopeAB = (xB - xA << 16) / zSlopeAB;
			lightSlopeAB = (colorB - colorA << 15) / zSlopeAB;
			zSlopeAB = (zB - zA << 16) / zSlopeAB;
		}

		if (yC != yB) {
			slopeBC = (xC - xB << 16) / zSlopeBC;
			lightSlopeBC = (colorC - colorB << 15) / zSlopeBC;
			zSlopeBC = (zC - zB << 16) / zSlopeBC;
		}

		if (yC != yA) {
			slopeCA = (xA - xC << 16) / zSlopeCA;
			lightSlopeCA = (colorA - colorC << 15) / zSlopeCA;
			zSlopeCA = (zA - zC << 16) / zSlopeCA;
		}

		if (yA <= yB && yA <= yC) {
			if (yA >= Graphics2D.bottom) {
				return;
			}

			if (yB > Graphics2D.bottom) {
				yB = Graphics2D.bottom;
			}

			if (yC > Graphics2D.bottom) {
				yC = Graphics2D.bottom;
			}

			if (yB < yC) {
				xC = xA <<= 16;
				colorC = colorA <<= 15;
				zC = zA <<= 16;

				if (yA < 0) {
					xC -= slopeCA * yA;
					colorC -= lightSlopeCA * yA;
					zC -= zSlopeCA * yA;

					xA -= slopeAB * yA;
					colorA -= lightSlopeAB * yA;
					zA -= zSlopeAB * yA;

					yA = 0;
				}

				xB <<= 16;
				colorB <<= 15;
				zB <<= 16;

				if (yB < 0) {
					xB -= slopeBC * yB;
					colorB -= lightSlopeBC * yB;
					zB -= zSlopeBC * yB;

					yB = 0;
				}

				if (yA != yB && slopeCA < slopeAB || yA == yB && slopeCA > slopeBC) {
					// yC is now the difference between B and C vertically
					yC -= yB;

					// yB is now the difference between A and B vertically
					yB -= yA;

					// yA is now our vertical offset.
					yA = offsets[yA];

					// while we have a vertical gap between A and B
					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xC >> 16, xA >> 16, zC, zA, colorC >> 7, colorA >> 7);

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						// move yA down a row of pixels.
						yA += Graphics2D.targetWidth;
					}

					// while we have a vertical gap between B and C
					while (--yC >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xC >> 16, xB >> 16, zC, zB, colorC >> 7, colorB >> 7);

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						xB += slopeBC;
						colorB += lightSlopeBC;
						zB += zSlopeBC;

						yA += Graphics2D.targetWidth;
					}
				} else {
					yC -= yB;
					yB -= yA;
					yA = offsets[yA];

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xA >> 16, xC >> 16, zA, zC, colorA >> 7, colorC >> 7);

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						yA += Graphics2D.targetWidth;
					}

					while (--yC >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xB >> 16, xC >> 16, zB, zC, colorB >> 7, colorC >> 7);

						xB += slopeBC;
						colorB += lightSlopeBC;
						zB += zSlopeBC;

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						yA += Graphics2D.targetWidth;
					}
				}
			} else {
				xB = xA <<= 16;
				colorB = colorA <<= 15;
				zB = zA <<= 16;

				if (yA < 0) {
					xB -= slopeCA * yA;
					colorB -= lightSlopeCA * yA;
					zB -= zSlopeCA * yA;

					xA -= slopeAB * yA;
					colorA -= lightSlopeAB * yA;
					zA -= zSlopeAB * yA;

					yA = 0;
				}

				xC <<= 16;
				colorC <<= 15;
				zC <<= 16;

				if (yC < 0) {
					xC -= slopeBC * yC;
					colorC -= lightSlopeBC * yC;
					zC -= zSlopeBC * yC;

					yC = 0;
				}

				if (yA != yC && slopeCA < slopeAB || yA == yC && slopeBC > slopeAB) {
					yB -= yC;
					yC -= yA;
					yA = offsets[yA];

					while (--yC >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xB >> 16, xA >> 16, zB, zA, colorB >> 7, colorA >> 7);

						xB += slopeCA;
						colorB += lightSlopeCA;
						zB += zSlopeCA;

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						yA += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xC >> 16, xA >> 16, zC, zA, colorC >> 7, colorA >> 7);

						xC += slopeBC;
						colorC += lightSlopeBC;
						zC += zSlopeBC;

						xA += slopeAB;
						colorA += lightSlopeAB;
						yA += Graphics2D.targetWidth;
					}
				} else {
					yB -= yC;
					yC -= yA;
					yA = offsets[yA];

					while (--yC >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xA >> 16, xB >> 16, zA, zB, colorA >> 7, colorB >> 7);

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						xB += slopeCA;
						colorB += lightSlopeCA;
						zB += zSlopeCA;

						yA += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yA, 0, 0, xA >> 16, xC >> 16, zA, zC, colorA >> 7, colorC >> 7);

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						xC += slopeBC;
						colorC += lightSlopeBC;
						zC += zSlopeBC;

						yA += Graphics2D.targetWidth;
					}
				}
			}
		} else if (yB <= yC) {
			if (yB < Graphics2D.bottom) {
				if (yC > Graphics2D.bottom) {
					yC = Graphics2D.bottom;
				}

				if (yA > Graphics2D.bottom) {
					yA = Graphics2D.bottom;
				}

				if (yC < yA) {
					xA = xB <<= 16;
					colorA = colorB <<= 15;
					zA = zB <<= 16;

					if (yB < 0) {
						xA -= slopeAB * yB;
						colorA -= lightSlopeAB * yB;
						zA -= zSlopeAB * yB;

						xB -= slopeBC * yB;
						colorB -= lightSlopeBC * yB;
						zB -= zSlopeBC * yB;

						yB = 0;
					}

					xC <<= 16;
					colorC <<= 15;
					zC <<= 16;

					if (yC < 0) {
						xC -= slopeCA * yC;
						colorC -= lightSlopeCA * yC;
						zC -= zSlopeCA * yC;

						yC = 0;
					}

					if (yB != yC && slopeAB < slopeBC || yB == yC && slopeAB > slopeCA) {
						yA -= yC;
						yC -= yB;

						yB = offsets[yB];

						while (--yC >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xA >> 16, xB >> 16, zA, zB, colorA >> 7, colorB >> 7);

							xA += slopeAB;
							colorA += lightSlopeAB;
							zA += zSlopeAB;

							xB += slopeBC;
							colorB += lightSlopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yA >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xA >> 16, xC >> 16, zA, zC, colorA >> 7, colorC >> 7);

							xA += slopeAB;
							colorA += lightSlopeAB;
							zA += zSlopeAB;

							xC += slopeCA;
							colorC += lightSlopeCA;
							zC += zSlopeCA;

							yB += Graphics2D.targetWidth;
						}
					} else {
						yA -= yC;
						yC -= yB;

						yB = offsets[yB];

						while (--yC >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xB >> 16, xA >> 16, zB, zA, colorB >> 7, colorA >> 7);

							xA += slopeAB;
							colorA += lightSlopeAB;
							zA += zSlopeAB;

							xB += slopeBC;
							colorB += lightSlopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yA >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xC >> 16, xA >> 16, zC, zA, colorC >> 7, colorA >> 7);

							xA += slopeAB;
							colorA += lightSlopeAB;
							zA += zSlopeAB;

							xC += slopeCA;
							colorC += lightSlopeCA;
							zC += zSlopeCA;

							yB += Graphics2D.targetWidth;
						}
					}
				} else {
					xC = xB <<= 16;
					colorC = colorB <<= 15;
					zC = zB <<= 16;

					if (yB < 0) {
						xC -= slopeAB * yB;
						colorC -= lightSlopeAB * yB;
						zC -= zSlopeAB * yB;

						xB -= slopeBC * yB;
						colorB -= lightSlopeBC * yB;
						zB -= zSlopeBC * yB;

						yB = 0;
					}

					xA <<= 16;
					colorA <<= 15;
					zA <<= 16;

					if (yA < 0) {
						xA -= slopeCA * yA;
						colorA -= lightSlopeCA * yA;
						yA = 0;
					}

					if (slopeAB < slopeBC) {
						yC -= yA;
						yA -= yB;

						yB = offsets[yB];

						while (--yA >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xC >> 16, xB >> 16, zC, zB, colorC >> 7, colorB >> 7);

							xC += slopeAB;
							colorC += lightSlopeAB;
							zC += zSlopeAB;

							xB += slopeBC;
							colorB += lightSlopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yC >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xA >> 16, xB >> 16, zA, zB, colorA >> 7, colorB >> 7);

							xA += slopeCA;
							colorA += lightSlopeCA;
							zA += zSlopeCA;

							xB += slopeBC;
							colorB += lightSlopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}
					} else {
						yC -= yA;
						yA -= yB;

						yB = offsets[yB];

						while (--yA >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xB >> 16, xC >> 16, zB, zC, colorB >> 7, colorC >> 7);

							xC += slopeAB;
							colorC += lightSlopeAB;
							zC += zSlopeAB;

							xB += slopeBC;
							colorB += lightSlopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}

						while (--yC >= 0) {
							drawShadedScanlineDepth(Graphics2D.target, yB, 0, 0, xB >> 16, xA >> 16, zB, zA, colorB >> 7, colorA >> 7);

							xA += slopeCA;
							colorA += lightSlopeCA;
							zA += zSlopeCA;

							xB += slopeBC;
							colorB += lightSlopeBC;
							zB += zSlopeBC;

							yB += Graphics2D.targetWidth;
						}
					}
				}
			}
		} else if (yC < Graphics2D.bottom) {
			if (yA > Graphics2D.bottom) {
				yA = Graphics2D.bottom;
			}

			if (yB > Graphics2D.bottom) {
				yB = Graphics2D.bottom;
			}

			if (yA < yB) {
				xB = xC <<= 16;
				colorB = colorC <<= 15;
				zB = zC <<= 16;

				if (yC < 0) {
					xB -= slopeBC * yC;
					colorB -= lightSlopeBC * yC;
					zB -= zSlopeBC * yC;

					xC -= slopeCA * yC;
					colorC -= lightSlopeCA * yC;
					zC -= zSlopeCA * yC;

					yC = 0;
				}

				xA <<= 16;
				colorA <<= 15;
				zA <<= 16;

				if (yA < 0) {
					xA -= slopeAB * yA;
					colorA -= lightSlopeAB * yA;
					zA -= zSlopeAB * yA;
					yA = 0;
				}

				if (slopeBC < slopeCA) {
					yB -= yA;
					yA -= yC;

					yC = offsets[yC];

					while (--yA >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xB >> 16, xC >> 16, zB, zC, colorB >> 7, colorC >> 7);

						xB += slopeBC;
						colorB += lightSlopeBC;
						zB += zSlopeBC;

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xB >> 16, xA >> 16, zB, zA, colorB >> 7, colorA >> 7);

						xB += slopeBC;
						colorB += lightSlopeBC;
						zB += zSlopeBC;

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						yC += Graphics2D.targetWidth;
					}
				} else {
					yB -= yA;
					yA -= yC;

					yC = offsets[yC];

					while (--yA >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xC >> 16, xB >> 16, zC, zB, colorC >> 7, colorB >> 7);

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						xB += slopeBC;
						colorB += lightSlopeBC;
						zB += zSlopeBC;

						yC += Graphics2D.targetWidth;
					}

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xA >> 16, xB >> 16, zA, zB, colorA >> 7, colorB >> 7);

						xA += slopeAB;
						colorA += lightSlopeAB;
						zA += zSlopeAB;

						xB += slopeBC;
						colorB += lightSlopeBC;
						zB += zSlopeBC;

						yC += Graphics2D.targetWidth;
					}
				}
			} else {
				xA = xC <<= 16;
				colorA = colorC <<= 15;
				zA = zC <<= 16;

				if (yC < 0) {
					xA -= slopeBC * yC;
					colorA -= lightSlopeBC * yC;
					zA -= zSlopeBC * yC;

					xC -= slopeCA * yC;
					colorC -= lightSlopeCA * yC;
					zC -= zSlopeCA * yC;

					yC = 0;
				}

				xB <<= 16;
				colorB <<= 15;
				zB <<= 16;

				if (yB < 0) {
					xB -= slopeAB * yB;
					colorB -= lightSlopeAB * yB;
					zB -= zSlopeAB * yB;

					yB = 0;
				}

				if (slopeBC < slopeCA) {
					yA -= yB;
					yB -= yC;

					yC = offsets[yC];

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xA >> 16, xC >> 16, zA, zC, colorA >> 7, colorC >> 7);

						xA += slopeBC;
						colorA += lightSlopeBC;
						zA += zSlopeBC;

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}

					while (--yA >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xB >> 16, xC >> 16, zB, zC, colorB >> 7, colorC >> 7);

						xB += slopeAB;
						colorB += lightSlopeAB;
						zB += zSlopeAB;

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						yC += Graphics2D.targetWidth;
					}
				} else {
					yA -= yB;
					yB -= yC;

					yC = offsets[yC];

					while (--yB >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xC >> 16, xA >> 16, zC, zA, colorC >> 7, colorA >> 7);

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						xA += slopeBC;
						colorA += lightSlopeBC;
						zA += zSlopeBC;

						yC += Graphics2D.targetWidth;
					}

					while (--yA >= 0) {
						drawShadedScanlineDepth(Graphics2D.target, yC, 0, 0, xC >> 16, xB >> 16, zC, zB, colorC >> 7, colorB >> 7);

						xC += slopeCA;
						colorC += lightSlopeCA;
						zC += zSlopeCA;

						xB += slopeAB;
						colorB += lightSlopeAB;
						zB += zSlopeAB;

						yC += Graphics2D.targetWidth;
					}
				}
			}
		}
	}

	/**
	 * Draws a scanline and linearly translates the lightness.
	 *
	 * @param dst the destination.
	 * @param off the initial offset.
	 * @param rgb the INT24_RGB.
	 * @param length the length.
	 * @param xA the start x.
	 * @param xB the end x.
	 * @param zA the start depth.
	 * @param zB the end depth.
	 * @param colorA the start color. (24.8)
	 * @param colorB the end color. (24.8)
	 */
	public static final void drawShadedScanlineDepth(int[] dst, int off, int rgb, int length, int xA, int xB, int zA, int zB, int colorA, int colorB) {
		if (texturedShading) {
			int lightnessSlope;
			int zSlope;

			if (testX) {
				if (xB - xA > 3) {
					lightnessSlope = (colorB - colorA) / (xB - xA);
					zSlope = (zB - zA) / (xB - xA);
				} else {
					lightnessSlope = 0;
					zSlope = 0;
				}

				if (xB > Graphics2D.rightX) {
					xB = Graphics2D.rightX;
				}

				// clip off screen part and recalculate initial color
				if (xA < 0) {
					colorA -= xA * lightnessSlope;
					zA -= xA * zSlope;

					xA = 0;
				}

				// if we start ahead of our end point, don't do anything.
				if (xA >= xB) {
					return;
				}

				off += xA;
				length = (xB - xA) >> 2;
				lightnessSlope <<= 2;
				zSlope <<= 2;
			} else {
				if (xA >= xB) {
					return;
				}

				off += xA;
				length = (xB - xA) >> 2;

				if (length > 0) {
					lightnessSlope = ((colorB - colorA) * oneOverFixed1715[length]) >> 15;
					zSlope = ((zB - zA) * oneOverFixed1616[length]) >> 16;
				} else {
					lightnessSlope = 0;
					zSlope = 0;
				}
			}

			if (alpha == 0) {
				while (--length >= 0) {
					rgb = palette[colorA >> 8];
					colorA += lightnessSlope;
					zA += zSlope;

					for (int i = 0; i < 4; i++) {
						if (zA <= depthBuffer[off]) {
							dst[off] = rgb;
							depthBuffer[off] = zA;
						}
						off++;
					}
				}

				length = xB - xA & 0x3;

				if (length > 0) {
					rgb = palette[colorA >> 8];

					do {
						if (zA <= depthBuffer[off]) {
							dst[off] = rgb;
							depthBuffer[off] = zA;
						}
						zA += zSlope;
						off++;
					} while (--length > 0);
				}
			} else {
				int a0 = alpha;
				int a1 = 256 - alpha;

				while (--length >= 0) {
					rgb = palette[colorA >> 8];
					colorA += lightnessSlope;
					rgb = (((rgb & 0xFF00FF) * a1 >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * a1 >> 8 & 0xFF00));

					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;

					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;

					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;

					dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
					off++;
				}

				length = xB - xA & 0x3;

				if (length > 0) {
					rgb = palette[colorA >> 8];
					rgb = (((rgb & 0xFF00FF) * a1 >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * a1 >> 8 & 0xFF00));
					do {
						dst[off] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
						off++;
					} while (--length > 0);
				}
			}
		} else if (xA < xB) {
			int lightnessSlope = (colorB - colorA) / (xB - xA);
			int zSlope = (zB - zA) / (xB - xA);

			if (testX) {
				if (xB > Graphics2D.rightX) {
					xB = Graphics2D.rightX;
				}

				if (xA < 0) {
					colorA -= xA * lightnessSlope;
					zA -= xA * zSlope;
					xA = 0;
				}

				if (xA >= xB) {
					return;
				}
			}

			off += xA;
			length = xB - xA;

			if (alpha == 0) {
				do {
					if (zA <= depthBuffer[off]) {
						depthBuffer[off] = zA;
						dst[off] = palette[colorA >> 8];
					}

					off++;
					zA += zSlope;
					colorA += lightnessSlope;
				} while (--length > 0);
			} else {
				int a0 = alpha;
				int a1 = 256 - alpha;
				do {
					rgb = palette[colorA >> 8];
					colorA += lightnessSlope;
					rgb = (((rgb & 0xFF00FF) * a1 >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * a1 >> 8 & 0xFF00));
					dst[off++] = (rgb + ((dst[off] & 0xFF00FF) * a0 >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * a0 >> 8 & 0xFF00));
				} while (--length > 0);
			}
		}
	}

	private Graphics3D() {

	}

}
