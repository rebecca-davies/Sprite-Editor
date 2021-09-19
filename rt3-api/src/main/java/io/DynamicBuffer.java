package io;

public class DynamicBuffer extends Buffer {

	public DynamicBuffer() {
		super(0);
	}

	private void ensureCapacity(int bytes) {
		if (position + bytes >= data.length) {
			byte[] old = data;
			data = new byte[position + bytes];
			System.arraycopy(old, 0, data, 0, old.length);
		}
	}

	@Override
	public void startVariableSize(int opcode, int bytes) {
		ensureCapacity(bytes + 1);
		super.startVariableSize(opcode, bytes);
	}

	@Override
	public void write(int i) {
		ensureCapacity(1);
		super.write(i);
	}

	@Override
	public void writeShort(int i) {
		ensureCapacity(2);
		super.writeShort(i);
	}

	@Override
	public void writeInt24(int i) {
		ensureCapacity(3);
		super.writeInt24(i);
	}

	@Override
	public void writeInt(int i) {
		ensureCapacity(4);
		super.writeInt(i);
	}

	@Override
	public void writeLong(long l) {
		ensureCapacity(8);
		super.writeLong(l);
	}

	@Override
	public void write(byte[] src, int off, int len) {
		ensureCapacity(len);
		super.write(src, off, len);
	}

	@Override
	public void writeString(String s) {
		ensureCapacity(s.length() + 1);
		super.writeString(s);
	}

	@Override
	public void writeBits(int numBits, int value) {
		int bytePosition = (bitPosition + 7) * 8;

		if (bytePosition + 1 > data.length) {
			byte[] old = data;
			data = new byte[data.length + 1];
			System.arraycopy(old, 0, data, 0, old.length);
		}

		super.writeBits(numBits, value);
	}

}
