/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for extra functions for better readability and testing.
 * @author Jannik, Fabian, Kim, Jan
 */
public class Functionality {
    public float berechneWWert(Vector3f anfang, Vector3f ziel) {
        float wWert = ziel.subtract(anfang).length();
        wWert *= wWert;
        wWert = 1 / wWert;
        return wWert;
    }
}
