import org.testng.annotations.Test
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Sphere(val center: PointVector, val radius: Double) : Containable {
    private val radiusSq = radius * radius

    override fun contains(point: PointVector) : Boolean {
        var distanceFromCenter = center - point

        log("" + distanceFromCenter.absSq() + " " + radiusSq)
        return distanceFromCenter.absSq() <= radiusSq
    }

    private val maxXYZv: PointVector by lazy { center + PointVector(radius, radius,radius) }
    private val minXYZv: PointVector by lazy { center - PointVector(radius, radius,radius) }

    override fun getMaxXYZ(): PointVector {
        return maxXYZv;
    }

    override fun getMinXYZ(): PointVector {
        return minXYZv;
    }

    /**
     * Генерира samples * 3 цилиндъра, които са разпраделени в 3 групи:
     * 1. цилиндър с център съвпадащ със сферата
     * 2. цилиндър, в който сферата е вписана до горния му край
     * 3. цилиндър, в който сферата е вписана до долния му край
     *
     *
     * тук може да се използва и по-оптимален метод - прави се плъзгащ се прозорец по точките и се отчита
     * оптималното разположение, така че:
     * 1. да има максимално плевели в зоната на лазера
     * 2. лазера да не засяга полезни разстения
     * 3. плевящия инструмент да не засяга камъни и полезни разстения
     * точките се сортират по оста на двата цилиндъра и за всяко възможно пресичане
     * (горна/долна част на цилиндър - лазер или плевящ инструмент) се пресмята колко е оптимална
     */
    fun generateFibonacciSphere(cylynderHeight: Double, rFoc:Double,  samples:Int = SAMPLES_FIBONACCI) : ArrayList<Cylinder> {
        var result = ArrayList<Cylinder>(samples*3)

        var phi = Math.PI * (sqrt(5.0) - 1.0)  // golden angle in radians

        for(i in 0 until samples) {
            val y = 1 - (i / (samples - 1).toDouble()) * 2.0  // y goes from 1 to -1
            val r = sqrt(1 - y * y)  // radius at y

            val theta = phi * i  // golden angle increment

            val x = cos(theta) * r
            val z = sin(theta) * r

            var direction = PointVector(x, y, z)
            var directionReal = (direction / direction.abs())

            result.add(Cylinder(
                center - directionReal * (cylynderHeight - radius),
                center + directionReal * (radius),
                rFoc))

            result.add(Cylinder(
                center - directionReal * (cylynderHeight / 2.0),
                center + directionReal * (cylynderHeight / 2.0),
                rFoc))

            result.add(Cylinder(
                center - directionReal * (radius),
                center + directionReal * (cylynderHeight - radius),
                rFoc))
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

        @Test
        fun testFibonacciSphere() {
            val sphere = Sphere(PointVector(0.0,0.0,0.0), 2.0)
            var cylinders = sphere.generateFibonacciSphere(5.0, 2.0,2)

            assert(cylinders[0] == Cylinder(PointVector(0.0, -3.0, 0.0), PointVector(0.0, 2.0, 0.0), 2.0)) {cylinders[0]}
            assert(cylinders[1] == Cylinder(PointVector(0.0, -2.5, 0.0), PointVector(0.0, 2.5, 0.0), 2.0)) {cylinders[1]}
            assert(cylinders[2] == Cylinder(PointVector(0.0, -2.0, 0.0), PointVector(0.0, 3.0, 0.0), 2.0)) {cylinders[2]}

            assert(cylinders[3] == Cylinder(PointVector(0.0, 3.0, 0.0), PointVector(0.0, -2.0, 0.0), 2.0)) {cylinders[3]}
            assert(cylinders[4] == Cylinder(PointVector(0.0, 2.5, 0.0), PointVector(0.0, -2.5, 0.0), 2.0)) {cylinders[4]}
            assert(cylinders[5] == Cylinder(PointVector(0.0, 2.0, 0.0), PointVector(0.0, -3.0, 0.0), 2.0)) {cylinders[5]}
        }

        /**
         * Using Ritter algorithm
         * https://en.wikipedia.org/wiki/Bounding_sphere#Ritter's_bounding_sphere
         */
        fun fromPoints(wPointVectors: ArrayList<PointVector>, maximumRadius: Double): Sphere {
            if (wPointVectors.size == 0 )
                return Sphere(PointVector(0.0, 0.0, 0.0), EPS)

            if (wPointVectors.size == 1)
                return Sphere(wPointVectors[0], EPS)

            val maximumDiameterSq = maximumRadius * maximumRadius * 4.0
            var p0 = wPointVectors[0];
            var p1: PointVector? = null
            var maximumDistance = 0.0
            // find point p1 farthest from p0, but not farther than maximumRadius
            for(p in wPointVectors) { // can be multithreaded!
                val distance = (p0-p).absSq()
                if (distance > maximumDistance && distance <= maximumDiameterSq) {
                    maximumDistance = distance
                    p1 = p
                }
            }
            if (p1 == null)
                return Sphere(wPointVectors[0], EPS)

            var p2: PointVector? = null
            // find point p2 farthest from p1, but not farther than maximumRadius
            maximumDistance = 0.0
            for(p in wPointVectors) { // can be multithreaded!
                val distance = (p1-p).absSq()
                if (distance > maximumDistance && distance <= maximumDiameterSq) {
                    maximumDistance = distance
                    p2 = p
                }
            }

            if (p2 == null)
                return Sphere((p0+p1)/2.0, (p0-p1).abs()/2.0)

            var x = (p1.x + p2.x) / 2.0
            var y = (p1.y + p2.y) / 2.0
            var z = (p1.z + p2.z) / 2.0
            var rsq = maximumDistance / 4.0
            var r = sqrt(rsq)

            for(p in wPointVectors) { // can be multithreaded!
                val dx = p.x - x
                val dy = p.y - y
                val dz = p.z - z
                val dsq = dx * dx + dy * dy + dz * dz
                if (dsq > rsq && dsq <= (maximumDiameterSq/4.0)) {
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