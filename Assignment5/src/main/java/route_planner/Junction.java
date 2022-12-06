package route_planner;

import graphs.Identifiable;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;

public class Junction implements Identifiable {
    private String name;            // unique name of the junction
    private double locationX;       // RD x-coordinate in km
    private double locationY;       // RD y-coordinate in km
    private int population;         // indicates importance of the junction, used for graphical purposes only

    public Junction() {}

    public Junction(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLocationX() {
        return locationX;
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    /**
     * draws the junction onto a svg image with a given colour
     * @param svgWriter the printsteam you want to use
     * @param colour the colour you want the junction to have
     */
    public void svgDraw(PrintStream svgWriter, String colour) {
        // calculate the size of the dot relative to population at the junction
        double radius = 0.1 + 0.3 * Math.log(1 + population / 2000);
        //radius = 0.1;
        int fontSize = 3;

        // accounts for the reversed y-direction of the svg coordinate system relative to RD-coordinates
        svgWriter.printf(Locale.ENGLISH,"<circle cx='%.3f' cy='%.3f' r='%.3f' fill='%s'/>\n",
                locationX, -locationY, radius, colour);
        svgWriter.printf(Locale.ENGLISH,"<text x='%.3f' y='%.3f' font-size='%d' fill='%s' text-anchor='middle'>%s</text>\n",
                locationX, -locationY - 1.3, fontSize, colour, name);

    }

    /**
     * Draws the road segment onto a .svg image with the specified colour
     * If no colour is provided, a default will be calculated on the basis of the maxSpeed
     * @param svgWriter the printsteam you want to use
     * @param from the junction the line comes from
     * @param width the width of the road line
     * @param colour the colour you want the junction to have
     */
    public void svgDrawRoad(PrintStream svgWriter, Junction from, double width, String colour) {
        if (from == null) return;
        // accounts for the reversed y-direction of the svg coordinate system relative from RD-coordinates
        svgWriter.printf(Locale.ENGLISH, "<line x1='%.3f' y1='%.3f' x2='%.3f' y2='%.3f' stroke-width='%.3f' stroke='%s'/>\n",
                this.getLocationX(), -this.getLocationY(),
                from.getLocationX(), -from.getLocationY(),
                width, colour);
}

    @Override
    public String toString() {
        return name;
    }

    /**
     *  calculates the carthesion distance between two junctions
     * @param target the junction you want to compare with
     * @return the carthesion distance between this and the target junction
     */
    double getDistance(Junction target) {
        // calculate the cartesion distance between this and the target junction
        // using the locationX and locationY as provided in the dutch RD-coordinate system
        double dX = target.locationX - locationX;
        double dY = target.locationY - locationY;
        return Math.sqrt(dX*dX + dY*dY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Junction junction = (Junction) o;
        return Objects.equals(name, junction.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
