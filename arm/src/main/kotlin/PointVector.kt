import java.io.BufferedReader
import java.util.*
import kotlin.math.sqrt

data class PointVector(val x: Double, val y: Double, val z: Double) {

    override fun toString(): String {
        return "($x, $y, $z)"
    }

    operator fun plus(other: PointVector) : PointVector {
        return PointVector(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    operator fun unaryMinus() : PointVector {
        return this * -1.0
    }

    operator fun minus(other: PointVector) : PointVector {
        return this + (-other)
    }

    operator fun times(scalar: Double) : PointVector {
        return PointVector(this.x * scalar, this.y * scalar, this.z * scalar)
    }

    fun abs(): Double {
        return sqrt(absSq())
    }

    /**
     * We don't need to call sqrt for comparison
     */
    fun absSq(): Double {
        return x*x + y*y + z*z
    }

    operator fun div(div: Double): PointVector {
        return this * (1 / div)
    }

    companion object {
        fun readFromScanner(scanner: Scanner): PointVector {
            return PointVector(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble())
        }

        fun readFromScannerMultiple(scanner: Scanner, size: Int): ArrayList<PointVector> {
            log("Reading $size points")
            var pointVectors = ArrayList<PointVector>(size)
            for(i in 0 until size) {
                pointVectors.add(readFromScanner(scanner))
                log("point:${pointVectors.get(i)}")
            }
            return pointVectors
        }

        fun read(reader: BufferedReader): PointVector {
            var line = readNextLineAndSplit(reader)
            return PointVector(line[0].toDouble(), line[1].toDouble(), line[2].toDouble())
        }

        fun readMultiple(reader: BufferedReader, size: Int): ArrayList<PointVector> {
            log("Reading $size points")
            var pointVectors = ArrayList<PointVector>(size)
            for(i in 0 until size) {
                pointVectors.add(read(reader))
                log("point:${pointVectors.get(i)}")
            }
            return pointVectors
        }

        fun crossProduct(a: PointVector, b: PointVector): PointVector {
            return PointVector(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x,
            )
        }

        fun dotProduct(a: PointVector, b: PointVector): Double {
            return a.x * b.x + a.y * b.y + a.z * b.z
        }
    }
}