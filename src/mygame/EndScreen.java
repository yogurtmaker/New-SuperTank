/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.state.AbstractAppState;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author 2
 */
public class EndScreen extends AbstractAppState implements ActionListener {

    private AudioNode audio_nature;
    Main main;
    Node EndNode;
    Vector3f center = new Vector3f(0, 180, 0);
    float time = 0;
    Box startbox;
    Geometry endGeom;
    InputManager inputManager;
    BitmapText hudText;
    BitmapText rankText;
    BitmapText remindText;
    String name = "player";
    String duration;
    String date;
    List<Rank> list;
    TextGUI gui;
    String str = String.format("%-12s%-16s%-12s%14s\n", "Rank", "Name", "Date", "Duration");

    public EndScreen(Main main, int duration) {
        this.duration = String.valueOf(duration);
        date = getDate();
        this.main = main;
        initEndScreen();
        initKey();
        initCam();
        initText();
        initBGM1();

        list = readFile();
//       list = new   ArrayList<Rank>();
//String strData = String.format("%s,%s,%s", "John Micle","2016/4/21","1200");
//String strData1 = String.format("%s,%s,%s", "Sombody","2016/12/21","200");
//
//list.add(Rank.parseAsRank(strData));
//list.add(Rank.parseAsRank(strData1));

        if (this.duration.equals("-1")) {
            String temp = "";
            boolean first3 = false;
            for (int i = 0; i < 3 && i < list.size(); i++) {
                Rank temprank = list.get(i);
                String strData = String.format("%-16s%-16s%-16s%-14s\n", String.valueOf(i + 1),
                        temprank.name, temprank.date, temprank.duration);
                temp += strData;

                if (temprank.name.equals("player") && temprank.date.equals(date) && temprank.duration.equals(duration)) {
                    first3 = true;
                }
                rankText.setText(str + temp);
            }
        } else {
            initRank();
        }

    }

    private void initBGM1() {
        audio_nature = new AudioNode(main.getAssetManager(), "Sound/Thomas_Greenberg.wav", true);
        audio_nature.setPositional(false);
        audio_nature.setLooping(false);
        audio_nature.setVolume(3);
        main.getRootNode().attachChild(audio_nature);
        audio_nature.play();
    }

    public void initRank() {
        Rank rank = new Rank(name, date, duration);
        list.add(rank);

        Collections.sort(list, Rank.durationComparator);
        writeFile(list);
        String temp = "";
        boolean first3 = false;
        for (int i = 0; i < 3 && i < list.size(); i++) {
            Rank temprank = list.get(i);
            String strData = String.format("%-16s%-16s%-10s%14s\n", String.valueOf(i + 1),
                    temprank.name, temprank.date, temprank.duration);
            temp += strData;

            if (temprank.name.equals("player") && temprank.date.equals(date) && temprank.duration.equals(duration)) {
                first3 = true;
            }
            rankText.setText(str + temp);

            if (first3) {
                gui = new TextGUI(this);
            }
        }

    }

    public void changeName(String name) {
        //gui.dispose();
        this.name = name;
        for (int i = 0; i < list.size(); i++) {
            Rank r = list.get(i);
            if (r.name.equals("player") && r.date.equals(date) && r.duration.equals(duration)) {
                r = new Rank(name, date, duration);
                list.set(i, r);
            }
            writeFile(list);
        }




        String temp = "";
        for (int i = 0; i < 3 && i < list.size(); i++) {
            Rank temprank = list.get(i);
            String strData = String.format("%-12s%-16s%-10s%14s\n", String.valueOf(i + 1),
                    temprank.name, temprank.date, temprank.duration);
            temp += strData;
        }
        rankText.setText(str + temp);

    }

    private List<Rank> readFile() {
        List<Rank> templist = new ArrayList<Rank>();
        try {
            String fileName = "info.txt";
            String line = null;
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                Rank rank = new Rank();
                templist.add(rank.parseAsRank(line));
            }
            bufferedReader.close();
        } catch (Exception ex) {
        }
        return templist;
    }

    private void writeFile(List<Rank> templist) {
        try {
            File f = new File("info.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            String str = "";
            for (Rank r : templist) {
                str += String.format("%s,%s,%s\n", r.name, r.date, r.duration);;
            }
            bw.write(str);
            bw.newLine();
            bw.close();
        } catch (Exception ex) {
        }
    }

    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void initEndScreen() {
        EndNode = new Node();
        EndNode.setLocalTranslation(center);
        main.getRootNode().attachChild(EndNode);

    }

    private void initKey() {
        inputManager = main.getInputManager();
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Click");
    }

    private void initCam() {
//        main.getFlyByCamera().setEnabled(false);
//        main.getCamera().setLocation(center);

        main.getFlyByCamera().setEnabled(false);
        Node camNode = new CameraNode("CamNode", main.getCamera());
        camNode.setLocalTranslation(new Vector3f(0, 10, -40));
        EndNode.attachChild(camNode);
    }

    private void initText() {
        startbox = new Box(5, 1, .1f);
        endGeom = new Geometry("endBox", startbox);
        Material mat = new Material(main.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//mat.setColor("Color", ColorRGBA.Gray);
        endGeom.setMaterial(mat);
//g.setQueueBucket(RenderQueue.Bucket.Transparent);

        BitmapFont guiFont = main.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.scale(2);
        hudText.setColor(ColorRGBA.Black);
        hudText.setText("Restart\n");
        hudText.setLocalTranslation(670, 180, 0);



//
        rankText = new BitmapText(guiFont, false);
        rankText.setSize(guiFont.getCharSet().getRenderedSize());
        rankText.scale(2);
        rankText.setColor(ColorRGBA.Black);

//String strData = String.format("%-12s%-16s%10s%14s\n", "1","John Micle","4/21/2016","1200");
//String strData1 = String.format("%-12s%-16s%10s%14s\n", "2","Sombody","12/21/2016","200");
//rankText.setText(str+strData+strData1);             // the text
        rankText.setLocalTranslation(560, 680, 0);


        remindText = new BitmapText(guiFont, false);
        remindText.setSize(guiFont.getCharSet().getRenderedSize() * 5);
//remindText.scale(5);
        remindText.setColor(ColorRGBA.Red);
        String a = "Game Over !\n";
        if (!duration.equals("-1")) {
            a += "Your duration is " + duration;
        }
        remindText.setText(a);
        remindText.setLocalTranslation(470, 420, 0);





        EndNode.attachChild(endGeom);
        main.getGuiNode().attachChild(hudText);
        main.getGuiNode().attachChild(rankText);
        main.getGuiNode().attachChild(remindText);
    }

    private void cleanAll() {
        audio_nature.stop();
        main.getRootNode().detachChild(audio_nature);
        EndNode.detachAllChildren();
        main.getRootNode().detachChild(EndNode);
        main.getGuiNode().detachChild(hudText);
        main.getGuiNode().detachChild(rankText);
        main.getGuiNode().detachChild(remindText);
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
                if (target.equals("endBox")) {
                    StartScreen startScreen = new StartScreen(main);
                    cleanAll();
                    main.getStateManager().detach(this);
                    main.getStateManager().attach(startScreen);
                }
            }

        }
    }
}
