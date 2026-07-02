package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MonsterDrink
import com.example.data.model.MonsterFlavor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.graphics.Bitmap
import com.example.ui.components.CustomWeeklyChart
import com.example.ui.theme.DangerRed
import com.example.ui.theme.VibrantBlackBg
import com.example.ui.theme.VibrantCardBg
import com.example.ui.theme.VibrantCardBorder
import com.example.ui.theme.VibrantGreen
import com.example.ui.theme.VibrantTextPrimary
import com.example.ui.theme.VibrantTextSecondary
import com.example.ui.theme.VibrantDarkGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.MonsterUiState
import com.example.ui.viewmodel.MonsterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MonsterTrackerApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MonsterTrackerApp(
    modifier: Modifier = Modifier,
    viewModel: MonsterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showCustomDialog by remember { mutableStateOf(false) }
    var lastLoggedFlavorName by remember { mutableStateOf("") }
    var showLoggedToast by remember { mutableStateOf(false) }

    // Dialog state for details
    var customFlavorName by remember { mutableStateOf("") }
    var customNotes by remember { mutableStateOf("") }
    var customCaffeineMg by remember { mutableStateOf("160") }
    var customVolumeMl by remember { mutableStateOf("500") }
    var customLocation by remember { mutableStateOf("") }
    var customTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var customBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedFlavorId by remember { mutableStateOf("custom") }

    // Camera and gallery launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val source = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    android.graphics.ImageDecoder.createSource(context.contentResolver, it)
                } else {
                    null
                }
                val bitmap = if (source != null) {
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                customBitmap = bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            customBitmap = it
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibrantBlackBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header / Brand banner
            item {
                HeaderSection()
            }

            // Stats row (Cards for current day counters)
            item {
                StatsDashboard(uiState = uiState)
            }

            // Caffeine Limit Gauge and warning banner if over safe limits
            item {
                CaffeineGauge(uiState = uiState)
            }

            // Achievements row
            item {
                AchievementsSection(achievements = uiState.achievements)
            }

            // Weekly history chart (Custom drawn bar chart)
            item {
                CustomWeeklyChart(
                    weeklyData = uiState.weeklyHistory,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Preset Flavors quick logging carousel
            item {
                PresetFlavorsSection(
                    onFlavorLog = { flavor ->
                        selectedFlavorId = flavor.id
                        customFlavorName = flavor.name
                        customCaffeineMg = flavor.caffeineMg.toString()
                        customVolumeMl = flavor.volumeMl.toString()
                        customNotes = flavor.description
                        customLocation = ""
                        customTimestamp = System.currentTimeMillis()
                        // Pre-generate an illustrative bitmap for this flavor automatically!
                        customBitmap = createMonsterMockBitmap(flavor.name, flavor.brandColorHex, flavor.accentColorHex)
                        showCustomDialog = true
                    },
                    onCustomLogClick = {
                        selectedFlavorId = "custom"
                        customFlavorName = ""
                        customNotes = ""
                        customCaffeineMg = "160"
                        customVolumeMl = "500"
                        customLocation = ""
                        customTimestamp = System.currentTimeMillis()
                        customBitmap = null
                        showCustomDialog = true
                    }
                )
            }

            // Timeline header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSUMPTION LOG",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = VibrantGreen
                        )
                    )
                    if (uiState.drinks.isNotEmpty()) {
                        Text(
                            text = "RESET ALL",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DangerRed,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier
                                .clickable {
                                    viewModel.clearHistory()
                                    Toast.makeText(context, "Cleared tracker log history.", Toast.LENGTH_SHORT).show()
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // History Log items list
            if (uiState.drinks.isEmpty()) {
                item {
                    EmptyLogState()
                }
            } else {
                items(uiState.drinks, key = { it.id }) { drink ->
                    HistoryItemCard(
                        drink = drink,
                        onDelete = { viewModel.deleteDrink(drink) }
                    )
                }
            }

            // Bottom spacer for comfortable navigation scrolling
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Custom Add Dialog
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = { showCustomDialog = false },
                containerColor = VibrantCardBg,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = VibrantGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "LOG NEW CAN",
                            color = VibrantTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                text = {
                    val scrollState = androidx.compose.foundation.rememberScrollState()
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(scrollState)
                    ) {
                        // Image attachment / Simulator section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MONSTER CAN PHOTO & AI SCAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantGreen,
                                    letterSpacing = 1.sp
                                )
                            )

                            if (customBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, VibrantGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = customBitmap!!.asImageBitmap(),
                                        contentDescription = "Attached photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Remove button overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f))
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Can Photo Attached",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { customBitmap = null },
                                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(28.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Remove", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                
                                // Analyze button
                                Button(
                                    onClick = {
                                        val bmp = customBitmap
                                        if (bmp != null) {
                                            isAnalyzing = true
                                            scope.launch {
                                                val result = com.example.data.api.GeminiService.analyzeMonsterCan(bmp)
                                                isAnalyzing = false
                                                if (result != null) {
                                                    customFlavorName = result.flavorName
                                                    customCaffeineMg = result.caffeineMg.toString()
                                                    customVolumeMl = result.volumeMl.toString()
                                                    customNotes = result.notes
                                                    Toast.makeText(context, "AI Scan Succeeded! Identified: ${result.flavorName} ⚡", Toast.LENGTH_LONG).show()
                                                } else {
                                                    // Simulation fallback
                                                    delay(1200)
                                                    val identifiedFlavor = when {
                                                        customFlavorName.contains("White", ignoreCase = true) || customFlavorName.contains("Zero", ignoreCase = true) -> MonsterFlavor.PRESETS[1]
                                                        customFlavorName.contains("Mango", ignoreCase = true) -> MonsterFlavor.PRESETS[2]
                                                        customFlavorName.contains("Pipeline", ignoreCase = true) -> MonsterFlavor.PRESETS[3]
                                                        else -> MonsterFlavor.PRESETS[0]
                                                    }
                                                    customFlavorName = identifiedFlavor.name
                                                    customCaffeineMg = identifiedFlavor.caffeineMg.toString()
                                                    customVolumeMl = identifiedFlavor.volumeMl.toString()
                                                    customNotes = "AI Scan: ${identifiedFlavor.description}"
                                                    Toast.makeText(context, "AI Scan Simulated successfully! (Add real Gemini key to secrets for live API calls)", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen.copy(alpha = 0.2f), contentColor = VibrantGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isAnalyzing
                                ) {
                                    if (isAnalyzing) {
                                        Text("AI analyzing can... ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("IDENTIFY & ANALYZE WITH GEMINI AI", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            } else {
                                // Empty state: Select / scan option
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = 1.dp,
                                            color = VibrantGreen.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            cameraLauncher.launch()
                                        }
                                        .background(VibrantGreen.copy(alpha = 0.02f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AddAPhoto,
                                            contentDescription = "Attach image",
                                            tint = VibrantGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Tap to take a photo of the Can",
                                            color = VibrantTextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "OR CHOOSE A SCAN SIMULATOR BELOW:",
                                            color = VibrantGreen.copy(alpha = 0.7f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Quick simulator row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        Triple("Original", "#111111", "#39FF14"),
                                        Triple("Ultra White", "#EFEFEF", "#9CA3AF"),
                                        Triple("Mango Loco", "#0C729E", "#FFA500"),
                                        Triple("Pipeline", "#FF5D73", "#FFE600")
                                    ).forEach { (name, bg, accent) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(android.graphics.Color.parseColor(bg)))
                                                .border(1.dp, Color(android.graphics.Color.parseColor(accent)).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .clickable {
                                                    customFlavorName = name
                                                    customBitmap = createMonsterMockBitmap(name, bg, accent)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name.split(" ").first(),
                                                color = if (bg == "#EFEFEF") Color.Black else Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Flavor Name
                        OutlinedTextField(
                            value = customFlavorName,
                            onValueChange = { customFlavorName = it },
                            label = { Text("Flavor Name (e.g. Pipeline Punch)") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_flavor_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = VibrantTextPrimary,
                                unfocusedTextColor = VibrantTextPrimary,
                                focusedBorderColor = VibrantGreen,
                                unfocusedBorderColor = VibrantCardBorder,
                                focusedLabelColor = VibrantGreen,
                                unfocusedLabelColor = VibrantTextSecondary
                            )
                        )

                        // Date & Time adjusters
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val sdf = remember { SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) }
                            OutlinedTextField(
                                value = sdf.format(Date(customTimestamp)),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date/Time of Consumption") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = null, tint = VibrantGreen)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = VibrantTextPrimary,
                                    unfocusedTextColor = VibrantTextPrimary,
                                    focusedBorderColor = VibrantGreen,
                                    unfocusedBorderColor = VibrantCardBorder,
                                    focusedLabelColor = VibrantGreen,
                                    unfocusedLabelColor = VibrantTextSecondary
                                )
                            )

                            // Quick adjustment chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "-1 Hr" to -3600000L,
                                    "+1 Hr" to 3600000L,
                                    "-1 Day" to -86400000L,
                                    "+1 Day" to 86400000L,
                                    "Now" to 0L
                                ).forEach { (label, offset) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(VibrantCardBorder)
                                            .clickable {
                                                if (label == "Now") {
                                                    customTimestamp = System.currentTimeMillis()
                                                } else {
                                                    customTimestamp += offset
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = label, color = VibrantTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Location Encountered
                        OutlinedTextField(
                            value = customLocation,
                            onValueChange = { customLocation = it },
                            label = { Text("Location Encountered (e.g. Gym, Store)") },
                            placeholder = { Text("Local Gas Station") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Place, contentDescription = null, tint = VibrantGreen)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("custom_location_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = VibrantTextPrimary,
                                unfocusedTextColor = VibrantTextPrimary,
                                focusedBorderColor = VibrantGreen,
                                unfocusedBorderColor = VibrantCardBorder,
                                focusedLabelColor = VibrantGreen,
                                unfocusedLabelColor = VibrantTextSecondary
                            )
                        )

                        // Size and Caffeine row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customCaffeineMg,
                                onValueChange = { customCaffeineMg = it },
                                label = { Text("Caffeine (mg)") },
                                modifier = Modifier.weight(1f).testTag("custom_caffeine_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = VibrantTextPrimary,
                                    unfocusedTextColor = VibrantTextPrimary,
                                    focusedBorderColor = VibrantGreen,
                                    unfocusedBorderColor = VibrantCardBorder,
                                    focusedLabelColor = VibrantGreen,
                                    unfocusedLabelColor = VibrantTextSecondary
                                )
                            )

                            OutlinedTextField(
                                value = customVolumeMl,
                                onValueChange = { customVolumeMl = it },
                                label = { Text("Size (ml)") },
                                modifier = Modifier.weight(1f).testTag("custom_volume_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = VibrantTextPrimary,
                                    unfocusedTextColor = VibrantTextPrimary,
                                    focusedBorderColor = VibrantGreen,
                                    unfocusedBorderColor = VibrantCardBorder,
                                    focusedLabelColor = VibrantGreen,
                                    unfocusedLabelColor = VibrantTextSecondary
                                )
                            )
                        }

                        // Notes
                        OutlinedTextField(
                            value = customNotes,
                            onValueChange = { customNotes = it },
                            label = { Text("Optional Notes") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_notes_input"),
                            placeholder = { Text("Study session, late night drive") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = VibrantTextPrimary,
                                unfocusedTextColor = VibrantTextPrimary,
                                focusedBorderColor = VibrantGreen,
                                unfocusedBorderColor = VibrantCardBorder,
                                focusedLabelColor = VibrantGreen,
                                unfocusedLabelColor = VibrantTextSecondary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = customFlavorName.trim().ifEmpty { "Custom Monster" }
                            val caffeine = customCaffeineMg.toIntOrNull() ?: 160
                            val volume = customVolumeMl.toIntOrNull() ?: 500
                            
                            // Save image if present
                            val finalImagePath = customBitmap?.let { bmp ->
                                try {
                                    saveBitmapToInternalStorage(context, bmp)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            viewModel.logDrink(
                                MonsterFlavor(
                                    id = selectedFlavorId,
                                    name = name,
                                    brandColorHex = "#1A1C18",
                                    accentColorHex = "#99FF00",
                                    caffeineMg = caffeine,
                                    volumeMl = volume,
                                    description = "Custom logged flavor"
                                ),
                                notes = customNotes,
                                location = customLocation,
                                imagePath = finalImagePath,
                                timestamp = customTimestamp
                            )
                            showCustomDialog = false
                            Toast.makeText(context, "Logged $name! ⚡", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen, contentColor = VibrantDarkGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_button")
                    ) {
                        Text("Log Drink", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showCustomDialog = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantTextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(VibrantCardBorder, VibrantCardBorder))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        // Aesthetic Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VibrantGreen.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MONSTER METER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    color = VibrantGreen.copy(alpha = 0.7f),
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Daily Fuel",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = VibrantTextPrimary
                )
            }

            // Profile indicator box with background and border
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VibrantCardBg)
                    .border(1.dp, VibrantCardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = "Profile energy status",
                    tint = VibrantGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun TodayHeroCard(cansToday: Int) {
    val formattedCount = String.format("%02d", cansToday)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp), ambientColor = VibrantGreen.copy(alpha = 0.2f))
            .testTag("today_stat_card"),
        colors = CardDefaults.cardColors(containerColor = VibrantGreen),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedCount,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = VibrantDarkGreen,
                letterSpacing = (-2).sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Text(
                text = "CANS TODAY",
                color = VibrantDarkGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(VibrantDarkGreen.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(VibrantDarkGreen)
                )
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(VibrantDarkGreen.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun StatsDashboard(uiState: MonsterUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Today's Count Hero Card (Vibrant Green)
        TodayHeroCard(cansToday = uiState.cansToday)

        // Remaining Stats in Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Streak Card
            StatCard(
                title = "STREAK",
                value = "${uiState.streakDays}d",
                subtitle = "CONSECUTIVE DAYS",
                icon = Icons.Rounded.LocalFireDepartment,
                accentColor = VibrantGreen,
                modifier = Modifier.weight(1f)
            )

            // Lifetime Card
            StatCard(
                title = "TOTAL CANS",
                value = "${uiState.totalCans}",
                subtitle = "ALL TIME",
                icon = Icons.Rounded.EmojiEvents,
                accentColor = VibrantGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, VibrantCardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = VibrantCardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VibrantTextPrimary
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = VibrantTextSecondary,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 9.sp,
                        color = VibrantTextSecondary.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
fun CaffeineGauge(uiState: MonsterUiState) {
    val progressLimit = 400f
    val currentCaffeine = uiState.caffeineTodayMg.toFloat()
    val progress = (currentCaffeine / progressLimit).coerceIn(0f, 1f)
    
    val gaugeColor by animateColorAsState(
        targetValue = when {
            currentCaffeine > progressLimit -> DangerRed
            currentCaffeine > 300 -> WarningYellow
            else -> VibrantGreen
        },
        label = "gauge_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(VibrantCardBg)
            .border(1.dp, VibrantCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DAILY CAFFEINE INTAKE",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = VibrantGreen
                    )
                )
                Text(
                    text = "Recommended safe limit: 400mg",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VibrantTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
            Text(
                text = "${uiState.caffeineTodayMg} / 400 mg",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = if (uiState.isCaffeineOverLimit) DangerRed else VibrantTextPrimary
                )
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = gaugeColor,
            trackColor = Color.White.copy(alpha = 0.05f)
        )

        // Safety Warnings & Active recommendation banner
        AnimatedVisibility(
            visible = uiState.isCaffeineOverLimit,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DangerRed.copy(alpha = 0.12f))
                    .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.BatteryAlert,
                    contentDescription = "Warning Limit",
                    tint = DangerRed,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "CAFFEINE LIMIT EXCEEDED! Pace yourself, drink plenty of water, and rest your system.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PresetFlavorsSection(
    onFlavorLog: (MonsterFlavor) -> Unit,
    onCustomLogClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "QUICK ADD MONSTERS",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = VibrantGreen
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Preset flavor cards
            items(MonsterFlavor.PRESETS) { flavor ->
                PresetFlavorCard(
                    flavor = flavor,
                    onLogClick = { onFlavorLog(flavor) }
                )
            }

            // Custom log trigger button card
            item {
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(180.dp)
                        .border(1.dp, VibrantGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable { onCustomLogClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantCardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VibrantGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add custom",
                                tint = VibrantGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "CUSTOM CAN",
                            color = VibrantGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log custom flavor & notes",
                            color = VibrantTextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetFlavorCard(
    flavor: MonsterFlavor,
    onLogClick: () -> Unit
) {
    val canColor = Color(android.graphics.Color.parseColor(flavor.brandColorHex))
    val accentColor = Color(android.graphics.Color.parseColor(flavor.accentColorHex))

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .border(1.dp, VibrantCardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VibrantCardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Can-styled header area representing the flavor's colors
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                canColor,
                                canColor.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Small decorative energy logo
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )

                    // Flavor Name
                    Text(
                        text = flavor.name,
                        color = if (flavor.brandColorHex == "#EFEFEF") Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        maxLines = 2,
                        lineHeight = 15.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Details and actions area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${flavor.caffeineMg}mg • ${flavor.volumeMl}ml",
                        color = VibrantTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = flavor.description,
                        color = VibrantTextSecondary,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = onLogClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        text = "+ QUICK ADD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyLogState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(VibrantCardBg)
            .border(1.dp, VibrantCardBorder, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Render generated image beautifully
        Image(
            painter = painterResource(id = R.drawable.img_monster_logo),
            contentDescription = "No drinks logged",
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, VibrantGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "NO DRINKS LOGGED",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                color = VibrantTextPrimary
            )
            Text(
                text = "Tap a preset flavor above or add a custom can to start tracking!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = VibrantTextSecondary,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    drink: MonsterDrink,
    onDelete: () -> Unit
) {
    val flavorPreset = MonsterFlavor.getById(drink.flavorId)
    val accentColor = if (drink.flavorId == "custom") {
        VibrantGreen
    } else {
        Color(android.graphics.Color.parseColor(flavorPreset.accentColorHex))
    }

    val timeFormatted = remember(drink.timestamp) {
        val dateFormat = SimpleDateFormat("EEE hh:mm a", Locale.getDefault())
        dateFormat.format(Date(drink.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, VibrantCardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VibrantCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left visual indicator: Image if present, else fallback cup icon
            if (drink.imagePath != null) {
                AsyncImage(
                    model = drink.imagePath,
                    contentDescription = "Monster Can",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalCafe,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Middle info details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = drink.flavorName,
                        fontWeight = FontWeight.Bold,
                        color = VibrantTextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Volume / details badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${drink.volumeMl}ml",
                            color = VibrantTextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!drink.location.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = "Location",
                            tint = VibrantGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = drink.location!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VibrantTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (drink.notes.isNotEmpty()) {
                    Text(
                        text = "Note: ${drink.notes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VibrantGreen.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "$timeFormatted • ${drink.caffeineMg}mg Caffeine",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VibrantTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            // Delete action button
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = VibrantTextSecondary
                ),
                modifier = Modifier.size(36.dp).testTag("delete_drink_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete entry",
                    modifier = Modifier.size(18.dp),
                    tint = DangerRed.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AchievementsSection(
    achievements: List<com.example.ui.viewmodel.Achievement>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MILESTONES & ACHIEVEMENTS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = VibrantGreen
                )
            )
            val unlockedCount = achievements.count { it.isUnlocked }
            Text(
                text = "$unlockedCount / ${achievements.size} UNLOCKED",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = VibrantGreen
                )
            )
        }

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(achievements) { achievement ->
                val icon = when (achievement.iconName) {
                    "star" -> Icons.Rounded.Star
                    "sports_score" -> Icons.Rounded.EmojiEvents
                    "workspace_premium" -> Icons.Rounded.WorkspacePremium
                    "palette" -> Icons.Rounded.Palette
                    else -> Icons.Rounded.Star
                }

                val cardBg = if (achievement.isUnlocked) {
                    VibrantGreen.copy(alpha = 0.08f)
                } else {
                    VibrantCardBg
                }
                val borderColor = if (achievement.isUnlocked) {
                    VibrantGreen.copy(alpha = 0.4f)
                } else {
                    VibrantCardBorder
                }

                Card(
                    modifier = Modifier
                        .width(170.dp)
                        .height(130.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (achievement.isUnlocked) VibrantGreen else VibrantTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = achievement.progressText,
                                color = if (achievement.isUnlocked) VibrantGreen else VibrantTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = achievement.title,
                                color = if (achievement.isUnlocked) VibrantTextPrimary else VibrantTextSecondary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = achievement.description,
                                color = VibrantTextSecondary,
                                fontSize = 9.sp,
                                lineHeight = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        LinearProgressIndicator(
                            progress = { achievement.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = VibrantGreen,
                            trackColor = VibrantCardBorder
                        )
                    }
                }
            }
        }
    }
}

fun createMonsterMockBitmap(flavorName: String, primaryColorHex: String, accentColorHex: String): Bitmap {
    val bitmap = Bitmap.createBitmap(300, 400, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()
    
    // Background primary color
    paint.color = android.graphics.Color.parseColor(primaryColorHex)
    canvas.drawRect(0f, 0f, 300f, 400f, paint)
    
    // Draw claws / logo details in accent color
    paint.color = android.graphics.Color.parseColor(accentColorHex)
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 14f
    
    // Claw 1
    canvas.drawLine(110f, 120f, 110f, 280f, paint)
    // Claw 2
    canvas.drawLine(150f, 100f, 150f, 300f, paint)
    // Claw 3
    canvas.drawLine(190f, 120f, 190f, 280f, paint)
    
    // Draw text "MONSTER"
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 28f
    paint.isUnderlineText = true
    canvas.drawText("MONSTER", 85f, 340f, paint)
    
    // Draw Flavor name
    paint.textSize = 18f
    paint.isUnderlineText = false
    canvas.drawText(flavorName.uppercase(), 65f, 375f, paint)
    
    return bitmap
}

fun saveBitmapToInternalStorage(context: android.content.Context, bitmap: Bitmap): String {
    val filename = "monster_can_${System.currentTimeMillis()}.jpg"
    val file = java.io.File(context.filesDir, filename)
    java.io.FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}
