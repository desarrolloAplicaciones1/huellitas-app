package com.uade.huellitas.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.StatusFound
import com.uade.huellitas.ui.theme.StatusLost
import com.uade.huellitas.ui.theme.Urbanist

@Composable
fun AlertCard(alert: Alert, onClick: () -> Unit) {
    val timeAgo = remember(alert.createdAt) { alertTimeAgo(alert.createdAt) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (alert.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = alert.photoUrls.first(),
                        contentDescription = "Foto de ${alert.petName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = "Sin foto",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (alert.type == AlertType.LOST) StatusLost else StatusFound)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (alert.type == AlertType.LOST) "PERDIDO" else "ENCONTRADO",
                            color = Color.White,
                            fontFamily = Urbanist,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (alert.status == AlertStatus.RESOLVED) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8F7F6))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "RESUELTO",
                                color = HuellitasTeal,
                                fontFamily = Urbanist,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.petName,
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (!alert.color.isNullOrBlank()) {
                    Text(
                        text = alert.color!!,
                        fontFamily = Urbanist,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!alert.location.address.isNullOrBlank()) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = HuellitasTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = alert.location.address!!,
                            fontFamily = Urbanist,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " - ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = timeAgo,
                        fontFamily = Urbanist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun alertTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        mins < 60 -> "Hace ${mins}min"
        hours < 24 -> "Hace ${hours}h"
        else -> "Hace ${days}d"
    }
}
