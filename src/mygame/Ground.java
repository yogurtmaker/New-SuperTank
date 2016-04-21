package mygame;

import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainGridLodControl;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.grid.FractalTileLoader;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.ShaderUtils;
import com.jme3.terrain.noise.basis.FilteredBasis;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.terrain.noise.modulator.NoiseModulator;
import com.jme3.texture.Texture;

public class Ground {

    private Main main;
    private Material mat_terrain;
    private TerrainGrid terrain;
    private Game game;

    public Ground(Main main) {
        this.main = main;
        initTerrain();
        initPhysics();
    }

    private void initTerrain() {
        mat_terrain = new Material(main.getAssetManager(), "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

        Texture dirt = main.getAssetManager().loadTexture("Textures/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("region1ColorMap", dirt);
        mat_terrain.setVector3("region1", new Vector3f(16, 258, 16));
        mat_terrain.setTexture("slopeColorMap", dirt);
        mat_terrain.setFloat("slopeTileFactor", 32);
        mat_terrain.setFloat("terrainSize", 512);

        FractalSum base = new FractalSum();
        base.setRoughness(0.7f);
        base.setFrequency(1.0f);
        base.setAmplitude(1.0f);
        base.setLacunarity(2.12f);
        base.setOctaves(8);
        base.setScale(0.02125f);
        base.addModulator(new NoiseModulator() {
            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });

        FilteredBasis ground = new FilteredBasis(base);

        PerturbFilter perturb = new PerturbFilter();
        perturb.setMagnitude(0.119f);

        OptimizedErode therm = new OptimizedErode();
        therm.setRadius(5);
        therm.setTalus(0.011f);

        SmoothFilter smooth = new SmoothFilter();
        smooth.setRadius(1);
        smooth.setEffect(0.7f);

        IterativeFilter iterate = new IterativeFilter();
        iterate.addPreFilter(perturb);
        iterate.addPostFilter(smooth);
        iterate.setFilter(therm);
        iterate.setIterations(1);

        ground.addPreFilter(iterate);

        terrain = new TerrainGrid("terrain", 65, 1025, new FractalTileLoader(ground, 256f));
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0, 0, 0);
        terrain.setLocalScale(2f, 1f, 2f);
        terrain.setShadowMode(RenderQueue.ShadowMode.Receive);
        main.getRootNode().attachChild(terrain);

        TerrainLodControl control = new TerrainGridLodControl(terrain, main.getCamera());
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f));
        terrain.addControl(control);
    }

    private void initPhysics() {
        terrain.addControl(new RigidBodyControl(0));
        terrain.addListener(new TerrainGridListener() {
            public void gridMoved(Vector3f newCenter) {
            }

            public void tileAttached(Vector3f cell, TerrainQuad quad) {
                while (quad.getControl(RigidBodyControl.class) != null) {
                    quad.removeControl(RigidBodyControl.class);
                }
                quad.addControl(new RigidBodyControl(new HeightfieldCollisionShape(quad.getHeightMap(), terrain.getLocalScale()), 0));
                main.bulletAppState.getPhysicsSpace().add(quad);
            }

            public void tileDetached(Vector3f cell, TerrainQuad quad) {
                if (quad.getControl(RigidBodyControl.class) != null) {
                    main.bulletAppState.getPhysicsSpace().remove(quad);
                    quad.removeControl(RigidBodyControl.class);
                }
            }
        });
    }
}
