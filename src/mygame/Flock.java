/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.instancing.InstancedNode;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;
import static mygame.Main.staticCentroidEnabled;
/**
 * This class controls and manages all boids within a flock (swarm)
 * @author philipp lensing
 */
public class Flock {
    private Material boidMaterial;
    private Spatial boidMesh;
    private Node scene;
    private InstancedNode instancedNode;
    private List<Boid> boids;
    
    public static int numberOfBoids = 0;
    
    private Functionality func;
    public kdTree tree;
    
    //MANIPULATION FACTORS
    public float cohesionFactor = 1;
    public float separationFactor = 1;
    public float alignmentFactor = 1;
    //COHESION
    public Vector3f dynamicCentroid;
    public Vector3f staticCentroid;
    //SEPARATION
    
    //ALIGNMENT
    public float maxDistance = 2f;
    public float theta = 90;
    public Vector3f direction;
    
    public static long framecount = 0;
    private int updateFrame = 3;

    /**
     * 
     * @param scene a reference to the root node of the scene graph (e. g. rootNode from SimpleApplication).
     * @param boidCount number of boids to create.
     * @param boidMesh reference mesh (geometric model) which should be used for a single boid.
     * @param boidMaterial the material controls the visual appearance (e. g. color or reflective behavior) of the surface of the boid model.
     * @param cohesion cohesion Factor
     * @param separation separation Factor
     */
    public Flock( Node scene, int boidCount, Spatial boidMesh, Material boidMaterial, AssetManager ass, float cohesion, float separation, float alignment, Vector3f direction) {
        this.boidMesh = boidMesh;
        this.boidMaterial = boidMaterial;
        this.scene = scene;
        this.boidMaterial.setBoolean("UseInstancing", true);
        this.instancedNode = new InstancedNode("instanced_node");
        this.scene.attachChild(instancedNode);
        
        boids = createBoids(boidCount, ass);
        this.staticCentroid = findStaticCentroid();
        
        instancedNode.instance();
        this.cohesionFactor = cohesion;
        this.separationFactor = separation;
        this.alignmentFactor = alignment;
        this.direction = direction;
        this.func = new Functionality();
        this.tree = new kdTree();
    }
    
     /**
     * Constructor with edited factors, but no direction
     * @param scene
     * @param boidCount
     * @param boidMesh
     * @param boidMaterial 
     * @param ass 
     */
    public Flock( Node scene, int boidCount, Spatial boidMesh, Material boidMaterial, AssetManager ass, float cohesion, float separation, float alignment) {
        this(scene, boidCount, boidMesh, boidMaterial, ass,cohesion, separation, alignment, new Vector3f(0,0,0));
    }
    
    /**
     * Constructor with standard vector factors
     * @param scene
     * @param boidCount
     * @param boidMesh
     * @param boidMaterial 
     * @param ass 
     */
    public Flock( Node scene, int boidCount, Spatial boidMesh, Material boidMaterial, AssetManager ass) {
        this(scene, boidCount, boidMesh, boidMaterial, ass, 1, 1, 1, new Vector3f(0,0,0));
    }
    
        /**
     * Constructor for testing
     * @param scene
     * @param boidCount
     * @param boidMesh
     * @param boidMaterial 
     * @param ass 
     * @param testType 
     */
    public Flock( Node scene, int boidCount, Spatial boidMesh, Material boidMaterial, AssetManager ass, String testType ) {
        this.boidMesh = boidMesh;
        this.boidMaterial = boidMaterial;
        this.scene = scene;
        this.boidMaterial.setBoolean("UseInstancing", true);
        this.instancedNode = new InstancedNode("instanced_node");
        this.scene.attachChild(instancedNode);
        this.direction = new Vector3f(0,0,0);
        this.func = new Functionality();
        this.tree = new kdTree();
        
        if (testType.equalsIgnoreCase("cohesion")) {
            boids = createBoids(boidCount, ass);
            this.staticCentroid = findStaticCentroid();
            this.cohesionFactor = 1;
            this.separationFactor = 0;
            this.alignmentFactor = 0;
        } else if (testType.equalsIgnoreCase("separation")) {
            boids = createBoidsSeparation(ass);
            this.staticCentroid = findStaticCentroid();
            this.cohesionFactor = 0;
            this.separationFactor = 0.5f;
            this.alignmentFactor = 0;
        }  else if (testType.equalsIgnoreCase("alignment")) {
            boids = createBoidsAlignment(ass);
            this.staticCentroid = findStaticCentroid();
            this.cohesionFactor = 0;
            this.separationFactor = 0;
            this.alignmentFactor = 1;
        } else if (testType.equalsIgnoreCase("kdTree")) {
            boids = createBoidskdTree(ass);
            this.staticCentroid = findStaticCentroid();
            this.cohesionFactor = 0;
            this.separationFactor = 0;
            this.alignmentFactor = 0;
            for (Boid boid : boids) {
               if (tree.root == null) {
                   tree.root = boid;
               } else {
                   tree.insert(tree.root, boid, 0);
               }
            }
            for (Boid boid : boids) {
                tree.nearestNeighbors(tree.root, boid, 0, maxDistance, theta);
                System.out.println(boid.boidNumber + " Links: " + boid.left + " Rechts: " + boid.right);
                System.out.println(boid + " Nachbar: " + boid.closestBoid + "Distanz: " + boid.bestDistance);
            }
            
        }
        instancedNode.instance();
    }

    /**
     * The update method should be called once per frame
     * @param dtime determines the elapsed time in seconds (floating-point) between two consecutive frames
     */
    
    /**
     * COHESION
     * Idee: Addiere alle Positionen und Teile durch Anzahl der Boids; Berechne dann Vektor zum Centroid vom spezifischen Boid
     * Sollte relativ performant sein
     * 
     * SEPARATION
     * Gehe für jeden Boid alle anderen Boids durch und finde den Boid mit geringstem Abstand; Wende dann Formel darauf an
     * Durch KD-Baum stark optimiert
     * 
     * 
     * ALIGNMENT
     * Finde alle Boids im Sichtfeld und fuege diesen der Liste des Boids hinzu, wende danach Formel darauf an
     * Stark optimiert durch KD-Baum
     * 
     */
    public void update(float dtime)
    {
        framecount++;
        if (framecount % updateFrame == 0) {
            for (Boid boid : boids) {
            if (tree.root == null) {
                tree.root = boid;
               } else {
                tree.insert(tree.root, boid, 0);
            }
        }
        Vector3f cohesionSum = new Vector3f(0,0,0);
        for (Boid boid : boids) {
            tree.nearestNeighbors(tree.root, boid, 0, maxDistance, theta);
            
            if (!staticCentroidEnabled) {
                cohesionSum.addLocal(boid.position);
            }
            //SEPARATION PART 2
                Vector3f richtungNachbar = boid.position.subtract(boid.closestBoid.position);
                float lengthSquared = boid.bestDistance * boid.bestDistance;
                boid.separationVector = new Vector3f(richtungNachbar.x / lengthSquared, richtungNachbar.y / lengthSquared, richtungNachbar.z / lengthSquared);
            //ALIGNMENT PART 2 (Durchschnitt der Ausrichtungen)
            if (!boid.neighbors.isEmpty()) {
                float wWertSum = 0;
                Vector3f wVSum = new Vector3f(0,0,0);
                for (Boid alignBoid : boid.neighbors) {
                    alignBoid.wWert = func.berechneWWert(boid.position, alignBoid.position);
                    wWertSum = wWertSum + alignBoid.wWert;
                    wVSum.addLocal(alignBoid.velocity.mult(alignBoid.wWert));
                }
                boid.alignmentVector = wVSum.divide(wWertSum).normalize();
            }
        }
        if (!staticCentroidEnabled) {
            dynamicCentroid = cohesionSum.divide(boids.size());
        }
        }
        
        for( Boid boid : boids )
        {
            Vector3f cohesionDirection;
            Vector3f cohesionVector;
            if (staticCentroidEnabled) {
                cohesionDirection = staticCentroid.subtract(boid.position);
                cohesionVector = cohesionDirection.subtract(boid.velocity).mult(cohesionFactor);
            } else {
                cohesionDirection = dynamicCentroid.subtract(boid.position);
                cohesionVector = cohesionDirection.subtract(boid.velocity).mult(cohesionFactor);  
            }

            Vector3f separationVector = boid.separationVector.mult(separationFactor);
            Vector3f alignmentVector = boid.alignmentVector.mult(alignmentFactor);
            Vector3f directionVector = direction;
            
            Vector3f netAccelarationForBoid = cohesionVector.add(separationVector.add(alignmentVector).add(directionVector)); // accelaration=boid.position.negate()) means that there is a permanent acceleration towards the origin of the coordinate system (0,0,0) which decreases if the distance of the boid to origin decreases.
            boid.update(netAccelarationForBoid, dtime); 

        }
        
        if (framecount % updateFrame == 0) {
            for (Boid boid : boids) {
                boid.left = null;
                boid.right = null;
                boid.closestBoid = null;
                boid.bestDistance = Float.MAX_VALUE;
                boid.neighbors.clear();
            }
        }
    }
    
    /**
     * Creates a list of Boid objects and adds corresponding instanced models (based on boidMesh) to the scene graph
     * @param boidCount The number of boids to create
     * @return A list of Boid objects. For each object a corresponding instanced geometry is added to the scene graph (Boid.geometry)
     */ 
    private List<Boid> createBoids(int boidCount, AssetManager assetManager)
    {
        List<Boid> boidList = new ArrayList<Boid>();
        
        for(int i=0; i<boidCount; ++i)
        {
            Boid newBoid = new Boid(createInstance(assetManager));
            newBoid.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(newBoid);
        }
        
        return boidList;
    }
    
    /**
     * Only creates a few specific boids on their way to a collision for showing working separation.
     * @return A list of Boid objects. For each object a corresponding instanced geometry is added to the scene graph (Boid.geometry)
     */ 
    private List<Boid> createBoidsSeparation(AssetManager assetManager)
    {
        List<Boid> boidList = new ArrayList<Boid>();
        
            Boid boid1 = new Boid(createInstance(assetManager));
            boid1.position = new Vector3f(-3,0,5);
            boid1.velocity = new Vector3f(1,0,0);
            boid1.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid1);
            Boid boid2 = new Boid(createInstance(assetManager));
            boid2.position = new Vector3f(3,0,5);
            boid2.velocity = new Vector3f(-1,0,0);
            boid2.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid2);
            Boid boid3 = new Boid(createInstance(assetManager));
            boid3.position = new Vector3f(0,3,5);
            boid3.velocity = new Vector3f(0,-1,0);
            boid3.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid3);
       
        
        return boidList;
    }
    
    /**
     * Only creates a few specific boids on their way to a collision for showing working alignment.
     * @return A list of Boid objects. For each object a corresponding instanced geometry is added to the scene graph (Boid.geometry)
     */ 
    private List<Boid> createBoidsAlignment(AssetManager assetManager)
    {
        List<Boid> boidList = new ArrayList<Boid>();
        
            Boid boid1 = new Boid(createInstance(assetManager));
            boid1.position = new Vector3f(-3,3,5);
            boid1.velocity = new Vector3f(1,-1,0);
            boid1.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid1);
            Boid boid2 = new Boid(createInstance(assetManager));
            boid2.position = new Vector3f(-3,-3,5);
            boid2.velocity = new Vector3f(1,1,0);
            boid2.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid2);

       
        
        return boidList;
    }
    
    /**
     * Only creates a few static Boids for testing kdTree functionality
     * @return A list of Boid objects. For each object a corresponding instanced geometry is added to the scene graph (Boid.geometry)
     */ 
    private List<Boid> createBoidskdTree(AssetManager assetManager)
    {
        List<Boid> boidList = new ArrayList<Boid>();
        
            Boid boid1 = new Boid(createInstance(assetManager));
            boid1.position = new Vector3f(0,0,0);
            boid1.velocity = new Vector3f(1,0,0);
            boid1.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid1);
            Boid boid2 = new Boid(createInstance(assetManager));
            boid2.position = new Vector3f(5,0,0);
            boid2.velocity = new Vector3f(-1,0,0);
            boid2.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid2);
            Boid boid3 = new Boid(createInstance(assetManager));
            boid3.position = new Vector3f(-5,0,0);
            boid3.velocity = new Vector3f(0,1,0);
            boid3.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid3);
            Boid boid4 = new Boid(createInstance(assetManager));
            boid4.position = new Vector3f(10,0,-3);
            boid4.velocity = new Vector3f(0,0,3);
            boid4.boidNumber = numberOfBoids;
            numberOfBoids++;
            boidList.add(boid4);
        
        return boidList;
    }
    
    private Vector3f findStaticCentroid(){
    Vector3f centroid;
    Vector3f sumOfPositions = new Vector3f(0,0,0);
    for (Boid boid : boids) {
        sumOfPositions.addLocal(boid.position);
    }
    centroid = sumOfPositions.divide(boids.size());
    return centroid;
};
    
    /**
     * Creates an instanced copy of boidMesh using boidMaterial with individual geometric transform
     * @return The instanced geometry attached to the scene graph
     */
    private Spatial createInstance(AssetManager assetManager)
    {
        Spatial spatial = assetManager.loadModel("Models/fishRotate/fishobj.j3o");
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseInstancing", true);
        Texture diffuseMap = assetManager.loadTexture("Textures/fishobj.png");
        material.setTexture("DiffuseMap", diffuseMap);
        spatial.setMaterial(material);
        spatial.scale(0.2f);
        spatial.rotate(0.0f, 180.0f, 0.0f);
        instancedNode.attachChild(spatial);
        return spatial;
    }    

    
}
