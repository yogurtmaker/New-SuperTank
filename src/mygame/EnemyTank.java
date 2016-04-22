package mygame;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;
import java.util.Random;

public class EnemyTank extends Enemy {

    Random rn;
    String[] states = {"RotateLeft", "RotateRight", "WalkForward", "WalkBackward", "Shoot", "Stop"};
    String binding = states[5];
    Vector3f bulletPosition, velocity, tankPostion, playerDirection, leftDirection,
            rightDirection, view = new Vector3f(0, 0, 0), escapePos;
    public boolean walk = false, attached = false, detached = false, bulletCreated = false, track = false, escape = false, second = false;
    float stateTime, frequency, resetTime, time = 0, delay = 0, time2 = 0, time3 = 0;
    final int ROTATETIME = 5, WALKTIME = 5, FORCETIME = 3, SHOOTTIME = 10, TRACKDISTANCE = 450, ATTACKDISTANCE = 200,
            STOPTIME = 3, LEVELUPTIME = 60;
    float rotateLeftTime = 0;
    float rotateRightTime = 0;
    float walkForwardTime = 0;
    float walkBackwardTime = 0;
    float forceTime = 0;
    float shootTime = 0, level = 1;
    float stopTime = 0, levelTime = 0;
    int state;

    public EnemyTank(Main main, Material mat) {
        super(main, "Models/HoverTank/Tank2.mesh.xml", mat);
        initStuff();
    }

    void initStuff() {
        Box box = new Box(2.5f, 0.3f, 0.3f);
        bar = new Geometry("bar", box);
        Material matBar = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBar.setColor("Color", new ColorRGBA(1f, 0f, 0f, 0.2f));
        matBar.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bar.setMaterial(matBar);
        bar.setLocalTranslation(0, 3.3f, 2);
        bar.setQueueBucket(RenderQueue.Bucket.Transparent);
        enemyNode.attachChild(bar);

        rn = new Random();
        stateTime = rn.nextFloat() * 5 + 5;
        //System.out.println(stateTime);
        frequency = rn.nextFloat() * 2 + 1;
        //System.out.println(frequency);
        dust = new Dust(main);
        dust.emit.setParticlesPerSec(20f);
        enemyNode.attachChild(dust.emit);
    }

    protected void escapePlayer(Quaternion rotLeft, Quaternion rotRight, Quaternion limLeft, Quaternion limRight, Quaternion rotReset) {
        leftDirection = leftNode1.getWorldTranslation().subtract(tankPostion);
        rightDirection = rightNode1.getWorldTranslation().subtract(tankPostion);
        escapePos = playerDirection.negate().add(new Vector3f(playerDirection.x / 2, 0, 0));
        if (leftDirection.normalize().subtract(escapePos.normalize()).length() < 0.04
                || rightDirection.normalize().subtract(escapePos.normalize()).length() < 0.04) {
            rotateBack(rotLeft, rotRight, rotReset);
        } else if (leftDirection.subtract(escapePos).length() < rightDirection.subtract(escapePos).length()) {
            resetTime = 90;
            enemyControl.setViewDirection(leftDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                enemyNode.getChild(0).rotate(rotLeft);
            }
        } else if (leftDirection.subtract(escapePos).length() > rightDirection.subtract(escapePos).length()) {
            resetTime = 100;
            enemyControl.setViewDirection(rightDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                enemyNode.getChild(0).rotate(rotRight);
            }
        } else {
            rotateBack(rotLeft, rotRight, rotReset);
        }
        enemyControl.setWalkDirection(velocity.mult(0.3f * level));
    }

    protected void aimPlayer(Quaternion rotLeft, Quaternion rotRight, Quaternion limLeft, Quaternion limRight, Quaternion rotReset) {
        leftDirection = leftNode1.getWorldTranslation().subtract(tankPostion);
        rightDirection = rightNode1.getWorldTranslation().subtract(tankPostion);
        if (leftDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04
                || rightDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04) {
            rotateBack(rotLeft, rotRight, rotReset);
        } else if (leftDirection.subtract(playerDirection).length() < rightDirection.subtract(playerDirection).length()) {
            resetTime = 90;
            enemyControl.setViewDirection(leftDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                enemyNode.getChild(0).rotate(rotLeft);
            }
        } else if (leftDirection.subtract(playerDirection).length() > rightDirection.subtract(playerDirection).length()) {
            resetTime = 100;
            enemyControl.setViewDirection(rightDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                enemyNode.getChild(0).rotate(rotRight);
            }
        } else {
            rotateBack(rotLeft, rotRight, rotReset);
        }
        enemyControl.setWalkDirection(velocity.mult(0.2f * level));
    }

    protected void rotateBack(Quaternion rotLeft, Quaternion rotRight, Quaternion rotReset) {
        if (enemyNode.getChild(0).getLocalRotation().getZ() > rotReset.getZ() && resetTime > 0) {
            resetTime--;
            enemyNode.getChild(0).rotate(rotLeft);
        } else if (enemyNode.getChild(0).getLocalRotation().getZ() < rotReset.getZ() && resetTime > 0) {
            resetTime--;
            enemyNode.getChild(0).rotate(rotRight);
        } else if (resetTime <= 0) {
            enemyNode.getChild(0).setLocalRotation(rotReset);
        }
    }

    protected void move(Quaternion rotLeft, Quaternion rotRight, Quaternion limLeft, Quaternion limRight, Quaternion rotReset) {
        if (leftRotate) {
            resetTime = 90;
            leftDirection = leftNode.getWorldTranslation().subtract(tankPostion);
            enemyControl.setViewDirection(leftDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() >= limLeft.getZ()) {
                enemyNode.getChild(0).rotate(rotLeft);
            }
        } else if (rightRotate) {
            resetTime = 100;
            rightDirection = rightNode.getWorldTranslation().subtract(tankPostion);
            enemyControl.setViewDirection(rightDirection);
            if (enemyNode.getChild(0).getLocalRotation().getZ() <= limRight.getZ()) {
                enemyNode.getChild(0).rotate(rotRight);
            }
        } else {
            rotateBack(rotLeft, rotRight, rotReset);
        }
    }

    @Override
    protected void adjust(Vector3f playerPos, int i) {
        if (i == 0) {
            enemyControl.warp(new Vector3f(playerPos.x + 350, 200, playerPos.z + 350));
        }
        if (i == 1) {
            enemyControl.warp(new Vector3f(playerPos.x - 350, 200, playerPos.z + 350));
        }
        if (i == 2) {
            enemyControl.warp(new Vector3f(playerPos.x + 350, 200, playerPos.z - 350));
        }
        if (i == 3) {
            enemyControl.warp(new Vector3f(playerPos.x - 350, 200, playerPos.z - 350));
        }
    }

    @Override
    protected void updateEnemy(float tpf, Vector3f playerPos, int enemyRemain) {

        Quaternion rotLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI * tpf / 4);
        Quaternion rotRight = new Quaternion().fromAngles(0, 0, FastMath.PI * tpf / 4);
        Quaternion rotReset = new Quaternion().fromAngles(0, 0, 0);
        Quaternion limLeft = new Quaternion().fromAngles(0, 0, -FastMath.PI / 4);
        Quaternion limRight = new Quaternion().fromAngles(0, 0, FastMath.PI / 4);
        time += tpf;
        delay = delay - tpf;
        for (Bullet bullet : bulletList) {
            bullet.update(tpf);
        }
        tankPostion = enemyNode.getWorldTranslation();
        playerDirection = playerPos.subtract(tankPostion);
        if (enemyRemain == 1) {
            escape = true;
        } else {
            escape = false;
        }
        if (playerDirection.length() > TRACKDISTANCE) {
            track = true;
        } else {
            track = false;
        }
        if (playerDirection.length() < ATTACKDISTANCE) {
            attack = true;
        } else {
            attack = false;
        }

        if (hitPoints <= 0) {
            death = true;
        } else {
            death = false;
        }
        bulletPosition = bulletStartNode.getWorldTranslation();
        velocity = bulletPosition.subtract(tankPostion)
                .subtract(new Vector3f(0, 3, 0)).mult(1.01f);
        if (!death) {
            if (time > 14) {
                if ((int) time % LEVELUPTIME == 1) {
                    if (!second) {
                        level += 0.3;
                        second = true;
                    }
                } else {
                    second = false;
                }
                enemyControl.setGravity(20f);
                if (collideWithPlayer || collideWithEnemy) {
                    enemyControl.setWalkDirection(velocity.mult(0.3f).negate());
                } else if (escape) {
                    escapePlayer(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (track) {
                    aimPlayer(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (attack) {
                    leftDirection = leftNode1.getWorldTranslation().subtract(tankPostion);
                    rightDirection = rightNode1.getWorldTranslation().subtract(tankPostion);
                    if (leftDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04
                            || rightDirection.normalize().subtract(playerDirection.normalize()).length() < 0.04) {
                        shoot = true;
                    } else {
                        shoot = false;
                    }
                    aimPlayer(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (forward) {
                    dust.emit.setParticlesPerSec(20);
                    enemyControl.setWalkDirection(velocity.mult(0.2f * level));
                    move(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (backward) {
                    enemyControl.setWalkDirection(velocity.mult(0.2f * level).negate());
                    move(rotLeft, rotRight, limLeft, limRight, rotReset);
                } else if (stop) {
                    enemyControl.setWalkDirection(Vector3f.ZERO);
                    rotateBack(rotLeft, rotRight, rotReset);
                } else {
                    rotateBack(rotLeft, rotRight, rotReset);
                }
                if (shoot && delay <= 0) {
                    //delay = 2f;
                    float passTime = time - time2;
                    if (passTime > frequency) {
                        if (!bulletCreated) {
                            Bullet bullet = new Bullet(main, bulletStartNode.getWorldTranslation(),
                                    enemyNode.getWorldTranslation());
                            bullet.bullet.setLocalRotation(enemyNode.getLocalRotation());
                            bulletList.add(bullet);
                            main.getRootNode().attachChild(bullet.bullet);
                            bulletCreated = true;
                            time2 = time;
                        }
                    } else {
                        bulletCreated = false;
                    }
                }
            }
        }

        //update from previous abstracontrol
        if (time - time3 >= stateTime) {
            stateTime = rn.nextFloat() * 5 + 5;
            frequency = rn.nextFloat() * 2 + 1;
            state = rn.nextInt(states.length);
            binding = states[state];
            time3 = time;
        }
        if (binding.equals("RotateLeft") && !rightRotate) {
            leftRotate = true;
        } else if (binding.equals("RotateRight") && !leftRotate) {
            rightRotate = true;
        } else if (binding.equals("WalkForward") && !backward) {
            forward = true;
        } else if (binding.equals("WalkBackward") && !forward) {
            backward = true;
        } else if (binding.equals("Shoot")) {
            shoot = true;
        } else if (binding.equals("Stop")) {
            stop = true;
        }

        if (leftRotate) {
            rotateLeftTime += tpf;
            if (rotateLeftTime > ROTATETIME) {
                rotateLeftTime = 0;
                Quaternion quan = new Quaternion().fromAngles(0, 0, 0);
                enemyNode.getChild(0).setLocalRotation(quan);
                leftRotate = false;
            }
        }
        if (rightRotate) {
            rotateRightTime += tpf;
            if (rotateRightTime > ROTATETIME) {
                rotateRightTime = 0;
                Quaternion quan = new Quaternion().fromAngles(0, 0, 0);
                enemyNode.getChild(0).setLocalRotation(quan);
                rightRotate = false;
            }
        }
        if (forward) {
            walkForwardTime += tpf;
            if (walkForwardTime > WALKTIME) {
                walkForwardTime = 0;
                forward = false;
            }
        }
        if (backward) {
            walkBackwardTime += tpf;
            if (walkBackwardTime > WALKTIME) {
                walkBackwardTime = 0;
                backward = false;
            }
        }
        if (force) {
            forceTime += tpf;
            if (forceTime > FORCETIME) {
                force = false;
                forceTime = 0;
            }
        }
        if (stop) {
            stopTime += tpf;
            if (stopTime > STOPTIME) {
                stop = false;
                stopTime = 0;
            }
        }
        if (shoot) {
            shootTime += tpf;
            if (shootTime > SHOOTTIME) {
                shoot = false;
                shootTime = 0;
            }
        }
    }
}
