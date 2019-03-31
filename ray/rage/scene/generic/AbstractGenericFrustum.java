/**
 * Copyright (C) 2017 Raymond L. Rivera
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

import ray.rage.scene.*;
import ray.rml.*;

/**
 * An abstract, projection-agnostic, base implementation for a
 * {@link Camera.Frustum}.
 *
 * @author Raymond L. Rivera
 *
 */
abstract class AbstractGenericFrustum implements Camera.Frustum {

    private Camera  camera;

    private float   nearClipDistance       = .01f;
    private float   farClipDistance        = 1000f;

    // 16:9 (e.g. 1920x1080)
    private float   aspectRatio            = 1.43f;
    private boolean autoAspectRatioEnabled = true;

    private Angle   fieldOfViewY           = Degreef.createFrom(60);

    @Override
    public void notifyCamera(Camera cam) {
        if (cam == null)
            throw new NullPointerException("Null " + Camera.class.getSimpleName());

        camera = cam;
    }

    @Override
    public void setAspectRatio(float ratio) {
        if (ratio <= 0)
            throw new IllegalArgumentException("Ratio <= 0");

        aspectRatio = ratio;
    }

    @Override
    public float getAspectRatio() {
        return aspectRatio;
    }

    @Override
    public void setAutoAspectRatio(boolean enabled) {
        autoAspectRatioEnabled = enabled;
    }

    @Override
    public boolean getAutoAspectRatio() {
        return autoAspectRatioEnabled;
    }

    @Override
    public void setNearClipDistance(float nearDist) {
        if (nearDist <= 0)
            throw new IllegalArgumentException("Near clip plane is <= 0");

        if (nearDist >= farClipDistance)
            throw new IllegalArgumentException("Near clip plane is >= far clip plane");

        nearClipDistance = nearDist;
    }

    @Override
    public float getNearClipDistance() {
        return nearClipDistance;
    }

    @Override
    public void setFarClipDistance(float farDist) {
        if (farDist <= nearClipDistance)
            throw new IllegalArgumentException("Far clip plane is <= near clip plane");

        farClipDistance = farDist;
    }

    @Override
    public float getFarClipDistance() {
        return farClipDistance;
    }

    @Override
    public void setFieldOfViewY(Angle angle) {
        if (angle == null)
            throw new NullPointerException("Null " + Angle.class.getSimpleName());

        fieldOfViewY = angle;
    }

    @Override
    public Angle getFieldOfViewY() {
        return fieldOfViewY;
    }

    @Override
    public Matrix4 getViewMatrix() {
    	Vector3 u,v,n,p;
    	if (camera == null)
            throw new IllegalStateException(Camera.class.getSimpleName() + " not set");
        if (!camera.isAttached())
            throw new RuntimeException(Camera.class.getSimpleName() + " not attached");

        Node parent = camera.getParentNode();
        if (camera.getMode() == 'c') {
        	u = camera.getRt();
        	v = camera.getUp();
        	n = camera.getFd();
        	p = camera.getPo();
        }
        else {
        	u = parent.getWorldRightAxis().negate();
        	v = parent.getWorldUpAxis();
        	n = parent.getWorldForwardAxis();
        	p = parent.getWorldPosition();
        }
        return Matrix4f.createViewMatrix(u, v, n, p);
    }

}
