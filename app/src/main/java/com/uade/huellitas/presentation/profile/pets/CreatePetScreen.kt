package com.uade.huellitas.presentation.profile.pets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uade.huellitas.domain.model.PetType
import com.uade.huellitas.ui.theme.HuellitasTeal
import com.uade.huellitas.ui.theme.HuellitasTealSurface
import com.uade.huellitas.ui.theme.Urbanist

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreatePetScreen(
    onBack: () -> Unit,
    onPetCreated: () -> Unit,
    viewModel: CreatePetViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::onPhotoSelected)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreatePetUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            CreatePetUiState.Success -> onPetCreated()
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva mascota",
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Registra a tu mascota para reutilizar sus datos cuando publiques avisos.",
                fontFamily = Urbanist,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = HuellitasTealSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { photoPicker.launch("image/*") }
            ) {
                if (formState.selectedPhotoUri != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = formState.selectedPhotoUri,
                            contentDescription = "Foto de mascota",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        )
                        Text(
                            text = "Toca la imagen para cambiarla",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            fontFamily = Urbanist,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(HuellitasTealSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = HuellitasTeal,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Agregar foto",
                                fontFamily = Urbanist,
                                fontWeight = FontWeight.SemiBold,
                                color = HuellitasTeal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Abrir galeria",
                                fontFamily = Urbanist,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (formState.selectedPhotoUri != null) {
                TextButton(onClick = viewModel::clearSelectedPhoto) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Quitar foto",
                        fontFamily = Urbanist,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nombre", fontFamily = Urbanist) }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Especie",
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    petTypeOptions.forEach { option ->
                        FilterChip(
                            selected = formState.petType == option.type,
                            onClick = { viewModel.onPetTypeChange(option.type) },
                            label = { Text(option.label, fontFamily = Urbanist) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = formState.breed,
                onValueChange = viewModel::onBreedChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Raza", fontFamily = Urbanist) }
            )

            OutlinedTextField(
                value = formState.color,
                onValueChange = viewModel::onColorChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Color", fontFamily = Urbanist) }
            )

            OutlinedTextField(
                value = formState.microchipId,
                onValueChange = viewModel::onMicrochipChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Microchip", fontFamily = Urbanist) }
            )

            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text("Descripcion", fontFamily = Urbanist) }
            )

            Button(
                onClick = viewModel::savePet,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = uiState !is CreatePetUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = HuellitasTeal),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState is CreatePetUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar mascota",
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Podras eliminarla luego desde Mis mascotas.",
                modifier = Modifier.fillMaxWidth(),
                fontFamily = Urbanist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class PetTypeOption(
    val type: PetType,
    val label: String
)

private val petTypeOptions = listOf(
    PetTypeOption(PetType.DOG, "Perro"),
    PetTypeOption(PetType.CAT, "Gato"),
    PetTypeOption(PetType.OTHER, "Otro")
)
