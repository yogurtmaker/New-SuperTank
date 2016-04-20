package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class Test extends SimpleApplication {

    Spatial tank;
    DissolveTank dissolveTank;

    public static void main(final String[] args) {
        final Test app = new Test();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        this.cam.setLocation(new Vector3f(0, 1.5f, 10f));
        tank = assetManager.loadModel("Models/HoverTank/Tank2.mesh.xml");

        tank.setLocalTranslation(new Vector3f(0, 0, 0));
        this.rootNode.attachChild(tank);

        final AmbientLight a = new AmbientLight();
        a.setColor(ColorRGBA.White);
        this.rootNode.addLight(a);

        this.viewPort.setBackgroundColor(ColorRGBA.Gray);
        this.flyCam.setMoveSpeed(5);

        dissolveTank = new DissolveTank(this, tank);
        tank.addControl(dissolveTank);
    }

    @Override
    public void simpleUpdate(final float tpf) {
    }
}