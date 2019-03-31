/**
 * Copyright (C) 2016 Raymond L. Rivera <ray.l.rivera@gmail.com>
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

package ray.rage.rendersystem.states;

/**
 * A <i>z-buffer state</i> has information about settings related to depth
 * testing.
 * <p>
 * Depth testing is a non-programmable stage that processes the output fragment
 * of a fragment shader program (i.e. a "candidate pixel") to determine how
 * "close" to the viewer each one is. When it's determined that a new fragment
 * is "closer" to the viewer than the previously stored fragment at the same
 * position, the new value is used to overwrite the old one. Otherwise, the new
 * one is discarded because it's considered to be occluded (assuming no
 * transparency).
 *
 * @author Raymond L. Rivera
 *
 */
public interface ZBufferState extends RenderState {

    /**
     * The type of depth testing function to use.
     *
     * @author Raymond L. Rivera
     *
     */
    enum TestFunction {
        /**
         * Never passes.
         */
        ALWAYS_FAIL,

        /**
         * Always passes.
         * <p>
         * If depth testing is disabled or if no depth buffer exists, it is as
         * if the depth test always passes.
         */
        ALWAYS_PASS,

        /**
         * Passes if the incoming depth value is equal to the stored depth
         * value.
         */
        EQUAL,

        /**
         * Passes if the incoming depth value is not equal to the stored depth
         * value.
         */
        NOT_EQUAL,

        /**
         * Passes if the incoming depth value is less than the stored depth
         * value.
         */
        LESS,

        /**
         * Passes if the incoming depth value is less than or equal to the
         * stored depth value. <i>This is the default.</i>
         */
        LESS_OR_EQUAL,

        /**
         * Passes if the incoming depth value is greater than the stored depth
         * value.
         */
        GREATER,
        /**
         * Passes if the incoming depth value is greater than or equal to the
         * stored depth value.
         */
        GREATER_OR_EQUAL,
    }

    /**
     * Sets the {@link TestFunction test-function}.
     *
     * @param function
     *            The {@link TestFunction test-function}.
     * @throws NullPointerException
     *             If the argument is <code>null</code>.
     */
    void setTestFunction(TestFunction function);

    /**
     * Gets the {@link TestFunction test-function}.
     *
     * @return The {@link TestFunction test-function}.
     */
    TestFunction getTestFunction();

    /**
     * Sets whether {@link ZBufferState depth-testing} should be performed on
     * every fragment or not.
     * <p>
     * This should <i>not</i> be confused with {@link #setEnabled(boolean)
     * enabling/disabling} {@link RenderState render-state} itself, which would
     * determine whether <code>this</code> gets {@link #apply() applied} or not.
     * Enabling/Disabling {@link ZBufferState z-buffer testing} simply
     * determines whether fragments should or should not be tested at all.
     * <p>
     * For example, disabling the {@link RenderState render-state} would mean
     * that subsequent fragments will be processed using the last
     * {@link ZBufferState z-state} to have been {@link #apply() applied},
     * whereas disabling {@link ZBufferState depth-testing} itself will update
     * overwrite the previous {@link ZBufferState state}.
     *
     * @param enabled
     */
    void setTestEnabled(boolean enabled);

    /**
     * Gets whether {@link ZBufferState depth-testing} is being performed or
     * not.
     *
     * @return <code>true</code> if {@link ZBufferState depth-testing} is being
     *         performed. Otherwise <code>false</code>.
     * @see #setTestEnabled(boolean)
     */
    boolean hasTestEnabled();

    /**
     * Sets whether the depth-buffer can be written to or not.
     *
     * @param writable
     *            <code>true</code> if the buffer can be written to. Otherwise
     *            <code>false</code>.
     */
    void setWritable(boolean writable);

    /**
     * Gets whether the depth buffer can have new values written to it or not.
     *
     * @return <code>true</code> if the buffer can be written to. Otherwise
     *         <code>false</code>.
     */
    boolean isWritable();

}
