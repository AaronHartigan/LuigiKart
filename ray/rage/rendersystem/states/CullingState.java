package ray.rage.rendersystem.states;

public interface CullingState extends RenderState {
    enum Culling {
        /**
         * Default
         */
        ENABLED,

        DISABLED
    }

    void setCulling(Culling c);

    Culling getCulling();
}