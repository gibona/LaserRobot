import org.testng.annotations.Test
import kotlin.math.sqrt

data class Cylinder (val start:PointVector, val end: PointVector, val radius: Double) {

    private val radiusSq = radius * radius

    override fun toString(): String {
        return "F: $start, B:$end r:$radius"
    }
    // https://lukeplant.me.uk/blog/posts/check-if-a-point-is-in-a-cylinder-geometry-and-code/
    fun contains(point: PointVector) : Boolean {
        var cylinderDirection = end - start
        var cylinderDirectionLength = cylinderDirection.abs()
        if (cylinderDirectionLength == 0.0)
         // Empty cylinder. We also have to avoid a divide-by-zero below
                return false

        // Distance from point to centerline axis is less than the radius
        var pointDistance =
        PointVector.crossProduct(
            cylinderDirection,
            (point - start),
        ).absSq() / cylinderDirection.absSq() // performance optimization

        if (pointDistance > radiusSq)
            return false

        /*
        Second condition: point must lie below the top plane.
        Third condition: point must lie above the bottom plane
        We construct planes with normals pointing out of the cylinder at both
        ends, and exclude points that are outside ("above") either plane.
        */


        var startPlane = Plane(start, -cylinderDirection)
        if (startPlane.pointIsAbove(point))
            return false

        var endPlane = Plane(end, cylinderDirection)
        if (endPlane.pointIsAbove(point))
            return false

        return true
    }

    private fun length(): Double {
        return (start-end).abs()
    }

    /**
     * Описана сфера
     */
    fun toBoundingSphere(): Sphere {
        return Sphere((start + end) * 0.5, sqrt(((start-end) * 0.5).absSq() + radiusSq))
    }

    /**
     * Вписана сфера
     */
    fun toBoundedSphere(): Sphere {
        return Sphere((start + end) * 0.5, radius)
    }


    fun getWorkingZone(fMin: Double, fMax: Double, rFoc: Double): Cylinder {
        var laserDirection = start - end
        var normalizedDirection = laserDirection / laserDirection.abs()
        return Cylinder(
            start + (normalizedDirection) * fMax,
            start + (normalizedDirection) * fMin,
            rFoc)
    }

    private fun getManipulator(fMin: Double, length: Double, radius: Double): Any {
        var laserDirection = start - end
        var normalizedDirection = laserDirection / laserDirection.abs()

        return Cylinder(
            end - (normalizedDirection) * fMin,
            end - (normalizedDirection) * (fMin+length),
            radius)
    }

    companion object {
        @Test
        fun testContains() {
            var cylinder = Cylinder(PointVector(1.0, 0.0, 0.0), PointVector(6.196, 3.0, 0.0), 0.5)


            assert(cylinder.contains(PointVector(1.02, 0.0, 0.0)))
            assert(!cylinder.contains(PointVector(0.98, 0.0, 0.0))) { "outside bottom plane" }//!

            assert(cylinder.contains(PointVector(0.8, 0.4, 0.0)))
            assert(!cylinder.contains(PointVector(0.8, 0.5, 0.0))) { "too far from center" }//!
            assert(!cylinder.contains(PointVector(0.8, 0.3, 0.0))) { "outside bottom plane" }//!

            assert(cylinder.contains(PointVector(1.4, -0.3, 0.0)))
            assert(!cylinder.contains(PointVector(1.4, -0.4, 0.0))) { "too far from center" }//!

            assert(cylinder.contains(PointVector(6.2, 2.8, 0.0)))
            assert(!cylinder.contains(PointVector(6.2, 2.2, 0.0))) { "too far from center" }//!
            assert(!cylinder.contains(PointVector(6.2, 3.2, 0.0))) { "outside top plane" }//!

            // Away from Z plane
            assert(cylinder.contains(PointVector(1.02, 0.0, 0.2)))
            assert(!cylinder.contains(PointVector(1.02, 0.0, 1.0))) { "too far from center" }//!
            assert(!cylinder.contains(PointVector(0.8, 0.3,2.0))) { "too far from center, and outside bottom plane" }//!

            var zeroCylinder = Cylinder(PointVector(1.0, 0.0, 0.0), PointVector(1.0, 0.0, 0.0), 0.5)
            assert(!zeroCylinder.contains(PointVector(1.0, 0.0, 0.0)))
        }

        @Test
        fun workingZoneTest() {
            val manipulator = Cylinder(PointVector(0.0, 0.0, 0.0), PointVector(10.0, 0.0, 0.0), 0.5)
            val laser = manipulator.getWorkingZone(3.0, 5.0, 0.2);
            var fromLaser = laser.getManipulator(3.0, manipulator.length(), manipulator.radius)

            assert(manipulator == fromLaser)
        }

    }

}