package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.WireBox;

public abstract class Powerup extends Node {

    protected Node adjustmentNode;
    private Spatial modelData;
    private Geometry geomBoundingBox;
    private int num;
    Control powerControl;

    public Powerup(SimpleApplication sa, String filename, int num) {
        this.num = num;
        modelData = (Node) sa.getAssetManager().loadModel(filename);
        modelData.setModelBound(new BoundingBox());
        modelData.updateModelBound();
        modelData.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        modelData.scale(1.5f);

        BoundingBox b = (BoundingBox) modelData.getWorldBound();
        WireBox wb = new WireBox();
        wb.fromBoundingBox(b);
        geomBoundingBox = new Geometry("bb", wb);
        Material transparent = new Material(sa.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        transparent.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        transparent.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0f));
        geomBoundingBox.setQueueBucket(Bucket.Transparent);
        geomBoundingBox.setMaterial(transparent);
        geomBoundingBox.setLocalTranslation(b.getCenter());
        geomBoundingBox.setCullHint(CullHint.Always);

        adjustmentNode = new Node();
        adjustmentNode.attachChild(modelData);
        adjustmentNode.attachChild(geomBoundingBox);
        powerControl = new Powerup.PowerControl();
        adjustmentNode.addControl(powerControl);
        this.attachChild(adjustmentNode);
    }
    
    public int getValue(){
        return num;
    }

    class PowerControl extends AbstractControl {

        @Override
        protected void controlUpdate(float tpf) {
            modelData.rotate(0, tpf*5, 0);
            geomBoundingBox.rotate(0, tpf, 0);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }
    }
}
