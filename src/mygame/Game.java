/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
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
import java.awt.Dimension;

/**
 *
 * @author alien
 */
public class Game extends AbstractAppState implements ActionListener {

    BitmapText[] texts;
    static Dimension screen;
    private Node modelPlayer;
    private Node[] modelEnemyTank;
    private CharacterControl player;
    private CharacterControl[] controlEnemyTank;
    Main main;
    CameraNode camNode;
    Ground ground;
    Tank tank;
    Enemy[] enemyTank;
    DissolveTank dissolveTank;
    Vector3f playerBarPos = new Vector3f(820, 355, 0), gameEndPos = new Vector3f(520, 750, 0),
            numOfBulletRemainPos = new Vector3f(20, 800, 0);
    Material mats[];
    final int ENEMYNUMBER = 4, BULLETDAMAGE = 20;
    boolean rotate = false;
    int enemyRemain = 4;
    boolean pause = false, isDemo;

    protected Game(boolean iDemoMode) {
        isDemo = iDemoMode;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        main = (Main) app;
        processor();
        initText();
        initMat();
        createCharacter();
        initCam();

    }

    @Override
    public void update(float tpf) {
        if (pause) {
            for (int i = 0; i < ENEMYNUMBER; i++) {
                enemyTank[i].enemyControl.setWalkDirection(Vector3f.ZERO);
                tank.tankControl.setWalkDirection(Vector3f.ZERO);
            }
        } else {
            texts[3].setText("Bullet remain:" + tank.numberOfBulletRemain);
            tank.updateTank(tpf, texts[1]);
            for (int i = 0; i < ENEMYNUMBER; i++) {
                enemyTank[i].updateEnemy(tpf, tank.tankNode.getWorldTranslation());
            }
            collisionTest();
            for (int i = 0; i < ENEMYNUMBER; i++) {
                for (int j = 0; j < enemyTank[i].bulletList.size(); j++) {
                    if (enemyTank[i].bulletList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                            > 2000) {
                        main.getRootNode().detachChild(enemyTank[i].bulletList.get(j).bullet);
                        enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                    }
                }
            }
            for (int j = 0; j < tank.bulletList.size(); j++) {
                if (tank.bulletList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                        > 2000) {
                    main.getRootNode().detachChild(tank.bulletList.get(j).bullet);
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
                    main.getRootNode().detachChild(enemyTank[i].bulletList.get(j).bullet);
                    enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                    crs.clear();
                } else {
                    tank.tankNode.getChild(0).collideWith(bv, crs);
                    if (crs.size() > 0) {
                        new ExplosionEffect(main, tank.tankNode, Vector3f.ZERO);
                        System.out.println("Hit player!");
                        tank.hitPoints -= BULLETDAMAGE;
                        if (tank.hitPoints <= 0) {
                            texts[2].setText("Sorry, you lose!");
                            texts[2].setLocalTranslation(gameEndPos);
                            tank.hitPoints = 0;
                        }
                        tank.bar.setLocalScale((float) (tank.hitPoints / 100.0), 1, 1);
                        main.getRootNode().detachChild(enemyTank[i].bulletList.get(j).bullet);
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
                    new ExplosionEffect(main, enemyTank[i].enemyNode, Vector3f.ZERO);
                    System.out.println("Hit enemy!");
                    enemyTank[i].hitPoints -= BULLETDAMAGE;
                    if (enemyTank[i].hitPoints <= 0) {
                        enemyRemain--;
//                        dissolveTank = new DissolveTank(this, enemyTank[i].enemyNode);
//                        enemyTank[i].enemyNode.addControl(dissolveTank);
                        main.getRootNode().detachChild(enemyTank[i].enemyNode);
                        float rand = FastMath.nextRandomFloat();
                        if (0.5 > rand) {
                            Powerup health = new Health(main);
                            health.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
                            main.getRootNode().attachChild(health);
                        } else if (0.9 > rand) {
                            Powerup energy = new Battery(main);
                            energy.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
                            main.getRootNode().attachChild(energy);
                        } else {
                            Powerup defense = new Defense(main);
                            defense.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
                            main.getRootNode().attachChild(defense);
                        }
                        enemyTank[i].enemyNode.setLocalTranslation(-100, -100, -100);
                        enemyTank[i].hitPoints = 0;
                        if (enemyRemain == 0) {
                            texts[2].setText("Congratulations! You win the game!");
                            texts[2].setLocalTranslation(gameEndPos);
                        }
                    }
                    enemyTank[i].bar.setLocalScale((float) (enemyTank[i].hitPoints / 100.0), 1, 1);
                    main.getRootNode().detachChild(tank.bulletList.get(j).bullet);
                    tank.bulletList.remove(tank.bulletList.get(j));
                    crs.clear();
                }
            }
        }
        texts[0].setText("HP:" + (int) tank.hitPoints);
        texts[0].setLocalTranslation(playerBarPos);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Start") && isPressed) {
            pause = !pause;
        }
    }

    public void initText() {
        BitmapFont bmf = main.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        texts = new BitmapText[6];
        for (int j = 0; j < 6; j++) {
            texts[j] = new BitmapText(bmf);
            texts[j].setSize(bmf.getCharSet().getRenderedSize() * 2);
            texts[j].setColor(ColorRGBA.Red);
            main.getGuiNode().attachChild(texts[j]);
        }
        texts[1].setColor(ColorRGBA.Blue);
        texts[3].setColor(ColorRGBA.Black);
        texts[3].setLocalTranslation(numOfBulletRemainPos);
    }

    private void createCharacter() {
        tank = new Tank(main);
        modelPlayer = tank.tankNode;
        player = tank.tankControl;
        main.getRootNode().attachChild(modelPlayer);
        createEnemy();
        main.bulletAppState.getPhysicsSpace().add(player);
    }


    private void createEnemy() {
        enemyTank = new EnemyTank[ENEMYNUMBER];
        modelEnemyTank = new Node[ENEMYNUMBER];
        controlEnemyTank = new CharacterControl[ENEMYNUMBER];
        for (int i = 0; i < ENEMYNUMBER; i++) {
            enemyTank[i] = new EnemyTank(main, mats[i]);
            modelEnemyTank[i] = new Node();
            modelEnemyTank[i] = enemyTank[i].enemyNode;
            enemyTank[i].adjust(tank.tankNode.getWorldTranslation());
            controlEnemyTank[i] = enemyTank[i].enemyControl;
            main.bulletAppState.getPhysicsSpace().add(controlEnemyTank[i]);
            main.getRootNode().attachChild(modelEnemyTank[i]);
        }
    }

    private void initCam() {
        main.getFlyByCamera().setEnabled(false);
        camNode = new CameraNode("CamNode", main.getCamera());
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 5, -25));
        camNode.lookAt(new Vector3f(0, 5, 0), Vector3f.UNIT_Y);
        tank.tankNode.attachChild(camNode);
    }

    public void initMat() {
        mats = new Material[4];
        mats[0] = main.getAssetManager()
                .loadMaterial("Materials/Active/MultiplyColor_Base.j3m");
        mats[1] = main.getAssetManager()
                .loadMaterial("Materials/Active/MultiplyColor_1.j3m");
        mats[2] = main.getAssetManager()
                .loadMaterial("Materials/Active/MultiplyColor_2.j3m");
        mats[3] = main.getAssetManager()
                .loadMaterial("Materials/Active/MultiplyColor_3.j3m");
    }

    private void processor() {
        FilterPostProcessor fpp = new FilterPostProcessor(main.getAssetManager());
        int numSamples = main.getContext().getSettings().getSamples();
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
        main.getViewPort().addProcessor(fpp);
    }
}
