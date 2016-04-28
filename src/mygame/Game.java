package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bounding.BoundingVolume;
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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class Game extends AbstractAppState implements ActionListener {

    BitmapText[] texts, remindTexts;
    static Dimension screen;
    private Node modelPlayer;
    private Node[] modelEnemyTank;
    private CharacterControl player;
    private CharacterControl[] controlEnemyTank;
    private AudioNode audio_nature, audio_hit1, audio_hit2, audio_hit3, audio_hit4;
    Vector3f[] enemyPos = new Vector3f[ENEMYNUMBER], enemyDiePos = new Vector3f[ENEMYNUMBER],
            respawnPos = new Vector3f[ENEMYNUMBER];
    Main main;
    CameraNode camNode;
    Ground ground;
    Tank tank;
    boolean[] dieStatus = new boolean[ENEMYNUMBER];
    Enemy[] enemyTank;
    DissolveTank dissolveTank;
    Vector3f playerBarPos = new Vector3f(820, 355, 0), gameEndPos = new Vector3f(620, 580, 0),
            numOfBulletRemainPos = new Vector3f(20, 800, 0);
    Material mats[];
    public static final int ENEMYNUMBER = 4, BULLETDAMAGE = 20, RESPAWNTIME = 5;
    boolean rotate = false;
    int enemyRemain = 4;
    int music = 1;
    boolean pause = false, isDemo, cheat = false;
    List<Powerup> powerupList;
    float[] dieTime = new float[ENEMYNUMBER];
    float time = 0;

    protected Game(boolean iDemoMode) {
        isDemo = iDemoMode;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        for (int i = 0; i < ENEMYNUMBER; i++) {
            dieStatus[i] = false;
            respawnPos[i] = new Vector3f(20, numOfBulletRemainPos.y - (i + 1) * 20, 0);
        }
        main = (Main) app;
        processor();
        initText();
        initMat();
        createCharacter();
        initCam();
        initAudio();
        initBGM1();
        initKeys();
    }

    @Override
    public void update(float tpf) {
        if (enemyRemain == 0) {
            cleanAll();
            EndScreen es = new EndScreen(main, (int) time);
            main.getStateManager().detach(this);
            main.getStateManager().attach(es);
        } else if (tank.hitPoints <= 0) {
            tank.hitPoints = 0;
            cleanAll();
            EndScreen es = new EndScreen(main, -1);
            main.getStateManager().detach(this);
            main.getStateManager().attach(es);
        }
        if (pause) {
            for (int i = 0; i < ENEMYNUMBER; i++) {
                texts[2].setText("Game is paused");
                texts[2].setLocalTranslation(gameEndPos);
                enemyTank[i].enemyControl.setWalkDirection(Vector3f.ZERO);
                tank.tankControl.setWalkDirection(Vector3f.ZERO);
            }
        } else if (cheat) {
            time += tpf;
            tank.hitPoints = 1000;
            tank.bar.setLocalScale(1, 1, 1);
            tank.numberOfBulletRemain = 1000;
            tank.numOfMissile = 300;
            cheat = false;
        } else {
            time += tpf;
            texts[2].setLocalTranslation(0, 0, 0);
            texts[3].setText("Bullets remain:" + tank.numberOfBulletRemain + ". Missiles remain:" + tank.numOfMissile);
            for (int i = 0; i < ENEMYNUMBER; i++) {
                enemyPos[i] = enemyTank[i].enemyNode.getWorldTranslation();
            }
            tank.updateTank(tpf, texts[1], tank.tankNode.getWorldTranslation(), enemyPos);
            for (int i = 0; i < ENEMYNUMBER; i++) {
                if (dieStatus[i] == true) {
                    String t = String.format("Enemy %d is respawning in %.1f seconds!", i + 1, (RESPAWNTIME - time + dieTime[i]));
                    texts[i + 4].setText(t);
                    texts[i + 4].setLocalTranslation(respawnPos[i]);
                    if ((time - dieTime[i]) > RESPAWNTIME) {
                        enemyTank[i].hitPoints = 100;
                        dieStatus[i] = false;
                        enemyRemain++;
                        enemyTank[i].bar.setLocalScale((float) (enemyTank[i].hitPoints / 100.0), 1, 1);
                        Vector3f playerPos = tank.tankNode.getWorldTranslation();
                        enemyTank[i].enemyNode.setLocalTranslation(new Vector3f(playerPos.x + (float) Math.random() * 10 - 5, 160, playerPos.z + (float) Math.random() * 10 - 25));
                        enemyTank[i].enemyControl.setGravity(20);
                        main.getRootNode().attachChild(enemyTank[i].enemyNode);
                        enemyTank[i].updateEnemy(tpf, tank.tankNode.getWorldTranslation(), enemyRemain);
                    }
                } else {
                    texts[i + 4].setLocalTranslation(0, 0, 0);
                    enemyTank[i].updateEnemy(tpf, tank.tankNode.getWorldTranslation(), enemyRemain);
                }
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
            for (int j = 0; j < tank.missileList.size(); j++) {
                if (tank.missileList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                        > 2000) {
                    main.getRootNode().detachChild(tank.missileList.get(j).bullet);
                    tank.missileList.remove(tank.missileList.get(j));
                }
            }
        }
        if (audio_nature.getStatus() == AudioSource.Status.Stopped && music == 1) {
            main.getRootNode().detachChild(audio_nature);
            initBGM2();
            music = 2;
        } else if (audio_nature.getStatus() == AudioSource.Status.Stopped && music == 2) {
            main.getRootNode().detachChild(audio_nature);
            initBGM3();
            music = 3;
        } else if (audio_nature.getStatus() == AudioSource.Status.Stopped && music == 3) {
            main.getRootNode().detachChild(audio_nature);
            initBGM1();
            music = 1;
        }
    }

    public void collisionTest() {
        CollisionResults crs = new CollisionResults();
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < enemyTank[i].bulletList.size(); j++) {
                BoundingVolume bv = enemyTank[i].bulletList.get(j).bullet.getWorldBound();
                BoundingVolume shieldBound = tank.shield.nodeshield.getWorldBound();
                BoundingVolume tankBound = tank.tankNode.getWorldBound();
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
                        if (!isDemo) {
                            float rand = FastMath.nextRandomFloat();
                            if (0.25 > rand) {
                                audio_hit1.playInstance();
                            } else if (0.5 > rand) {
                                audio_hit2.playInstance();
                            } else if (0.75 > rand) {
                                audio_hit3.playInstance();
                            } else {
                                audio_hit4.playInstance();
                            }
                        }
                        tank.hitPoints -= BULLETDAMAGE;
                        if (tank.hitPoints < 100) {
                            tank.bar.setLocalScale((float) (tank.hitPoints / 100.0), 1, 1);
                        }
                        main.getRootNode().detachChild(enemyTank[i].bulletList.get(j).bullet);
                        enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                        crs.clear();
                    }
                }
            }
        }
        List<Powerup> tempList = new ArrayList<Powerup>();
        for (int i = 0; i < powerupList.size(); i++) {
            Powerup pUp = powerupList.get(i);
            BoundingVolume pUpBound = pUp.geomBoundingBox.getWorldBound();
            tank.shield.nodeshield.getChild(0).collideWith(pUpBound, crs);
            if (crs.size() > 0) {
                switch (pUp.num) {
                    case 1:
                        tank.hitPoints += BULLETDAMAGE;
                        if (tank.hitPoints < 100) {
                            tank.bar.setLocalScale((float) (tank.hitPoints / 100.0), 1, 1);
                        }
                        break;
                    case 2:
                        tank.numberOfBulletRemain += 100;
                        tank.numOfMissile++;
                        break;
                    case 3:
                        tank.shield.hitPoints += BULLETDAMAGE;
                        tank.shield.bar.setLocalScale((float) (tank.shield.hitPoints / 100.0), 1, 1);
                        break;
                }
                main.getRootNode().detachChild(pUp);
                tempList.add(pUp);
                crs.clear();
            } else {
                tank.tankNode.getChild(0).collideWith(pUpBound, crs);
                if (crs.size() > 0) {
                    switch (pUp.num) {
                        case 1:
                            tank.hitPoints += BULLETDAMAGE;
                            if (tank.hitPoints < 100) {
                                tank.bar.setLocalScale((float) (tank.hitPoints / 100.0), 1, 1);
                            }
                            break;
                        case 2:
                            tank.numberOfBulletRemain += 100;
                            tank.numOfMissile++;
                            break;

                        case 3:
                            tank.shield.hitPoints += BULLETDAMAGE;
                            tank.shield.bar.setLocalScale((float) (tank.shield.hitPoints / 100.0), 1, 1);
                            break;
                    }
                    main.getRootNode().detachChild(pUp);
                    tempList.add(pUp);
                    crs.clear();
                }
            }
        }
        powerupList.removeAll(tempList);


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
                    if (!isDemo) {
                        float rand2 = FastMath.nextRandomFloat();
                        if (0.25 > rand2) {
                            audio_hit1.playInstance();
                        } else if (0.5 > rand2) {
                            audio_hit2.playInstance();
                        } else if (0.75 > rand2) {
                            audio_hit3.playInstance();
                        } else {
                            audio_hit4.playInstance();
                        }
                    }
                    enemyTank[i].hitPoints -= BULLETDAMAGE;
                    if (enemyTank[i].hitPoints <= 0) {
                        enemyRemain--;
                        for (Bullet bullet : enemyTank[i].bulletList) {
                            bullet.bullet.setLocalTranslation(-2000, -2000, -2000);
                            main.getRootNode().detachChild(bullet.bullet);
                        }
//                        dissolveTank = new DissolveTank(this, enemyTank[i].enemyNode);
//                        enemyTank[i].enemyNode.addControl(dissolveTank);
                        main.getRootNode().detachChild(enemyTank[i].enemyNode);
                        float rand = FastMath.nextRandomFloat();
                        if (0.5 > rand) {
                            Powerup health = new Health(main);
                            addPowerup(health, i);
                        } else if (0.9 > rand) {
                            Powerup energy = new Battery(main);
                            addPowerup(energy, i);
                        } else {
                            Powerup defense = new Defense(main);
                            addPowerup(defense, i);
                        }
                        enemyDiePos[i] = enemyTank[i].enemyNode.getWorldTranslation();
                        dieStatus[i] = true;
                        dieTime[i] = time;
                        enemyTank[i].enemyNode.setLocalTranslation(tank.tankNode.getWorldTranslation().x + 1000, 1000, tank.tankNode.getWorldTranslation().z + 1000);
                        enemyTank[i].enemyControl.setGravity(0);
                        enemyTank[i].hitPoints = 0;
                    }
                    enemyTank[i].bar.setLocalScale((float) (enemyTank[i].hitPoints / 100.0), 1, 1);
                    main.getRootNode().detachChild(tank.bulletList.get(j).bullet);
                    tank.bulletList.remove(tank.bulletList.get(j));
                    crs.clear();
                }
            }
        }

        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < tank.missileList.size(); j++) {
                BoundingVolume bv = tank.missileList.get(j).missile.getWorldBound();
                enemyTank[i].enemyNode.getChild(0).collideWith(bv, crs);
                if (crs.size() > 0) {

//                    dissolveTank = new DissolveTank(this, enemyTank[i].enemyNode);
//                    enemyTank[i].enemyNode.addControl(dissolveTank);
                    new ExplosionEffect(main, enemyTank[i].enemyNode, Vector3f.ZERO);
                    enemyTank[i].hitPoints -= BULLETDAMAGE;
                    if (enemyTank[i].hitPoints <= 0) {
                        enemyRemain--;
                        for (Bullet bullet : enemyTank[i].bulletList) {
                            bullet.bullet.setLocalTranslation(-2000, -2000, -2000);
                            main.getRootNode().detachChild(bullet.bullet);
                        }
//                        dissolveTank = new DissolveTank(this, enemyTank[i].enemyNode);
//                        enemyTank[i].enemyNode.addControl(dissolveTank);
                        main.getRootNode().detachChild(enemyTank[i].enemyNode);
                        float rand = FastMath.nextRandomFloat();
                        if (0.5 > rand) {
                            Powerup health = new Health(main);
                            addPowerup(health, i);
                        } else if (0.9 > rand) {
                            Powerup energy = new Battery(main);
                            addPowerup(energy, i);
                        } else {
                            Powerup defense = new Defense(main);
                            addPowerup(defense, i);
                        }
                        enemyDiePos[i] = enemyTank[i].enemyNode.getWorldTranslation();
                        dieStatus[i] = true;
                        dieTime[i] = time;
                        enemyTank[i].enemyNode.setLocalTranslation(tank.tankNode.getWorldTranslation().x + 1000, 1000, tank.tankNode.getWorldTranslation().z + 1000);
                        enemyTank[i].enemyControl.setGravity(0);
                        enemyTank[i].hitPoints = 0;
                    }
                    enemyTank[i].bar.setLocalScale((float) (enemyTank[i].hitPoints / 100.0), 1, 1);
                    //main.getRootNode().detachChild(tank.bulletList.get(j).bullet);
                    //tank.bulletList.remove(tank.bulletList.get(j));
                    crs.clear();
                }
            }
        }
        texts[0].setText("HP:" + (int) tank.hitPoints);
        texts[0].setLocalTranslation(playerBarPos);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Pause") && isPressed) {
            pause = !pause;
        } else if (name.equals("Cheat") && isPressed) {
            cheat = true;
        }
    }

    public void initText() {
        BitmapFont bmf = main.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        texts = new BitmapText[8];
        remindTexts = new BitmapText[4];
        for (int j = 0; j < 3; j++) {
            remindTexts[j] = new BitmapText(bmf);
            remindTexts[j].setSize(bmf.getCharSet().getRenderedSize() * 2);
            remindTexts[j].setColor(ColorRGBA.Black);
            main.getGuiNode().attachChild(remindTexts[j]);
        }
        remindTexts[0].setText("Press s or d to shoot a bullet or missile.");
        remindTexts[1].setText("Press left, up, left or right to move.");
        remindTexts[2].setText("Press c to cheat.Press t for shield. Press m for map.");
        remindTexts[0].setLocalTranslation(20, 680, 0);
        remindTexts[2].setLocalTranslation(20, 660, 0);
        remindTexts[1].setLocalTranslation(20, 640, 0);
        for (int j = 0; j < 8; j++) {
            texts[j] = new BitmapText(bmf);
            texts[j].setSize(bmf.getCharSet().getRenderedSize() * 2);
            texts[j].setColor(ColorRGBA.Red);
            main.getGuiNode().attachChild(texts[j]);
        }
        texts[1].setColor(ColorRGBA.Blue);
        texts[3].setColor(ColorRGBA.Black);
        texts[3].setLocalTranslation(numOfBulletRemainPos);
        texts[4].setColor(ColorRGBA.Black);
        texts[5].setColor(ColorRGBA.Black);
        texts[6].setColor(ColorRGBA.Black);
        texts[7].setColor(ColorRGBA.Black);
    }

    private void createCharacter() {
        powerupList = new ArrayList<Powerup>();
        tank = new Tank(main);
        modelPlayer = tank.tankNode;
        player = tank.tankControl;
        main.getRootNode().attachChild(modelPlayer);
        createEnemy();
        main.bulletAppState.getPhysicsSpace().add(player);
    }

    private void addPowerup(Powerup powerup, int i) {
        powerupList.add(powerup);
        powerup.setLocalTranslation(enemyTank[i].enemyNode.getWorldTranslation());
        main.getRootNode().attachChild(powerup);

    }

    private void createEnemy() {
        enemyTank = new EnemyTank[ENEMYNUMBER];
        modelEnemyTank = new Node[ENEMYNUMBER];
        controlEnemyTank = new CharacterControl[ENEMYNUMBER];
        for (int i = 0; i < ENEMYNUMBER; i++) {
            enemyTank[i] = new EnemyTank(main, mats[i]);
            modelEnemyTank[i] = new Node();
            modelEnemyTank[i] = enemyTank[i].enemyNode;
            enemyTank[i].adjust(tank.tankNode.getWorldTranslation(), i);
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

    public void cleanAll() {
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (Bullet bullet : enemyTank[i].bulletList) {
                bullet.bullet.setLocalTranslation(-2000, -2000, -2000);
                main.getRootNode().detachChild(bullet.bullet);
            }
        }
        for (Bullet bullet : tank.bulletList) {
            bullet.bullet.setLocalTranslation(-2000, -2000, -2000);
            main.getRootNode().detachChild(bullet.bullet);
        }
        for (Missile missile : tank.missileList) {
            missile.bullet.setLocalTranslation(-2000, -2000, -2000);
            main.getRootNode().detachChild(missile.bullet);
        }
        music = 4;
        audio_nature.stop();
        audio_hit1.stop();
        main.getRootNode().detachChild(audio_hit1);
        audio_hit2.stop();
        main.getRootNode().detachChild(audio_hit2);
        audio_hit3.stop();
        main.getRootNode().detachChild(audio_hit3);
        audio_hit4.stop();
        main.getRootNode().detachChild(audio_hit4);
        main.getRootNode().detachChild(audio_nature);
        for (Powerup power : powerupList) {
            main.getRootNode().detachChild(power);
        }
        for (int i = 0; i < ENEMYNUMBER; i++) {
            main.getRootNode().detachChild(enemyTank[i].enemyNode);
        }
        for (int i = 0; i < 8; i++) {
            main.getGuiNode().detachChild(texts[i]);
        }
        for (int i = 0; i < 3; i++) {
            main.getGuiNode().detachChild(remindTexts[i]);
        }
        main.getRootNode().detachChild(tank.tankNode);
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

    private void initAudio() {
        audio_hit1 = new AudioNode(main.getAssetManager(), "Sound/hit-1.wav", false);
        audio_hit1.setPositional(false);
        audio_hit1.setLooping(false);
        audio_hit1.setVolume(1);
        main.getRootNode().attachChild(audio_hit1);

        audio_hit2 = new AudioNode(main.getAssetManager(), "Sound/hit-2.wav", false);
        audio_hit2.setPositional(false);
        audio_hit2.setLooping(false);
        audio_hit2.setVolume(1);
        main.getRootNode().attachChild(audio_hit2);

        audio_hit3 = new AudioNode(main.getAssetManager(), "Sound/hit-3.wav", false);
        audio_hit3.setPositional(false);
        audio_hit3.setLooping(false);
        audio_hit3.setVolume(1);
        main.getRootNode().attachChild(audio_hit3);

        audio_hit4 = new AudioNode(main.getAssetManager(), "Sound/hit-4.wav", false);
        audio_hit4.setPositional(false);
        audio_hit4.setLooping(false);
        audio_hit4.setVolume(1);
        main.getRootNode().attachChild(audio_hit4);
    }

    private void initBGM1() {
        audio_nature = new AudioNode(main.getAssetManager(), "Sound/Silver Surfer.ogg", true);
        audio_nature.setPositional(false);
        audio_nature.setLooping(false);
        audio_nature.setVolume(3);
        main.getRootNode().attachChild(audio_nature);
        audio_nature.play();
    }

    private void initBGM2() {
        audio_nature = new AudioNode(main.getAssetManager(), "Sound/Metal Squad.ogg", true);
        audio_nature.setPositional(false);
        audio_nature.setLooping(false);
        audio_nature.setVolume(3);
        main.getRootNode().attachChild(audio_nature);
        audio_nature.play();
    }

    private void initBGM3() {
        audio_nature = new AudioNode(main.getAssetManager(), "Sound/Pictionary.ogg", true);
        audio_nature.setPositional(false);
        audio_nature.setLooping(false);
        audio_nature.setVolume(3);
        main.getRootNode().attachChild(audio_nature);
        audio_nature.play();
    }

    public void initKeys() {
        main.getInputManager().addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        main.getInputManager().addMapping("Cheat", new KeyTrigger(KeyInput.KEY_C));
        main.getInputManager().addListener(this, "Pause", "Cheat");
    }
}
