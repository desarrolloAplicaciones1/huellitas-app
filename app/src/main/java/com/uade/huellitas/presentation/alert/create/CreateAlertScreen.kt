package com.uade.huellitas.presentation.alert.create

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uade.huellitas.domain.model.AlertType
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.StatusFound
import com.uade.huellitas.ui.theme.Urbanist
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertScreen(
    onBack: () -> Unit,
    onAlertCreated: () -> Unit,
    viewModel: CreateAlertViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var barrioError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var showAbandonDialog by remember { mutableStateOf(false) }
    var barrioExpanded by remember { mutableStateOf(false) }
    val barrioSuggestions = remember(formState.address) {
        if (formState.address.isBlank()) emptyList()
        else BARRIOS_CABA.filter { it.contains(formState.address.trim(), ignoreCase = true) }.take(5)
    }

    val isFormDirty = formState.petName.isNotBlank()
        || formState.description.isNotBlank()
        || formState.address.isNotBlank()
        || formState.selectedPhotoUri != null

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            imagePickerLauncher.launch("image/*")
        } else {
            scope.launch { snackbarHostState.showSnackbar("Necesitamos acceso a tus fotos para agregar una imagen") }
        }
    }

    val openGallery = {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            imagePickerLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    if (showAbandonDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("¿Salir sin publicar?", fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            },
            text = {
                Text("Perderás los datos ingresados. Esta acción no se puede deshacer.",
                    fontFamily = Urbanist, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAbandonDialog = false; onBack() }) {
                    Text("Salir", color = Color.Red, fontFamily = Urbanist)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAbandonDialog = false }) {
                    Text("Continuar editando", fontFamily = Urbanist,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is CreateAlertUiState.Success -> {
                viewModel.resetState()
                onAlertCreated()
            }
            is CreateAlertUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "Reportar mascota",
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .drawBehind {
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
                        .clickable(onClick = { if (isFormDirty) showAbandonDialog = true else onBack() }),
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
                SectionLabel("FOTO")
                PhotoPickerBox(
                    imageUri = formState.selectedPhotoUri,
                    onClick = openGallery
                )

                SectionLabel("ESTADO")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertType.entries.forEach { type ->
                        val isSelected = formState.alertType == type
                        val bgColor = when {
                            isSelected && type == AlertType.LOST -> Color(0xFFF43F47)
                            isSelected && type == AlertType.FOUND -> StatusFound
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

                SectionLabel("NOMBRE DEL ANIMAL *")
                OutlinedTextField(
                    value = formState.petName,
                    onValueChange = { viewModel.onPetNameChange(it); nameError = null },
                    placeholder = { Text("Ej. Buddy, Luna...", color = Color.Gray, fontFamily = Urbanist) },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuellitasTeal,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )
                if (nameError != null) {
                    Text(nameError!!, color = Color.Red, fontFamily = Urbanist, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel("ESPECIE")
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallChip("Perro", formState.petType == PetType.DOG) {
                                viewModel.onPetTypeChange(PetType.DOG)
                            }
                            SmallChip("Gato", formState.petType == PetType.CAT) {
                                viewModel.onPetTypeChange(PetType.CAT)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel("CASTRADO")
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallChip("No", !formState.isCastrated) { viewModel.onIsCastratedChange(false) }
                            SmallChip("Si", formState.isCastrated) { viewModel.onIsCastratedChange(true) }
                        }
                    }
                }

                SectionLabel("TAMANIO")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Chico", "Mediano", "Grande").forEach { size ->
                        SmallChip(size, formState.size == size) { viewModel.onSizeChange(size) }
                    }
                }

                SectionLabel("COLLAR")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallChip("No", !formState.hasCollar) { viewModel.onHasCollarChange(false) }
                    SmallChip("Si", formState.hasCollar) { viewModel.onHasCollarChange(true) }
                }

                SectionLabel("DESCRIPCION *")
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onDescriptionChange(it); descriptionError = null },
                    placeholder = { Text("Cualquier detalle que ayude...", color = Color.Gray, fontFamily = Urbanist) },
                    singleLine = false,
                    minLines = 4,
                    maxLines = 6,
                    isError = descriptionError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuellitasTeal,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )
                if (descriptionError != null) {
                    Text(descriptionError!!, color = Color.Red, fontFamily = Urbanist, fontSize = 12.sp)
                }

                SectionLabel("BARRIO *")
                ExposedDropdownMenuBox(
                    expanded = barrioExpanded && barrioSuggestions.isNotEmpty(),
                    onExpandedChange = { barrioExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.address,
                        onValueChange = {
                            viewModel.onAddressTyped(it)
                            barrioError = null
                            barrioExpanded = true
                        },
                        placeholder = { Text("Ej. Palermo, CABA", color = Color.Gray, fontFamily = Urbanist) },
                        singleLine = true,
                        isError = barrioError != null,
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        shape = RoundedCornerShape(3.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuellitasTeal,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = barrioExpanded && barrioSuggestions.isNotEmpty(),
                        onDismissRequest = { barrioExpanded = false }
                    ) {
                        barrioSuggestions.forEach { barrio ->
                            DropdownMenuItem(
                                text = { Text(barrio, fontFamily = Urbanist) },
                                onClick = {
                                    val coords = BARRIOS_CABA_COORDS[barrio]
                                    if (coords != null) {
                                        viewModel.onLocationChange(coords.first, coords.second, barrio)
                                    } else {
                                        viewModel.onLocationChange(0.0, 0.0, barrio)
                                    }
                                    barrioExpanded = false
                                    barrioError = null
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                if (barrioError != null) {
                    Text(barrioError!!, color = Color.Red, fontFamily = Urbanist, fontSize = 12.sp)
                }

                SectionLabel("CALLE (opcional)")
                OutlinedTextField(
                    value = formState.street,
                    onValueChange = viewModel::onStreetChange,
                    placeholder = { Text("Ej. Av. Santa Fe 3000", color = Color.Gray, fontFamily = Urbanist) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuellitasTeal,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )

                SectionLabel("COLOR")
                OutlinedTextField(
                    value = formState.color,
                    onValueChange = viewModel::onColorChange,
                    placeholder = { Text("Ej. Tricolor", color = Color.Gray, fontFamily = Urbanist) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuellitasTeal,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )

                SectionLabel("TELEFONO DE CONTACTO *")
                OutlinedTextField(
                    value = formState.contactPhone,
                    onValueChange = { viewModel.onContactPhoneChange(it); phoneError = null },
                    placeholder = { Text("Ej. +54 11 1234-5678", color = Color.Gray, fontFamily = Urbanist) },
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuellitasTeal,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )
                if (phoneError != null) {
                    Text(phoneError!!, color = Color.Red, fontFamily = Urbanist, fontSize = 12.sp)
                } else if (formState.contactPhone.isNotBlank()) {
                    Text(
                        "Usamos tu telefono de perfil por defecto, pero podes cambiarlo para este aviso.",
                        color = Color(0xFF888888),
                        fontFamily = Urbanist,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = {
                        nameError = if (formState.petName.isBlank()) "El nombre es obligatorio" else null
                        descriptionError = if (formState.description.isBlank()) "La descripcion es obligatoria" else null
                        barrioError = if (formState.address.isBlank()) "El barrio es obligatorio" else null
                        phoneError = if (formState.contactPhone.isBlank()) "El telefono es obligatorio" else null
                        if (nameError == null && descriptionError == null && barrioError == null && phoneError == null) {
                            viewModel.submitAlert()
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Completá todos los campos obligatorios") }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .height(52.dp),
                    enabled = uiState !is CreateAlertUiState.Loading && uiState !is CreateAlertUiState.UploadingPhoto,
                    shape = RoundedCornerShape(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal)
                ) {
                    when (uiState) {
                        is CreateAlertUiState.UploadingPhoto -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                "Subiendo foto...",
                                fontFamily = Urbanist,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        is CreateAlertUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            Text(
                                "Publicar",
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

private val BARRIOS_CABA_COORDS = mapOf(
    "Agronomía, CABA" to (-34.607 to -58.489),
    "Almagro, CABA" to (-34.614 to -58.428),
    "Balvanera, CABA" to (-34.614 to -58.401),
    "Barracas, CABA" to (-34.641 to -58.382),
    "Belgrano, CABA" to (-34.563 to -58.458),
    "Boedo, CABA" to (-34.630 to -58.420),
    "Caballito, CABA" to (-34.620 to -58.443),
    "Chacarita, CABA" to (-34.587 to -58.455),
    "Coghlan, CABA" to (-34.556 to -58.470),
    "Colegiales, CABA" to (-34.574 to -58.448),
    "Constitución, CABA" to (-34.628 to -58.382),
    "Flores, CABA" to (-34.630 to -58.468),
    "Floresta, CABA" to (-34.630 to -58.490),
    "La Boca, CABA" to (-34.636 to -58.363),
    "La Paternal, CABA" to (-34.601 to -58.472),
    "Liniers, CABA" to (-34.638 to -58.521),
    "Mataderos, CABA" to (-34.654 to -58.516),
    "Monte Castro, CABA" to (-34.624 to -58.503),
    "Montserrat, CABA" to (-34.617 to -58.377),
    "Nueva Pompeya, CABA" to (-34.651 to -58.411),
    "Núñez, CABA" to (-34.546 to -58.462),
    "Palermo, CABA" to (-34.585 to -58.433),
    "Parque Avellaneda, CABA" to (-34.647 to -58.480),
    "Parque Chacabuco, CABA" to (-34.637 to -58.440),
    "Parque Chas, CABA" to (-34.573 to -58.477),
    "Parque Patricios, CABA" to (-34.638 to -58.399),
    "Puerto Madero, CABA" to (-34.609 to -58.361),
    "Recoleta, CABA" to (-34.589 to -58.395),
    "Retiro, CABA" to (-34.592 to -58.374),
    "Saavedra, CABA" to (-34.552 to -58.485),
    "San Cristóbal, CABA" to (-34.626 to -58.405),
    "San Nicolás, CABA" to (-34.603 to -58.376),
    "San Telmo, CABA" to (-34.622 to -58.370),
    "Vélez Sársfield, CABA" to (-34.638 to -58.505),
    "Versalles, CABA" to (-34.634 to -58.508),
    "Villa Crespo, CABA" to (-34.599 to -58.449),
    "Villa del Parque, CABA" to (-34.608 to -58.482),
    "Villa Devoto, CABA" to (-34.605 to -58.502),
    "Villa General Mitre, CABA" to (-34.617 to -58.475),
    "Villa Lugano, CABA" to (-34.668 to -58.477),
    "Villa Luro, CABA" to (-34.641 to -58.492),
    "Villa Ortúzar, CABA" to (-34.575 to -58.469),
    "Villa Pueyrredón, CABA" to (-34.586 to -58.499),
    "Villa Real, CABA" to (-34.637 to -58.506),
    "Villa Riachuelo, CABA" to (-34.669 to -58.460),
    "Villa Santa Rita, CABA" to (-34.626 to -58.488),
    "Villa Soldati, CABA" to (-34.660 to -58.445),
    "Villa Urquiza, CABA" to (-34.573 to -58.485)
)

private val BARRIOS_CABA = listOf(
    "Agronomía, CABA", "Almagro, CABA", "Balvanera, CABA", "Barracas, CABA",
    "Belgrano, CABA", "Boedo, CABA", "Caballito, CABA", "Chacarita, CABA",
    "Coghlan, CABA", "Colegiales, CABA", "Constitución, CABA", "Flores, CABA",
    "Floresta, CABA", "La Boca, CABA", "La Paternal, CABA", "Liniers, CABA",
    "Mataderos, CABA", "Monte Castro, CABA", "Montserrat, CABA", "Nueva Pompeya, CABA",
    "Núñez, CABA", "Palermo, CABA", "Parque Avellaneda, CABA", "Parque Chacabuco, CABA",
    "Parque Chas, CABA", "Parque Patricios, CABA", "Puerto Madero, CABA", "Recoleta, CABA",
    "Retiro, CABA", "Saavedra, CABA", "San Cristóbal, CABA", "San Nicolás, CABA",
    "San Telmo, CABA", "Vélez Sársfield, CABA", "Versalles, CABA", "Villa Crespo, CABA",
    "Villa del Parque, CABA", "Villa Devoto, CABA", "Villa General Mitre, CABA",
    "Villa Lugano, CABA", "Villa Luro, CABA", "Villa Ortúzar, CABA",
    "Villa Pueyrredón, CABA", "Villa Real, CABA", "Villa Riachuelo, CABA",
    "Villa Santa Rita, CABA", "Villa Soldati, CABA", "Villa Urquiza, CABA"
)

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = Urbanist,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SmallChip(label: String, selected: Boolean, onClick: () -> Unit) {
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

@Composable
private fun PhotoPickerBox(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF9F9F9))
            .border(width = 1.dp, color = Color(0xFFDDDDDD), shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Foto seleccionada",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = HuellitasTeal,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Toca para sacar una foto", fontFamily = Urbanist, fontSize = 14.sp, color = Color(0xFF3D3D3D))
                Text("o elegir desde galeria", fontFamily = Urbanist, fontSize = 12.sp, color = Color(0xFF888888))
            }
        }
    }
}
