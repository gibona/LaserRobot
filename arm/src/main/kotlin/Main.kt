import java.io.*
import java.lang.Double.min
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

const val PRINT_DEBUG = false
const val EPS = 0.0001
const val SAMPLES_FIBONACCI = 100
fun log(string : String) {
    //System.out.println(string)
}

fun readInput() {
    var fileIn = File("RoboticArm.in")
    var input = Scanner(BufferedInputStream(FileInputStream(fileIn))).useLocale(Locale.US)

    val rMin = input.nextDouble()
    val rMax = input.nextDouble()
    log("rMin:$rMin, rMax:$rMax")

    val rInst = input.nextDouble()
    val fMin = input.nextDouble()
    val fMax = input.nextDouble()
    val rFoc = input.nextDouble()
    log("rInst:$rInst, fMin:$fMin, fMax:$fMax, rFoc:$rFoc")

    val pointVectorF = PointVector.readFromScanner(input)
    val pointVectorB = PointVector.readFromScanner(input)
    log("F:$pointVectorF, B:$pointVectorB")

    val nU = input.nextInt()
    var nW = input.nextInt()
    var nB = input.nextInt()
    log("nU:$nU, nW:$nW, nB:$nB")

    val uPointVectors = PointVector.readFromScannerMultiple(input, nU)
    val wPointVectors = PointVector.readFromScannerMultiple(input, nW)
    val bPointVectors = PointVector.readFromScannerMultiple(input, nB)
}

fun readNextLineAndSplit(input: BufferedReader): List<String> {
    var line = ""
    do {
        line = input.readLine()
    } while(line.isEmpty())

    return line.split(" ")

}
fun readInputFast(input: BufferedReader, output: OutputStreamWriter) {

//    ListTreeComparator.testImplementations()
    var t1 = System.currentTimeMillis()


    var line = readNextLineAndSplit(input)
    val rMin = line[0].toDouble()
    val rMax = line[1].toDouble()
    log("rMin:$rMin, rMax:$rMax")

    line = readNextLineAndSplit(input)
    val rInst = line[0].toDouble()
    val fMin = line[1].toDouble()
    val fMax = line[2].toDouble()
    val rFoc = line[3].toDouble()
    log("rInst:$rInst, fMin:$fMin, fMax:$fMax, rFoc:$rFoc")

    var manipulatorInput = readNextLineAndSplit(input)
    val pointF = PointVector.read(manipulatorInput)
    val pointB = PointVector.read(manipulatorInput, 3)
    val manipulator = Cylinder(pointF, pointB, rInst)
    log("F:$pointF, B:$pointB")

    line = readNextLineAndSplit(input)
    val nU = line[0].toInt()
    var nW = line[1].toInt()
    var nB = line[2].toInt()
    log("nU:$nU, nW:$nW, nB:$nB")

    val uuPoints = PointVector.readMultiple(input, nU) // полезно разстение
    val wwPoints = PointVector.readMultiple(input, nW) // плевел
    val bbPoints = PointVector.readMultiple(input, nB) // камък

    OctaPointsTree.setMinGranularity(min(rFoc, (fMax-fMin)/2.0))

    val uPoints = OctaPointsTree(uuPoints)
    val wPoints = OctaPointsTree(wwPoints)
    val bPoints = OctaPointsTree(bbPoints)

    //used for comparing both implementations
//    val uPoints = ListTreeComparator(ListWrapper(uuPoints), OctaPointsTree(uuPoints))
//    val wPoints = ListTreeComparator(ListWrapper(wwPoints), OctaPointsTree(wwPoints))
//    val bPoints = ListTreeComparator(ListWrapper(bbPoints), OctaPointsTree(bbPoints))
    //val wRemainingPoints = ListTreeComparator(ListWrapper(wwPoints), OctaPointsTree(wwPoints))
    //println(uPoints.toString())

    val pointsCloud = PointsCloud(uPoints, wPoints, bPoints)

    var t2 = System.currentTimeMillis();
    if (PRINT_DEBUG)
        System.out.println("reading:" + (t2 - t1))
    if (PRINT_DEBUG)
        println("uPoints ${uPoints.size}  wPoints:${wPoints.size}  bPoints:${bPoints.size}")


    var wRemainingPoints = ArrayList<PointVector>(wwPoints) // copy points

    while(wRemainingPoints.isNotEmpty()) {
        if (PRINT_DEBUG)
            println("Left: ${wRemainingPoints.size}")
        var potentialTarget = Sphere.fromPoints(wRemainingPoints, min(rFoc, (fMax-fMin)/2.0)) // опитваме се да групираме плевели в сфера и да го облъчим
        if (potentialTarget.contains(uPoints)) { // има полезни разстения в сферата - не става за облъчване
            potentialTarget = Sphere(wRemainingPoints[0], EPS) // фокусираме се само на тази точка
            if (potentialTarget.contains(uPoints)) { // няма как да я облъчим без да засегнем полезни разстения - махаме я
                wRemainingPoints.removeAt(0)
                continue;
            }
        }

        if (PRINT_DEBUG)
            println("Inside: " + potentialTarget.countInside(wRemainingPoints))

        var bestTargetPoints = 0
        var bestTrajectory: List<Cylinder>? = null
        var lasers = potentialTarget.generateFibonacciSphere(fMax-fMin, rFoc)


        for(laser in lasers) {

            //println("Laser cycle: $i")

            val harmPlants = laser.contains(uPoints)
            if (harmPlants) // без да облъчваме разстения
                continue

            val targetPoints = laser.countInside(wRemainingPoints)
            if (bestTargetPoints > targetPoints) // вече сме намерили по-добра позиция
                continue

            var potentialManipulator = laser.getManipulator(fMin, manipulator.height(), manipulator.radius)
            if ( !potentialManipulator.inWorkingArea(rMin, rMax)
                || potentialManipulator.contains(pointsCloud)) // манипулатора засяга точкиплевел
                continue

            var trajectory = pointsCloud.calculateManipulatorTrajectory(manipulator, potentialManipulator)
            if (trajectory != null)
                if (bestTrajectory.isNullOrEmpty() || bestTrajectory.size > trajectory.size) {
                    bestTrajectory = trajectory
                    bestTargetPoints = targetPoints
                    // TODO: don't use break if you need optimal result - we don't need optimal solution, just a solution
                     break;
                }
        }

        if(bestTrajectory.isNullOrEmpty()) { // няма как да стигнем до 0-левата точка
            //println("Nema pat do ${wRemainingPoints[0]}")
            wRemainingPoints.removeAt(0)
            continue
        }

        move(bestTrajectory, output)
        var laser = bestTrajectory.last().getLaser(fMin, fMax, rFoc)

        if (PRINT_DEBUG)
            wRemainingPoints.forEach {
                if (laser.contains(it))
                    println("Eliminating: $it")
            }

        // TODO: Multithreading
        wRemainingPoints = wRemainingPoints.filter { !laser.contains(it) } as ArrayList<PointVector>

        fire(laser.getLaser(fMin, fMax, rFoc), output)

        /*
        if(wRemainingPoints.isNotEmpty())
            println("Remaining: ${wRemainingPoints[0]}")

         */

    }


}

fun fire(l: Cylinder, output: OutputStreamWriter) {
    var fire = "FIRE\r"
    /*
    fire = String.format("FIRE %.4f %.4f %.4f %.4f %.4f %.4f",
        l.start.x, l.start.y, l.start.z,
        l.end.x, l.end.y, l.end.z,
    )
     */
    if (PRINT_DEBUG)
        println(fire)
    output.write(fire)
}

fun move(trajectory: List<Cylinder>, output: OutputStreamWriter) {
    for (t in trajectory) {
        val move = String.format(Locale.US, "MOVE %.4f %.4f %.4f %.4f %.4f %.4f\n",
            t.start.x, t.start.y, t.start.z,
            t.end.x, t.end.y, t.end.z)
        if (PRINT_DEBUG)
            println(move)
        output.write(move)
    }
}

fun main(args: Array<String>) {

    val fileIn = File("RoboticArm.in")
    var fileOut = File("RoboticArm.out")
    var output = OutputStreamWriter(FileOutputStream(fileOut))
    val input = BufferedReader(InputStreamReader(FileInputStream(fileIn)))
    try {
        readInputFast(input, output)
    } catch (t: Throwable) {
        t.printStackTrace()
    } finally {
        output.close()
        input.close()
    }

    /*
    for(i in 0 until 1000000) {
        var x = Math.random()+200
        var y = Math.random()+200
        var z = Math.random()+200
        System.out.println("$x $y $z")
    }*/
}

fun testParallel() {
    val dispatcher = Executors.newFixedThreadPool(4)
    var numbers = 1 .. 100000000
    var chunks = numbers.asIterable().chunked((numbers.count()/4)+1)

    var lookFor = numbers.last -1

    var t0 = System.currentTimeMillis()
    for(n in numbers)
        if (n == lookFor)
            break

    var t1 = System.currentTimeMillis()
    println("Normal " +(t1-t0))

    var contains = false;
    val barrier = CountDownLatch(chunks.size)
    for(chunk in chunks) {
        dispatcher.submit {
            println("Start ${chunk[0]} " + Thread.currentThread().id)
            for (p in chunk) {
                //println(p)
                //if (contains(p))
                if (p == lookFor)
                    contains = true

                if (contains) {
                    barrier.countDown()
                    return@submit
                }
            }
            barrier.countDown()
        }
    }

    var t2 = System.currentTimeMillis()
    println("Await" +(t2-t1))

    barrier.await()


    println(contains )

    var t3 = System.currentTimeMillis()
    println("Done" +(t3-t2))

    return
}