import org.testng.annotations.Test

class ListTreeComparator<T>(private val a: T, private val b: T) : ContainedIn, PointsCollection where T :ContainedIn, T : PointsCollection{
    override val size: Int
        get() {
            var res1 = a.size
            var res2 = b.size
            if(res1 != res2)
                println("Diff in size($res1 vs $res2): $a vs $b")
            return res1
        }

    override fun first(): PointVector {
        var res1 = a.first()
        var res2 = b.first()
        if(res1 != res2)
            println("Diff in first($res1 vs $res2): $a vs $b")
        return res1
    }

    override fun remove(item: PointVector) {
        a.remove(item)
        b.remove(item)

        var res1 = a.size
        var res2 = b.size
        if(res1 != res2)
            println("Diff in size after remove($res1 vs $res2): $a vs $b")
    }

    override fun removeAllInFigure(figure: Containable) {
        a.removeAllInFigure(figure)
        b.removeAllInFigure(figure)

        var res1 = a.size
        var res2 = b.size
        if(res1 != res2)
            println("Diff in size after removeAll($res1 vs $res2): $a vs $b")
    }

    override fun containedIn(figure: Containable): Boolean {
        var res1 = a.containedIn(figure)
        var res2 = b.containedIn(figure)
        if(res1 != res2)
            println("Diff in containedIn($figure)($res1 vs $res2): $a vs $b")
        return res1
    }

    override fun countIn(figure: Containable): Int {
        var res1 = a.countIn(figure)
        var res2 = b.countIn(figure)
        if(res1 != res2)
            println("Diff in countIn($figure)($res1 vs $res2): $a vs $b")
        return res1
    }

    companion object {
        @Test
        fun testImplementations() {
            var points = arrayListOf<PointVector>(

                /*PointVector(-47.0, -50.0, 44.0),
                PointVector(-42.0, -58.0, 37.0),
                PointVector(-40.0, -44.0, 30.0),
                PointVector(-45.0, -51.0, 49.0),
                PointVector(-57.0, -54.0, 31.0),*/

                PointVector(-47.36, -50.01, 44.41),
                PointVector(-42.37, -58.19, 37.22),
                PointVector(-40.76, -44.48, 30.97),
                PointVector(-45.48, -51.46, 49.7),
                PointVector(-57.5, -54.93, 31.41),



                PointVector(-54.32, -44.78, 30.84),
                PointVector(-43.86, -48.58, 39.91),
                PointVector(-41.51, -48.59, 45.33),
                PointVector(-55.39, -54.97, 34.95),


                PointVector(-47.99, -45.21, 39.93),
                PointVector(119.0, 123.09, 84.85),
                PointVector(115.73, 121.03, 80.74),
                PointVector(123.93, 127.33, 83.31),
                PointVector(126.06, 115.34, 72.37),
                PointVector(125.05, 116.72, 88.52),
                PointVector(117.77, 113.33, 73.01),
                PointVector(120.61, 110.74, 77.02),
                PointVector(121.42, 116.74, 72.67),
                PointVector(112.36, 124.68, 86.81),
                PointVector(111.27, 129.0, 71.9)
            )

                /* var cylinder = Cylinder(
                PointVector(-33.0, -33.0, 51.0),
                PointVector(0.0, 0.0, 18.0),
                30.0).toBoundingSphere()*/

            var sphere = Sphere(PointVector(-16.5,	-16.5, 34.5),42.0)

            var tree = OctaPointsTree(points)
            var list = ListWrapper(points)
            var treeContains = tree.containedIn(sphere)
            var listContains = list.containedIn(sphere)
            assert(treeContains == listContains) { "$treeContains vs $listContains" }
        }
    }
}