/**
 * Copyright (C) 2017 Raymond L. Rivera <ray.l.rivera@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ray.rage.util;

import java.nio.*;

/**
 * A <i>buffer utility</i> to handle the creation of <i>direct</i> data buffers.
 * <p>
 * Game clients and this framework should avoid writing code that creates
 * buffers explicitly. For example, the following line of code creates an
 * <i>indirect</i> buffer, by default:
 *
 * <pre>
 *
 * FloatBuffer buffer = FloatBuffer.wrap(new float[] { 1, 2, 3, 4 });
 * </pre>
 *
 * Instead, use something like this:
 *
 * <pre>
 *
 * FloatBuffer buffer = BufferUtils.directFloatBuffer(new float[] { 1, 2, 3, 4 });
 * </pre>
 *
 * @author Raymond L. Rivera
 *
 */
public final class BufferUtil {

    private BufferUtil() {}

    /**
     * Creates a new {@link ByteBuffer} with the specified capacity.
     *
     * @param capacity
     *            The number of bytes the {@link ByteBuffer} can hold.
     * @return A new <i>direct</i> {@link ByteBuffer}.
     * @throws IllegalArgumentException
     *             If capacity is negative.
     */
    public static ByteBuffer directByteBuffer(int capacity) {
        return (ByteBuffer) ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    /**
     * Creates a new {@link ByteBuffer} with the specified values.
     *
     * @param values
     *            An array of bytes to insert into the {@link ByteBuffer}.
     * @return A new <i>direct</i> {@link ByteBuffer} with the values in it.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public static ByteBuffer directByteBuffer(byte[] values) {
        return (ByteBuffer) directByteBuffer(values.length).put(values).rewind();
    }

    /**
     * Creates a new {@link IntBuffer} with the specified capacity.
     *
     * @param capacity
     *            The number of integers the {@link IntBuffer} can hold.
     * @return A new <i>direct</i> {@link IntBuffer}.
     * @throws IllegalArgumentException
     *             If capacity is negative.
     */
    public static IntBuffer directIntBuffer(int capacity) {
        return directByteBuffer(capacity * Integer.BYTES).asIntBuffer();
    }

    /**
     * Creates a new {@link IntBuffer} with the specified values.
     *
     * @param values
     *            An array of integers to insert into the {@link IntBuffer}.
     * @return A new <i>direct</i> {@link IntBuffer} with the values in it.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public static IntBuffer directIntBuffer(int[] values) {
        return (IntBuffer) directIntBuffer(values.length).put(values).rewind();
    }

    /**
     * Creates a new {@link FloatBuffer} with the specified capacity.
     *
     * @param capacity
     *            The number of floats the {@link FloatBuffer} can hold.
     * @return A new <i>direct</i> {@link FloatBuffer}.
     * @throws IllegalArgumentException
     *             If capacity is negative.
     */
    public static FloatBuffer directFloatBuffer(int capacity) {
        return directByteBuffer(capacity * Float.BYTES).asFloatBuffer();
    }

    /**
     * Creates a new {@link FloatBuffer} with the specified values.
     *
     * @param values
     *            An array of floats to insert into the {@link FloatBuffer}.
     * @return A new <i>direct</i> {@link FloatBuffer} with the values in it.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    public static FloatBuffer directFloatBuffer(float[] values) {
        return (FloatBuffer) directFloatBuffer(values.length).put(values).rewind();
    }

}
