import org.testng.annotations.Test

data class Targets(val cylinder: Cylinder, val potentialManipulator: Cylinder, val targetPoints: Int) : Comparable<Targets>{
    override fun compareTo(other: Targets): Int  = -targetPoints.compareTo(other.targetPoints)

    companion object {
        @Test
        fun testSort() {
            var lasersPoints = ArrayList<Targets>()
            val p = PointVector(0.0, 0.0, 0.0)
            val c = Cylinder(p, p, 0.5)
            lasersPoints.add(Targets(c, c, 5))
            lasersPoints.add(Targets(c, c, 10))
            lasersPoints.add(Targets(c, c, 7))

            lasersPoints.sort()

            assert(lasersPoints[0].targetPoints == 10) { " result $lasersPoints"}
        }
    }
}
