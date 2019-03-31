/**
 * Copyright (C) 2016 Raymond L. Rivera
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

package ray.rage.scene.generic;

import ray.rage.scene.generic.AbstractGenericFrustum;
import ray.rml.*;

/**
 * A {Camera.Frustum} that provides a <i>perspective</i> projection.
 *
 * @author Raymond L. Rivera
 *
 */
final class GenericPerspectiveFrustum extends AbstractGenericFrustum {

    @Override
    public Projection getProjection() {
        return Projection.PERSPECTIVE;
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        // @formatter:off
        return Matrix4f.createPerspectiveMatrix(
            getFieldOfViewY(),
            getAspectRatio(),
            getNearClipDistance(),
            getFarClipDistance()
        );
        // @formatter:on
    }

}
