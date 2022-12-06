package models;

public class Locomotive {
    private final int locNumber;
    private final int maxWagons;

    public Locomotive(int locNumber, int maxWagons) {
        this.locNumber = locNumber;
        this.maxWagons = maxWagons;
    }

    public int getMaxWagons() {
        return maxWagons;
    }

    @Override
    public String toString() {
        return "[Loc-"+ locNumber + ']';
    }
}
