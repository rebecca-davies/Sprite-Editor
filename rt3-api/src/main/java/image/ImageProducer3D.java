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

/**
 * A {@code ImageProducer} with easy usability to {@code Graphics3D}. This class encapsulates the offsets and zbuffer
 * values for 3d drawing. {@code bind()} initializes the offsets and zbuffer if they are {@code null}.
 *
 * @see #bind()
 * @author Dane
 */
public class ImageProducer3D extends ImageProducer {

	/**
	 * The pixel offsets for each pixel row.
	 */
	private int[] offsets;

	/**
	 * The depth buffer.
	 */
	private int[] depthBuffer;

	/**
	 * Constructs a new empty 3d image producer.
	 *
	 * @param width the width.
	 * @param height the height.
	 */
	public ImageProducer3D(int width, int height) {
		super(width, height);
	}

	public int[] getOffsets() {
		return this.offsets;
	}

	public int[] getDepthBuffer() {
		return this.depthBuffer;
	}

	/**
	 * Binds the backing image as the target to the {@link Graphics2D} class.
	 *
	 * @see Graphics2D
	 */
	@Override
	public void bind() {
		// we still need our superclass bind() to set us as the target
		super.bind();

		// make sure we have our vertical offsets
		if (this.offsets == null) {
			this.offsets = Graphics3D.setOffsets();
		}

		// and our zbuffer initialized
		if (this.depthBuffer == null) {
			this.depthBuffer = Graphics3D.setDepthBuffer();
		}

		Graphics3D.offsets = this.offsets;
		Graphics3D.depthBuffer = this.depthBuffer;
	}

}
