data class Plane(val origin: PointVector, val normal: PointVector) {

    fun pointIsAbove(point: PointVector) : Boolean {
        // See https://math.stackexchange.com/a/2998886/78071
        return PointVector.dotProduct(point - origin, normal) > 0
    }
}