class PointsCloud (val uPoints: List<PointVector>, // полезно разстение
                   val wPoints: List<PointVector>, // плевел
                   val bPoints: List<PointVector>, // камък
){
    fun calculateManipulatorTrajectory(manipulator: Cylinder, potentialManipulator: Cylinder): List<Cylinder>? {
        var direct = tryDirectMovement(manipulator, potentialManipulator)
        if (direct?.isNotEmpty() == true)
            return direct
        else { // Modified A* https://iopscience.iop.org/article/10.1088/1757-899X/928/3/032016/pdf
            return AStar.calculate(this, manipulator, potentialManipulator)
        }
    }

    fun tryDirectMovement(from: Cylinder, to: Cylinder): List<Cylinder>? {
        /**
         * Проверка дали from -> to пътя е свободен:
         * 1. в Описаната сфера на from няма точки
         * 2. в Описаната сфера на to няма точки
         * 3. в цилиндъра с радиус описаната сферата между двете сфери няма точки
         * 4. в цилиндъра с радиус вписаната сферата между двете сфери няма точки
         * => по тази треактория може да се върти без ограничение
         * манипулатора и няма да засегне точки
         */

        if (from.contains(this))
            return null // в цилиндъра има точки ?!?!?!

        if (to.contains(this))
            return null // в целта има точки ?!?!?!

        assert(from.height() == to.height())

        var pathBoundedCylinder = Cylinder(from.centerPoint, to.centerPoint, from.radius)
        // ако ориентираме манипулатора по посоката на пътя може да мине
        if (pathBoundedCylinder.contains(this))
            return null // няма директен път

        val fromSphere = from.toBoundingSphere()
        val toSphere = to.toBoundingSphere()

        assert(fromSphere.radius == toSphere.radius)

        var pathBoundingCylinder = Cylinder(fromSphere.center, toSphere.center, fromSphere.radius)
        val canUsePathBoundingCylinder = !pathBoundingCylinder.contains(this)

        if (canUsePathBoundingCylinder) { // манипулатора може да се върти свободно по пътя
            // => пътя е достатъчно широк за да се направи въртенето вътре в него
            if (fromSphere.radius * 2.0 >= pathBoundingCylinder.height())
                return listOf(to)

            else if(!toSphere.contains(this)) // трябва да се завърти в края
                return listOf(to)
        }

        // може да се завърти в началото и да се движи паралелно на пътя
        if(!fromSphere.contains(this))
            return listOf(to)

       return null
    }

}