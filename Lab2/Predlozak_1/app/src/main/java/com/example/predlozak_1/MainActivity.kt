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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Predlozak_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserPreview(191, 100, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UserPreview(heightCm: Int, weightKg: Int, modifier: Modifier = Modifier) {
    val heightMeters = heightCm / 100f
    val initialBmi = weightKg / (heightMeters * heightMeters)
    val bmiStatus = when {
        initialBmi < 18.5 -> "Prenizak BMI"
        initialBmi in 18.5..24.9 -> "Idealan BMI"
        else -> "Previsok BMI"
    }

    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var newWeightText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var newBmiText by remember { mutableStateOf("") }
    var showProgress by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var rezultat by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fitness),
            contentDescription = "Pozadinska slika",
            contentScale = ContentScale.Crop,
            alpha = 0.1f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_pic),
                    contentDescription = "Profilna slika",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Pozdrav, Miljenko", fontSize = 18.sp)
                    Text(text = bmiStatus, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Unesi novu težinu (kg):", fontSize = 16.sp)
            TextField(
                value = newWeightText,
                onValueChange = { newWeightText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                errorMessage = ""
                val newTezina = newWeightText.toFloatOrNull()
                if (newTezina == null) {
                    errorMessage = "Neispravan unos težine!"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    delay(1000L)

                    val newBmi = newTezina / (heightMeters * heightMeters)
                    val idealBmi = 21.7f

                    val progressValue = when {
                        newBmi > initialBmi -> 0f
                        newBmi <= idealBmi -> 1f
                        else -> ((initialBmi - newBmi) / (initialBmi - idealBmi)).coerceIn(0f, 1f)
                    }

                    progress = progressValue
                    newBmiText = "Novi BMI: %.1f – Napredak: %.0f%%".format(newBmi, progressValue * 100)
                    showProgress = true
                    isLoading = false
                }
            }) {
                Text("Izračunaj napredak prema idealnom BMI-ju")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red)
            } else if (showProgress) {
                Text(text = newBmiText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                val newTezina = newWeightText.toFloatOrNull()
                if (newTezina == null) {
                    errorMessage = "Neispravan unos za Firebase!"
                    return@Button
                }

                val docRef = db.collection("BMI").document("cZJRliv3334331y5Aeng")
                docRef.update("Tezina", newTezina)
                    .addOnSuccessListener {
                        rezultat = "Težina uspješno ažurirana u Firebase!"
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Error updating Tezina: $e")
                        rezultat = "Greška: ${e.message}"
                    }
            }) {
                Text("Unesi Tezinu u Firebase")
            }

            if (rezultat.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rezultat,
                    color = if (rezultat.contains("uspješno")) Color.Green else Color.Red
                )
            }
        }
    }
}