import org.testng.annotations.Test

data class Sphere(val center: PointVector, var radius: Double) {
    private val radiusSq = radius * radius

    fun contains(point: PointVector) : Boolean {
        var distanceFromCenter = center - point

        log("" + distanceFromCenter.absSq() + " " + radiusSq)
        return distanceFromCenter.absSq() <= radiusSq
    }

    companion object {
        @Test
        fun testContains() {
            var sphere = Sphere(PointVector(0.0, 0.0, 0.0), 2.0)

            assert(sphere.contains(PointVector(0.0, 0.0, 0.0)))
            assert(sphere.contains(PointVector(1.0, 1.0, 1.0)))
            assert(!sphere.contains(PointVector(2.0, 2.0, 2.0)))
            assert(sphere.contains(PointVector(0.11, 1.24, 1.55)))
            assert(sphere.contains(PointVector(-1.48, 0.15, 1.32)))
            assert(!sphere.contains(PointVector(-3.96, 3.16, 1.0)))

            var sphere1 = Sphere(PointVector(0.0, 1.0, 0.0), 2.0)

            assert(sphere1.contains(PointVector(0.0, 1.0, 0.0)))
            assert(sphere1.contains(PointVector(1.0, 2.0, 1.0)))
            assert(!sphere1.contains(PointVector(2.0, 3.0, 2.0)))
            assert(!sphere1.contains(PointVector(0.13, 2.25, 1.56)))
            assert(!sphere1.contains(PointVector(-1.49, 1.16, 1.33)))
            assert(!sphere1.contains(PointVector(-3.96, 4.16, 1.0)))
        }
    }
}