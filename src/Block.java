/*
 * file: FinalPRoject.java
 * author: Sebastian, Diane, Nghi, Dakota
 * group: Synchronized Bronco
 * class: CS4450 - Computer Graphic
 *
 * assignment: Semester Project
 * date last modified: 3/12/2019
 *
 * purpose: This class can create a six different type of block and 
 * they holds an boolean value to determine whether it is active
 */

public class Block {

    private boolean IsActive;
    private BlockType Type;
    private float x, y, z;

    //Define the block types
    public enum BlockType {
        BlockType_Grass(0),
        BlockType_Sand(1),
        BlockType_Water(2),
        BlockType_Dirt(3),
        BlockType_Stone(4),
        BlockType_Bedrock(5);
        private int BlockID;

        BlockType(int i) {
            BlockID = i;
        }

        public int GetID() {
            return BlockID;
        }

        public void SetID(int i) {
            BlockID = i;
        }
    }

    //Method: Block
    //Purpose: Constructor
    public Block(BlockType type) {
        Type = type;
    }

    //Method: setCoords
    //Purpose: Set coordinate position
    public void setCoords(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    //Method: IsActie
    //Purpose: Return true if active, false otherwise
    public boolean IsActive() {
        return IsActive;
    }

    //Method: SetActive
    //Purpose: Set block status to active
    public void SetActive(boolean active) {
        IsActive = active;
    }

    //Method: Get ID
    //Purpose: return the id of the block
    public int GetID() {
        return Type.GetID();
    }
}
