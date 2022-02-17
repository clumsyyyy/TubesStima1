package za.co.entelect.challenge.enums;

public enum Direction {
    // inisialisasi enum arah
    FORWARD(0, 1, "FORWARD"),
    BACKWARD(0, -1, "BACKWARD"),
    LEFT(-1, 0, "LEFT"),
    RIGHT(1, 0, "RIGHT");

    public final int lane;
    public final int block;
    public final String label;

    // constructor
    Direction(int lane, int block, String label) {
        this.lane = lane;
        this.block = block;
        this.label = label;
    }

    // return label buat ditampilin di match logs nya
    public String getLabel() {
        return this.label;
    }
}
