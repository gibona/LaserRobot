import org.testng.annotations.Test
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Sphere(val center: PointVector, val radius: Double) {
    private val radiusSq = radius * radius

    fun contains(point: PointVector) : Boolean {
        var distanceFromCenter = center - point

        log("" + distanceFromCenter.absSq() + " " + radiusSq)
        return distanceFromCenter.absSq() <= radiusSq
    }

    fun countInside(points: ArrayList<PointVector>): Int {
        var result = 0
        for(p in points)
            if(contains(p))
                result++
        return result
    }

    fun generateFibonacciSphere(cylynderHeight: Double, samples:Int = 1000) : ArrayList<Cylinder> {
        var result = ArrayList<Cylinder>(samples)

        var phi = Math.PI * (sqrt(5.0) - 1.0)  // golden angle in radians

        for(i in 0 until samples) {
            var y = 1 - (i / (samples - 1).toDouble()) * 2.0  // y goes from 1 to -1
            var r = sqrt(1 - y * y)  // radius at y

            var theta = phi * i  // golden angle increment

            var x = cos(theta) * r
            var z = sin(theta) * r

            var direction = PointVector(x, y, z)
            var directionReal = (direction / direction.abs()) * (cylynderHeight / 2.0)
            var start = center - directionReal
            var end = center + directionReal

            result.add(Cylinder(start, end, radius))
        }

        return result
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

        /**
         * Using Ritter algorithm
         * https://en.wikipedia.org/wiki/Bounding_sphere#Ritter's_bounding_sphere
         */
        fun fromPoints(wPointVectors: ArrayList<PointVector>, maximumRadius: Double): Sphere {
            if (wPointVectors.size == 0 )
                return Sphere(PointVector(0.0, 0.0, 0.0), 0.0)

            if (wPointVectors.size == 1)
                return Sphere(wPointVectors[0], 0.0)

            val maximumRadiusSq = maximumRadius * maximumRadius
            var p0 = wPointVectors[0];
            var p1: PointVector? = null
            var maximumDistance = 0.0
            // find point p1 farthest from p0, but not farther than maximumRadius
            for(p in wPointVectors) { // can be multithreaded!
                val distance = (p0-p).absSq()
                if (distance > maximumDistance && distance <= maximumRadiusSq) {
                    maximumDistance = distance
                    p1 = p
                }
            }
            if (p1 == null)
                return Sphere(wPointVectors[0], 0.0)

            var p2: PointVector? = null
            // find point p2 farthest from p1, but not farther than maximumRadius
            maximumDistance = 0.0
            for(p in wPointVectors) { // can be multithreaded!
                val distance = (p1-p).absSq()
                if (distance > maximumDistance && distance <= maximumRadiusSq) {
                    maximumDistance = distance
                    p2 = p
                }
            }

            if (p2 == null)
                return Sphere((p0+p1)/2.0, (p0-p1).abs()/2.0)

            var x = (p1.x + p2.x) / 2.0
            var y = (p1.y + p2.y) / 2.0
            var z = (p1.x + p2.x) / 2.0
            var rsq = maximumDistance / 4.0
            var r = sqrt(rsq)

            for(p in wPointVectors) { // can be multithreaded!
                val dx = p.x - x
                val dy = p.y - y
                val dz = p.z - z
                val dsq = dx * dx + dy * dy + dz * dz
                if (dsq > rsq && dsq <= maximumRadiusSq) {
                    var d = sqrt(dsq)
                    r = (r + d) / 2.0
                    val factor = r / d
                    x = p.x - dx * factor
                    y = p.y - dy * factor
                    z = p.z - dz * factor
                    rsq = r * r
                }
            }

            return Sphere(PointVector(x,y,z), sqrt(rsq))
        }
    }
}