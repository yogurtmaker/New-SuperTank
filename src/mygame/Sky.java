package mygame;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import jme3utilities.Misc;
import jme3utilities.MyAsset;
import jme3utilities.TimeOfDay;
import jme3utilities.sky.CloudLayer;
import jme3utilities.sky.GlobeRenderer;
import jme3utilities.sky.LunarPhase;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.Updater;

public class Sky {

    private Main main;
    final private static int shadowMapSize = 4096, shadowMapSplits = 3;
    final private TimeOfDay timeOfDay = new TimeOfDay(4.75f);
    private int clockDirection = +1;
    private Node sceneNode = new Node("scene node");
    private SkyControl skyControl = null;
    private Spatial cubeMap = null;
    private AmbientLight ambientLight = null;
    private DirectionalLight mainLight = null;

    public Sky(Main main) {
        this.main = main;
        main.getStateManager().attach(timeOfDay);
        initializeLights();
        initializeSky();
        addShadows(main.getViewPort());
        main.getRootNode().attachChild(sceneNode);
    }

    float getHours() {
        float hour = timeOfDay.getHour();
        return hour;
    }

    public void skyUpdate(float elapsedTime) {
        timeOfDay.setRate(clockDirection * 1000f);
        skyControl.getSunAndStars().setHour(getHours());
        skyControl.setPhase(LunarPhase.FULL);
    }

    private void addShadows(ViewPort viewPort) {
        Updater updater = skyControl.getUpdater();
        DirectionalLightShadowFilter dlsf =
                new DirectionalLightShadowFilter(main.getAssetManager(),
                shadowMapSize, shadowMapSplits);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
        dlsf.setLight(mainLight);
        Misc.getFpp(viewPort, main.getAssetManager()).addFilter(dlsf);
        updater.addShadowFilter(dlsf);
    }

    private void initializeSky() {
        cubeMap = MyAsset.createStarMap(main.getAssetManager(), "purple-nebula-complex");
        sceneNode.attachChild(cubeMap);
        float cloudFlattening;
        boolean starMotion;
        boolean bottomDome;
        cloudFlattening = 0.9f;
        starMotion = true;
        bottomDome = true;
        skyControl = new SkyControl(main.getAssetManager(), main.getCamera(), cloudFlattening,
                starMotion, bottomDome);
        sceneNode.addControl(skyControl);
        CloudLayer mainLayer = skyControl.getCloudLayer(0);
        mainLayer.setMotion(0.37f, 0f, 0.2f, 0.001f);
        mainLayer.setTexture("Textures/skies/cyclone.png", 0.3f);
        skyControl.getCloudLayer(1).clearTexture();
        Texture moonTexture = MyAsset.loadTexture(main.getAssetManager(),
                "Textures/skies/clementine.png");
        Material moonMaterial = MyAsset.createShadedMaterial(main.getAssetManager(),
                moonTexture);
        int equatorSamples = 12;
        int meridianSamples = 24;
        int resolution = 512;
        GlobeRenderer moonRenderer = new GlobeRenderer(moonMaterial,
                Image.Format.Alpha8, equatorSamples, meridianSamples,
                resolution);
        main.getStateManager().attach(moonRenderer);
        skyControl.setMoonRenderer(moonRenderer);
        skyControl.setSunStyle("Textures/skies/hazy-disc.png");
        skyControl.setCloudiness(1f);
        skyControl.setCloudModulation(true);
        skyControl.setCloudRate(1f);
        skyControl.setCloudYOffset(0f);
        skyControl.setLunarDiameter(0.031f);
        skyControl.setSolarDiameter(0.031f);
        skyControl.getSunAndStars().setSolarLongitude(0f);
        skyControl.setTopVerticalAngle(FastMath.HALF_PI);
        skyControl.setStarMaps("Textures/skies/star-maps/16m");
        skyControl.getSunAndStars().orientExternalSky(cubeMap);
        skyControl.getUpdater().setAmbientMultiplier(1f);
        skyControl.getUpdater().setMainMultiplier(1f);
        skyControl.getUpdater().setBloomEnabled(true);
        skyControl.getUpdater().setShadowFiltersEnabled(true);
        skyControl.setEnabled(true);
        Updater updater = skyControl.getUpdater();
        updater.addViewPort(main.getViewPort());
        updater.setAmbientLight(ambientLight);
        updater.setMainLight(mainLight);
    }

    private void initializeLights() {
        mainLight = new DirectionalLight();
        main.getRootNode().addLight(mainLight);
        ambientLight = new AmbientLight();
        main.getRootNode().addLight(ambientLight);
    }
}