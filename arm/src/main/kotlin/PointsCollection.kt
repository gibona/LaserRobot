interface PointsCollection {
    val size: Int
    fun isNotEmpty(): Boolean  {
        return size > 0
    }

    fun first(): PointVector
    fun remove(item: PointVector)
    fun removeAllInFigure(figure: Containable)

}