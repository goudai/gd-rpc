package io.goudai.net.buffer;

import java.nio.ByteBuffer;

/**
 * Created by freeman on 2016/1/8.
 */
public final class IoBuffer {
    public ByteBuffer buf = null;

    private IoBuffer(ByteBuffer buf) {
        this.buf = buf;
    }

    public static IoBuffer wrap(ByteBuffer buf) {
        return new IoBuffer(buf);
    }

    public static IoBuffer wrap(byte[] buf) {
        return new IoBuffer(ByteBuffer.wrap(buf));
    }

    public static IoBuffer wrap(String str){
        return wrap(str.getBytes());
    }

    public static IoBuffer allocate(int capacity) {
        return new IoBuffer(ByteBuffer.allocate(capacity));
    }

    public static IoBuffer allocateDirect(int capacity) {
        return new IoBuffer(ByteBuffer.allocateDirect(capacity));
    }

    public byte[] array(){
        return this.buf.array();
    }

    public byte readByte() {
        return this.buf.get();
    }

    public IoBuffer readBytes(byte[] dst) {
        this.buf.get(dst);
        return this;
    }

    public IoBuffer readBytes(byte[] dst, int offset, int length) {
        this.buf.get(dst, offset, length);
        return this;
    }

    public short readShort() {
        return this.buf.getShort();
    }

    public int readInt() {
        return this.buf.getInt();
    }

    public long readLong() {
        return this.buf.getLong();
    }

    public IoBuffer writeByte(byte value) {
        return writeBytes(new byte[] { value });
    }

    public IoBuffer writeBytes(byte[] src) {
        return writeBytes(src, 0, src.length);
    }

    public IoBuffer writeBytes(byte[] src, int offset, int length) {
        autoExpand(length);
        this.buf.put(src, offset, length);
        return this;
    }

    public IoBuffer writeChar(char c) {
        autoExpand(2);
        this.buf.putChar(c);
        return this;
    }


    public IoBuffer writeShort(short value) {
        autoExpand(2);
        this.buf.putShort(value);
        return this;
    }

    public IoBuffer writeLong(long value) {
        autoExpand(8);
        this.buf.putLong(value);
        return this;
    }

    public IoBuffer writeInt(int value) {
        autoExpand(4);
        this.buf.putInt(value);
        return this;
    }

    public IoBuffer writeString(String value) {
        this.writeBytes(value.getBytes());
        return this;
    }

    public final IoBuffer flip() {
        this.buf.flip();
        return this;
    }

    public final byte get(int index) {
        return this.buf.get(index);
    }

    public final IoBuffer unflip() {
        buf.position(buf.limit());
        if (buf.limit() != buf.capacity())
            buf.limit(buf.capacity());
        return this;
    }

    public final IoBuffer rewind() {
        this.buf.rewind();
        return this;
    }

    public final int remaining() {
        return this.buf.remaining();
    }

    public final IoBuffer mark() {
        this.buf.mark();
        return this;
    }

    public final IoBuffer reset() {
        this.buf.reset();
        return this;
    }

    public final int position() {
        return this.buf.position();
    }

    public final IoBuffer position(int newPosition) {
        this.buf.position(newPosition);
        return this;
    }

    public final int limit(){
        return this.buf.limit();
    }

    public final IoBuffer duplicate() {
        return IoBuffer.wrap(this.buf.duplicate());
    }

    public final IoBuffer compact() {
        this.buf.compact();
        return this;
    }

    public final ByteBuffer buf() {
        return this.buf;
    }

    private void autoExpand(int length) {
        int newCapacity = this.buf.capacity();
        int newSize = this.buf.position() + length;
        ByteBuffer newBuffer = null;

        while (newSize > newCapacity) {
            newCapacity = newCapacity * 2;
        }

        // Auto expand capacity
        if (newCapacity != this.buf.capacity()) {
            if (this.buf.isDirect()) {
                newBuffer = ByteBuffer.allocateDirect(newCapacity);
            } else {
                newBuffer = ByteBuffer.allocate(newCapacity);
            }

            newBuffer.put(this.buf.array());
            newBuffer.position(this.buf.position());

            this.buf = newBuffer;
        }
    }


    @Override
    public String toString() {
        String str = new String(buf.array(), buf.position(), buf.remaining());
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("IoBuffer(%d)=", buf.remaining()));
        for(int i=0; i<str.length();i++){
            char c = str.charAt(i);
            if(Character.isLetter(c)){
                sb.append(c);
            } else {
                sb.append(String.format("<%s>", Integer.toBinaryString(c)));
            }
        }
        return sb.toString();
    }

}