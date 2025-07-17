import com.google.common.primitives.Doubles.max
import com.google.common.primitives.Doubles.min

class OctaPointsTree : ContainedIn, PointsCollection {

    class Node(val minXYZ: PointVector, val maxXYZ: PointVector) {

        private var nodePoints: ArrayList<PointVector>? = null
        private val children = arrayOfNulls<Node>(8)
        private val middlePoint: PointVector by lazy { (minXYZ + maxXYZ)/2.0 }

        fun put(p: PointVector) {
            if (nodePoints == null || (minXYZ - maxXYZ).absSq() < minGranularitySq) { // no need for children, put all in the bucket
                addPoint(p);
                return
            }

            if (nodePoints?.size == 1) {
                putRecursive(nodePoints!![0])
                nodePoints = ArrayList(0)
            }

            putRecursive(p)
        }

        private fun putRecursive(p: PointVector) {
            val dX = if (p.x <= middlePoint.x) 1 else 0
            val dY = if (p.y <= middlePoint.y) 2 else 0
            val dZ = if (p.z <= middlePoint.z) 4 else 0

            val index = dX + dY + dZ

            if (children[index] == null)
                children[index] = Node(PointVector(
                    if (p.x <= middlePoint.x) minXYZ.x else middlePoint.x,
                    if (p.y <= middlePoint.y) minXYZ.y else middlePoint.y,
                    if (p.z <= middlePoint.z) minXYZ.z else middlePoint.z
                ), PointVector(
                    if (p.x <= middlePoint.x) middlePoint.x else maxXYZ.x,
                    if (p.y <= middlePoint.y) middlePoint.y else maxXYZ.y,
                    if (p.z <= middlePoint.z) middlePoint.z else maxXYZ.z
                ))

            children[index]?.put(p)
        }

        override fun toString(): String {
            return toString("")
        }

        fun toString(indent: String): String {
            var result = "Node: $minXYZ - $maxXYZ\n"

            if (nodePoints != null)
                result += "${indent}Points: $nodePoints \n"
            result += "${indent}children:\n"
            for(c in children.withIndex()) {
                if (c.value != null)
                    result += "${indent}[${c.index}]: " + (c.value as Node).toString("  $indent")
            }
            return result
        }



        private fun addPoint(p: PointVector) {
            if (nodePoints == null)
                nodePoints = ArrayList()

            nodePoints?.add(p)
        }

        fun hasContainedIn(figure: Containable): Boolean {
            var figureMinXYZ = figure.getMinXYZ()
            var figureMaxXYZ = figure.getMaxXYZ()

            if (nodePoints != null && nodePoints!!.size > 0)
                return figure.contains(nodePoints!!)

            for (child in children) {
                if (child == null)
                    continue
                if (child.overlaps(figureMinXYZ, figureMaxXYZ))
                    if (child.hasContainedIn(figure))
                        return true
            }
            return false
        }

        private fun overlaps(figureMinXYZ: PointVector, figureMaxXYZ: PointVector) =
                maxXYZ.x >= figureMinXYZ.x && minXYZ.x <= figureMaxXYZ.x //X overlap
             && maxXYZ.y >= figureMinXYZ.y && minXYZ.y <= figureMaxXYZ.y //Y overlap
             && maxXYZ.z >= figureMinXYZ.z && minXYZ.z <= figureMaxXYZ.z //Z overlap
/*
        private fun overlaps(figureMinXYZ: PointVector, figureMaxXYZ: PointVector) =
            figureMaxXYZ.x >= minXYZ.x && figureMinXYZ.x <= maxXYZ.x //X overlap
         && figureMaxXYZ.y >= minXYZ.y && figureMinXYZ.y <= maxXYZ.y //Y overlap
         && figureMaxXYZ.z >= minXYZ.z && figureMinXYZ.z <= maxXYZ.z //Z overlap*/

        fun countContainedIn(figure: Containable): Int {
            var result = 0
            var figureMinXYZ = figure.getMinXYZ()
            var figureMaxXYZ = figure.getMaxXYZ()

            if (nodePoints != null)
                return figure.countInside(nodePoints!!)

            for (child in children) {
                if (child == null)
                    continue

                if (child.overlaps(figureMinXYZ, figureMaxXYZ))
                    result += child.countContainedIn(figure)
            }
            return result
        }

        fun first(): PointVector {
            if (nodePoints != null && nodePoints!!.size>0)
                return nodePoints!![0]

            for(child in children) {
                if (child != null)
                    return child.first()
            }

            throw NoSuchElementException("Node empty?!?!? $this")
        }

    }

    private val points: ArrayList<PointVector>

    override val size: Int
        get() = points.size

    override fun first(): PointVector {
        if (root == null)
            throw NoSuchElementException("Root is null.")
        return root!!.first()

    }

    override fun remove(item: PointVector) {
        if (root == null)
            throw NoSuchElementException("Root is null.")
    }

    override fun removeAllInFigure(figure: Containable) {
        TODO("Not yet implemented")
    }

    private var root: OctaPointsTree.Node? = null


    constructor(points: List<PointVector>) {
        this.points = ArrayList(points)

        if (points.isNullOrEmpty())
            return

        var minX = points[0].x
        var minY = points[0].y
        var minZ = points[0].z

        var maxX = points[0].x
        var maxY = points[0].y
        var maxZ = points[0].z

        for(p in points) {
            minX = min(minX, p.x)
            minY = min(minY, p.y)
            minZ = min(minZ, p.z)

            maxX = max(maxX, p.x)
            maxY = max(maxY, p.y)
            maxZ = max(maxZ, p.z)
        }

        root = Node(PointVector(minX, minY, minZ), PointVector(maxX, maxY, maxZ))

        for(p in points) {
            root?.put(p)
        }
    }

    override fun containedIn(figure: Containable): Boolean {
        return root?.hasContainedIn(figure)?:false
    }

    override fun countIn(figure: Containable): Int {
        return root?.countContainedIn(figure)?:0
    }

    override fun toString(): String {
        return root.toString()
    }

    companion object {
        private var minGranularitySq: Double = 10.0

        fun setMinGranularity(granularity: Double) {
            minGranularitySq = granularity * granularity
        }
    }
}