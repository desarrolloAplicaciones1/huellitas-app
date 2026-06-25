package com.uade.huellitas.presentation.alert.express

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.Urbanist

@Composable
fun ExpressAlertScreen(
    onBack: () -> Unit,
    onPublished: () -> Unit = {},
    viewModel: ExpressAlertViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var barrioError by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraUri = remember { createCameraUri(context) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::onPhotoSelected)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) viewModel.onPhotoSelected(cameraUri)
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Agregar foto", fontFamily = Urbanist) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showPhotoDialog = false; galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Elegir de galería", fontFamily = Urbanist, color = HuellitasTeal) }
                    TextButton(
                        onClick = { showPhotoDialog = false; cameraLauncher.launch(cameraUri) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Sacar foto", fontFamily = Urbanist, color = HuellitasTeal) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancelar", fontFamily = Urbanist)
                }
            }
        )
    }

    LaunchedEffect(uiState) {
        if (uiState is ExpressAlertUiState.Success) {
            viewModel.resetState()
            onPublished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reporte express",
                fontFamily = Urbanist,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height
                    drawLine(HuellitasTeal, Offset(0f, y), Offset(size.width, y), strokeWidth)
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE8F7F6))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = HuellitasTeal,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState is ExpressAlertUiState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (uiState as ExpressAlertUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE8F7F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = HuellitasTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Completa lo que puedas. Cada dato ayuda.",
                        fontFamily = Urbanist,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = HuellitasTeal
                    )
                }
            }

            ExpressLabel("FOTO")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9F9F9))
                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (formState.selectedPhotoUri != null) {
                    AsyncImage(
                        model = formState.selectedPhotoUri,
                        contentDescription = "Foto seleccionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = HuellitasTeal, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Agregar foto (opcional)", fontFamily = Urbanist, fontSize = 13.sp, color = Color(0xFF888888))
                    }
                }
            }
            if (formState.selectedPhotoUri != null) {
                TextButton(onClick = viewModel::clearPhoto) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Text(" Quitar foto", fontFamily = Urbanist, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            ExpressLabel("ESTADO")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertType.entries.forEach { type ->
                    val isSelected = formState.alertType == type
                    val bgColor = when {
                        isSelected && type == AlertType.LOST -> Color(0xFFF43F47)
                        isSelected && type == AlertType.FOUND -> Color(0xFF43A047)
                        else -> Color(0xFFF5F5F5)
                    }
                    Button(
                        onClick = { viewModel.onAlertTypeChange(type) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor = if (isSelected) Color.White else Color(0xFF888888)
                        )
                    ) {
                        Text(
                            text = if (type == AlertType.LOST) "Perdido" else "Encontrado",
                            fontFamily = Urbanist,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            ExpressLabel("NOMBRE DEL ANIMAL")
            OutlinedTextField(
                value = formState.petName,
                onValueChange = viewModel::onPetNameChange,
                placeholder = { Text("Ej: Buddy, Luna...", color = Color.Gray, fontFamily = Urbanist) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuellitasTeal,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )

            ExpressLabel("ESPECIE")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressChip("Perro", formState.petType == PetType.DOG) { viewModel.onPetTypeChange(PetType.DOG) }
                ExpressChip("Gato", formState.petType == PetType.CAT) { viewModel.onPetTypeChange(PetType.CAT) }
            }

            ExpressLabel("TAMANIO")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Chico", "Mediano", "Grande").forEach { size ->
                    ExpressChip(size, formState.size == size) { viewModel.onSizeChange(size) }
                }
            }

            ExpressLabel("DESCRIPCION")
            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = { Text("Cualquier detalle que ayude...", color = Color.Gray, fontFamily = Urbanist) },
                singleLine = false,
                minLines = 4,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuellitasTeal,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )

            ExpressLabel("BARRIO")
            OutlinedTextField(
                value = formState.address,
                onValueChange = {
                    viewModel.onAddressChange(it)
                    barrioError = null
                },
                placeholder = { Text("Ej: Palermo, CABA", color = Color.Gray, fontFamily = Urbanist) },
                singleLine = true,
                isError = barrioError != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuellitasTeal,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )
            if (barrioError != null) {
                Text(barrioError!!, color = Color.Red, fontFamily = Urbanist, fontSize = 12.sp)
            }

            ExpressLabel("CALLE (opcional)")
            OutlinedTextField(
                value = formState.street,
                onValueChange = viewModel::onStreetChange,
                placeholder = { Text("Ej: Av. Santa Fe 3000", color = Color.Gray, fontFamily = Urbanist) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuellitasTeal,
                    unfocusedBorderColor = Color(0xFFDDDDDD)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Button(
                onClick = {
                    barrioError = if (formState.address.isBlank()) "El barrio es obligatorio" else null
                    if (barrioError == null) {
                        viewModel.publishAlert()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .height(52.dp),
                enabled = uiState !is ExpressAlertUiState.Loading,
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
            ) {
                if (uiState is ExpressAlertUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Publicar alerta",
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").also { it.mkdirs() }
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun ExpressLabel(text: String) {
    Text(
        text = text,
        fontFamily = Urbanist,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ExpressChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) HuellitasTeal else Color(0xFFF0F0F0))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontFamily = Urbanist,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (selected) Color.White else Color(0xFF3D3D3D)
        )
    }
}
