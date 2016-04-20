package mygame;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class Dust {
    
    Main main;
    public ParticleEmitter emit;
    
    public Dust(Main main){
        this.main = main;
        initDust();
    }
    
    private void initDust(){
        emit = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 300);
        emit.setLocalTranslation(0, -3.5f, 0f);
        emit.setStartSize(3f);
        emit.setStartColor(ColorRGBA.Brown);
        emit.setEndColor(ColorRGBA.DarkGray);
        emit.setGravity(0, 0, 0);
        emit.setVelocityVariation(1);
        emit.setLowLife(0.5f);
        emit.setHighLife(0.5f);
        emit.setInitialVelocity(new Vector3f(0, .5f, 0));
        emit.setImagesX(15);
        Material mat = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", main.getAssetManager().loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);
    }
}