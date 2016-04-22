package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class Bullet {

    SimpleApplication sa;
    Spatial bullet;
    boolean alive = true;
    float time = 0;
    Vector3f position, velocity, tankWorldTranslation;

    public Bullet(SimpleApplication sa, Vector3f bulletWorldTranslation, 
            Vector3f tankWorldTranslation) {
        this.sa = sa;
        this.position = bulletWorldTranslation;
        this.tankWorldTranslation = tankWorldTranslation;
        velocity = position.subtract(tankWorldTranslation)
                .subtract(new Vector3f(0, 2f, 0)).mult(1.2f);
        initBullet();
    }

    private void initBullet() {
        bullet = sa.getAssetManager().loadModel("Models/Bullet/Bullet.mesh.j3o");
    }

    public void update(float tpf) {
        if (alive) {
            position = position.add(velocity);
            bullet.setLocalTranslation(position);
        }
    }

    public Vector3f getPosition() {
        return bullet.getLocalTranslation();
    }
}
