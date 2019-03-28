/*
 * file: FinalPRoject.java
 * author: Sebastian, Diane, Nghi, Dakota
 * group: Synchronized Bronco
 * class: CS4450 - Computer Graphic
 *
 * assignment: Semester Project
 * date last modified: 3/12/2019
 *
 * purpose: This class is to create a chunk of blocks with textures
 * and can be able to renders it
 */

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
//Chunk class stores block vertices in order to render them more efficiently
public class Chunk {

    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    static final float persistanceMin = 0.07f;
    static final float persistanceMax = 0.12f;

    private Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private int StartX, StartY, StartZ;
    private Random r;
    private Texture texture;

    //Method: render
    //Purpose: renders the chunks
    public void render() {
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2, GL_FLOAT, 0, 0L);

        glDrawArrays(GL_QUADS, 0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 24);
        glPopMatrix();
    }

    //Method: rebuildMest
    //Purpose: Primary method for generating terrain with simplex noise and drawing the chunk
    public void rebuildMesh(float startX, float startY, float startZ) {

        Random random = new Random();
        int sandXmin = r.nextInt(15);
        int sandXmax = r.nextInt(15)+15;
        int sandZmin = r.nextInt(15);
        int sandZmax = r.nextInt(15)+15;
        
        int waterXmin = r.nextInt(15);
        int waterXmax = r.nextInt(15)+15;
        int waterZmin = r.nextInt(15);
        int waterZmax = r.nextInt(15)+15;
        float persistance = 0;
        while (persistance < persistanceMin) {
            persistance = (persistanceMax) * random.nextFloat();
        }
        int seed = (int) (50 * random.nextFloat());

        SimplexNoise noise = new SimplexNoise(CHUNK_SIZE, persistance, seed);

        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer(
                (CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer(
                (CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer(
                (CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        
        for (float x = 0; x < CHUNK_SIZE; x++) {
            for (float z = 0; z < CHUNK_SIZE; z++) {
                for (float y = 0; y < CHUNK_SIZE; y++) {
                    //generate height from simplex noise
                    int height = (int) (startY + Math.abs((int) (CHUNK_SIZE * noise.getNoise((int) x, (int) z)))*CUBE_LENGTH);
                    
                    if (y >= height) {
                        break;
                    }
                    //Generate Grass at the top layer
                    if(y == height -1){
                        Blocks[(int)x][(int)y][(int)z] = new Block(Block.BlockType.BlockType_Grass);
                    }
                    //Generate random water area
                    if( x>=waterXmin && x<=waterXmax && z >= waterZmin && z <= waterZmax && y == 3){
                        Blocks[(int)x][(int)y][(int)z] = new Block(Block.BlockType.BlockType_Water);
                    }
                    //Generate random sand area
                    else if( x>=sandXmin && x<=sandXmax && z >= sandZmin && z <= sandZmax && y == 3){
                        Blocks[(int)x][(int)y][(int)z] = new Block(Block.BlockType.BlockType_Sand);
                    }
                    VertexPositionData.put(createCube(
                            -(float) (startX + x * CUBE_LENGTH),
                            (float) (y * CUBE_LENGTH + (int) (CHUNK_SIZE * .8)-42),
                            -(float) (startZ + z * CUBE_LENGTH)));

                    VertexColorData.put(createCubeVertexCol(getCubeColor(
                            Blocks[(int) x][(int) y][(int) z])));

                    VertexTextureData.put(createTexCube((float) 0,
                            (float) 0, Blocks[(int) (x)][(int) (y)][(int) (z)]));
                }
            }
        }
        VertexColorData.flip();
        VertexPositionData.flip();
        VertexTextureData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    //Method: createCubeVertexCol
    //Purpose: create and return a float array that stores the color
    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i
                    % CubeColorArray.length];
        }
        return cubeColors;
    }

    //Method: createCube
    //Purpuse: define the vertices of the cube and store and return in a float array
    public static float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[]{
            // TOP QUAD
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            // FRONT QUAD
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            // BACK QUAD
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z};
    }

    //Method: getCubeColor
    //Purpose: Default cube color
    private float[] getCubeColor(Block block) {
        return new float[]{1, 1, 1};
    }

    //Constructor
    //Chunk constructor which assigns block types to all blocks and then calls rebuildMesh() to draw terrain
    public Chunk(int startX, int startY, int startZ) {
        try {
            texture = TextureLoader.getTexture("PNG",
                    ResourceLoader.getResourceAsStream("terrain.png"));
        } catch (Exception e) {
            System.out.print("No Texture File");
        }
        r = new Random();
        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    float rand = r.nextFloat();
                    boolean dog = true; //this is for testing
                    if (y == 0) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Bedrock);
                    } else if (rand > 0.5f && (y == 1 || y == 2)) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
                    } else if (rand <= 0.5f && (y == 1 || y == 2)) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Dirt);
                    } else if (rand > 0.4f) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Grass);
                    } else {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Sand);
                    } 
                }
            }
        }

        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        StartX = startX;
        StartY = startY;
        StartZ = startZ;
        rebuildMesh(startX, startY, startZ);
    }

    //Method: createTexCube
    //Purpose: Assigns textures from terrain.png to the sides of each cube
    public static float[] createTexCube(float x, float y, Block block) {

        float offset = (1024f / 16) / 1024f;
        switch (block.GetID()) {
            case 0: //Grass block type
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 3, y + offset * 10,
                    x + offset * 2, y + offset * 10,
                    x + offset * 2, y + offset * 9,
                    x + offset * 3, y + offset * 9,
                    // TOP!
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // FRONT QUAD
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // BACK QUAD
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    // LEFT QUAD
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    // RIGHT QUAD
                    x + offset * 3, y + offset * 0,
                    x + offset * 4, y + offset * 0,
                    x + offset * 4, y + offset * 1,
                    x + offset * 3, y + offset * 1};
            case 1: //sand block type
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    // TOP!
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    // FRONT QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    // BACK QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    // LEFT QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2,
                    // RIGHT QUAD
                    x + offset * 2, y + offset * 1,
                    x + offset * 3, y + offset * 1,
                    x + offset * 3, y + offset * 2,
                    x + offset * 2, y + offset * 2};
            case 2: //water block type
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 15, y + offset * 12,
                    x + offset * 16, y + offset * 12,
                    x + offset * 15, y + offset * 13,
                    x + offset * 16, y + offset * 13,
                    // TOP!
                    x + offset * 15, y + offset * 12,
                    x + offset * 16, y + offset * 12,
                    x + offset * 15, y + offset * 13,
                    x + offset * 16, y + offset * 13,
                    // FRONT QUAD
                    x + offset * 15, y + offset * 12,
                    x + offset * 16, y + offset * 12,
                    x + offset * 16, y + offset * 13,
                    x + offset * 15, y + offset * 13,
                    // BACK QUAD
                    x + offset * 15, y + offset * 12,
                    x + offset * 16, y + offset * 12,
                    x + offset * 16, y + offset * 13,
                    x + offset * 15, y + offset * 13,
                    // LEFT QUAD
                    x + offset * 15, y + offset * 12,
                    x + offset * 16, y + offset * 12,
                    x + offset * 16, y + offset * 13,
                    x + offset * 15, y + offset * 13,
                    // RIGHT QUAD
                    x + offset * 15, y + offset * 12,
                    x + offset * 16, y + offset * 12,
                    x + offset * 16, y + offset * 13,
                    x + offset * 15, y + offset * 13};
            case 3: //dirt block type
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 1, y + offset * 10,
                    x + offset * 2, y + offset * 10,
                    x + offset * 2, y + offset * 11,
                    x + offset * 1, y + offset * 11,
                    // TOP!
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // FRONT QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // BACK QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // LEFT QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0,
                    // RIGHT QUAD
                    x + offset * 3, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 0,
                    x + offset * 3, y + offset * 0};
            case 4: //stone block type
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    // TOP!
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    // FRONT QUAD
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    // BACK QUAD
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    // LEFT QUAD
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1,
                    // RIGHT QUAD
                    x + offset * 1, y + offset * 0,
                    x + offset * 2, y + offset * 0,
                    x + offset * 2, y + offset * 1,
                    x + offset * 1, y + offset * 1};
            default: //bedrock block type
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    // TOP!
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    // FRONT QUAD
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    // BACK QUAD
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    // LEFT QUAD
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2,
                    // RIGHT QUAD
                    x + offset * 1, y + offset * 1,
                    x + offset * 2, y + offset * 1,
                    x + offset * 2, y + offset * 2,
                    x + offset * 1, y + offset * 2};
        }
    }
    

}
