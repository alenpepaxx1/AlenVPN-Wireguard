// Copyright Alen Pepa

package com.alenvpn.al.ui.screens

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.alenvpn.al.ui.tr
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alenvpn.al.data.WireguardProfile
import com.alenvpn.al.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import android.net.VpnService
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Bolt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    profiles: List<WireguardProfile>,
    onAddProfileClick: () -> Unit,
    onEditProfileClick: (WireguardProfile) -> Unit,
    onSettingsClick: () -> Unit,
    onSpeedTestClick: () -> Unit,
    onConnectClick: (WireguardProfile) -> Unit,
    onDisconnectClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    viewModel: com.alenvpn.al.viewmodel.VpnViewModel
) {
    val activeProfile = profiles.find { it.isActive }
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var profileToConnect by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<WireguardProfile?>(null) }
    
    var txBytes by androidx.compose.runtime.remember { mutableLongStateOf(0L) }
    var rxBytes by androidx.compose.runtime.remember { mutableLongStateOf(0L) }
    var connectionDuration by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0L) }
    var isPinging by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var publicIp by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Detecting...") }
    var txSpeed by androidx.compose.runtime.remember { mutableLongStateOf(0L) }
    var rxSpeed by androidx.compose.runtime.remember { mutableLongStateOf(0L) }
    val rxHistory = remember { mutableStateListOf<Long>() }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Name") } // Name, Active, Ping
    val pings = remember { mutableStateMapOf<Int, Long>() }

    LaunchedEffect(activeProfile?.id) {
        if (activeProfile != null) {
            val startTime = System.currentTimeMillis()
            var lastRx = 0L
            var lastTx = 0L
            rxHistory.clear()
            
            val fetchJob = launch(Dispatchers.IO) {
                try {
                    val result = java.net.URL("https://api.ipify.org").readText()
                    withContext(Dispatchers.Main) {
                        publicIp = result
                    }
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        publicIp = "Unknown"
                    }
                }
            }

            try {
                while (true) {
                    val stats = com.alenvpn.al.services.WireGuardManager.getStatistics()
                    val currentRx = stats.first
                    val currentTx = stats.second
                    
                    if (lastRx > 0) {
                        val s = currentRx - lastRx
                        rxSpeed = s
                        if (rxHistory.size >= 50) rxHistory.removeAt(0)
                        rxHistory.add(s)
                    }
                    if (lastTx > 0) txSpeed = currentTx - lastTx
                    
                    lastRx = currentRx
                    lastTx = currentTx
                    
                    rxBytes = currentRx
                    txBytes = currentTx
                    connectionDuration = (System.currentTimeMillis() - startTime) / 1000
                    delay(1000)
                }
            } finally {
                fetchJob.cancel()
            }
        } else {
            txBytes = 0L
            rxBytes = 0L
            txSpeed = 0L
            rxSpeed = 0L
            connectionDuration = 0L
            publicIp = "Not connected"
            rxHistory.clear()
        }
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }
    
    fun formatBytes(bytes: Long): String {
        val b = if (bytes < 0) -bytes else bytes
        if (b < 1024) return "$bytes B"
        val exp = (Math.log(b.toDouble()) / Math.log(1024.0)).toInt()
        if (exp <= 0) return "$bytes B"
        val pre = "KMGTPE"[exp - 1]
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }
    
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            profileToConnect?.let { onConnectClick(it) }
            profileToConnect = null
        }
    }
    
    val handleConnectClick = { profile: WireguardProfile ->
        val intent = VpnService.prepare(context)
        if (intent != null) {
            profileToConnect = profile
            vpnPermissionLauncher.launch(intent)
        } else {
            onConnectClick(profile)
        }
    }

    val filteredProfiles by remember(profiles, searchQuery, sortBy) {
        derivedStateOf {
            var list = if (searchQuery.isEmpty()) profiles 
                      else profiles.filter { it.name.contains(searchQuery, ignoreCase = true) }
            
            list = when(sortBy) {
                "Active" -> list.sortedByDescending { it.isActive }
                "Ping" -> list.sortedBy { pings[it.id] ?: 9999L }
                else -> list.sortedBy { it.name }
            }
            list
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AlenVpn", 
                        color = MaterialTheme.colorScheme.onBackground, 
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    IconButton(onClick = onSpeedTestClick) {
                        Icon(Icons.Filled.Bolt, contentDescription = "Speed Test", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProfileClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_profile_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Profile")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        )) {
            val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded || 
                             windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
            
            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Connection Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ConnectionStatusShield(
                            isActive = activeProfile != null,
                            onClick = {
                                if (activeProfile != null) onDisconnectClick()
                                else profiles.firstOrNull()?.let { handleConnectClick(it) }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = if (activeProfile != null) "CONNECTED" else "DISCONNECTED",
                            color = if (activeProfile != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                        
                        Text(
                            text = if (activeProfile != null) "Protected: ${activeProfile.address}" else "Unprotected",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        if (activeProfile != null) {
                            Text(
                                text = "Public IP: $publicIp",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            SpeedChart(history = rxHistory.toList(), modifier = Modifier.fillMaxWidth().height(60.dp))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatsItem("DURATION", formatDuration(connectionDuration))
                                StatsItem("DOWNLOAD", formatBytes(rxBytes))
                                StatsItem("UPLOAD", formatBytes(txBytes))
                            }
                        }
                    }
                    
                    // Right Column: Profiles
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(24.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        ProfilesHeader(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            sortBy = sortBy,
                            onSortClick = {
                                sortBy = when(sortBy) {
                                    "Name" -> "Active"
                                    "Active" -> "Ping"
                                    else -> "Name"
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ProfilesList(
                            filteredProfiles = filteredProfiles,
                            profiles = profiles,
                            pings = pings,
                            onDisconnectClick = onDisconnectClick,
                            handleConnectClick = handleConnectClick,
                            onEditProfileClick = onEditProfileClick,
                            onDeleteClick = onDeleteClick,
                            viewModel = viewModel,
                            pingsMap = pings
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    ConnectionStatusShield(
                        isActive = activeProfile != null,
                        onClick = {
                            if (activeProfile != null) onDisconnectClick()
                            else profiles.firstOrNull()?.let { handleConnectClick(it) }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (activeProfile != null) "CONNECTED" else "DISCONNECTED",
                        color = if (activeProfile != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                    
                    Text(
                        text = if (activeProfile != null) "Protected: ${activeProfile.address}" else "Unprotected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    if (activeProfile != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Public IP: $publicIp",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        SpeedChart(history = rxHistory.toList(), modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatsItem("DURATION", formatDuration(connectionDuration))
                            StatsItem("DOWNLOAD", formatBytes(rxBytes))
                            StatsItem("UPLOAD", formatBytes(txBytes))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfilesHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        sortBy = sortBy,
                        onSortClick = {
                            sortBy = when(sortBy) {
                                "Name" -> "Active"
                                "Active" -> "Ping"
                                else -> "Name"
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfilesList(
                        filteredProfiles = filteredProfiles,
                        profiles = profiles,
                        pings = pings,
                        onDisconnectClick = onDisconnectClick,
                        handleConnectClick = handleConnectClick,
                        onEditProfileClick = onEditProfileClick,
                        onDeleteClick = onDeleteClick,
                        viewModel = viewModel,
                        pingsMap = pings
                    )
                }
            }
        }
    }
}

@Composable
fun StatsItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfilesHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "PROFILES", 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSortClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(tr("Search..."), fontSize = 12.sp) },
            modifier = Modifier.width(150.dp).height(40.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            singleLine = true
        )
    }
}

@Composable
fun ProfilesList(
    filteredProfiles: List<WireguardProfile>,
    profiles: List<WireguardProfile>,
    pings: Map<Int, Long>,
    onDisconnectClick: () -> Unit,
    handleConnectClick: (WireguardProfile) -> Unit,
    onEditProfileClick: (WireguardProfile) -> Unit,
    onDeleteClick: (Int) -> Unit,
    viewModel: com.alenvpn.al.viewmodel.VpnViewModel,
    pingsMap: SnapshotStateMap<Int, Long>
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (filteredProfiles.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (profiles.isEmpty()) "No profiles available.\nTap + to import or create one." 
                        else "No matching profiles found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredProfiles) { profile ->
                ProfileItem(
                    profile = profile,
                    ping = pings[profile.id],
                    onConnectClick = { 
                        if (profile.isActive) onDisconnectClick() 
                        else handleConnectClick(profile) 
                    },
                    onEditClick = { onEditProfileClick(profile) },
                    onDeleteClick = { onDeleteClick(profile.id) },
                    onPingClick = {
                        scope.launch {
                            val latency = viewModel.checkPing(profile.peerEndpoint)
                            pingsMap[profile.id] = latency
                        }
                    }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun SpeedChart(history: List<Long>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas
        val maxSpeed = history.maxOrNull()?.coerceAtLeast(1024L) ?: 1024L
        val width = size.width
        val height = size.height
        val step = width / 50F
        
        val path = Path()
        history.forEachIndexed { i, speed ->
            val x = width - (history.size - 1 - i) * step
            val y = height - (speed.toFloat() / maxSpeed.toFloat() * height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun ConnectionStatusShield(isActive: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val shieldColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(600),
        label = "shield_color"
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .scale(if (isActive) scale else 1f)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = shieldColor.copy(alpha = 0.15f),
                    radius = size.width / 2
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = "Shield",
            tint = shieldColor,
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
fun ProfileItem(
    profile: WireguardProfile,
    ping: Long? = null,
    onConnectClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPingClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConnectClick() },
        shape = RoundedCornerShape(24.dp),
        border = if (profile.isActive) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) 
                 else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (ping != null && ping > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${ping}ms",
                                color = if (ping < 100) Color(0xFF00C853) else if (ping < 300) Color(0xFFFFD600) else Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = if (profile.peerEndpoint.isNotEmpty()) profile.peerEndpoint else "Internal Config",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                
                Row {
                    IconButton(onClick = onPingClick) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = "Ping",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(onClick = {
                        val configText = if (profile.rawConfig.isNotEmpty()) profile.rawConfig else {
                            """
                            [Interface]
                            PrivateKey = ${profile.privateKey}
                            Address = ${profile.address}
                            DNS = ${profile.dns}
                            
                            [Peer]
                            PublicKey = ${profile.peerPublicKey}
                            Endpoint = ${profile.peerEndpoint}
                            AllowedIPs = ${profile.peerAllowedIps}
                            """.trimIndent()
                        }
                        clipboardManager.setText(AnnotatedString(configText))
                        android.widget.Toast.makeText(context, "Config copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share Config",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Profile",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (profile.isActive) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = "Disconnect")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("DISCONNECT"), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Connect")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("CONNECT TO VPN"), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun TextSecondary(): Color {
    return MaterialTheme.colorScheme.onSurfaceVariant
}
