package com.alifba.alifba.presenation.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.painterResource
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.*
import com.caverock.androidsvg.SVG
import java.io.InputStream
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

data class ColoringPath(val path: Path, val color: Color)

data class ColoringState(
    val selectedColor: Color = Color.Red,
    val paths: List<ColoringPath> = emptyList(),
    val undoStack: List<List<ColoringPath>> = emptyList(),
    val redoStack: List<List<ColoringPath>> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoringScreen(
    onBackPressed: () -> Unit = {}
) {
    val alifbaFont = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))
    val context = LocalContext.current
    var coloringState by remember { mutableStateOf(ColoringState()) }
    var svgBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Set orientation to landscape when entering ColoringScreen and restore on dispose
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        onDispose {
            // Restore portrait orientation when leaving
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    
    // Handle back press and restore portrait orientation
    BackHandler {
        val activity = context as? ComponentActivity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onBackPressed()
    }

    LaunchedEffect(Unit) {
        try {
            println("DEBUG: Starting SVG load...")
            
            // Check if assets folder is accessible
            val assetsList = context.assets.list("")
            println("DEBUG: Assets folder contents: ${assetsList?.toList()}")
            
            println("DEBUG: Trying to open popsicles.svg...")
            val inputStream: InputStream = context.assets.open("popsicles.svg")
            println("DEBUG: InputStream opened successfully")
            
            val svg = SVG.getFromInputStream(inputStream)
            println("DEBUG: SVG parsed successfully")
            println("DEBUG: SVG document dimensions - Width: ${svg.documentWidth}, Height: ${svg.documentHeight}")
            println("DEBUG: SVG viewBox - ${svg.documentViewBox}")
            
            // Use fixed size instead of document size which might be 0
            val size = 800
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            println("DEBUG: Created bitmap and canvas")
            
            // Set SVG size and render
            svg.documentWidth = size.toFloat()
            svg.documentHeight = size.toFloat()
            println("DEBUG: Set SVG dimensions to ${size}x${size}")
            
            svg.renderToCanvas(canvas)
            println("DEBUG: SVG rendered to canvas")
            
            svgBitmap = bitmap
            println("DEBUG: SVG bitmap set successfully - Final bitmap size: ${bitmap.width}x${bitmap.height}")
        } catch (e: Exception) {
            println("ERROR: Failed to load SVG: ${e.message}")
            println("ERROR: Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
            // Create a visible fallback so you can see SOMETHING
            val size = 800
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.LTGRAY)
            
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                textSize = 48f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            canvas.drawText("SVG LOAD FAILED", size/2f, size/2f - 50f, paint)
            canvas.drawText("${e.message}", size/2f, size/2f + 50f, paint)
            svgBitmap = bitmap
            println("DEBUG: Fallback bitmap created with error message")
        }
    }

    fun saveState() {
        coloringState = coloringState.copy(
            undoStack = coloringState.undoStack + listOf(coloringState.paths),
            redoStack = emptyList()
        )
    }

    fun undo() {
        if (coloringState.undoStack.isNotEmpty()) {
            val previousPaths = coloringState.undoStack.last()
            coloringState = coloringState.copy(
                paths = previousPaths,
                undoStack = coloringState.undoStack.dropLast(1),
                redoStack = coloringState.redoStack + listOf(coloringState.paths)
            )
        }
    }

    fun redo() {
        if (coloringState.redoStack.isNotEmpty()) {
            val nextPaths = coloringState.redoStack.last()
            coloringState = coloringState.copy(
                paths = nextPaths,
                redoStack = coloringState.redoStack.dropLast(1),
                undoStack = coloringState.undoStack + listOf(coloringState.paths)
            )
        }
    }

    fun reset() {
        saveState()
        coloringState = coloringState.copy(paths = emptyList())
    }

    val colorPalette = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Magenta, Color.Cyan, Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF795548), // Brown
        Color.Black, Color.Gray, Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff8ad38a)) // Soft green background color
    ) {
        // Main content row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp) // Increased padding to show background
        ) {
            // Zoomable SVG Card on the left side
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 24.dp) // More padding to show soft background
            ) {
                ZoomableCard(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = svgBitmap,
                    paths = coloringState.paths,
                    onFill = { path ->
                        saveState()
                        coloringState = coloringState.copy(
                            paths = coloringState.paths + ColoringPath(path, coloringState.selectedColor)
                        )
                    },
                    alifbaFont = alifbaFont
                )
                
                // Back button overlay - top left edge
                IconButton(
                    onClick = { onBackPressed() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp) // Even closer to edge
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = Color.Black,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Undo/Redo buttons overlay - bottom left edge (vertical)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp), // Even closer to edge
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Undo button
                    IconButton(
                        onClick = { undo() },
                        enabled = coloringState.undoStack.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.undo),
                            contentDescription = "Undo",
                            tint =  Color.Black ,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Redo button
                    IconButton(
                        onClick = { redo() },
                        enabled = coloringState.redoStack.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.redo),
                            contentDescription = "Redo",
                            tint =  Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Right side with color palette grid - moved further right
            GridColorPalette(
                colors = colorPalette,
                selectedColor = coloringState.selectedColor,
                onColorSelected = { color ->
                    coloringState = coloringState.copy(selectedColor = color)
                },
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .padding(start = 32.dp) // Move further right
            )
        }
    }
}

@Composable
fun GridColorPalette(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columns grid
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp) // Minimal padding so borders can touch edges
        ) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp) // Smaller circular colors
                        .clip(CircleShape) // Circular colors instead of squares
                        .background(color)
                        .clickable { onColorSelected(color) }
                        .then(
                            if (color == selectedColor) {
                                Modifier
                                    .border(
                                        width = 3.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    )
                            } else {
                                Modifier
                                    .border(
                                        width = 1.dp,
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (color == selectedColor) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (color == Color.White || color == Color.Yellow) {
                                Color.Black
                            } else {
                                Color.White
                            },
                            modifier = Modifier.size(16.dp) // Smaller check icon
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableCard(
    bitmap: Bitmap?,
    paths: List<ColoringPath>,
    onFill: (Path) -> Unit,
    alifbaFont: FontFamily,
    modifier: Modifier = Modifier
) {
    // State for zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f) // Limit zoom between 0.5x and 3x
        offset += offsetChange
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformableState),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = white
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            bitmap?.let { bmp ->
                SVGColoringView(
                    bitmap = bmp,
                    paths = paths,
                    onFill = onFill,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Loading SVG...",
                            fontSize = 24.sp,
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            color = navyBlue
                        )
                        Text(
                            text = "If this doesn't change, SVG failed to load",
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableSVGView(
    bitmap: Bitmap?,
    paths: List<ColoringPath>,
    onFill: (Path) -> Unit,
    alifbaFont: FontFamily,
    modifier: Modifier = Modifier
) {
    // State for zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f) // Limit zoom between 0.5x and 3x
        offset += offsetChange
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { bmp ->
            SVGColoringView(
                bitmap = bmp,
                paths = paths,
                onFill = onFill,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformableState)
            )
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Loading SVG...",
                        fontSize = 24.sp,
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue
                    )
                    Text(
                        text = "If this doesn't change, SVG failed to load",
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun SVGColoringView(
    bitmap: Bitmap,
    paths: List<ColoringPath>,
    onFill: (Path) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bitmapWidth = bitmap.width
                    val bitmapHeight = bitmap.height

                    // Scale coordinates from canvas to bitmap
                    val scaleX = bitmapWidth.toFloat() / canvasWidth
                    val scaleY = bitmapHeight.toFloat() / canvasHeight

                    val x = (offset.x * scaleX).toInt()
                    val y = (offset.y * scaleY).toInt()

                    if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                        val targetColor = bitmap.getPixel(x, y)
                        val path = floodFill(bitmap, Point(x, y), targetColor)
                        onFill(path)
                    }
                }
            }
    ) {
        // Draw the SVG bitmap scaled to fit the canvas
        drawImage(
            image = imageBitmap,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
        
        // Draw the colored paths scaled to match the image
        val scaleX = size.width / imageBitmap.width
        val scaleY = size.height / imageBitmap.height
        
        paths.forEach { (path, color) ->
            withTransform({
                scale(scaleX, scaleY, pivot = androidx.compose.ui.geometry.Offset.Zero)
            }) {
                drawPath(path, color)
            }
        }
    }
}

fun colorDifference(c1: Int, c2: Int): Int {
    val r1 = android.graphics.Color.red(c1)
    val g1 = android.graphics.Color.green(c1)
    val b1 = android.graphics.Color.blue(c1)
    val r2 = android.graphics.Color.red(c2)
    val g2 = android.graphics.Color.green(c2)
    val b2 = android.graphics.Color.blue(c2)
    return abs(r1 - r2) + abs(g1 - g2) + abs(b1 - b2)
}

fun floodFill(bitmap: Bitmap, start: Point, targetColor: Int, tolerance: Int = 16): Path {
    val path = Path()
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val visited = Array(height) { BooleanArray(width) }
    val filledPixels = mutableListOf<Point>()

    val queue: Queue<Point> = LinkedList()
    queue.add(start)

    while (queue.isNotEmpty()) {
        val p = queue.poll() ?: continue
        if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && !visited[p.y][p.x]) {
            val index = p.y * width + p.x
            if (colorDifference(pixels[index], targetColor) < tolerance) {
                visited[p.y][p.x] = true
                filledPixels.add(p)
                queue.add(Point(p.x + 1, p.y))
                queue.add(Point(p.x - 1, p.y))
                queue.add(Point(p.x, p.y + 1))
                queue.add(Point(p.x, p.y - 1))
            }
        }
    }

    if (filledPixels.isNotEmpty()) {
        val contour = findContour(filledPixels, width, height)
        path.moveTo(contour[0].x.toFloat(), contour[0].y.toFloat())
        for (i in 1 until contour.size) {
            path.lineTo(contour[i].x.toFloat(), contour[i].y.toFloat())
        }
        path.close()
    }

    return path
}

fun findContour(points: List<Point>, width: Int, @Suppress("UNUSED_PARAMETER") height: Int): List<Point> {
    val pointSet = points.toSet()
    val startPoint = points.minByOrNull { it.y * width + it.x } ?: return emptyList()

    val contour = mutableListOf<Point>()
    var currentPoint = startPoint
    var dir = 0

    do {
        contour.add(currentPoint)
        for (i in 0 until 8) {
            val nextDir = (dir + i) % 8
            val nextPoint = getNextPoint(currentPoint, nextDir)
            if (pointSet.contains(nextPoint)) {
                currentPoint = nextPoint
                dir = (nextDir + 6) % 8
                break
            }
        }
    } while (currentPoint != startPoint)

    return contour
}

fun getNextPoint(p: Point, dir: Int): Point {
    return when (dir) {
        0 -> Point(p.x + 1, p.y)
        1 -> Point(p.x + 1, p.y + 1)
        2 -> Point(p.x, p.y + 1)
        3 -> Point(p.x - 1, p.y + 1)
        4 -> Point(p.x - 1, p.y)
        5 -> Point(p.x - 1, p.y - 1)
        6 -> Point(p.x, p.y - 1)
        7 -> Point(p.x + 1, p.y - 1)
        else -> p
    }
}

@Composable
fun ColorPalette(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (color == selectedColor) {
                            Modifier.padding(4.dp)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (color == Color.White || color == Color.Yellow) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ColoringBottomBar(
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onUndo,
            enabled = canUndo,
            colors = ButtonDefaults.buttonColors(
                containerColor = lightNavyBlue,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Undo", color = white)
        }

        Button(
            onClick = onRedo,
            enabled = canRedo,
            colors = ButtonDefaults.buttonColors(
                containerColor = lightNavyBlue,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Redo", color = white)
        }

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE74C3C)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = white,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", color = white)
        }
    }
}

@Composable
fun VerticalColorPalette(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (color == selectedColor) {
                            Modifier.padding(4.dp)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (color == Color.White || color == Color.Yellow) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalColoringBottomBar(
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onUndo,
            enabled = canUndo,
            colors = ButtonDefaults.buttonColors(
                containerColor = lightNavyBlue,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(120.dp)
        ) {
            Text("Undo", color = white)
        }

        Button(
            onClick = onRedo,
            enabled = canRedo,
            colors = ButtonDefaults.buttonColors(
                containerColor = lightNavyBlue,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(120.dp)
        ) {
            Text("Redo", color = white)
        }

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE74C3C)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(120.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = white,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", color = white)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColoringScreenPreview() {
    ColoringScreen()
}