/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * Boid represents one individual boid in the flock.
 * It's motion is integrated within the update method, which should be called once per frame.
 * @author philipp lensing
 */
public class Boid {
    public static float spawnVolumeSize = 10.0f;
    public Vector3f position;
    public Vector3f velocity;
    private Spatial geometry;
    
    public int boidNumber;
    
    //SEPARATION
    public Vector3f separationVector = new Vector3f(0,0,0);
    
    //ALIGNMENT
    public Vector3f alignmentVector = new Vector3f(0,0,0);
    public float wWert;

    
    //KDTREE
    public Boid left = null;
    public Boid right = null;
    public Boid closestBoid = null;
    public float bestDistance = Float.MAX_VALUE;
    public List<Boid> neighbors = new ArrayList();
    
    
    
    /**
     * The constructor instantiates a Boid a random position p within -spawnVolumeSize/2 <= p <= spawnVolumeSize/2.
     * The initial velocity is set to random 3D-vector with a magnitude of one.
     * @param geom corresponds to a geometry object within the scene graph and has to exist.
     */
    public Boid(Spatial geom) {
        this.geometry = geom;
        velocity = new Vector3f();
        position = new Vector3f();
        position.x = (FastMath.nextRandomFloat() -0.5f) * spawnVolumeSize;
        position.y = (FastMath.nextRandomFloat() -0.5f) * spawnVolumeSize;
        position.z = (FastMath.nextRandomFloat() -0.5f) * spawnVolumeSize;
        velocity.x = (FastMath.nextRandomFloat() -0.5f);
        velocity.y = (FastMath.nextRandomFloat() -0.5f);
        velocity.z = (FastMath.nextRandomFloat() -0.5f);
        velocity.normalizeLocal();
    }
    
    /**
     * update calculates the new position of the Boid based on its current position and velocity influenced by accelaration. update should be called once per frame
     * @param accelaration The net accelaration of all forces that influence the boid
     * @param dtime The elapsed time in seconds between two consecutive frames
     */
    public void update(Vector3f accelaration, float dtime)
    {
        //integrate velocity: v = v + a*dt; keep in mind: [m/s^2 * s = m/s]
        //integrate position: p = p + v*dt; keep in mind: [m/s * s = m]
        velocity = velocity.add(accelaration.mult(dtime));
        position = position.add(velocity.mult(dtime));        
        //update scene instance
        geometry.setLocalTranslation(position); 
        geometry.lookAt(position.add(velocity), Vector3f.UNIT_Y);
    }
}
