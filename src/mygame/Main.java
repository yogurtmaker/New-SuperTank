package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Main extends SimpleApplication {

    static Dimension screen;
    BulletAppState bulletAppState;
    Ground ground;
    Sky sky;
    int enemyRemain = 4;

    public static void main(final String[] args) {
        Main app = new Main();
        initAppScreen(app);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initPhysics();
        ground = new Ground(this);
        sky = new Sky(this);
        StartScreen st = new StartScreen(this);
        stateManager.attach(st);

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
        sky.skyUpdate(tpf);
    }

    private void initPhysics() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
    }

    public AppSettings getSettings() {
        return (settings);
    }
}
