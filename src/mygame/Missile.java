package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

public class Missile {

    SimpleApplication sa;
    Node bullet;
    boolean alive = true, hit = true;
    float time = 0;
    Vector3f position, velocity, tankWorldTranslation;
    Spatial missile;

    public Missile(SimpleApplication sa, Vector3f bulletWorldTranslation,
            Vector3f tankWorldTranslation) {
        this.sa = sa;
        this.position = bulletWorldTranslation;
        this.tankWorldTranslation = tankWorldTranslation;
        velocity = position.subtract(tankWorldTranslation)
                .subtract(new Vector3f(0, 2f, 0)).mult(1.5f);
        initBullet();
    }

    private void initBullet() {
        final Material mat = new Material(sa.getAssetManager(),
                "MatDefs/FakeParticleBlow.j3md");
        final Texture maskTex = sa.getAssetManager().loadTexture("Textures/mask.png");
        mat.setTexture("MaskMap", maskTex);
        final Texture aniTex = sa.getAssetManager().loadTexture("Textures/particles.png");
        aniTex.setWrap(Texture.WrapMode.MirroredRepeat);
        mat.setTexture("AniTexMap", aniTex);
        mat.setFloat("TimeSpeed", 2);
        mat.setColor("BaseColor", ColorRGBA.Orange.clone());
        mat.setBoolean("Animation_Y", true);
        mat.setBoolean("Change_Direction", true);
        mat.getAdditionalRenderState().setDepthTest(true);
        mat.getAdditionalRenderState().setDepthWrite(false);          
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        final ColorRGBA fogColor = ColorRGBA.Orange.clone();
        fogColor.a = 10;
        mat.setColor("FogColor", fogColor);
        
        final Quad quad = new Quad(10, 15);
        final Geometry geom = new Geometry("Particle", quad);
        geom.setMaterial(mat);
        TangentBinormalGenerator.generate(geom);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        Quaternion rotate = new Quaternion();
        rotate.fromAngleNormalAxis(FastMath.PI/2, new Vector3f(-1, 0, 0));
        geom.setLocalRotation(rotate);
        geom.setLocalTranslation(new Vector3f(-5f, 0, -6));
        missile = sa.getAssetManager().loadModel("Models/Missile/AAM.mesh.j3o");
        bullet = new Node();
        bullet.attachChild(missile);
        bullet.attachChild(geom);
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