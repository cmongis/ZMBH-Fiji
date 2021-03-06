package zmbh.commands.roi.jarvis;

import static java.lang.Math.sqrt;

/**
 * Represents a 2D point as on a Cartesian plane.
 */
public class Point {

    public static final Point ORIGIN = new Point(0, 0);

    public final int x;
    public final int y;

    public Point(int x, int y) {
	this.x = x;
	this.y = y;
    }

    /**
     * Calculates the difference between this point and <code>point</code>.
     * 
     * @param point The point to subtract from this point.
     * @return A new point representing the difference between the points.
     */
    public Point minus(Point point) {
	int x = this.x - point.x;
	int y = this.y - point.y;

	return new Point(x, y);
    }

    /**
     * Calculate the distance from this point to <code>point</code>.
     * 
     * @param point The point to calculate distance to.
     * @return The distance between this point and <code>point</code>.
     */
    public double distanceTo(Point point) {
	double xDiff = (double) (point.x - x);
	double yDiff = (double) (point.y - y);
	double x2 = xDiff * xDiff;
	double y2 = yDiff * yDiff;

	return sqrt(x2 + y2);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
	return "Point [" + x + ", " + y + "]";
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + x;
	result = prime * result + y;
	return result;
    }

    /** {@InheritDoc} */
    @Override
    public boolean equals(Object obj) {
	// Reflexivity
	if (obj == this) return true;

	// Obj must not be null.
	if (obj == null) return false;

	// Ensure obj is a point.
	if (!(obj instanceof Point)) return false;

	// Ensure x and y values are the same.
	Point point = (Point) obj;
	if (point.x != x) return false;
	if (point.y != y) return false;
	
	return true;
    }
}