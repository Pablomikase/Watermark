package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.IndexOutOfBoundsException
import kotlin.system.exitProcess

enum class Position {
    SINGLE, GRID
}

fun main() {
    //Welcome user
    val imageToProcess = getImageToProcess()
    val whiteMark = getWhiteMarkImage(imageToProcess.height, imageToProcess.width)

    if (whiteMark.height > imageToProcess.height || whiteMark.width > imageToProcess.width) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }

    if (whiteMark.transparency == Transparency.TRANSLUCENT) {
        if (askUserForAlpha()) {
            createWaterMark(imageToProcess, whiteMark, true)
        } else {
            createWaterMark(imageToProcess, whiteMark, false)
        }
    }

    if (askUserToSetTransparencyColor()) {
        createWaterMarkWithTransparencyColor(imageToProcess, whiteMark, false)
    } else {
        createWaterMark(imageToProcess, whiteMark, false)
    }


}

fun createWaterMarkWithTransparencyColor(imageToProcess: BufferedImage, whiteMark: BufferedImage, hasAlpha: Boolean) {
    println("Input a transparency color ([Red] [Green] [Blue]):")
    try {
        val userInput = readln().split(" ").map {
            it.toInt()
        }
        val genericException = IndexOutOfBoundsException()

        userInput.forEach {
            if (it > 255) throw genericException
        }
        if (userInput.size != 3) throw genericException

        createWaterMark(
            imageToProcess,
            whiteMark,
            hasAlpha,
            hasTransparencyColor = true,
            transparencyColor = Color(userInput.first(), userInput[1], userInput.last())
        )
    } catch (ex: Exception) {
        println("The transparency color input is invalid.")
        exitProcess(0)
    }
}

fun askUserToSetTransparencyColor(): Boolean {
    println("Do you want to set a transparency color?")
    return readln().lowercase() == "yes"
}

fun createWaterMark(
    imageToProcess: BufferedImage,
    whiteMark: BufferedImage,
    hasAlpha: Boolean,
    hasTransparencyColor: Boolean = false,
    transparencyColor: Color = Color(0, 0, 0),
) {
    val whiteMarkTransparency = getWhiteMarkTransparency()
    val positionMethod = askForPosition()
    var startingPosition = listOf(0, 0)
    //Position single
    if (positionMethod == Position.SINGLE) {
        val maxXPosition = imageToProcess.width - whiteMark.width
        val maYPosition = imageToProcess.height - whiteMark.height
        println("Input the watermark position ([x 0-$maxXPosition] [y 0-$maYPosition]):")
        startingPosition = readln().split(" ").map {
            try {
                it.toInt()
            } catch (ex: Exception) {
                println("The position input is invalid.")
                exitProcess(0)
            }
        }
        if (startingPosition.first() !in 0..maxXPosition || startingPosition.last() !in 0..maYPosition) {
            println("The position input is out of range.")
            exitProcess(0)
        }
    }

    val finalFileName = askForOutfileName()
    generateWhiteMark(
        imageToProcess,
        whiteMark,
        whiteMarkTransparency,
        finalFileName,
        hasAlpha,
        hasTransparencyColor = hasTransparencyColor,
        transparencyColor = transparencyColor,
        startingPosition = startingPosition,
        position = positionMethod
    )
    exitProcess(0)
}

fun askForPosition(): Position {
    println("Choose the position method (single, grid):")
    return when (readln()) {
        "single" -> Position.SINGLE
        "grid" -> Position.GRID
        else -> {
            println("The position method input is invalid.")
            exitProcess(0)
        }
    }
}

fun createWaterMarkWithAlpha(imageToProcess: BufferedImage, whiteMark: BufferedImage) {
    val finalFileName = askForOutfileName()
    val finalImage = BufferedImage(
        imageToProcess.width,
        imageToProcess.height,
        BufferedImage.TYPE_INT_RGB
    )
    for (x in 0 until finalImage.width) {
        for (y in 0 until finalImage.height) {
            val imageColor = Color(imageToProcess.getRGB(x, y))
            val waterMarkColor = Color(whiteMark.getRGB(x, y), true)
        }
    }
}

fun askUserForAlpha(): Boolean {
    println("Do you want to use the watermark's Alpha channel?")
    if (readln().lowercase() == "yes") {
        return true
    }
    return false
}


fun generateWhiteMark(
    imageToProcess: BufferedImage,
    whiteMark: BufferedImage,
    whiteMarkTransparency: Int,
    finalFileName: String,
    hasAlpha: Boolean,
    hasTransparencyColor: Boolean = false,
    transparencyColor: Color = Color(0, 0, 0),
    startingPosition: List<Int> = listOf(0, 0),
    position: Position
) {

    val finalImage = BufferedImage(
        imageToProcess.width,
        imageToProcess.height,
        BufferedImage.TYPE_INT_RGB
    )

    for (x in 0 until finalImage.width) {
        for (y in 0 until finalImage.height) {

            if (position == Position.SINGLE) {
                val imageColor = Color(imageToProcess.getRGB(x, y))
                if (x in startingPosition.first() until (startingPosition.first() + whiteMark.width)
                    && y in startingPosition.last() until startingPosition.last() + whiteMark.height
                ) {
                    val whiteMarkColor =
                        Color(whiteMark.getRGB(x - startingPosition.first(), y - startingPosition.last()), hasAlpha)
                    val resultColor = if (hasTransparencyColor && transparencyColor == whiteMarkColor) {
                        imageColor
                    } else {
                        Color(
                            (whiteMarkTransparency * whiteMarkColor.red + (100 - whiteMarkTransparency) * imageColor.red) / 100,
                            (whiteMarkTransparency * whiteMarkColor.green + (100 - whiteMarkTransparency) * imageColor.green) / 100,
                            (whiteMarkTransparency * whiteMarkColor.blue + (100 - whiteMarkTransparency) * imageColor.blue) / 100
                        )
                    }
                    if (whiteMarkColor.alpha == 0) {
                        finalImage.setRGB(x, y, imageColor.rgb)
                    } else {
                        finalImage.setRGB(x, y, resultColor.rgb)
                    }
                } else {
                    //Cuando no hay marka de agua en la sección
                    finalImage.setRGB(x, y, imageColor.rgb)
                }
            } else {
                val xInRange = x % whiteMark.width
                val yInRange = y % whiteMark.height

                val imageColor = Color(imageToProcess.getRGB(x, y))
                val whiteMarkColor = Color(whiteMark.getRGB(xInRange, yInRange), hasAlpha)

                val resultColor = if (hasTransparencyColor && transparencyColor == whiteMarkColor) {
                    imageColor
                } else {
                    Color(
                        (whiteMarkTransparency * whiteMarkColor.red + (100 - whiteMarkTransparency) * imageColor.red) / 100,
                        (whiteMarkTransparency * whiteMarkColor.green + (100 - whiteMarkTransparency) * imageColor.green) / 100,
                        (whiteMarkTransparency * whiteMarkColor.blue + (100 - whiteMarkTransparency) * imageColor.blue) / 100
                    )
                }
                if (whiteMarkColor.alpha == 0) {
                    finalImage.setRGB(x, y, imageColor.rgb)
                } else {
                    finalImage.setRGB(x, y, resultColor.rgb)
                }

            }

        }
    }


    val finalFile = File(finalFileName)
    ImageIO.write(finalImage, if (finalFileName.endsWith(".png")) "png" else "jpg", finalFile)
    println("The watermarked image ${finalFile.path} has been created.")
}

fun askForOutfileName(): String {
    println("Input the output image filename (jpg or png extension):")
    val userInput = readln()
    if (userInput.endsWith(".jpg") || userInput.endsWith(".png")) {
        return userInput
    } else {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }
}

fun getWhiteMarkTransparency(): Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val userInput = readln()
    try {
        val transparency = userInput.toInt()
        if (transparency in 1 until 101) {
            return transparency
        }
        println("The transparency percentage is out of range.")
        exitProcess(0)
    } catch (ex: Exception) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
}

fun getWhiteMarkImage(height: Int, width: Int): BufferedImage {
    println("Input the watermark image filename:")
    val userInput = readln()
    //analizando la imagen
    val imageFile = File(userInput)

    try {
        val image = ImageIO.read(imageFile)
        when {
            !imageFile.exists() -> {
                println("The file $userInput doesn't exist.")
                exitProcess(0)
            }

            image.colorModel.numColorComponents != 3 -> {
                println("The number of watermark color components isn't 3.")
                exitProcess(0)
            }

            !intArrayOf(24, 32).contains(image.colorModel.pixelSize) -> {
                println("The watermark isn't 24 or 32-bit.")
                exitProcess(0)
            }

            /*image.height != height || image.width != width -> {
                println("The image and watermark dimensions are different.")
                exitProcess(0)
            }*/

            else -> return image
        }
    } catch (ex: Exception) {
        println("The file $userInput doesn't exist.")
        exitProcess(0)
    }

}

fun getImageToProcess(): BufferedImage {
    println("Input the image filename:")
    val userInput = readln()
    //analizando la imagen
    val imageFile = File(userInput)
    try {
        val image = ImageIO.read(imageFile)
        when {
            !imageFile.exists() -> {
                println("The file $userInput doesn't exist.")
                exitProcess(0)
            }

            image.colorModel.numColorComponents != 3 -> {
                println("The number of image color components isn't 3.")
                exitProcess(0)
            }

            !intArrayOf(24, 32).contains(image.colorModel.pixelSize) -> {
                println("The image isn't 24 or 32-bit.")
                exitProcess(0)
            }

            else -> return image
        }
    } catch (ex: Exception) {
        println("The file $userInput doesn't exist.")
        exitProcess(0)
    }
}

fun showFeedback(image: BufferedImage, userInput: String) {
    //Printing results
    println("Image file: $userInput")
    println("Width: ${image.width}")
    println("Height: ${image.height}")
    println("Number of components: ${image.colorModel.numComponents}")
    println("Number of color components: ${image.colorModel.numColorComponents}")
    println("Bits per pixel: ${image.colorModel.pixelSize}")
    println(
        "Transparency: ${
            when (image.transparency) {
                Transparency.OPAQUE -> "OPAQUE"
                Transparency.BITMASK -> "BITMASK"
                Transparency.TRANSLUCENT -> "TRANSLUCENT"
                else -> "Undefined"
            }
        }"
    )
}