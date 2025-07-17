class ListWrapper(private val points:ArrayList<PointVector>): ContainedIn, PointsCollection {
    override val size: Int
        get() = points.size

    override fun first(): PointVector {
        return points.first()
    }

    override fun remove(item: PointVector) {
        points.remove(item)
    }

    override fun removeAllInFigure(figure: Containable) {
        points.removeAll { figure.contains(it) }
    }

    override fun containedIn(figure: Containable): Boolean {
        return figure.contains(points)
    }

    override fun countIn(figure: Containable): Int {
        return figure.countInside(points)
    }

    override fun toString(): String {
        return points.toString()
    }
}