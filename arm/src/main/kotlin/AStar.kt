
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.set

class AStar {
    class Cost(var numberOfMovements: Int, var distance: Double) : Comparable<AStar.Cost> {
        override fun compareTo(other: AStar.Cost): Int {
            if (numberOfMovements == other.numberOfMovements)
                return distance.compareTo(other.distance)
            return numberOfMovements.compareTo(other.numberOfMovements)
        }

        override fun toString(): String {
            return "M:$numberOfMovements D:$distance"
        }

        companion object {
            fun create(pointsCloud: PointsCloud, manipulator: Cylinder, potentialManipulator: Cylinder): AStar.Cost {
                val move = pointsCloud.tryDirectMovement(manipulator, potentialManipulator)
                return Cost(move?.size?:0,
                    (manipulator.centerPoint - potentialManipulator.centerPoint).absSq()
                )
            }

            fun min(a: Cost, b: Cost):Cost {
                return if (a < b) a else b
            }
        }

    }

    class Node(val point: PointVector, val cost: Cost) : Comparable<Node> {

        override fun compareTo(other: Node): Int {
            return cost.compareTo(other.cost)
        }

        override fun toString(): String {
            return "$point + Cost: ($cost)"
        }
    }

    companion object {

        /**
         * https://www.redblobgames.com/pathfinding/a-star/implementation.html
         */
        fun calculate(pointsCloud: PointsCloud, manipulator: Cylinder, potentialManipulator: Cylinder): List<Cylinder>? {

            //println("A* from ${manipulator.centerPoint} to ${potentialManipulator.centerPoint}")
            var step3D = potentialManipulator.toBoundingSphere().radius
            var offsets = ArrayList<PointVector>(26)

            for (x in -1 .. 1)
                for(y in -1 .. 1)
                    for (z in -1 .. 1)
                        if(x != 0 && y != 0 && z != 0)
                            offsets.add(PointVector(x * step3D, y * step3D, z * step3D))

            val frontier = PriorityQueue<Node>()
            val start = manipulator.centerPoint
            val costStart = Cost.create(pointsCloud, manipulator, potentialManipulator)
            frontier.add(Node(start, costStart))

            val cameFrom = HashMap<PointVector, PointVector>()
            val costSoFar = HashMap<PointVector, Int>()
            val visited = HashMap<PointVector, Boolean>()

            cameFrom[start] = start;
            costSoFar[start] = 0;

            var maxIteration = 0
            while(frontier.isNotEmpty()) {
                var current = frontier.poll()

                //println(current)

                if(visited.contains(current.point))
                    continue

                maxIteration++

                if(maxIteration > 100)
                    return null

                visited[current.point] = true

                var currentManipulator = manipulator.moveToCenterPoint(current.point)

                if (current.point == potentialManipulator.centerPoint)
                    return reconstructThePath(current.point, potentialManipulator, cameFrom)

                var neighbours = ArrayList<PointVector>()

                val manipulatorCurrent = potentialManipulator.moveToCenterPoint(current.point)

                if (!pointsCloud.tryDirectMovement(manipulatorCurrent, potentialManipulator).isNullOrEmpty()) {
                    // try reaching the goal directly
                    neighbours.add(potentialManipulator.centerPoint)
                }

                for (offset in offsets) {
                    var potentialNeighbour = potentialManipulator.moveToCenterPoint(current.point + offset)

                    if (!pointsCloud.tryDirectMovement(manipulatorCurrent, potentialNeighbour).isNullOrEmpty()) {
                        // try reaching the goal directly
                        neighbours.add(potentialNeighbour.centerPoint)
                    }
                }


                for (next in neighbours) {
                    val manipulatorNext = currentManipulator.moveToCenterPoint(next)
                    val nextStepCost = Cost.create(pointsCloud, currentManipulator, manipulatorNext)
                    val directCost = Cost.create(pointsCloud, manipulator, manipulatorNext)
                    val bestCost = Cost.min(nextStepCost, directCost)

                    val costSoFarNext = costSoFar[next]
                    if (costSoFarNext == null || bestCost.numberOfMovements < costSoFarNext ) {
                        costSoFar[next] = bestCost.numberOfMovements
                        frontier.add(Node(next, bestCost))
                        cameFrom[next] = if (nextStepCost == bestCost) currentManipulator.centerPoint else manipulator.centerPoint
                    }
                }


            }

            return null
        }

        private fun reconstructThePath(
            start: PointVector,
            potentialManipulator: Cylinder,
            cameFrom: HashMap<PointVector, PointVector>
        ): List<Cylinder>? {

            //println("Reconstruct")
            var path = ArrayDeque<Cylinder>()
            var current:PointVector = start
            while (cameFrom[current] != current) {
                //println(current)
                path.addFirst(potentialManipulator.moveToCenterPoint(current))
                val next = cameFrom[current];
                if (next == null)
                    break;
                current = next
            }
            return path
        }
    }

}
