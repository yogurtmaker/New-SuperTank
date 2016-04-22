package mygame;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;

public abstract class Enemy {

    Main main;
    Node enemyNode, bulletStartNode, walkDirNode, leftNode, leftNode1, rightNode, rightNode1;
    CharacterControl enemyControl;
    Geometry collisionTest, bar;
    List<Bullet> bulletList;
    Dust dust;
    Vector3f walkDirection = new Vector3f(0, 0, 0), viewDirection = new Vector3f(0, 0, 0);
    boolean force = false, second = false, forward = false, backward = false,
            leftRotate = false, rightRotate = false, attack = false, shoot = false, 
            death = false, stop = false, collideWithPlayer = false, collideWithEnemy = false;
    protected float airTime = 0, hitPoints = 100;

    public Enemy(Main main, String enemyType, Material mat) {
        this.main = main;
        initEnemy(enemyType, mat);
    }

    private void initEnemy(String enemyType, Material mat) {
        Sphere sphereLarge = new Sphere(32, 32, 1.5f);
        collisionTest = new Geometry("Shiny", sphereLarge);
        Material collidemat = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        collidemat.setColor("Color", new ColorRGBA(0f, 0f, 0.8f, 0f));
        collidemat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        collidemat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0f));
        collisionTest.setQueueBucket(RenderQueue.Bucket.Transparent);
        collisionTest.setMaterial(collidemat);
        collisionTest.setCullHint(Spatial.CullHint.Always);
        
        bulletList = new ArrayList<Bullet>();
        SphereCollisionShape sphere = new SphereCollisionShape(5f);
        enemyControl = new CharacterControl(sphere, 0.01f);
        enemyControl.setFallSpeed(15f);
        enemyControl.setGravity(0f);
        enemyNode = (Node) main.getAssetManager().loadModel(enemyType);
        enemyNode.setMaterial(mat);
        enemyNode.addControl(enemyControl);
        enemyNode.attachChild(collisionTest);

        enemyNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        bulletStartNode = new Node();
        bulletStartNode.setLocalTranslation(0, 2, 3);
        enemyNode.attachChild(bulletStartNode);

        leftNode = new Node();
        leftNode.setLocalTranslation(0.02f, 0, 6);
        enemyNode.attachChild(leftNode);

        leftNode1 = new Node();
        leftNode1.setLocalTranslation(0.08f, 0, 6);
        enemyNode.attachChild(leftNode1);

        rightNode = new Node();
        rightNode.setLocalTranslation(-0.02f, 0, 6);
        enemyNode.attachChild(rightNode);

        rightNode1 = new Node();
        rightNode1.setLocalTranslation(-0.08f, 0, 6);
        enemyNode.attachChild(rightNode1);

        dust = new Dust(main);
        dust.emit.setParticlesPerSec(0f);
        enemyNode.attachChild(dust.emit);
    }

    protected abstract void adjust(Vector3f palyerPos, int i);

    protected abstract void updateEnemy(float tpf, Vector3f playerPos, int enemyRemain);
}
