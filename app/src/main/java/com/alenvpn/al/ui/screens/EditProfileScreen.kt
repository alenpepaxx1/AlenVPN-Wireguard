// Copyright Alen Pepa

package com.alenvpn.al.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alenvpn.al.data.WireguardProfile
import com.alenvpn.al.ui.theme.*

import com.alenvpn.al.ui.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: WireguardProfile,
    onNavigateBack: () -> Unit,
    onUpdateProfile: (Int, String, String, String, String, String, String, String, Int, Int, Boolean, Boolean, String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var privateKey by remember { mutableStateOf(profile.privateKey) }
    var address by remember { mutableStateOf(profile.address) }
    var dns by remember { mutableStateOf(profile.dns) }
    var peerPublicKey by remember { mutableStateOf(profile.peerPublicKey) }
    var peerEndpoint by remember { mutableStateOf(profile.peerEndpoint) }
    var peerAllowedIps by remember { mutableStateOf(profile.peerAllowedIps) }
    var persistentKeepalive by remember { mutableStateOf(profile.persistentKeepalive.toString()) }
    var defaultMtu by remember { mutableStateOf(profile.defaultMtu.toString()) }
    var killSwitch by remember { mutableStateOf(profile.killSwitch) }
    var autoConnect by remember { mutableStateOf(profile.autoConnect) }
    var rawConfig by remember { mutableStateOf(profile.rawConfig) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Edit Profile"), fontWeight = FontWeight.Medium) },
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            SectionTitle("Interface")
            VpnTextField(value = name, onValueChange = { name = it }, label = "Profile Name")
            VpnTextField(value = privateKey, onValueChange = { privateKey = it }, label = "Private Key")
            VpnTextField(value = address, onValueChange = { address = it }, label = "Address")
            VpnTextField(value = dns, onValueChange = { dns = it }, label = "DNS")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionTitle("Peer")
            VpnTextField(value = peerPublicKey, onValueChange = { peerPublicKey = it }, label = "Peer Public Key")
            VpnTextField(value = peerEndpoint, onValueChange = { peerEndpoint = it }, label = "Endpoint (IP:Port)")
            VpnTextField(value = peerAllowedIps, onValueChange = { peerAllowedIps = it }, label = "Allowed IPs")

            VpnTextField(value = persistentKeepalive, onValueChange = { persistentKeepalive = it }, label = "Persistent Keepalive")
            VpnTextField(value = defaultMtu, onValueChange = { defaultMtu = it }, label = "Default MTU")
            
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kill Switch")
                Switch(checked = killSwitch, onCheckedChange = { killSwitch = it })
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Auto Connect Profile")
                Switch(checked = autoConnect, onCheckedChange = { autoConnect = it })
            }

            if (rawConfig.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Raw Config (Advanced)")
                OutlinedTextField(
                    value = rawConfig,
                    onValueChange = { rawConfig = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    onUpdateProfile(
                        profile.id, name, privateKey, address, dns, peerPublicKey, peerEndpoint, peerAllowedIps,
                        persistentKeepalive.toIntOrNull() ?: 25,
                        defaultMtu.toIntOrNull() ?: 1280,
                        killSwitch, autoConnect, rawConfig
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                enabled = name.isNotBlank() && (privateKey.isNotBlank() || rawConfig.isNotBlank())
            ) {
                Text("SAVE CHANGES", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
