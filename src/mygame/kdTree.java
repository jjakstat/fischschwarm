package mygame;

import com.jme3.math.Vector3f;
import static java.lang.Math.abs;
import java.util.List;

/**
 * Drei Dimensionaler KD-Baum zur schnellen Suche von nahen Boids
 * @author jjaks
 */
public class kdTree {
    public int dimension = 3;
    public Boid root = null;
    
    /**
     * Fuegt Boids in den KD-Baum ein
     * @param current
     * @param insertBoid
     * @param currentDepth
     * @return 
     */
    public Boid insert(Boid current, Boid insertBoid, int currentDepth) {
        int variableToCompare = currentDepth % dimension;
        if (current == null) {
            current = insertBoid;
            return current;
        }
        
        if (insertBoid.boidNumber == current.boidNumber) {
            return null;
        }
        
        if (getBoidDim(insertBoid, variableToCompare) <= getBoidDim(current, variableToCompare)) {
           current.left = insert(current.left, insertBoid, currentDepth + 1);
        } else if (getBoidDim(insertBoid, variableToCompare) > getBoidDim(current, variableToCompare)) {
            current.right = insert(current.right, insertBoid, currentDepth + 1);
        }
        
        return current;
    }
    
    /**
     * Sucht den naechsten Nachbar und fuegt alle sichtbaren Boids fuer Alignment, der Liste des Boids hinzu
     * @param currentBoid
     * @param searchBoid
     * @param currentDepth Variable, die verglichen wird
     * @param maxDistance Sichtweite der Boids
     * @param theta 
     */
    public void nearestNeighbors(Boid currentBoid, Boid searchBoid, int currentDepth, float maxDistance, float theta) {
        if (currentBoid == null) {
            return;
        }
        int variableToCompare = currentDepth % dimension;
        
        if (currentBoid.boidNumber != searchBoid.boidNumber) {
            float currentDistance = currentBoid.position.subtract(searchBoid.position).length();
            //ALIGNMENT
            if (currentDistance <= maxDistance) {
                Vector3f directionalVector = searchBoid.position.subtract(currentBoid.position);
                float directionalTimesVelocity = directionalVector.normalize().dot(searchBoid.velocity.normalize());
                if (directionalTimesVelocity >= Math.cos(theta/2)) {
                    searchBoid.neighbors.add(currentBoid);
                }
            }
            if (currentDistance < searchBoid.bestDistance) {
                searchBoid.bestDistance = currentDistance;
                searchBoid.closestBoid = currentBoid;
            }
        }
        
        Boid nextBranch = null;
        Boid otherBranch = null;
        if (getBoidDim(searchBoid, variableToCompare) < getBoidDim(currentBoid, variableToCompare)) {
            nextBranch = currentBoid.left;
            otherBranch = currentBoid.right;
        } else {
            nextBranch = currentBoid.right;
            otherBranch = currentBoid.left;
        }
        
        nearestNeighbors(nextBranch, searchBoid, currentDepth + 1, maxDistance, theta);
        
        float distanceAxis = abs(getBoidDim(currentBoid, variableToCompare) - getBoidDim(searchBoid, variableToCompare));
        if (distanceAxis < searchBoid.bestDistance) {
            nearestNeighbors(otherBranch, searchBoid, currentDepth + 1, maxDistance, theta);
        }
    }
    
    /**
     * Gibt aktuelle Variable zurueck, die verglichen wird
     * @param boid
     * @param dim
     * @return 
     */
    public float getBoidDim(Boid boid, int dim) {
        if (dim == 0) {
            return boid.position.x;
        } else if (dim == 1) {
            return boid.position.y;
        } else if (dim == 2) {
            return boid.position.z;
        }
        else {
            return 0;
        }
    }
}