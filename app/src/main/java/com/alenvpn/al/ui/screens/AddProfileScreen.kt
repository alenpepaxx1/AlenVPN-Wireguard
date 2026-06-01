// Copyright Alen Pepa

package com.alenvpn.al.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alenvpn.al.ui.theme.*
import java.io.BufferedReader
import com.alenvpn.al.ui.tr
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileScreen(
    onNavigateBack: () -> Unit,
    onSaveProfile: (String, String, String, String, String, String, String) -> Unit,
    onImportConfig: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Add Profile"), fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Import Config") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Manual Entry") }
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (selectedTab == 0) {
                    ImportConfigContent(onImportConfig)
                } else {
                    ManualEntryContent(onSaveProfile)
                }
            }
        }
    }
}

@Composable
fun ImportConfigContent(onImportConfig: (String) -> Unit) {
    var configText by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    configText = content
                    reader.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Paste Wireguard Config parameters or import a file (.conf)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Button(
            onClick = {
                try {
                    launcher.launch("*/*")
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Cannot open file picker", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Select File from Device", fontWeight = FontWeight.Medium)
        }
        
        OutlinedTextField(
            value = configText,
            onValueChange = { configText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            placeholder = { Text("[Interface]\nPrivateKey = ...\nAddress = ...\n\n[Peer]\nPublicKey = ...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onImportConfig(configText) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("import_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp),
            enabled = configText.isNotBlank()
        ) {
            Text("IMPORT AND ADD", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ManualEntryContent(onSaveProfile: (String, String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dns by remember { mutableStateOf("") }
    var peerPublicKey by remember { mutableStateOf("") }
    var peerEndpoint by remember { mutableStateOf("") }
    var peerAllowedIps by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SectionTitle("Interface")
        VpnTextField(value = name, onValueChange = { name = it }, label = "Profile Name (e.g. AlenVpn Server)")
        VpnTextField(value = privateKey, onValueChange = { privateKey = it }, label = "Private Key")
        VpnTextField(value = address, onValueChange = { address = it }, label = "Address")
        VpnTextField(value = dns, onValueChange = { dns = it }, label = "DNS")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SectionTitle("Peer")
        VpnTextField(value = peerPublicKey, onValueChange = { peerPublicKey = it }, label = "Public Key")
        VpnTextField(value = peerEndpoint, onValueChange = { peerEndpoint = it }, label = "Endpoint (IP:Port)")
        VpnTextField(value = peerAllowedIps, onValueChange = { peerAllowedIps = it }, label = "Allowed IPs")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onSaveProfile(name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp),
            enabled = name.isNotBlank() && privateKey.isNotBlank() && peerPublicKey.isNotBlank()
        ) {
            Text("SAVE PROFILE", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun VpnTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
