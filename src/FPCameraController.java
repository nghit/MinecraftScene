/*
 * file: FinalPRoject.java
 * author: Sebastian, Diane, Nghi, Dakota
 * group: Synchronized Bronco
 * class: CS4450 - Computer Graphic
 *
 * assignment: Semester Project
 * date last modified: 3/12/2019
 *
 * purpose: this class draw out the 3d cube and control the camera according to
 * the input of the user.
 */

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.Sys;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;

//This is our First Person Camera Controller class - handles motion, user input
public class FPCameraController {
    private Vector3Float position;
    private Vector3Float lPosition;
    private Vector3Float me;
    
    private float yaw;
    private float pitch;
    
    private Chunk chunkObject;
    private boolean firstTime;
    private boolean firstRender;
    
    /**
     * Takes in x, y, z coordinates and creates new camera positions 
     * as well as settings the yaw and pitch of camera.
     * @param x
     * @param y
     * @param z 
     */
    public FPCameraController(float x, float y, float z) {
        position = new Vector3Float(25.91f, -35.7f, 28.53f);
        
        lPosition = new Vector3Float(x, y, z);
        lPosition.setX(0f);
        lPosition.setY(15f);
        lPosition.setZ(0f);
        
        yaw = 0.0f;
        pitch = 80.0f;
        firstRender = true;
        //chunkObject = new Chunk((int)x,(int)y,(int)z);
    }
    /**
     * Allows user to use mouse to move camera around horizontally
     * @param amount 
     */
    public void yaw(float amount) {
        yaw += amount;
    }
    
    /**
     * Allows user to use mouse to move camera vertically.
     * @param amount 
     */
    public void pitch(float amount) {
        pitch -= amount;
    }
    
    /**
     * Allows user to camera forward. Input distance is default movement speed for move
     * and strafe functions.
     * @param distance 
     */
    public void walkForward(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        
        position.setX(position.getX() - xOffset);
        position.setZ(position.getZ() + zOffset);
    }
    
    /**
     * Allows user to move camera backward. 
     * @param distance 
     */
    public void walkBackwards(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        
        position.setX(position.getX() + xOffset);
        position.setZ(position.getZ() - zOffset);
    }
    
    /**
     * Allows user to move camera to the leftward.
     * @param distance 
     */
    public void strafeLeft(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
        
        position.setX(position.getX() - xOffset);
        position.setZ(position.getZ() + zOffset);
    }
    
    /**
     * Allows user to move camera rightward.
     * @param distance 
     */
    public void strafeRight(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
        
        position.setX(position.getX() - xOffset);
        position.setZ(position.getZ() + zOffset);
    }
    
    /**
     * Allows user to move camera upwards.
     * @param distance 
     */
    public void moveUp(float distance) {
        position.setY(position.getY() - distance);
    }
    
    /**
     * Allows user to move camera downwards.
     * @param distance 
     */
    public void moveDown(float distance) {
        position.setY(position.getY() + distance);
    }
    
    /**
     * Translates and rotates matrix so that it looks through the camera.
     */
    public void lookThrough() {
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        glTranslatef(position.getX(), position.getY(), position.getZ());
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.getX()).put(
        lPosition.getY()).put(lPosition.getZ()).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    /**
     * Main loop of the program, calls all movement and camera movement functions.
     * Sets and controls values such as distance of movement, mouse sensitivity, and movement speed.
     * Locks mouse into window and removes cursor. Calls render() to render objects. Loop will close
     * and program will close when user clicks Close or the X on window, or if user presses ESC.
     */
    public void gameLoop() {
        FPCameraController camera = new FPCameraController(0, 0, 0);
        
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f;
        float lastTime = 0.0f; 
        long time = 0;
        float mouseSensitivity = 0.09f;
        float movementSpeed = .35f;
        
        Mouse.setGrabbed(true);
        
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            time = Sys.getTime();
            lastTime = time;
            
            dx = Mouse.getDX();
            dy = Mouse.getDY();
            
            camera.yaw(dx * mouseSensitivity);
            camera.pitch(dy * mouseSensitivity);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                camera.walkForward(movementSpeed);
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                camera.walkBackwards(movementSpeed);
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                camera.strafeLeft(movementSpeed);
            }
        
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                camera.strafeRight(movementSpeed);
            }
    
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                camera.moveUp(movementSpeed);
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                camera.moveDown(movementSpeed);
            }
            
            glLoadIdentity();
            camera.lookThrough();
            glEnable(GL_DEPTH_TEST); //had to use this. If not enabled cube shows other side's colors and looks weird.
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            if(firstRender) {
                chunkObject = new Chunk(0,4,0);
                firstRender = false;
            }  
            chunkObject.render();
            
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
    }
    
}