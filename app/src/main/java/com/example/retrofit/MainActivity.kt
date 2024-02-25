package com.example.retrofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.retrofit.model.Juego
import com.example.retrofit.shared.ViewModelRetrofit
import com.example.retrofit.shared.estadoApi
import com.example.retrofit.ui.theme.RetrofitTheme
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RetrofitTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel : ViewModelRetrofit = viewModel()
                    val estado by viewModel.estadoLlamada.collectAsState()
                    val listaJuegos by viewModel.listaJuegos.collectAsState()
                    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
                    val activo by viewModel.activo.collectAsState()

                    if(estado == estadoApi.LOADING){
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                            Text(
                                text = "Cargando juegos!",
                                fontSize = 18.sp
                            )
                        }
                    }
                    else{
                        Column() {
                            SearchBar(
                                query = textoBusqueda,
                                onQueryChange = { viewModel.actualizarTextoBusqueda(it)
                                },
                                onSearch = {
                                    viewModel.actualizarActivo(false)
                                    if (viewModel.textoBusqueda.value.isNotBlank()) {
                                        viewModel.buscarJuegoPorTitulo()
                                    } else {
                                        viewModel.obtenerJuegos()
                                    }
                                },
                                active = activo,
                                onActiveChange = {
                                    viewModel.actualizarActivo(it)
                                },
                                placeholder = { Text("Selecciona un juego") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null)},
                                modifier = Modifier.fillMaxWidth(),
                                content = {
                                    Column(Modifier.verticalScroll(rememberScrollState())) {
                                        listaJuegos!!.distinctBy { it.titulo }!!
                                            .let { juego ->
                                                if (textoBusqueda.isNotEmpty() && textoBusqueda.isNotBlank()) {
                                                    juego.filter { it.titulo!!.startsWith(textoBusqueda,
                                                        true) }
                                                } else juego
                                            }.sortedBy { it.titulo!! }.forEach {
                                                ListItem(
                                                    headlineContent = { it.titulo?.let { it1 -> Text(it1) } },
                                                    modifier = Modifier
                                                        .clickable {
                                                            viewModel.actualizarTextoBusqueda(it.titulo!!)
                                                            viewModel.actualizarActivo(false)
                                                            viewModel.buscarJuegoPorTitulo()
                                                        }
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 16.dp,
                                                            vertical = 4.dp
                                                        )
                                                )
                                            }
                                    }
                                }
                            )
                            var openDialog by remember { mutableStateOf(false) }

                            Button(modifier = Modifier
                                .padding(vertical = 10.dp)
                                .align(alignment = Alignment.CenterHorizontally)
                                ,onClick = {
                                openDialog = true
                            }) {
                                Text(text = "Crear Juego")
                            }
                            if(openDialog){
                                dialogoCrear(viewModel = viewModel) {
                                    openDialog = false
                                }
                            }
                            LazyColumn(){
                                items(listaJuegos!!){
                                    ItemJuego(a = it, viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemJuego(a : Juego, viewModel: ViewModelRetrofit){

    var openDialog by remember { mutableStateOf(false) }

    if (openDialog) {
        dialogoModificar(a = a, viewModel = viewModel){
            openDialog = false
        }
    }

    Card(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        modifier = Modifier.clickable {
            openDialog = true
        }
        ){
        Row(Modifier.fillMaxWidth()) {
            var imageSize by remember { mutableStateOf(Size.Zero) }
            AsyncImage(model = a.imagenurl, contentDescription = null, modifier =
            Modifier
                .size(200.dp)
                .wrapContentWidth(
                    if (imageSize.width < 400) Alignment.CenterHorizontally else
                        Alignment.Start
                )
                .onSizeChanged { imageSize = it.toSize() }, contentScale = ContentScale.Fit,
                alignment = Alignment.Center)
            println("Image size: ${imageSize.width} x ${imageSize.height}")
            Column( modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
            ) {
                a.titulo?.let { Text(text = it, modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .wrapContentWidth(Alignment.CenterHorizontally, true),
                    textAlign = TextAlign.Center) }
                a.desarrolladora?.let { Text(text = it, modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .wrapContentWidth(Alignment.CenterHorizontally, true),
                    textAlign = TextAlign.Center) }
            }
        }
    }
}

@Composable
fun dialogoModificar(a: Juego, viewModel: ViewModelRetrofit,onCloseDialog: () -> Unit){
    var nuevaRuta by remember { mutableStateOf(a.titulo) }
    var nuevaDesarrolladora by remember { mutableStateOf(a.desarrolladora) }
    var nuevaImagen by remember { mutableStateOf(a.imagenurl) }
    AlertDialog(
        onDismissRequest = {
            onCloseDialog()
        },
        title = {
            Text(text = "Titulo: " + a.titulo)
        },
        text = {
            Column {
                Text("Modificar:")
                Spacer(modifier = Modifier.height(8.dp))
                nuevaRuta?.let {
                    TextField(
                        value = it,
                        onValueChange = { nuevaRuta = it },
                        label = { Text("Cambiar Titulo") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                nuevaDesarrolladora?.let {
                    TextField(
                        value = it,
                        onValueChange = { nuevaDesarrolladora = it },
                        label = { Text("Cambiar Desarrolladora") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                nuevaImagen?.let {
                    TextField(
                        value = it,
                        onValueChange = { nuevaImagen = it },
                        label = { Text("Cambiar Imagen") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    a.titulo = nuevaRuta
                    a.desarrolladora = nuevaDesarrolladora
                    a.imagenurl = nuevaImagen
                    a.id?.let { viewModel.actualizarJuego(a) }
                    onCloseDialog()
                }) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    a.id?.let { viewModel.eliminarJuego(it) }
                    onCloseDialog()

                }) {
                Text("Borrar")
            }
        }
    )
}

@Composable
fun dialogoCrear( viewModel: ViewModelRetrofit,onCloseDialog: () -> Unit){
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaDesarrolladora by remember { mutableStateOf("") }
    var nuevaImagen by remember { mutableStateOf("") }
    val a = Juego("", nuevoTitulo, nuevaDesarrolladora, nuevaImagen )

    AlertDialog(
        onDismissRequest = {
            onCloseDialog()
        },
        title = {
            Text(text = "Crear Juego ")
        },
        text = {
            Column {
                Text("Introduce el nuevo juego:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = nuevoTitulo,
                    onValueChange = { nuevoTitulo = it },
                    label = { Text("Titulo") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                nuevaDesarrolladora?.let {
                    TextField(
                        value = it,
                        onValueChange = { nuevaDesarrolladora = it },
                        label = { Text("Desarrolladora") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = nuevaImagen,
                    onValueChange = { nuevaImagen = it },
                    label = { Text("Imagen") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    a.titulo = nuevoTitulo
                    a.desarrolladora = nuevaDesarrolladora
                    a.imagenurl = nuevaImagen
                    viewModel.postJuego(a)
                    onCloseDialog()
                }) {
                Text("Crear")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onCloseDialog()
                }) {
                Text("Salir")
            }
        }
    )
}