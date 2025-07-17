interface Containable {

    fun contains(point: PointVector) : Boolean
    fun getMaxXYZ() : PointVector
    fun getMinXYZ() : PointVector

    fun contains(points: List<PointVector>): Boolean {
        //TODO: Multithreading
        //TODO: OctaTree (2D QuadTree equivalent like https://medium.com/@hakizimanafreddoctype/simplifying-quad-tree-indexing-b53f0a72d508
        for(p in points)
            if(contains(p))
                return true
        return false
    }

    fun contains(pointStructure: ContainedIn):Boolean {
        return pointStructure.containedIn(this)
    }

    fun countInside(pointStructure: ContainedIn) :Int {
        return pointStructure.countIn(this)
    }

    fun countInside(points: List<PointVector>): Int {
        var result = 0
        for(p in points)
            if(contains(p))
                result++
        return result
    }
}
