package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Main extends SimpleApplication implements ActionListener {
    
    BitmapText[] texts;
    static Dimension screen;
    BulletAppState bulletAppState;
    private Node modelPlayer;
    private Node[] modelEnemyTank;
    private CharacterControl player;
    private CharacterControl[] controlEnemyTank;
    CameraNode camNode;
    Ground ground;
    Sky sky;
    Tank tank;
    Enemy[] enemyTank;
    DissolveTank dissolveTank;
    Vector3f playerBarPos = new Vector3f(820, 355, 0), gameEndPos = new Vector3f(520, 750, 0),
            numOfBulletRemainPos = new Vector3f(20, 800, 0);
    Material mats[];
    final int ENEMYNUMBER = 4, BULLETDAMAGE = 20;
    boolean rotate = false;
    int enemyRemain = 4;
    boolean pause = false;
    
    public static void main(final String[] args) {
        Main app = new Main();
        initAppScreen(app);
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        
        processor();
        initText();
        ground = new Ground(this);
        sky = new Sky(this);
        initMat();
        initPhysics();
        createCharacter();
        initCam();
        setUpKeys();
        
    }
    
    private static void initAppScreen(SimpleApplication app) {
        AppSettings aps = new AppSettings(true);
        screen = Toolkit.getDefaultToolkit().getScreenSize();
        screen.width *= 0.75;
        screen.height *= 0.75;
        aps.setResolution(screen.width, screen.height);
        app.setSettings(aps);
        app.setShowSettings(false);
    }
    
    @Override
    public void simpleUpdate(final float tpf) {
        if (pause) {
            for (int i = 0; i < ENEMYNUMBER; i++) {
                enemyTank[i].enemyControl.setWalkDirection(Vector3f.ZERO);
                tank.tankControl.setWalkDirection(Vector3f.ZERO);
            }
        } else {
            texts[3].setText("Bullet remain:" + tank.numberOfBulletRemain);
            tank.updateTank(tpf, texts[1]);
            sky.skyUpdate(tpf);
            for (int i = 0; i < ENEMYNUMBER; i++) {
                enemyTank[i].updateEnemy(tpf, tank.tankNode.getWorldTranslation());
            }
            collisionTest();
            for (int i = 0; i < ENEMYNUMBER; i++) {
                for (int j = 0; j < enemyTank[i].bulletList.size(); j++) {
                    if (enemyTank[i].bulletList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                            > 2000) {
                        rootNode.detachChild(enemyTank[i].bulletList.get(j).bullet);
                        enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                    }
                }
            }
            for (int j = 0; j < tank.bulletList.size(); j++) {
                if (tank.bulletList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                        > 2000) {
                    rootNode.detachChild(tank.bulletList.get(j).bullet);
                    tank.bulletList.remove(tank.bulletList.get(j));
                }
            }
        }
    }
    
    public void collisionTest() {
        CollisionResults crs = new CollisionResults();
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < enemyTank[i].bulletList.size(); j++) {
                BoundingVolume bv = enemyTank[i].bulletList.get(j).bullet.getWorldBound();
                tank.shield.nodeshield.getChild(0).collideWith(bv, crs);
                if (crs.size() > 0) {
                    tank.shield.forceShieldControl.registerHit(enemyTank[i].bulletList.get(j).bullet.getWorldTranslation());
                    tank.shield.hitPoints -= BULLETDAMAGE;
                    if (tank.shield.hitPoints <= 0) {
                        tank.tankNode.detachChild(tank.shield.nodeshield);
                        texts[1].setLocalTranslation(0, 0, 0);
                        tank.shield.hitPoints = 0;
                    }
                    tank.shield.bar.setLocalScale((float) (tank.shield.hitPoints / 100.0), 1, 1);
                    rootNode.detachChild(enemyTank[i].bulletList.get(j).bullet);
                    enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                    crs.clear();
                } else {
                    tank.tankNode.getChild(0).collideWith(bv, crs);
                    if (crs.size() > 0) {
                        new ExplosionEffect(this, tank.tankNode, Vector3f.ZERO);
                        System.out.println("Hit player!");
                        tank.hitPoints -= BULLETDAMAGE;
                        if (tank.hitPoints <= 0) {
                            texts[2].setText("Sorry, you lose!");
                            texts[2].setLocalTranslation(gameEndPos);
                            tank.hitPoints = 0;
                        }
                        tank.bar.setLocalScale((float) (tank.hitPoints / 100.0), 1, 1);
                        rootNode.detachChild(enemyTank[i].bulletList.get(j).bullet);
                        enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                        crs.clear();
                    }
                }
            }
        }
        for (int i = 0; i < ENEMYNUMBER; i++) {
            BoundingVolume bv = tank.tankNode.getChild(0).getWorldBound();
            enemyTank[i].enemyNode.getChild(0).collideWith(bv, crs);
            if (crs.size() > 0) {
                enemyTank[i].collideWithPlayer = true;
                crs.clear();
            } else {
                bv = tank.shield.nodeshield.getChild(0).getWorldBound();
                enemyTank[i].enemyNode.getChild(0).collideWith(bv, crs);
                if (crs.size() > 0) {
                    enemyTank[i].collideWithPlayer = true;
                    crs.clear();
                } else {
                    enemyTank[i].collideWithPlayer = false;
                }
            }
        }
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < ENEMYNUMBER && j != i; j++) {
                BoundingVolume bv = enemyTank[j].enemyNode.getChild(0).getWorldBound();
                enemyTank[i].enemyNode.getChild(0).collideWith(bv, crs);
                if (crs.size() > 0) {
                    enemyTank[i].collideWithEnemy = true;
                    crs.clear();
                } else {
                    enemyTank[i].collideWithEnemy = false;
                }
            }
        }
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < tank.bulletList.size(); j++) {
                BoundingVolume bv = tank.bulletList.get(j).bullet.getWorldBound();
                enemyTank[i].enemyNode.getChild(0).collideWith(bv, crs);
                if (crs.size() > 0) {
//                    dissolveTank = new DissolveTank(this, enemyTank[i].enemyNode);
//                    enemyTank[i].enemyNode.addControl(dissolveTank);
                    new ExplosionEffect(this, enemyTank[i].enemyNode, Vector3f.ZERO);
                    System.out.println("Hit enemy!");
                    enemyTank[i].hitPoints -= BULLETDAMAGE;
                    if (enemyTank[i].hitPoints <= 0) {
                        enemyRemain--;
//                        dissolveTank = new DissolveTank(this, enemyTank[i].enemyNode);
//                        enemyTank[i].enemyNode.addControl(dissolveTank);
                        rootNode.detachChild(enemyTank[i].enemyNode);
                        float rand = FastMath.nextRandomFloat();
                        if (0.5 > rand) {
                            Powerup health = new Health(this);
                            health.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
                            this.rootNode.attachChild(health);
                        } else if (0.9 > rand) {
                            Powerup energy = new Battery(this);
                            energy.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
                            this.rootNode.attachChild(energy);
                        } else {
                            Powerup defense = new Defense(this);
                            defense.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
                            this.rootNode.attachChild(defense);
                        }
                        enemyTank[i].enemyNode.setLocalTranslation(-100, -100, -100);
                        enemyTank[i].hitPoints = 0;
                        if (enemyRemain == 0) {
                            texts[2].setText("Congratulations! You win the game!");
                            texts[2].setLocalTranslation(gameEndPos);
                        }
                    }
                    enemyTank[i].bar.setLocalScale((float) (enemyTank[i].hitPoints / 100.0), 1, 1);
                    rootNode.detachChild(tank.bulletList.get(j).bullet);
                    tank.bulletList.remove(tank.bulletList.get(j));
                    crs.clear();
                }
            }
        }
        texts[0].setText("HP:" + (int) tank.hitPoints);
        texts[0].setLocalTranslation(playerBarPos);
    }
    
    public void initText() {
        BitmapFont bmf = assetManager.loadFont("Interface/Fonts/Console.fnt");
        texts = new BitmapText[6];
        for (int j = 0; j < 6; j++) {
            texts[j] = new BitmapText(bmf);
            texts[j].setSize(bmf.getCharSet().getRenderedSize() * 2);
            texts[j].setColor(ColorRGBA.Red);
            getGuiNode().attachChild(texts[j]);
        }
        texts[1].setColor(ColorRGBA.Blue);
        texts[3].setColor(ColorRGBA.Black);
        texts[3].setLocalTranslation(numOfBulletRemainPos);
    }
    
    private void createCharacter() {
        tank = new Tank(this);
        modelPlayer = tank.tankNode;
        player = tank.tankControl;
        rootNode.attachChild(modelPlayer);
        createEnemy();
        bulletAppState.getPhysicsSpace().add(player);
    }
    
    private void initPhysics() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
    }
    
    private void createEnemy() {
        enemyTank = new EnemyTank[ENEMYNUMBER];
        modelEnemyTank = new Node[ENEMYNUMBER];
        controlEnemyTank = new CharacterControl[ENEMYNUMBER];
        for (int i = 0; i < ENEMYNUMBER; i++) {
            enemyTank[i] = new EnemyTank(this, mats[i]);
            modelEnemyTank[i] = new Node();
            modelEnemyTank[i] = enemyTank[i].enemyNode;
            enemyTank[i].adjust(tank.tankNode.getWorldTranslation());
            controlEnemyTank[i] = enemyTank[i].enemyControl;
            bulletAppState.getPhysicsSpace().add(controlEnemyTank[i]);
            rootNode.attachChild(modelEnemyTank[i]);
        }
    }
    
    private void setUpKeys() {
        inputManager.addMapping("Start", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener(this, "Start");
        inputManager.addMapping("Rotate Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Rotate Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Walk Forward", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Walk Backward", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Shot", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Shield", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(tank, "Rotate Left", "Rotate Right");
        inputManager.addListener(tank, "Walk Forward", "Walk Backward");
        inputManager.addListener(tank, "Shot");
        inputManager.addListener(tank, "Shield");
    }
    
    private void initCam() {
        flyCam.setEnabled(false);
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 5, -25));
        camNode.lookAt(new Vector3f(0, 5, 0), Vector3f.UNIT_Y);
        tank.tankNode.attachChild(camNode);
    }
    
    public void initMat() {
        mats = new Material[4];
        mats[0] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_Base.j3m");
        mats[1] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_1.j3m");
        mats[2] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_2.j3m");
        mats[3] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_3.j3m");
    }
    
    private void processor() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        int numSamples = getContext().getSettings().getSamples();
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(165f, 145f, 121f, 1.0f));
        fog.setFogDistance(2000);
        fog.setFogDensity(0.00425f);
        fpp.addFilter(fog);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloom.setBloomIntensity(2.5f);
        bloom.setBlurScale(2.5f);
        bloom.setExposurePower(1f);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Start") && isPressed) {
            pause = !pause;
        }
    }
}