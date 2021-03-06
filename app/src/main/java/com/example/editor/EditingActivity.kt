package com.example.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.Time
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_editing.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class EditingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        val uriString: String? = intent.getStringExtra("imageUri")
        val uri = Uri.parse(uriString)

        var startImage = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        if (startImage.width or startImage.height > 2048) {
            startImage = bilinearInterpolation(startImage, 0.5)
        }

        imageView.setImageBitmap(startImage)

        seekBarInvisible(seekBar1, text1)
        progressBarInvisible()
        allowInvisible()
        cancelInvisible()

        //--------поворот изображения
        rotateButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            rotate(image)
        }
        rotateText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            rotate(image)
        }

        //--------сохранение изображения
        saveButton.setOnClickListener {
            save()
        }
        saveText.setOnClickListener {
            save()
        }

        //--------сброс изменений
        undoButton.setOnClickListener {
            undo(startImage)
        }
        undoText.setOnClickListener {
            undo(startImage)
        }

        //--------эффекты
        effectsButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            effects(image)
        }
        effectsText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            effects(image)
        }

        //--------масштабирование
        scalingButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            scaling(image)
        }
        scalingText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            scaling(image)
        }

        //--------ретушь
        retouchButton.setOnClickListener {
            retouch()
            //Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }
        retouchText.setOnClickListener {
            retouch()
            //Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }

        //--------сегментация
        segmentationButton.setOnClickListener {
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT)
                .show()
        }
        segmentationText.setOnClickListener {
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT)
                .show()
        }

        //--------нерезкое маскирование
        unsharpMaskingButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            unsharpMasking(image)
        }
        unsharpMaskingText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            unsharpMasking(image)
        }
    }

    //----------изменение активности SeekBar, ProgressBar и нижних кнопок
    private fun seekBarVisible(seek: SeekBar, text: TextView) {
        text.visibility = View.VISIBLE
        seek.visibility = View.VISIBLE
    }

    private fun seekBarInvisible(seek: SeekBar, text: TextView) {
        text.visibility = View.GONE
        seek.visibility = View.GONE
    }

    private fun buttonsInvisible() {
        effectsButton.visibility = View.GONE
        rotateButton.visibility = View.GONE
        scalingButton.visibility = View.GONE
        retouchButton.visibility = View.GONE
        segmentationButton.visibility = View.GONE
        unsharpMaskingButton.visibility = View.GONE
        saveButton.visibility = View.GONE
        undoButton.visibility = View.GONE

        effectsText.visibility = View.GONE
        rotateText.visibility = View.GONE
        scalingText.visibility = View.GONE
        retouchText.visibility = View.GONE
        segmentationText.visibility = View.GONE
        unsharpMaskingText.visibility = View.GONE
        saveText.visibility = View.GONE
        undoText.visibility = View.GONE
    }

    private fun buttonsVisible() {
        effectsButton.visibility = View.VISIBLE
        rotateButton.visibility = View.VISIBLE
        scalingButton.visibility = View.VISIBLE
        retouchButton.visibility = View.VISIBLE
        segmentationButton.visibility = View.VISIBLE
        unsharpMaskingButton.visibility = View.VISIBLE
        saveButton.visibility = View.VISIBLE
        undoButton.visibility = View.VISIBLE

        effectsText.visibility = View.VISIBLE
        rotateText.visibility = View.VISIBLE
        scalingText.visibility = View.VISIBLE
        retouchText.visibility = View.VISIBLE
        segmentationText.visibility = View.VISIBLE
        unsharpMaskingText.visibility = View.VISIBLE
        saveText.visibility = View.VISIBLE
        undoText.visibility = View.VISIBLE
    }

    private fun progressBarVisible() {
        progressBar.visibility = View.VISIBLE
    }

    private fun progressBarInvisible() {
        progressBar.visibility = View.GONE
    }

    // активность кнопок подтверждения/отмены
    private fun allowVisible() {
        allowButton.visibility = View.VISIBLE
    }

    private fun cancelVisible() {
        cancelButton.visibility = View.VISIBLE
    }

    private fun allowInvisible() {
        allowButton.visibility = View.GONE
    }

    private fun cancelInvisible() {
        cancelButton.visibility = View.GONE
    }

    //----------поворот изображения
    private fun rotate(originalImage: Bitmap) {
        var rotatedImage: Bitmap = originalImage

        cancelVisible()
        buttonsInvisible()
        seekBarVisible(seekBar1, text1)
        seekBar1.max = 0
        seekBar1.max = 360
        seekBar1.progress = 180
        text1.text = "0°"

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(originalImage)
            seekBarInvisible(seekBar1, text1)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }
        allowButton.setOnClickListener {
            imageView.setImageBitmap(rotatedImage)
            seekBarInvisible(seekBar1, text1)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progess: Int, fromUser: Boolean) {
                text1.text = (seek.progress - 180).toString() + "°"
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                progressBarVisible()
                seekBarInvisible(seekBar1, text1)

                doAsync {
                    cancelInvisible()
                    rotatedImage = rotation(seekBar1.progress - 180)

                    uiThread {
                        imageView.setImageBitmap(rotatedImage)
                        progressBarInvisible()
                        allowVisible()
                        cancelVisible()
                        seekBarVisible(seekBar1, text1)
                        Toast.makeText(
                            this@EditingActivity,
                            "Выполнен поворот на " + (seekBar1.progress - 180) + "°",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // алгоритм поворота
    private fun rotation(angleStart: Int): Bitmap {
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val width: Int = image.width
        val height: Int = image.height

        val pixelsArray = IntArray(width * height)
        image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
        val newPixelsArray = IntArray(width * height)

        val angle = Math.toRadians(angleStart.toDouble())
        val sin = sin(angle)
        val cos = cos(angle)

        val midX = 0.5 * (width - 1) // point to rotate about
        val midY = 0.5 * (height - 1) // center of image

        if (angleStart in -44 until 44) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val a = x - midX
                    val b = y - midY
                    val xx = (+a * cos - b * sin + midX).toInt()
                    val yy = (+a * sin + b * cos + midY).toInt()
                    if (xx in 0 until width && yy >= 0 && yy < height) {
                        newPixelsArray[y * width + x] = pixelsArray[yy * width + xx]
                    } else {
                        newPixelsArray[y * width + x] = Color.argb(100, 0, 0, 0)
                    }
                }
            }
            return Bitmap.createBitmap(newPixelsArray, width, height, image.config)
        } else {
            for (y in 0 until width) {
                for (x in 0 until height) {
                    val a = x - midY
                    val b = y - midX
                    val xx = (+a * cos - b * sin + midX).toInt()
                    val yy = (+a * sin + b * cos + midY).toInt()
                    if (xx in 0 until width && yy >= 0 && yy < height) {
                        newPixelsArray[y * height + x] = pixelsArray[yy * width + xx]
                    } else {
                        newPixelsArray[y * height + x] = Color.argb(100, 0, 0, 0)
                    }
                }
            }
            return Bitmap.createBitmap(newPixelsArray, height, width, image.config)
        }
    }

    //----------сохранение изображения
    private fun save() {
        var temp = 0
        buttonsInvisible()
        progressBarVisible()

        doAsync {
            val mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val time = Time()
            time.setToNow()
            val externalStorageState = Environment.getExternalStorageState()
            if (externalStorageState == Environment.MEDIA_MOUNTED) {
                val storageDirectory = Environment.getExternalStorageDirectory().toString()
                val file = File(
                    storageDirectory,
                    "new_image" + time.year.toString() + (time.month + 1).toString() +
                            time.monthDay.toString() + time.hour.toString() + time.minute.toString() +
                            time.second.toString() + ".jpg"
                )
                try {
                    val stream: OutputStream = FileOutputStream(file)
                    mainImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                    stream.close()
                    temp = 1
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                temp = 0
            }

            uiThread {
                progressBarInvisible()
                buttonsVisible()
                if (temp == 1) {
                    Toast.makeText(
                        this@EditingActivity,
                        "Изображение успешно сохранено",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@EditingActivity,
                        "Не удалось получить доступ к памяти",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //----------сброс изменений
    private fun undo(startImage: Bitmap) {
        val image: Bitmap = startImage
        imageView.setImageBitmap(image)
        Toast.makeText(this@EditingActivity, "Изменения отменены", Toast.LENGTH_SHORT).show()
    }

    //----------эффекты
    private fun effects(originalImage: Bitmap) {
        val menu = PopupMenu(this, effectsButton)
        menu.inflate(R.menu.effects_menu)

        var editableImage: Bitmap = originalImage

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(originalImage)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }
        allowButton.setOnClickListener {
            imageView.setImageBitmap(editableImage)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                // Неоновый розовый
                R.id.effect1 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    cancelVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        cancelInvisible()
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y * width + x])
                                val pixelRed = Color.red(pixelsArray[y * width + x])
                                val pixelGreen = Color.green(pixelsArray[y * width + x])
                                val pixelBlue = Color.blue(pixelsArray[y * width + x])

                                newPixelsArray[y * width + x] =
                                    Color.argb(pixelAlpha, pixelBlue, pixelRed / 2, pixelGreen)
                            }
                        }
                        uiThread {
                            editableImage =
                                Bitmap.createBitmap(newPixelsArray, width, height, image.config)
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Неоновый розовый",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Розовый
                R.id.effect2 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    cancelVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        cancelInvisible()
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                var pixelAlpha = Color.alpha(pixelsArray[y * width + x])
                                val pixelRed = Color.red(pixelsArray[y * width + x])
                                var pixelGreen = Color.green(pixelsArray[y * width + x])
                                var pixelBlue = Color.blue(pixelsArray[y * width + x])

                                if (pixelAlpha >= 10) {
                                    pixelAlpha -= 10
                                }
                                if (pixelGreen >= 10) {
                                    pixelGreen -= 10
                                }
                                if (pixelBlue <= 10) {
                                    pixelBlue += 10
                                }

                                newPixelsArray[y * width + x] =
                                    Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        uiThread {
                            editableImage =
                                Bitmap.createBitmap(newPixelsArray, width, height, image.config)
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Розовый",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Черно-белый 1
                R.id.effect3 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    cancelVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        cancelInvisible()
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y * width + x])
                                val pixelRed = Color.red(pixelsArray[y * width + x])
                                val pixelGreen = Color.green(pixelsArray[y * width + x])
                                val pixelBlue = Color.blue(pixelsArray[y * width + x])

                                val grey = (pixelRed + pixelGreen + pixelBlue) / 3

                                newPixelsArray[y * width + x] =
                                    Color.argb(pixelAlpha, grey, grey, grey)
                            }
                        }
                        uiThread {
                            editableImage =
                                Bitmap.createBitmap(newPixelsArray, width, height, image.config)
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Черно-белый 1",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Черно-белый 2
                R.id.effect4 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    cancelVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        cancelInvisible()
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y * width + x])
                                val pixelRed = Color.red(pixelsArray[y * width + x])
                                val pixelGreen = Color.green(pixelsArray[y * width + x])
                                val pixelBlue = Color.blue(pixelsArray[y * width + x])

                                var mid = (pixelRed + pixelGreen + pixelBlue) / 3
                                when (mid) {
                                    in 0..115 -> {
                                        mid = 0
                                    }
                                    in 116..149 -> {
                                        mid = 134
                                    }
                                    in 155..255 -> {
                                        mid = 255
                                    }
                                }

                                newPixelsArray[y * width + x] =
                                    Color.argb(pixelAlpha, mid, mid, mid)
                            }
                        }
                        uiThread {
                            editableImage =
                                Bitmap.createBitmap(newPixelsArray, width, height, image.config)
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Черно-белый 2",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Негатив
                R.id.effect5 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    cancelVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        cancelInvisible()
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y * width + x])
                                val pixelRed = Color.red(pixelsArray[y * width + x])
                                val pixelGreen = Color.green(pixelsArray[y * width + x])
                                val pixelBlue = Color.blue(pixelsArray[y * width + x])

                                newPixelsArray[y * width + x] = Color.argb(
                                    pixelAlpha, 255 - pixelRed,
                                    255 - pixelGreen, 255 - pixelBlue
                                )
                            }
                        }
                        uiThread {
                            editableImage =
                                Bitmap.createBitmap(newPixelsArray, width, height, image.config)
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Негатив",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Сепия
                R.id.effect6 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    cancelVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        cancelInvisible()
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y * width + x])
                                var pixelRed = Color.red(pixelsArray[y * width + x])
                                var pixelGreen = Color.green(pixelsArray[y * width + x])
                                var pixelBlue = Color.blue(pixelsArray[y * width + x])

                                pixelRed =
                                    (pixelRed * 0.393 + pixelGreen * 0.769 + pixelBlue * 0.189).toInt()
                                pixelGreen =
                                    (pixelRed * 0.349 + pixelGreen * 0.686 + pixelBlue * 0.168).toInt()
                                pixelBlue =
                                    (pixelRed * 0.272 + pixelGreen * 0.534 + pixelBlue * 0.131).toInt()

                                if (pixelRed > 255) pixelRed = 255
                                if (pixelGreen > 255) pixelGreen = 255
                                if (pixelBlue > 255) pixelBlue = 255

                                newPixelsArray[y * width + x] = Color.argb(
                                    pixelAlpha, pixelRed, pixelGreen, pixelBlue
                                )
                            }
                        }
                        uiThread {
                            editableImage =
                                Bitmap.createBitmap(newPixelsArray, width, height, image.config)
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Сепия",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    //----------масштабирование
    private fun scaling(originalImage: Bitmap) {
        var scaledImage: Bitmap = originalImage

        buttonsInvisible()
        seekBarVisible(seekBar1, text1)
        cancelVisible()
        seekBar1.max = 0
        seekBar1.max = 200
        seekBar1.progress = 100
        text1.text = "100%"

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(originalImage)
            seekBarInvisible(seekBar1, text1)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }
        allowButton.setOnClickListener {
            imageView.setImageBitmap(scaledImage)
            seekBarInvisible(seekBar1, text1)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progess: Int, fromUser: Boolean) {
                text1.text = (seek.progress).toString() + "%"
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                progressBarVisible()
                seekBarInvisible(seekBar1, text1)

                doAsync {
                    cancelInvisible()
                    scaledImage = bilinearInterpolation(
                        (imageView.drawable as BitmapDrawable).bitmap,
                        seek.progress.toDouble() / 100
                    )

                    uiThread {
                        imageView.setImageBitmap(scaledImage)
                        progressBarInvisible()
                        allowVisible()
                        cancelVisible()
                        seekBarVisible(seekBar1, text1)
                        if (seekBar1.progress > 100) {
                            Toast.makeText(
                                this@EditingActivity,
                                "Изображение увеличено на " + (seekBar1.progress - 100) + "%",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@EditingActivity,
                                "Изображение уменьшено на " + (100 - seekBar1.progress) + "%",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

    //----------алгоритм масштабирования (билинейная интерполяция)
    private fun bilinearInterpolation(image: Bitmap, ratio: Double): Bitmap {
        // высота и ширина оригинала
        val width1 = image.width
        val height1 = image.height

        val pixelsArray = IntArray(width1 * height1)
        image.getPixels(pixelsArray, 0, width1, 0, 0, width1, height1)

        // новые ширина и высота с учет
        val width2 = (image.width * ratio).toInt()
        val height2 = (image.height * ratio).toInt()
        val newPixelsArray = IntArray(width2 * height2)

        // рассмотрим квадрат пикселей 2х2
        var a: Int   // верхний левый пиксель
        var b: Int   // верхий правый
        var c: Int   // нижний левый
        var d: Int   // нижний правый
        var x: Int
        var y: Int
        var index: Int

        val xRatio = (width1 - 1).toFloat() / width2
        val yRatio = (height1 - 1).toFloat() / height2
        var xDif: Float
        var yDif: Float
        var blue: Float
        var red: Float
        var green: Float
        var offset = 0

        for (i in 0 until height2) {
            for (j in 0 until width2) {
                x = (xRatio * j).toInt()
                y = (yRatio * i).toInt()
                xDif = xRatio * j - x
                yDif = yRatio * i - y
                index = y * width1 + x
                a = pixelsArray[index]
                b = pixelsArray[index + 1]
                c = pixelsArray[index + width1]
                d = pixelsArray[index + width1 + 1]

                blue = (a and 0xff) * (1 - xDif) * (1 - yDif) + (b and 0xff) *
                        xDif * (1 - yDif) + (c and 0xff) * yDif *
                        (1 - xDif) + (d and 0xff) * (xDif * yDif)

                green = (a shr 8 and 0xff) * (1 - xDif) * (1 - yDif) + (b shr 8 and 0xff) *
                        xDif * (1 - yDif) + (c shr 8 and 0xff) * yDif * (1 - xDif) +
                        (d shr 8 and 0xff) * (xDif * yDif)

                red = (a shr 16 and 0xff) * (1 - xDif) * (1 - yDif) + (b shr 16 and 0xff) *
                        xDif * (1 - yDif) + (c shr 16 and 0xff) * yDif * (1 - xDif) +
                        (d shr 16 and 0xff) * (xDif * yDif)

                newPixelsArray[offset++] = -0x1000000 or
                        (red.toInt() shl 16 and 0xff0000) or
                        (green.toInt() shl 8 and 0xff00) or
                        blue.toInt()
            }
        }
        return Bitmap.createBitmap(newPixelsArray, width2, height2, image.config)
    }

    //----------ретуширование
    private fun retouch() {
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val width = image.width
        val height = image.height
        val xCoordinates = IntArray(width * height)
        val yCoordinates = IntArray(width * height)
        val pixelValue = IntArray(width * height)
        var count = 0
        var editableImage: Bitmap = image
        buttonsInvisible()

        viewRet.setOnTouchListener { view, event ->
            when (event!!.action) {
                ACTION_DOWN or ACTION_MOVE -> {
                    var x = event.x.toInt()
                    var y = event.y.toInt()
                    if (x >= 0 && x < image.width) {
                        if (y >= 0 && y < image.height) {
                            for (i in 0 until 21) {
                                for (j in 0 until 21) {
                                    x = x - 10 + i
                                    y = y - 10 + j
                                    xCoordinates[count] = x
                                    yCoordinates[count] = y
                                    pixelValue[count] = image.getPixel(x, y)
                                    count++
                                }
                            }
                        }
                    }
                }
                ACTION_UP -> {
                    progressBarVisible()
                    doAsync {
                        editableImage =
                            retouchAlg(image, pixelValue, count, xCoordinates, yCoordinates)
                        uiThread {
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity, "Выполнена ретушь",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            view.onTouchEvent(event)
        }
    }

    private fun retouchAlg(
        image: Bitmap, pixelValue: IntArray, count: Int,
        xCoordinates: IntArray, yCoordinates: IntArray
    ): Bitmap {
        var pixelAlpha = 0
        var pixelRed = 0
        var pixelGreen = 0
        var pixelBlue = 0
        var pixelColor: Int

        for (i in 0 until count) {
            pixelColor = pixelValue[i]
            pixelAlpha += Color.alpha(pixelColor)
            pixelRed += Color.red(pixelColor)
            pixelGreen += Color.green(pixelColor)
            pixelBlue += Color.blue(pixelColor)
        }

        pixelAlpha /= count
        pixelRed /= count
        pixelGreen /= count
        pixelBlue /= count

        for (i in 0 until count) {
            image.setPixel(
                xCoordinates[i], yCoordinates[i],
                Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
            )
        }
        return image
    }

    //----------нерезкое маскирование
    private fun unsharpMasking(originalImage: Bitmap) {
        var editableImage: Bitmap = originalImage
        var amount = 0
        var threshold = 0
        var radius = 0
        var k = 0

        cancelVisible()
        buttonsInvisible()
        seekBarVisible(seekBar1, text1)

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(originalImage)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
            seekBarInvisible(seekBar1, text1)
        }
        allowButton.setOnClickListener {
            imageView.setImageBitmap(editableImage)
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
            seekBarInvisible(seekBar1, text1)
        }

        seekBar1.max = 0
        seekBar1.max = 100
        seekBar1.progress = 0
        text1.text = "Эффект: 0"
        Toast.makeText(
            this@EditingActivity,
            "Выберите параметр эффекта",
            Toast.LENGTH_SHORT
        ).show()

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar1: SeekBar, progess: Int, fromUser: Boolean) {
                if (k == 0 && threshold == 0 && radius == 0 && amount == 0) {
                    amount = seekBar1.progress
                    text1.text = seekBar1.progress.toString()
                }
                if (k == 1 && threshold == 0 && radius == 0 && amount != 0) {
                    threshold = seekBar1.progress
                    text1.text = seekBar1.progress.toString()
                }
                if (k == 2 && radius == 0 && threshold != 0 && amount != 0) {
                    radius = seekBar1.progress
                    text1.text = seekBar1.progress.toString()
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar1: SeekBar) {
                if (k == 0 && threshold == 0 && radius == 0 && amount != 0) {
                    seekBar1.progress = 0
                    text1.text = "Порог: 0"
                    Toast.makeText(
                        this@EditingActivity,
                        "Выберите параметр порога",
                        Toast.LENGTH_SHORT
                    ).show()
                    k = 1
                }
                if (k == 1 && radius == 0 && threshold != 0 && amount != 0) {
                    seekBar1.progress = 0
                    text1.text = "Радиус: 0"
                    Toast.makeText(
                        this@EditingActivity,
                        "Выберите параметр радиуса",
                        Toast.LENGTH_SHORT
                    ).show()
                    k = 2
                }
                if (k == 2 && radius != 0 && threshold != 0 && amount != 0) {
                    seekBarInvisible(seekBar1, text1)
                    progressBarVisible()
                    k == 0
                    doAsync {
                        cancelInvisible()
                        editableImage = unsharpAlg(amount, threshold, radius, editableImage)

                        uiThread {
                            imageView.setImageBitmap(editableImage)
                            progressBarInvisible()
                            allowVisible()
                            cancelVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применено нерезкое маскирование",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

    //---------нерезкое маскирование
    private fun unsharpAlg(amount: Int, threshold: Int, radius: Int, image: Bitmap): Bitmap {
        var red = 0
        var green = 0
        var blue = 0
        var blurredRed = 0
        var blurredGreen = 0
        var blurredBlue = 0
        var unsMaskPixel = 0
        val alpha = -0x1000000
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        val imageBlurred = blurring(image, radius)
        val temp = IntArray(imageBlurred!!.width * imageBlurred.height)
        imageBlurred.getPixels(
            temp,
            0,
            imageBlurred.width,
            0,
            0,
            imageBlurred.width,
            imageBlurred.height
        )

        for (i in 0 until image.height) {
            for (j in 0 until image.width) {

                val originalPixel = pixels[i * image.width + j]
                val blurredPixel = temp[i * imageBlurred.width + j]
                red = originalPixel shr 16 and 0xff
                green = originalPixel shr 8 and 0xff
                blue = originalPixel and 0xff
                blurredRed = blurredPixel shr 16 and 0xff
                blurredGreen = blurredPixel shr 8 and 0xff
                blurredBlue = blurredPixel and 0xff

                if (abs(red - blurredRed) >= threshold) {
                    red = (amount * (red - blurredRed) + red).toInt()
                    red = if (red > 255) 255 else if (red < 0) 0 else red
                }
                if (abs(green - blurredGreen) >= threshold) {
                    green = (amount * (green - blurredGreen) + green).toInt()
                    green = if (green > 255) 255 else if (green < 0) 0 else green
                }
                if (abs(blue - blurredBlue) >= threshold) {
                    blue = (amount * (blue - blurredBlue) + blue).toInt()
                    blue = if (blue > 255) 255 else if (blue < 0) 0 else blue
                }
                unsMaskPixel = alpha or (red shl 16) or (green shl 8) or blue
                temp[i * image.width + j] = unsMaskPixel
            }
        }
        return Bitmap.createBitmap(temp, image.width, image.height, Bitmap.Config.ARGB_8888)
    }

    private fun blurring(image: Bitmap, radius: Int): Bitmap? {
        assert(radius and 1 == 0) { "Range must be odd." }
        val blurredImage =
            Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(blurredImage)
        val width = image.width
        val height = image.height
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)
        blurringHorizontal(pixels, width, height, radius / 2)
        boxBlurVertical(pixels, width, height, radius / 2)
        c.drawBitmap(pixels, 0, width, 0.0f, 0.0f, width, height, true, null)
        return blurredImage
    }

    private fun blurringHorizontal(pixels: IntArray, width: Int, height: Int, halfRange: Int) {
        var index = 0
        val newColors = IntArray(width)
        for (y in 0 until height) {
            var count = 0
            var red: Long = 0
            var green: Long = 0
            var blue: Long = 0
            for (x in -halfRange until width) {
                val oldPixel = x - halfRange - 1
                if (oldPixel >= 0) {
                    val color = pixels[index + oldPixel]
                    if (color != 0) {
                        red -= Color.red(color).toLong()
                        green -= Color.green(color).toLong()
                        blue -= Color.blue(color).toLong()
                    }
                    count--
                }
                val newPixel = x + halfRange
                if (newPixel < width) {
                    val color = pixels[index + newPixel]
                    if (color != 0) {
                        red += Color.red(color).toLong()
                        green += Color.green(color).toLong()
                        blue += Color.blue(color).toLong()
                    }
                    count++
                }
                if (x >= 0) {
                    newColors[x] = Color.argb(
                        0xFF,
                        (red / count).toInt(),
                        (green / count).toInt(),
                        (blue / count).toInt()
                    )
                }
            }
            for (x in 0 until width) {
                pixels[index + x] = newColors[x]
            }
            index += width
        }
    }

    private fun boxBlurVertical(pixels: IntArray, width: Int, height: Int, halfRange: Int) {
        val newColors = IntArray(height)
        val oldPixelOffset = -(halfRange + 1) * width
        val newPixelOffset = halfRange * width
        for (x in 0 until width) {
            var count = 0
            var red: Long = 0
            var green: Long = 0
            var blue: Long = 0
            var index = -halfRange * width + x
            for (y in -halfRange until height) {
                val oldPixel = y - halfRange - 1
                if (oldPixel >= 0) {
                    val color = pixels[index + oldPixelOffset]
                    if (color != 0) {
                        red -= Color.red(color).toLong()
                        green -= Color.green(color).toLong()
                        blue -= Color.blue(color).toLong()
                    }
                    count--
                }
                val newPixel = y + halfRange
                if (newPixel < height) {
                    val color = pixels[index + newPixelOffset]
                    if (color != 0) {
                        red += Color.red(color).toLong()
                        green += Color.green(color).toLong()
                        blue += Color.blue(color).toLong()
                    }
                    count++
                }
                if (y >= 0) {
                    newColors[y] = Color.argb(
                        0xFF,
                        (red / count).toInt(),
                        (green / count).toInt(),
                        (blue / count).toInt()
                    )
                }
                index += width
            }
            for (y in 0 until height) {
                pixels[y * width + x] = newColors[y]
            }
        }
    }
}






