package mygame;

import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.shaderblow.forceshield.ForceShieldControl;

public class Shield implements ActionListener {

    private Main main;
    Node nodeshield;
    public ForceShieldControl forceShieldControl;
    Geometry bar;
    float hitPoints = 50;

    public Shield(Main main) {
        this.main = main;
        initShield();
    }

    private void initShield() {
        nodeshield = new Node();
        nodeshield.setShadowMode(RenderQueue.ShadowMode.Off);
        final Sphere sphere = new Sphere(60, 60, 6.5f);
        final Geometry shield = new Geometry("forceshield", sphere);
        nodeshield.attachChild(shield);
        shield.setQueueBucket(Bucket.Transparent);

        Material forceMaterial = new Material(main.getAssetManager(), "ShaderBlow/MatDefs/ForceShield/ForceShield.j3md");
        forceMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        forceMaterial.getAdditionalRenderState().setDepthWrite(false);
        forceMaterial.setFloat("MaxDistance", 1);
        forceMaterial.setTexture("ColorMap", main.getAssetManager().loadTexture("Textures/fs_texture.png"));
        forceMaterial.setFloat("MinAlpha", 0.1f);

        final Geometry shield2 = new Geometry("elecshield", sphere);
        shield2.setQueueBucket(Bucket.Transparent);
        Material mat = main.getAssetManager().loadMaterial("Materials/electricity.j3m");
        for (Spatial child : nodeshield.getChildren()) {
            if (child instanceof Geometry) {
                Geometry electricity = new Geometry("electrified_" + child.getName());
                electricity.setQueueBucket(Bucket.Transparent);
                electricity.setMesh(((Geometry) child).getMesh());
                electricity.setMaterial(mat);
                nodeshield.attachChild(electricity);
            }
        }

        this.forceShieldControl = new ForceShieldControl(forceMaterial);
        shield.addControl(this.forceShieldControl);
        this.forceShieldControl.setEffectSize(6f);
        this.forceShieldControl.setColor(new ColorRGBA(125 / 255f, 249 / 255f, 255 / 255f, 3));
        this.forceShieldControl.setVisibility(0.05f);
        this.forceShieldControl.setMaxTime(0.5f);
        main.getInputManager().addMapping("FIRE", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        main.getInputManager().addListener(this, "FIRE");

        Box box = new Box(2.5f, 0.3f, 0.3f);
        bar = new Geometry("bar", box);
        Material matBar = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBar.setColor("Color", new ColorRGBA(0f, 0f, 0.8f, 0.2f));
        matBar.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bar.setMaterial(matBar);
        bar.setLocalTranslation(0, 7.3f, 2);
        bar.setQueueBucket(RenderQueue.Bucket.Transparent);
        nodeshield.attachChild(bar);
    }

    @Override
    public void onAction(final String name, final boolean isPressed, final float arg) {
        if (name.equals("FIRE") && isPressed) {
            final CollisionResults crs = new CollisionResults();
            Vector2f click2d = main.getInputManager().getCursorPosition();
            Vector3f click3d = main.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = main.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            main.getRootNode().collideWith(new Ray(click3d, dir), crs);
            if (crs.getClosestCollision() != null) {
                this.forceShieldControl.registerHit(crs.getClosestCollision().getContactPoint());
            }
        }
    }

    public void force(CollisionResults crs) {
        this.forceShieldControl.registerHit(crs.getClosestCollision().getContactPoint());
    }
}
