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
package io;

/**
 *
 * @author Dane
 */
public class Buffer {

	private static final int[] BITMASK = new int[33];

	static {
		for (int i = 0; i < BITMASK.length - 1; i++) {
			BITMASK[i] = (1 << i) - 1;
		}
		BITMASK[BITMASK.length - 1] = -1;
	}

	public byte[] data;
	public int position = 0;
	public int bitPosition = 0;
	private int startPosition, variableSize;

	/**
	 * Constructs a buffer with a size of 32 bytes.
	 */
	public Buffer() {
		this(32);
	}

	/**
	 * Constructs a buffer to wrap around the provided byte array.
	 *
	 * @param src the byte array.
	 */
	public Buffer(byte[] src) {
		this.data = src;
	}

	/**
	 * Constructs a buffer.
	 *
	 * @param size the size.
	 */
	public Buffer(int size) {
		this.data = new byte[size];
	}

	public void startVariableSize(int opcode, int byteSize) {
		writeOpcode(opcode);
		position += byteSize;
		startPosition = position;
		variableSize = byteSize;
	}

	public void endVariableSize() {
		final int length = position - startPosition;
		final int bytes = variableSize + 1;

		for (int i = 1; i < bytes; i++) {
			data[position - length - i] = (byte) (length >> ((i - 1) * 8));
		}
	}

	public void writeOpcode(int i) {
		write(i);
	}

	public void write(int i) {
		data[position++] = (byte) i;
	}

	public void writeShort(int i) {
		data[position++] = (byte) (i >> 8);
		data[position++] = (byte) i;
	}

	public void writeInt24(int i) {
		data[position++] = (byte) (i >> 16);
		data[position++] = (byte) (i >> 8);
		data[position++] = (byte) i;
	}

	public void writeInt(int i) {
		data[position++] = (byte) (i >> 24);
		data[position++] = (byte) (i >> 16);
		data[position++] = (byte) (i >> 8);
		data[position++] = (byte) i;
	}

	public void writeLong(long l) {
		data[position++] = (byte) (int) (l >> 56);
		data[position++] = (byte) (int) (l >> 48);
		data[position++] = (byte) (int) (l >> 40);
		data[position++] = (byte) (int) (l >> 32);
		data[position++] = (byte) (int) (l >> 24);
		data[position++] = (byte) (int) (l >> 16);
		data[position++] = (byte) (int) (l >> 8);
		data[position++] = (byte) (int) l;
	}

	public void writeString(String s) {
		System.arraycopy(s.getBytes(), 0, data, position, s.length());
		position += s.length();
		data[position++] = (byte) 10;
	}

	public void write(byte[] src, int off, int len) {
		for (int i = off; i < off + len; i++) {
			data[position++] = src[i];
		}
	}

	public void writeLengthByte(int length) {
		data[position - length - 1] = (byte) length;
	}

	public void writeLengthShort(int length) {
		data[position - (length - 2)] = (byte) (length >> 8);
		data[position - (length - 1)] = (byte) (length);
	}

	public int read() {
		return data[position++] & 0xff;
	}

	public byte readByte() {
		return data[position++];
	}

	public int readUShort() {
		position += 2;
		return (((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
	}

	public int readShort() {
		position += 2;
		int i = (((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
		if (i > 32767) {
			i -= 65536;
		}
		return i;
	}

	public int readInt24() {
		position += 3;
		return (((data[position - 3] & 0xff) << 16) + ((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
	}

	public int readInt() {
		position += 4;
		return (((data[position - 4] & 0xff) << 24) + ((data[position - 3] & 0xff) << 16) + ((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
	}

	public long readLong() {
		long a = (long) readInt() & 0xffffffffL;
		long b = (long) readInt() & 0xffffffffL;
		return (a << 32) + b;
	}

	public String readString() {
		int start = position;
		while (data[position++] != 10) {
			/* empty */
		}
		return new String(data, start, position - start - 1);
	}

	public byte[] readStringBytes() {
		int start = position;
		while (data[position++] != 10) {
			/* empty */
		}
		byte[] bytes = new byte[position - start - 1];
		for (int i = start; i < position - 1; i++) {
			bytes[i - start] = data[i];
		}
		return bytes;
	}

	public void read(byte[] dst, int off, int len) {
		for (int i = off; i < off + len; i++) {
			dst[i] = data[position++];
		}
	}

	public int readSmart() {
		int i = data[position] & 0xff;
		if (i < 128) {
			return read() - 64;
		}
		return readUShort() - 49152;
	}

	public int readUSmart() {
		int i = data[position] & 0xff;
		if (i < 128) {
			return read();
		}
		return readUShort() - 32768;
	}

	public void startBitAccess() {
		bitPosition = position * 8;
	}

	public void writeBits(int bitCount, int value) {
		int i = bitPosition >> 3;
		int off = 8 - (bitPosition & 7);
		bitPosition += bitCount;

		for (; bitCount > off; off = 8) {
			data[i] &= ~BITMASK[off];
			data[i++] |= (value >> (bitCount - off)) & BITMASK[off];
			bitCount -= off;
		}

		if (bitCount == off) {
			data[i] &= ~BITMASK[off];
			data[i] |= value & BITMASK[off];
		} else {
			data[i] &= ~(BITMASK[bitCount] << (off - bitCount));
			data[i] |= (value & BITMASK[bitCount]) << (off - bitCount);
		}

	}

	public int readBits(int count) {
		int bytePos = bitPosition >> 3;
		int mask = 8 - (bitPosition & 0x7);
		int i = 0;

		bitPosition += count;

		for (/**/; count > mask; mask = 8) {
			i += ((data[bytePos++] & BITMASK[mask]) << count - mask);
			count -= mask;
		}

		if (count == mask) {
			i += data[bytePos] & BITMASK[mask];
		} else {
			i += (data[bytePos] >> mask - count & BITMASK[count]);
		}

		return i;
	}

	public void startByteAccess() {
		position = (bitPosition + 7) / 8;
	}

}
