package com.uade.huellitas.presentation.alert.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uade.huellitas.domain.model.Alert
import com.uade.huellitas.domain.model.AlertStatus
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.Urbanist
import java.net.URLEncoder

@Composable
fun AlertDetailScreen(
    alertId: String,
    onBack: () -> Unit,
    viewModel: AlertDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val distanceLabel by viewModel.distanceLabel.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(alertId) { viewModel.loadAlert(alertId) }
    LaunchedEffect(uiState) {
        if (uiState is AlertDetailUiState.Deleted) {
            onBack()
        }
    }
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            viewModel.clearSnackbarMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AlertDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HuellitasTeal)
                }
            }
            is AlertDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("Volver") }
                    }
                }
            }
            is AlertDetailUiState.Success -> {
                AlertDetailContent(
                    alert = state.alert,
                    isOwner = state.isOwner,
                    ownerName = state.ownerName,
                    isOnline = isOnline,
                    distanceLabel = distanceLabel,
                    onBack = onBack,
                    onUpdate = viewModel::updateAlert,
                    onSaveNameEdit = viewModel::saveNameEdit,
                    onSaveDescriptionEdit = viewModel::saveDescriptionEdit,
                    onSaveColorEdit = viewModel::saveColorEdit,
                    onResolve = viewModel::resolveAlert,
                    onDelete = viewModel::deleteAlert
                )
            }
            else -> Unit
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun AlertDetailContent(
    alert: Alert,
    isOwner: Boolean,
    ownerName: String? = null,
    isOnline: Boolean,
    distanceLabel: String?,
    onBack: () -> Unit,
    onUpdate: (String, String, String, String, String, String, String, Boolean?) -> Unit,
    onSaveNameEdit: (String) -> Unit,
    onSaveDescriptionEdit: (String) -> Unit,
    onSaveColorEdit: (String) -> Unit,
    onResolve: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isResolved = alert.status == AlertStatus.RESOLVED
    var isEditing         by remember { mutableStateOf(false) }
    var editName          by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.petName) }
    var editBreed         by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.breed ?: "") }
    var editDescription   by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.description) }
    var editColor         by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.color ?: "") }
    var editSize          by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.size ?: "") }
    var editAddress       by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.location.address.orEmpty()) }
    var editContactPhone  by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.contactPhone.orEmpty()) }
    var editHasCollar     by remember(alert.id, alert.updatedAt) { mutableStateOf(alert.hasCollar) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog  by remember { mutableStateOf(false) }

    if (!isResolved && showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Marcar como resuelto?", fontFamily = Urbanist,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("El aviso dejara de aparecer en el feed.", fontFamily = Urbanist,
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showResolveDialog = false; onResolve() }) {
                    Text("Confirmar", color = HuellitasTeal, fontFamily = Urbanist)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Cancelar", fontFamily = Urbanist,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Eliminar aviso?", fontFamily = Urbanist,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("Esta accion no se puede deshacer.", fontFamily = Urbanist,
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Eliminar", color = Color.Red, fontFamily = Urbanist)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", fontFamily = Urbanist,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFEEEEEE))
        ) {
            val photoUrl = alert.photoUrls.firstOrNull()
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de ${alert.petName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pets,
                        contentDescription = null,
                        tint = Color(0xFFBBBBBB),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Back button
            Box(
                modifier = Modifier
                    .padding(16.dp).statusBarsPadding().size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver",
                        tint = Color(0xFF1C1C1C), modifier = Modifier.size(20.dp))
                }
            }

            // Badge PERDIDO/ENCONTRADO
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart).padding(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (alert.type == AlertType.LOST) Color(0xFFF43F47) else Color(0xFF43A047)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (alert.type == AlertType.LOST) "PERDIDO" else "ENCONTRADO",
                    color = Color.White, fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp
                )
            }

            // Distancia
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd).padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(distanceLabel ?: (alert.location.address ?: "Sin ubicacion"), fontFamily = Urbanist,
                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                    color = Color(0xFF1C1C1C))
            }
        }

        if (!isOnline) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sin conexión · No se pueden guardar cambios",
                        fontFamily = Urbanist,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Modo edición — banner visible cuando el usuario está editando
        if (isEditing) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HuellitasTeal.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = HuellitasTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Modo edición activo · Modificá los campos y guardá los cambios",
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = HuellitasTeal
                    )
                }
            }
        }

        // Contenido scrolleable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre + lapiz
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = Urbanist,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuellitasTeal,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { editName = alert.petName }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cancelar"
                                    )
                                }
                                IconButton(onClick = {
                                    onSaveNameEdit(editName)
                                    isEditing = false
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "Guardar"
                                    )
                                }
                            }
                        }
                    )
                } else {
                    Text(
                        text = editName,
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (isOwner && !isResolved && !isEditing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp).clip(CircleShape)
                            .background(Color(0xFFE8F7F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = HuellitasTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Raza
            Text(
                text = buildString {
                    append(when (alert.petType) {
                        PetType.DOG -> "Perro"; PetType.CAT -> "Gato"; else -> "Animal"
                    })
                    if (!alert.breed.isNullOrBlank()) append(" - ${alert.breed}")
                },
                fontFamily = Urbanist, fontWeight = FontWeight.Normal,
                fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Ubicacion
            if (!alert.location.address.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${alert.location.address} - ${timeAgo(alert.createdAt)}",
                        fontFamily = Urbanist, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (ownerName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Publicado por $ownerName",
                            fontFamily = Urbanist, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Grid de datos
            val gridBg = if (MaterialTheme.colorScheme.background == Color(0xFF1C1C1C) ||
                MaterialTheme.colorScheme.background.red < 0.2f)
                Color(0xFF1A3333) else Color(0xFFE8F7F6)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = gridBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // COLOR editable
                        Column(modifier = Modifier.weight(1f)) {
                            Text("COLOR", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                                fontSize = 11.sp, color = HuellitasTeal, letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isEditing) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BasicInlineField(
                                        value = editColor,
                                        onValueChange = { editColor = it },
                                        placeholder = "Ej. Dorado",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { editColor = alert.color ?: "" },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Cancelar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onSaveColorEdit(editColor)
                                            isEditing = false
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            tint = MaterialTheme.colorScheme.primary,
                                            contentDescription = "Guardar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    editColor.ifBlank { "-" }, fontFamily = Urbanist,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("COLLAR", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                                fontSize = 11.sp, color = HuellitasTeal, letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isEditing) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SelectablePill(
                                        label = "Sí",
                                        selected = editHasCollar == true,
                                        onClick = { editHasCollar = true }
                                    )
                                    SelectablePill(
                                        label = "No",
                                        selected = editHasCollar == false,
                                        onClick = { editHasCollar = false }
                                    )
                                }
                            } else {
                                Text(
                                    text = when (editHasCollar) {
                                        true -> "Sí"
                                        false -> "No"
                                        null -> "-"
                                    },
                                    fontFamily = Urbanist,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailCell(
                            "ESPECIE",
                            when (alert.petType) {
                                PetType.DOG -> "Perro"; PetType.CAT -> "Gato"; else -> "Otro"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ESTADO", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                                fontSize = 11.sp, color = HuellitasTeal, letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isOwner && !isResolved) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = true,
                                        onClick = {},
                                        label = {
                                            Text(
                                                "Activo", fontFamily = Urbanist,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = HuellitasTeal.copy(alpha = 0.15f),
                                            selectedLabelColor = HuellitasTeal
                                        )
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = { showResolveDialog = true },
                                        label = {
                                            Text(
                                                "Resuelto", fontFamily = Urbanist,
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    if (isResolved) "Resuelto" else "Activo",
                                    fontFamily = Urbanist, fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("RAZA", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                                fontSize = 11.sp, color = HuellitasTeal, letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isEditing) {
                                BasicInlineField(
                                    value = editBreed,
                                    onValueChange = { editBreed = it },
                                    placeholder = "Ej. Mestizo"
                                )
                            } else {
                                Text(
                                    text = editBreed.ifBlank { "-" },
                                    fontFamily = Urbanist,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TAMAÑO", fontFamily = Urbanist, fontWeight = FontWeight.Bold,
                                fontSize = 11.sp, color = HuellitasTeal, letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isEditing) {
                                BasicInlineField(
                                    value = editSize,
                                    onValueChange = { editSize = it },
                                    placeholder = "Ej. Mediano"
                                )
                            } else {
                                Text(
                                    text = editSize.ifBlank { "-" },
                                    fontFamily = Urbanist,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "CONTACTO Y ZONA",
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.drawBehind {
                        val sw = 2.dp.toPx()
                        drawLine(HuellitasTeal, Offset(0f, this.size.height),
                            Offset(this.size.width, this.size.height), sw)
                    }
                )
                if (isEditing) {
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        placeholder = {
                            Text("Ej. Palermo, CABA",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = Urbanist)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuellitasTeal,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    OutlinedTextField(
                        value = editContactPhone,
                        onValueChange = { editContactPhone = it },
                        placeholder = {
                            Text("Ej. +54 11 1234-5678",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = Urbanist)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuellitasTeal,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                } else {
                    DetailInfoRow(
                        label = "Ubicación",
                        value = alert.location.address ?: "Sin ubicación cargada"
                    )
                    DetailInfoRow(
                        label = "Teléfono",
                        value = alert.contactPhone ?: "Sin teléfono de contacto"
                    )
                }
            }

            // Descripcion editable inline
            Column {
                Text(
                    "DESCRIPCION",
                    fontFamily = Urbanist, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.drawBehind {
                        val sw = 2.dp.toPx()
                        drawLine(HuellitasTeal, Offset(0f, this.size.height),
                            Offset(this.size.width, this.size.height), sw)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        placeholder = {
                            Text("Cualquier detalle que ayude...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = Urbanist)
                        },
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuellitasTeal,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { editDescription = alert.description }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                        IconButton(onClick = {
                            onSaveDescriptionEdit(editDescription)
                            isEditing = false
                        }) {
                            Icon(
                                Icons.Default.Check,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Guardar"
                            )
                        }
                    }
                } else {
                    Text(
                        text = editDescription,
                        fontFamily = Urbanist, fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Boton guardar solo en modo edicion
            if (isEditing) {
                Button(
                    onClick = {
                        onUpdate(
                            editName,
                            editBreed,
                            editColor,
                            editSize,
                            editAddress,
                            editContactPhone,
                            editDescription,
                            editHasCollar
                        )
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios", fontFamily = Urbanist,
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                }
            }

            // Botones normales ocultos en modo edicion
            if (!isEditing) {
                // Contactar por WhatsApp
                Button(
                    onClick = {
                        val phone = alert.contactPhone?.replace(Regex("[^0-9]"), "").orEmpty()
                        val msg = "Hola! Vi tu aviso en Huellitas sobre ${alert.petName}. Quiero mas info."
                        val url = "https://wa.me/$phone?text=${URLEncoder.encode(msg, "UTF-8")}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !alert.contactPhone.isNullOrBlank(),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null,
                        modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (alert.contactPhone.isNullOrBlank()) "Sin telefono de contacto" else "Contactar por WhatsApp", fontFamily = Urbanist,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                }

                // Eliminar — solo para el dueño del aviso
                if (isOwner) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isResolved) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE8F7F6),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HuellitasTeal.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ya resuelto",
                                    fontFamily = Urbanist,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HuellitasTeal
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Eliminar", fontFamily = Urbanist,
                            fontWeight = FontWeight.Normal, color = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BasicInlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, HuellitasTeal.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontFamily = Urbanist, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = Urbanist, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) HuellitasTeal else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontFamily = Urbanist,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            fontFamily = Urbanist,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = HuellitasTeal,
            letterSpacing = 0.8.sp
        )
        Text(
            text = value,
            fontFamily = Urbanist,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontFamily = Urbanist, fontWeight = FontWeight.Bold,
            fontSize = 11.sp, color = HuellitasTeal, letterSpacing = 0.8.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontFamily = Urbanist, fontWeight = FontWeight.Normal,
            fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

private fun timeAgo(timestamp: Long): String {
    val diff  = System.currentTimeMillis() - timestamp
    val mins  = diff / 60_000
    val hours = diff / 3_600_000
    val days  = diff / 86_400_000
    return when {
        mins  < 60 -> "Hace ${mins}min"
        hours < 24 -> "Hace ${hours}h"
        else       -> "Hace ${days}d"
    }
}
