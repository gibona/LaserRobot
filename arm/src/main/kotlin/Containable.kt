interface Containable {

    fun contains(point: PointVector) : Boolean

    fun contains(points: List<PointVector>): Boolean {
        //TODO: Multithreading
        for(p in points)
            if(contains(p))
                return true

        return false
    }
    fun contains(pointsCloud: PointsCloud): Boolean {
        if(contains(pointsCloud.uPoints))
            return true

        if(contains(pointsCloud.wPoints))
            return true

        if(contains(pointsCloud.bPoints))
            return true

        return false
    }

    fun countInside(points: List<PointVector>): Int {
        //TODO: Multithreading
        var result = 0
        for(p in points)
            if(contains(p))
                result++
        return result
    }
}
