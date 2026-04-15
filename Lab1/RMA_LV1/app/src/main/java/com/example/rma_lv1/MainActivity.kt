package com.example.rma_lv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rma_lv1.ui.theme.CustomFont
import com.example.rma_lv1.ui.theme.RMA_LV1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RMA_LV1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserPreview(
                        heightCm = 185,
                        weightKg = 73
                    )
                }
            }
        }
    }
}

@Composable
fun UserPreview(
    heightCm: Int,
    weightKg: Int,
    modifier: Modifier = Modifier
) {
    val heightInMeters = heightCm / 100.0
    val bmi = if (heightInMeters > 0) {
        weightKg / (heightInMeters * heightInMeters)
    } else 0.0

    val bmiStatus = when {
        bmi < 18.5 -> R.string.bmi_low
        bmi < 25.0 -> R.string.bmi_ideal
        else -> R.string.bmi_high
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.pozadinska_slika),
            contentDescription = "Pozadinska slika",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.10f),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profilna_slika),
                contentDescription = "Profilna slika korisnika",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.hello_miljenko),
                    style = TextStyle(
                        fontFamily = CustomFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = stringResource(R.string.your_bmi, bmi),
                    style = TextStyle(
                        fontFamily = CustomFont,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(bmiStatus),
                    style = TextStyle(
                        fontFamily = CustomFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        bmi < 18.5 -> MaterialTheme.colorScheme.error
                        bmi < 25.0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserPreviewPreview() {
    RMA_LV1Theme {
        UserPreview(
            heightCm = 185,
            weightKg = 73
        )
    }
}