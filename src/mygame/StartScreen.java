/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.state.AbstractAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import static mygame.Game.ENEMYNUMBER;

/**
 *
 * @author 2
 */
public class StartScreen extends AbstractAppState implements ActionListener {

    private AudioNode audio_nature;
    Main main;
    EnemyTank demotank;
    Vector3f center = new Vector3f(0, 220, 0);
    float time = 0;
    Box startbox;
    Geometry srartGeom;
    InputManager inputManager;
    BitmapText hudText;

    public StartScreen(Main main) {
        this.main = main;
        initKey();
        initTank();
        initCam();
        initText();
        initBGM1();
    }

    private void initCam() {
        main.getFlyByCamera().setEnabled(false);
        Node camNode = new CameraNode("CamNode", main.getCamera());
        camNode.setLocalTranslation(new Vector3f(0, 10, -40));
        demotank.enemyNode.attachChild(camNode);
    }

    private void initTank() {
        Material mat = main.getAssetManager().loadMaterial("Materials/Active/MultiplyColor_1.j3m");
        demotank = new EnemyTank(main, mat);
        demotank.adjust(center, 0);
        main.bulletAppState.getPhysicsSpace().add(demotank.enemyControl);
        demotank.enemyNode.detachChild(demotank.bar);
        main.getRootNode().attachChild(demotank.enemyNode);
    }

    private void initKey() {
        inputManager = main.getInputManager();
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Click");
    }

    private void initText() {
        startbox = new Box(5, 1, .1f);
        srartGeom = new Geometry("startBox", startbox);
        Material mat = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//mat.setColor("Color", ColorRGBA.Gray);
        srartGeom.setMaterial(mat);
        srartGeom.setLocalTranslation(0, 9, 0);
//g.setQueueBucket(RenderQueue.Bucket.Transparent);

        BitmapFont guiFont = main.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.scale(2);
        hudText.setColor(ColorRGBA.Black);                             // font color
        hudText.setText("Start");             // the text
        hudText.setLocalTranslation(680, 400, 0); // position


        demotank.enemyNode.attachChild(srartGeom);
        main.getGuiNode().attachChild(hudText);

    }

    private void cleanAll() {

        audio_nature.stop();
        main.getRootNode().detachChild(audio_nature);
        demotank.enemyNode.detachAllChildren();
        main.getRootNode().detachChild(demotank.enemyNode);
        main.getGuiNode().detachChild(hudText);

    }

    private void initBGM1() {
        audio_nature = new AudioNode(main.getAssetManager(), "Sound/Rhythm_of_port_town.ogg", true);
        audio_nature.setPositional(false);
        audio_nature.setLooping(false);
        audio_nature.setVolume(3);
        main.getRootNode().attachChild(audio_nature);
        audio_nature.play();
    }

    public void onAction(String name, boolean isPressed, float tpf) {

        if ("Click".equals(name) && isPressed) {

            final CollisionResults crs = new CollisionResults();
            Vector2f click2d = main.getInputManager().getCursorPosition();
            Vector3f click3d = main.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = main.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            main.getRootNode().collideWith(new Ray(click3d, dir), crs);
            if (crs.getClosestCollision() != null) {
                String target = crs.getClosestCollision().getGeometry().getName();
                if (target.equals("startBox")) {
                    Game game = new Game(false);
                    cleanAll();
                    main.getStateManager().detach(this);
                    main.getStateManager().attach(game);
                }
            }

        }
    }

    @Override
    public void update(float tpf) {
        if (time < 15) {
            time += tpf;
        } else {
            Vector3f newVec = demotank.enemyNode.getWorldTranslation()
                    .add(new Vector3f(200, 0, 200));
            demotank.updateEnemy(tpf, newVec, 2);
        }

    }
}
