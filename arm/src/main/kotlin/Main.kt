import java.io.*
import java.util.*

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
fun readInputFast() {
    val fileIn = File("RoboticArm.in")
    val input = BufferedReader(InputStreamReader(FileInputStream(fileIn)))

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

    val pointVectorF = PointVector.read(input)
    val pointVectorB = PointVector.read(input)
    log("F:$pointVectorF, B:$pointVectorB")

    line = readNextLineAndSplit(input)
    val nU = line[0].toInt()
    var nW = line[1].toInt()
    var nB = line[2].toInt()
    log("nU:$nU, nW:$nW, nB:$nB")

    val uPointVectors = PointVector.readMultiple(input, nU)
    val wPointVectors = PointVector.readMultiple(input, nW)
    val bPointVectors = PointVector.readMultiple(input, nB)
}

fun main(args: Array<String>) {
    var time = System.currentTimeMillis()
    readInputFast()
    System.out.println(System.currentTimeMillis() - time)
    /*
    for(i in 0 until 1000000) {
        var x = Math.random()+200
        var y = Math.random()+200
        var z = Math.random()+200
        System.out.println("$x $y $z")
    }*/
}