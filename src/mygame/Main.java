package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import jme3tools.optimize.GeometryBatchFactory;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 */

public class Main extends SimpleApplication {

    private Flock flock;
    private final int boidCount = 6000;
    public boolean isRunning;
    public static boolean staticCentroidEnabled = true;
    //HUD
    BitmapText cohesionText;
    BitmapText separationText;
    BitmapText alignmentText;
    BitmapText centroidText;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        isRunning = true;
        initKeys();
        Mesh mesh = new Box(0.01f, 0.01f, 0.03f); // the geometric model of one boid. Here a cube of size=(x:0.01,y:0.01,z:0.03)
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // the surface material for the geometric boid model.
        mat.setColor("Color", ColorRGBA.White);
        
        Spatial fishModel = assetManager.loadModel("Models/fish/fishobj.j3o");
        Material fishMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // instantiation of the flock
        flock = new Flock(rootNode, boidCount, fishModel, fishMat, assetManager, 0.5f, 1, 2);
        //flock = new Flock(rootNode, boidCount, fishModel, fishMat, assetManager, 0.5f, 1, 2, new Vector3f(1,0,0));
        //flock = new Flock(rootNode, boidCount, fishModel, fishMat, assetManager, "cohesion");
        //flock = new Flock(rootNode, boidCount, fishModel, fishMat, assetManager, "separation");
        //flock = new Flock(rootNode, boidCount, fishModel, fishMat, assetManager, "alignment");
        //flock = new Flock(rootNode, boidCount, fishModel, fishMat, assetManager, "kdTree");
        // camera rotation is controlled via mouse movement while the position is controlled via wasd-keys
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(30);
        initText();
        
        //LICHT
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        // Verringere die Intensität des Lichts
        float intensity = -0.5f; // Neue Intensität (im Bereich von 0 bis 1)
        sun.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, intensity));
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.Blue.mult(0.5f));
        PointLight myLight = new PointLight();
        rootNode.addLight(myLight);
        rootNode.addLight(al);
        rootNode.addLight(sun);
        
        //BACKGROUND
        Texture links = assetManager.loadTexture("Sky/wasserkorall.png");
        Texture rechts = assetManager.loadTexture("Sky/wasserkorall.png");
        Texture hinten = assetManager.loadTexture("Sky/wasserkorall.png");
        Texture vorne = assetManager.loadTexture("Sky/wasserkorall.png");
        Texture hoch = assetManager.loadTexture("Sky/wassertop2.png");
        Texture runter = assetManager.loadTexture("Sky/sand.png");

        final Vector3f normalScale = new Vector3f(-1, 1, 1);
        Spatial skyBox = SkyFactory.createSky(assetManager,links,rechts, hinten,vorne,hoch, runter);
        rootNode.attachChild(skyBox);
        
        // audio
        AudioNode backgroundMusic = new AudioNode(assetManager, "Sound/Dragon_Fish.wav");
        backgroundMusic.setLooping(true);
        backgroundMusic.setTimeOffset(72.0f);
        backgroundMusic.setPositional(false);
        backgroundMusic.setVolume(0.25f);
        backgroundMusic.play();
    }

    private void initText() {
        cohesionText = new BitmapText(guiFont, false);
        cohesionText.setSize(guiFont.getCharSet().getRenderedSize());
        cohesionText.setColor(ColorRGBA.Red);
        cohesionText.setText("Cohesion: ");
        cohesionText.setLocalTranslation(5, settings.getHeight() - 25, 0);
        guiNode.attachChild(cohesionText);
        separationText = new BitmapText(guiFont, false);
        separationText.setSize(guiFont.getCharSet().getRenderedSize());
        separationText.setColor(ColorRGBA.Red);
        separationText.setText("Separation: ");
        separationText.setLocalTranslation(5, settings.getHeight() - 50, 0);
        guiNode.attachChild(separationText);
        alignmentText = new BitmapText(guiFont, false);
        alignmentText.setSize(guiFont.getCharSet().getRenderedSize());
        alignmentText.setColor(ColorRGBA.Red);
        alignmentText.setText("Alignment: ");
        alignmentText.setLocalTranslation(5, settings.getHeight() - 75, 0);
        guiNode.attachChild(alignmentText);
        centroidText = new BitmapText(guiFont, false);
        centroidText.setSize(guiFont.getCharSet().getRenderedSize());
        centroidText.setColor(ColorRGBA.Red);
        centroidText.setText("Static Centroid");
        centroidText.setLocalTranslation(5, settings.getHeight() - 100, 0);
        guiNode.attachChild(centroidText);
    }
    
    private void updateText() {
        cohesionText.setText("Cohesion: " + flock.cohesionFactor);
        separationText.setText("Separation: " + flock.separationFactor);
        alignmentText.setText("Alignment: " + flock.alignmentFactor);
    }
    
    private void initKeys(){
        inputManager.addMapping("CohesionIncrease",  new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("CohesionDecrease",  new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("SeparationIncrease",   new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("SeparationDecrease",  new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("AlignmentIncrease",  new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("AlignmentDecrease",  new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("ToggleCentroid",  new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(analogListener, "CohesionIncrease", "CohesionDecrease", "SeparationIncrease", "SeparationDecrease", "AlignmentIncrease", "AlignmentDecrease", "ToggleCentroid");
    }
    
    final private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isRunning) {
                if (name.equals("CohesionIncrease")) {
                    flock.cohesionFactor += 0.01f;
                }
                if (name.equals("CohesionDecrease")) {
                    flock.cohesionFactor -= 0.01f;                    
                }
                if (name.equals("SeparationIncrease")) {
                    flock.separationFactor += 0.01f;                    
                }
                if (name.equals("SeparationDecrease")) {
                    flock.separationFactor -= 0.01f;                     
                }
                if (name.equals("AlignmentIncrease")) {
                    flock.alignmentFactor += 0.01f;
                }
                if (name.equals("AlignmentDecrease")) {
                    flock.alignmentFactor -= 0.01f;                    
                }
                if (name.equals("ToggleCentroid")) {
                    staticCentroidEnabled = !staticCentroidEnabled;
                    if (staticCentroidEnabled) {
                        centroidText.setText("Static Centroid");            
                        } else {
                        centroidText.setText("Dynamic Centroid"); 
                    }
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        flock.update(tpf); // called once per frame
        updateText();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        // add here custom rendering stuff if needed
    }
}
