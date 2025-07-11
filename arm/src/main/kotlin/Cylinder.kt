import org.testng.annotations.Test
import kotlin.math.sqrt

data class Cylinder (val start:PointVector, val end: PointVector, val radius: Double): Containable {

    private val radiusSq: Double by lazy { radius * radius }
    private val height: Double by lazy { (start - end).abs() }
    val centerPoint: PointVector by lazy { (start + end) / 2.0 }

    override fun toString(): String {
        return "F: $start, B:$end r:$radius"
    }
    // https://lukeplant.me.uk/blog/posts/check-if-a-point-is-in-a-cylinder-geometry-and-code/
    override fun contains(point: PointVector) : Boolean {
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

    fun parallelTo(another: Cylinder) : Boolean {
        val direction = start - end
        val anotherDirection = another.start - end
        val crossProduct = PointVector.crossProduct(direction, anotherDirection)
        return crossProduct.absSq() == 0.0
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


    fun getLaser(fMin: Double, fMax: Double, rFoc: Double): Cylinder {
        var laserDirection = start - end
        var normalizedDirection = laserDirection / laserDirection.abs()
        return Cylinder(
            start + (normalizedDirection) * fMax,
            start + (normalizedDirection) * fMin,
            rFoc)
    }

    fun getManipulator(fMin: Double, length: Double, radius: Double): Cylinder {
        var laserDirection = start - end
        var normalizedDirection = laserDirection / laserDirection.abs()

        return Cylinder(
            end - (normalizedDirection) * fMin,
            end - (normalizedDirection) * (fMin+length),
            radius)
    }

    fun height(): Double {
        return height
    }

    fun inWorkingArea(rMin: Double, rMax: Double): Boolean {
        return centerPoint.absSq() in rMin*rMin .. rMax*rMax
    }

    fun moveToCenterPoint(newCenterPoint: PointVector): Cylinder {
        var diff = newCenterPoint - centerPoint
        return Cylinder(start + diff, end + diff, radius)

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
            val laser = manipulator.getLaser(3.0, 5.0, 0.2);
            var fromLaser = laser.getManipulator(3.0, manipulator.length(), manipulator.radius)

            assert(manipulator == fromLaser)
        }

        @Test
        fun testParallel() {
            val manipulator = Cylinder(PointVector(Math.random(), Math.random(), Math.random()), PointVector(Math.random(), Math.random(), Math.random()), 0.5)
            var fMin = Math.random()
            val laser = manipulator.getLaser(fMin, 5.0, 0.2);
            var fromLaser = laser.getManipulator(fMin, manipulator.length(), manipulator.radius)

            assert(manipulator.parallelTo(laser))
            assert(fromLaser.parallelTo(laser))
            assert(laser.parallelTo(fromLaser))
            assert(manipulator.parallelTo(fromLaser))
        }

        @Test
        fun testMove() {
            val cylinder = Cylinder(PointVector(0.0, 0.0, 0.0), PointVector(10.0, 0.0, 0.0), 0.5)
            val moved = cylinder.moveToCenterPoint(PointVector(1.0, 1.0, 1.0))

            assert(moved.start == PointVector(-4.0, 1.0, 1.0)) {moved}
            assert(moved.end == PointVector(6.0, 1.0, 1.0)) {moved}
        }

    }

}