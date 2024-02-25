package com.example.retrofit.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retrofit.model.Juego
import com.example.retrofit.network.JuegoApi.retrofitService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class estadoApi{
    IDLE, LOADING, SUCCESS, ERROR
}

class ViewModelRetrofit : ViewModel() {

    private val _listaJuegos : MutableStateFlow<List<Juego>?> = MutableStateFlow(null)
    var listaJuegos = _listaJuegos.asStateFlow()

    private val _juegoEncontrado : MutableStateFlow<List<Juego>?> = MutableStateFlow(null)
    var juegoEncontrado = _juegoEncontrado.asStateFlow()

    private val _estadoLlamada : MutableStateFlow<estadoApi> = MutableStateFlow(estadoApi.IDLE)
    var estadoLlamada = _estadoLlamada.asStateFlow()

    private val _textoBusqueda : MutableStateFlow<String> = MutableStateFlow("")
    var textoBusqueda = _textoBusqueda.asStateFlow()

    fun actualizarTextoBusqueda(s: String) { _textoBusqueda.value = s }

    private val _activo = MutableStateFlow(false)
    var activo = _activo.asStateFlow()

    fun actualizarActivo(b : Boolean) {_activo.value = b}

    init {
        obtenerJuegos()
    }

    fun obtenerJuegos() {
        _estadoLlamada.value = estadoApi.LOADING
        viewModelScope.launch {
            val respuesta = retrofitService.getAllJuegos()
            if (respuesta.isSuccessful) {
                _listaJuegos.value = respuesta.body()
                _estadoLlamada.value = estadoApi.SUCCESS
                println(respuesta.body().toString())
            } else {
                println("Ha habido algún error " + respuesta.errorBody())
                _estadoLlamada.value = estadoApi.ERROR
            }
        }
    }

    fun buscarJuegoPorTitulo(){
        _estadoLlamada.value = estadoApi.LOADING
        println("Buscando juegos por titulo: ${_textoBusqueda.value}")
        viewModelScope.launch {
            val respuesta = retrofitService.getJuegoByTitulo(_textoBusqueda.value)
            if(respuesta.isSuccessful){
                _listaJuegos.value = null
                _listaJuegos.value = respuesta.body()
                _estadoLlamada.value = estadoApi.SUCCESS
            }
            else {
                println("Ha habido algún error " + respuesta.errorBody())
                _estadoLlamada.value = estadoApi.ERROR
            }
        }
    }

    fun eliminarJuego(id: String) {
        _estadoLlamada.value = estadoApi.LOADING
        viewModelScope.launch {
            try {
                val response = retrofitService.deleteJuego(id)
                if (response.isSuccessful) {
                    obtenerJuegos()
                    println("Juego borrado")
                    _estadoLlamada.value = estadoApi.SUCCESS
                } else {
                    println("Ha habido algún error al eliminar: ${response.errorBody()}")
                    _estadoLlamada.value = estadoApi.ERROR
                }
            } catch (e: Exception) {
                println("Ha habido algún error: ${e.message}")
                _estadoLlamada.value = estadoApi.ERROR
            }
        }
    }

    fun actualizarJuego(juego: Juego) {
        _estadoLlamada.value = estadoApi.LOADING
        viewModelScope.launch {
            try {
                val response = retrofitService.putJuego(juego)
                if (response.isSuccessful) {
                    obtenerJuegos()
                    println("Juego actualizado")
                    _estadoLlamada.value = estadoApi.SUCCESS
                } else {
                    println("Ha habido algún error: ${response.errorBody()}")
                    _estadoLlamada.value = estadoApi.ERROR
                }
            } catch (e: Exception) {
                println("Ha habido algún error: ${e.message}")
                _estadoLlamada.value = estadoApi.ERROR
            }
        }
    }

    fun postJuego(juego: Juego){
        _estadoLlamada.value = estadoApi.LOADING
        viewModelScope.launch {
            try {
                val response = retrofitService.postJuego(juego)
                if (response.isSuccessful) {
                    obtenerJuegos()
                    println("Juego insertado")
                    _estadoLlamada.value = estadoApi.SUCCESS
                } else {
                    println("Ha habido algún error: ${response.errorBody()}")
                    _estadoLlamada.value = estadoApi.ERROR
                }
            } catch (e: Exception) {
                println("Ha habido algún error: ${e.message}")
                _estadoLlamada.value = estadoApi.ERROR
            }
        }
    }
}