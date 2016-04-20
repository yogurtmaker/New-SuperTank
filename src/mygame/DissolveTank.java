package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class DissolveTank extends AbstractControl {

    Spatial tank;
    private final float speed = .25f;
    private float count = 0;
    private int dir = 1;
    public Vector2f DSParams;

    public DissolveTank(SimpleApplication sa, Spatial tank) {
        this.DSParams = new Vector2f(0, 0);
        final Material demat = sa.getAssetManager()
                .loadMaterial("Materials/Deactive/MultiplyColor_Base.j3m");
        demat.setTexture("DissolveMap", sa.getAssetManager().loadTexture("Textures/burnMap.png"));
        demat.setVector2("DissolveParams", this.DSParams);
        tank.setMaterial(demat);
    }

    @Override
    protected void controlUpdate(float tpf) {
        System.out.println(this.count);
        this.count += tpf * this.speed * this.dir;
        if (this.count > 1f) {
            this.dir = 1;
        }
        this.DSParams.setX(this.count);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
