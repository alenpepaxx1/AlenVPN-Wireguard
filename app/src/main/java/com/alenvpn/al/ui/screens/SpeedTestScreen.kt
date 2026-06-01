package com.alenvpn.al.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(onBack: () -> Unit) {
    var isTesting by remember { mutableStateOf(false) }
    var downloadSpeed by remember { mutableFloatStateOf(0f) }
    var uploadSpeed by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    var testStage by remember { mutableStateOf("Ready") } // Ready, Downloading, Uploading, Finished
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speed Test", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Speedometer(value = if (testStage == "Downloading") downloadSpeed else if (testStage == "Uploading") uploadSpeed else 0f, max = maxOf(100f, downloadSpeed * 1.2f, uploadSpeed * 1.2f))

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ResultCard(label = "Download", value = String.format(java.util.Locale.getDefault(), "%.1f", downloadSpeed), unit = "Mbps", color = Color(0xFF00E676))
                ResultCard(label = "Upload", value = String.format(java.util.Locale.getDefault(), "%.1f", uploadSpeed), unit = "Mbps", color = Color(0xFF2979FF))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = testStage,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (isTesting) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (!isTesting) {
                        isTesting = true
                        downloadSpeed = 0f
                        uploadSpeed = 0f
                        progress = 0f
                        scope.launch {
                            runSpeedTest { stage, p, speed ->
                                testStage = stage
                                progress = p
                                if (stage == "Downloading") downloadSpeed = speed
                                if (stage == "Uploading") uploadSpeed = speed
                            }
                            isTesting = false
                            testStage = "Finished"
                        }
                    }
                },
                enabled = !isTesting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(if (testStage == "Finished") Icons.Default.Refresh else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (testStage == "Finished") "RESTART TEST" else "START SPEED TEST", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun Speedometer(value: Float, max: Float) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 16.dp.toPx()
            val sweepAngle = 240f
            val startAngle = 150f

            // Background arc
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to Color(0xFF2979FF),
                    0.5f to Color(0xFF00E676),
                    1.0f to Color(0xFFFFD600)
                ),
                startAngle = startAngle,
                sweepAngle = (animatedValue / max) * sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(java.util.Locale.getDefault(), "%.0f", animatedValue),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Mbps",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ResultCard(label: String, value: String, unit: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(unit, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

suspend fun runSpeedTest(onUpdate: suspend (String, Float, Float) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            // Download Test
            withContext(Dispatchers.Main) { onUpdate("Downloading", 0.0f, 0f) }
            val downloadUrl = URL("https://speed.cloudflare.com/__down?bytes=25000000") // 25MB chunk
            val connection = downloadUrl.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 10000
            
            val totalBytes = 25000000.0f
            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime
            var lastDownloadedBytes = 0L
            
            // Limit download to 10 seconds maximum
            val MAX_DOWNLOAD_TIME_MS = 10000L
            
            connection.inputStream.use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    downloadedBytes += bytesRead
                    val currentTime = System.currentTimeMillis()
                    
                    if (currentTime - startTime > MAX_DOWNLOAD_TIME_MS) break
                    
                    if (currentTime - lastUpdateTime > 250) { 
                        val timeDiff = (currentTime - lastUpdateTime) / 1000.0f
                        val bytesDiff = downloadedBytes - lastDownloadedBytes
                        val currentSpeedBps = (bytesDiff * 8) / timeDiff
                        val currentSpeedMbps = currentSpeedBps / 1000000.0f
                        
                        val timeProgress = (currentTime - startTime).toFloat() / MAX_DOWNLOAD_TIME_MS.toFloat()
                        val byteProgress = downloadedBytes.toFloat() / totalBytes
                        val progress = maxOf(timeProgress, byteProgress).coerceAtMost(1f)
                        
                        withContext(Dispatchers.Main) { onUpdate("Downloading", progress, currentSpeedMbps) }
                        
                        lastUpdateTime = currentTime
                        lastDownloadedBytes = downloadedBytes
                    }
                }
            }
            
            val totalTime = (System.currentTimeMillis() - startTime) / 1000.0f
            val finalDownloadSpeedMbps = ((downloadedBytes * 8) / totalTime) / 1000000.0f
            withContext(Dispatchers.Main) { onUpdate("Downloading", 1.0f, finalDownloadSpeedMbps) }
            
            delay(500)
            
            // Upload Test
            withContext(Dispatchers.Main) { onUpdate("Uploading", 0.0f, 0f) }
            val uploadUrl = URL("https://speed.cloudflare.com/__up")
            val uploadConnection = uploadUrl.openConnection() as java.net.HttpURLConnection
            uploadConnection.requestMethod = "POST"
            uploadConnection.doOutput = true
            uploadConnection.setRequestProperty("Content-Type", "application/octet-stream")
            uploadConnection.setChunkedStreamingMode(8192)
            
            val bytesToUpload = 10000000L // 10MB
            var uploadedBytes = 0L
            val uploadStartTime = System.currentTimeMillis()
            var lastUpUpdateTime = uploadStartTime
            var lastUploadedBytes = 0L
            val randomData = ByteArray(8192)
            java.util.Random().nextBytes(randomData)
            
            val MAX_UPLOAD_TIME_MS = 10000L
            
            uploadConnection.outputStream.use { output ->
                while(uploadedBytes < bytesToUpload) {
                    val currentUpTime = System.currentTimeMillis()
                    if (currentUpTime - uploadStartTime > MAX_UPLOAD_TIME_MS) break
                    
                    output.write(randomData)
                    uploadedBytes += randomData.size
                    
                    if(currentUpTime - lastUpUpdateTime > 250) {
                        val timeDiff = (currentUpTime - lastUpUpdateTime) / 1000.0f
                        val bytesDiff = uploadedBytes - lastUploadedBytes
                        val currentSpeedBps = (bytesDiff * 8) / timeDiff
                        val currentSpeedMbps = currentSpeedBps / 1000000.0f
                        
                        val timeProgress = (currentUpTime - uploadStartTime).toFloat() / MAX_UPLOAD_TIME_MS.toFloat()
                        val byteProgress = uploadedBytes.toFloat() / bytesToUpload.toFloat()
                        val progress = maxOf(timeProgress, byteProgress).coerceAtMost(1f)
                        
                        withContext(Dispatchers.Main) { onUpdate("Uploading", progress, currentSpeedMbps) }
                        
                        lastUpUpdateTime = currentUpTime
                        lastUploadedBytes = uploadedBytes
                    }
                }
            }
            
            val totalUpTime = (System.currentTimeMillis() - uploadStartTime) / 1000.0f
            val finalUploadSpeedMbps = ((uploadedBytes * 8) / totalUpTime) / 1000000.0f
            withContext(Dispatchers.Main) { onUpdate("Uploading", 1.0f, finalUploadSpeedMbps) }
            
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { onUpdate("Error", 1.0f, 0f) }
        }
    }
}
