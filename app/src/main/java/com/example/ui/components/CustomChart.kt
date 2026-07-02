package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VibrantCardBorder
import com.example.ui.theme.VibrantGreen
import com.example.ui.theme.VibrantTextSecondary
import com.example.ui.viewmodel.WeeklyBarData

@Composable
fun CustomWeeklyChart(
    weeklyData: List<WeeklyBarData>,
    modifier: Modifier = Modifier
) {
    var animateChart by remember { mutableStateOf(false) }

    LaunchedEffect(weeklyData) {
        animateChart = true
    }

    val maxCount = weeklyData.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val chartHeight = 120.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, VibrantCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEEKLY ACTIVITY",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = VibrantGreen
                )
            )
            val weeklyTotal = weeklyData.sumOf { it.count }
            Text(
                text = "$weeklyTotal CANS TOTAL",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = VibrantTextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight + 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEach { data ->
                val targetHeight = if (data.count == 0) {
                    8.dp
                } else {
                    val ratio = data.count.toFloat() / maxCount.toFloat()
                    chartHeight * ratio
                }

                val animatedHeight by animateDpAsState(
                    targetValue = if (animateChart) targetHeight else 0.dp,
                    animationSpec = tween(durationMillis = 800),
                    label = "bar_height"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Bar count label
                    if (data.count > 0) {
                        Text(
                            text = data.count.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = VibrantGreen,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Bar capsule
                    Box(
                        modifier = Modifier
                            .height(chartHeight)
                            .width(14.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(7.dp)
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val barBrush = if (data.count > 0) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    VibrantGreen,
                                    VibrantGreen.copy(alpha = 0.4f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.1f),
                                    Color.Gray.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(animatedHeight)
                                .width(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(barBrush)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day label
                    Text(
                        text = data.dayName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (data.count > 0) VibrantGreen else VibrantTextSecondary,
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
