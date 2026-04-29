package com.example.predlozak_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.predlozak_1.ui.theme.Predlozak_1Theme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Predlozak_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserPreview(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UserPreview(modifier: Modifier = Modifier) {
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var heightCm by remember { mutableStateOf(0) }
    var weightKg by remember { mutableStateOf(0) }

    var newWeightInput by remember { mutableStateOf("") }
    var newHeightInput by remember { mutableStateOf("") }

    var heightError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var progress by remember { mutableStateOf(0f) }
    var newBmiText by remember { mutableStateOf("") }
    var showProgress by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val document = db.collection("bmidata").document("main").get().await()
            if (document.exists()) {
                heightCm = document.getLong("heightCm")?.toInt() ?: 191
                weightKg = document.getLong("weightKg")?.toInt() ?: 100
            }
        } catch (_: Exception) {}
    }

    val heightMeters = heightCm / 100f
    val currentBmi = if (heightMeters > 0) weightKg / (heightMeters * heightMeters) else 0f

    val bmiStatus = when {
        currentBmi < 18.5 -> "Prenizak BMI"
        currentBmi in 18.5..24.9 -> "Idealan BMI"
        else -> "Previsok BMI"
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.1f,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.profile_pic),
                    contentDescription = "Profil",
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Pozdrav, Miljenko", fontSize = 18.sp)
                    Text(bmiStatus, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Visina (cm):")
            TextField(
                value = newHeightInput,
                onValueChange = {
                    newHeightInput = it
                    heightError = false
                    saveMessage = ""
                },
                isError = heightError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (heightError) {
                Text("Unesite broj za visinu", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Težina (kg):")
            TextField(
                value = newWeightInput,
                onValueChange = {
                    newWeightInput = it
                    weightError = false
                    saveMessage = ""
                },
                isError = weightError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (weightError) {
                Text("Unesite broj za težinu", color = Color.Red)
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val newH = newHeightInput.toIntOrNull()
                val newW = newWeightInput.toIntOrNull()

                if (newH == null || newW == null) {
                    heightError = newH == null
                    weightError = newW == null
                    errorMessage = "Unesite ispravne vrijednosti!"
                    saveMessage = ""
                    return@Button
                }

                scope.launch {
                    try {
                        val newBmi = newW / ((newH / 100f) * (newH / 100f))

                        val data = hashMapOf(
                            "heightCm" to newH,
                            "weightKg" to newW,
                            "bmi" to newBmi
                        )

                        db.collection("bmidata").document("main").set(data).await()

                        heightCm = newH
                        weightKg = newW

                        saveMessage = "Podaci uspješno spremljeni!"
                        errorMessage = ""
                    } catch (e: Exception) {
                        saveMessage = "Greška pri spremanju!"
                    }
                }
            }) {
                Text("Spremi u Firestore")
            }

            if (saveMessage.isNotEmpty()) {
                Text(
                    saveMessage,
                    color = if (saveMessage.contains("uspješno")) Color.Green else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                val newH = newHeightInput.toIntOrNull()
                val newW = newWeightInput.toIntOrNull()

                if (newH == null || newW == null) {
                    heightError = newH == null
                    weightError = newW == null
                    errorMessage = "Unesite ispravne vrijednosti!"
                    saveMessage = ""
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    delay(1000)

                    val newBmi = newW / ((newH / 100f) * (newH / 100f))
                    val idealBmi = 21.7f

                    val progressValue = when {
                        newBmi > currentBmi -> 0f
                        newBmi <= idealBmi -> 1f
                        else -> ((currentBmi - newBmi) / (currentBmi - idealBmi)).coerceIn(0f, 1f)
                    }

                    progress = progressValue
                    newBmiText = "Novi BMI: %.1f – Napredak: %.0f%%".format(newBmi, progressValue * 100)
                    showProgress = true
                    isLoading = false
                    errorMessage = ""
                }
            }) {
                Text("Izračunaj napredak")
            }

            if (showProgress) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(newBmiText)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}