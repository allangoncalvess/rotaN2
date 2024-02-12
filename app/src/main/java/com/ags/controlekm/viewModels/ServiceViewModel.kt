package com.ags.controlekm.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.controlekm.database.firebaseRepositories.CurrentUserServices
import com.ags.controlekm.database.firebaseRepositories.FirebaseServiceRepository
import com.ags.controlekm.models.CurrentUser
import com.ags.controlekm.models.Service
import com.ags.controlekm.database.repositorys.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val repository: ServiceRepository
): ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading get() = _loading

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance()
            .reference
            .child("rotaN2")
            .child("services")

    var allTripsCurrentUser: Flow<List<Service>> =
        repository.getViagensCurrentUser(FirebaseAuth.getInstance().currentUser?.uid.toString())
    val allService: Flow<List<Service>> = repository.getAllServices()
    private val firebaseServiceRepository: FirebaseServiceRepository

    var countContent: MutableStateFlow<Int> = MutableStateFlow(0)
    var currentService = MutableStateFlow(Service())

    var currentWeekData = mutableStateOf<List<Service>>(emptyList())

    val calendar = Calendar.getInstance()
    val firstDayWeek = mutableStateOf(calendar.timeInMillis)
    val lastDayWeek = mutableStateOf(calendar.timeInMillis)

    init {
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        firstDayWeek.value = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        lastDayWeek.value = calendar.timeInMillis

        firebaseServiceRepository = FirebaseServiceRepository()

        homeCountContent()

        getCurrentWeekData(firstDayWeek.value, lastDayWeek.value)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList = mutableListOf<Service>()
                for (childSnapshot in snapshot.children) {
                    val data = childSnapshot.getValue(Service::class.java)
                    viewModelScope.launch(Dispatchers.IO) {
                        data?.let {
                            dataList.add(it)
                            repository.insert(it)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Trate erros, se necessário
            }
        })
    }

    // FUNÇÕES DE LÓGICA DE NEGOCIO
    fun iniciarViagem(
        currentUserViewModel: CurrentUserViewModel,
        currentUserServices: CurrentUserServices,
        userLoggedData: CurrentUser,
        novoAtendimento: Service,
        localSaida: String,
        localAtendimento: String,
        kmSaida: String,
        data: String,
        hora: String,
    ) {
        // VERIFICA SE ALGUM CAMPO ESTA VAZIO
        if (localSaida.equals(null) || localAtendimento.equals(null) || kmSaida.equals(null)) {
            println("Preencha todos os campos para continuar")
            // VERIFICA SE O LOCAL DE SAIDA É IGUAL AO LOCAL DE ATENDIMENTO
        } else if (localSaida.equals(localAtendimento)) {
            println("O local de saida não pode ser o mesmo local do atendimento")
            // VERIFICA SE O KM INFORMADO É VALIDO DE ACORDO COM O ULTIMO KM INFORMADO
        } else if (kmSaida.toInt() < userLoggedData.lastKm!!.toInt()) {
            println("O KM não pode ser inferior ao último informado")
        } else {
            novoAtendimento.departureDate = data
            novoAtendimento.departureTime = hora
            novoAtendimento.departureAddress = localSaida
            novoAtendimento.serviceAddress = localAtendimento
            novoAtendimento.departureKm = kmSaida
            novoAtendimento.technicianId = userLoggedData.id
            novoAtendimento.technicianName = "${userLoggedData.name} ${userLoggedData.lastName}"
            novoAtendimento.profileImgTechnician = userLoggedData.image.toString()
            novoAtendimento.statusService = "Em rota"

            userLoggedData.lastKm = kmSaida

            executar(
                function = {
                    viewModelScope.launch(Dispatchers.IO) {
                        insert(novoAtendimento)
                        currentUserViewModel.update(userLoggedData)
                        currentUserServices.addUltimoKm(kmSaida)
                    }
                },
                onExecuted = {
                    if(it) {
                        countContent.value = 2
                        _loading.value = false
                    }
                },
                onError = {}
            )
        }
    }

    fun confirmarChegada(
        currentUserViewModel: CurrentUserViewModel,
        currentUserServices: CurrentUserServices,
        userLoggedData: CurrentUser?,
        atendimentoAtual: Service,
        kmChegada: String,
        data: String,
        hora: String,
    ) {
        // VERIFICA SE O CAMPO ESTA VAZIO
        if (kmChegada.isEmpty()) {
            println("Você precisa informar o KM ao chegar no local de atendimento para continuar")
            // VERIFICA SE O KM INFORMADO É VALIDO DE ACORDO COM O ULTIMO KM INFORMADO
        } else if (kmChegada.toInt() < userLoggedData?.lastKm!!.toInt()) {
            println("O KM não pode ser inferior ao último informado")
        } else {
            if (atendimentoAtual.statusService.equals("Em rota")) {
                atendimentoAtual.dateArrival = data
                atendimentoAtual.timeArrival = hora
                atendimentoAtual.arrivalKm = kmChegada
                atendimentoAtual.kmDriven =
                    (kmChegada.toInt() - atendimentoAtual.departureKm!!.toInt()).toString()
                atendimentoAtual.statusService = "Em andamento"

                userLoggedData.lastKm = kmChegada

                executar(
                    function = {
                        viewModelScope.launch(Dispatchers.IO) {
                            update(atendimentoAtual)
                            currentUserViewModel.update(userLoggedData)
                            currentUserServices.addUltimoKm(kmChegada)
                        }
                    },
                    onExecuted = {
                        if(it) {
                            countContent.value = 3
                            _loading.value = false
                        }
                    },
                    onError = {}
                )
            } else if (atendimentoAtual.statusService.equals("Em rota, retornando")) {
                atendimentoAtual.dateArrivalReturn = data
                atendimentoAtual.timeCompletedReturn = hora
                atendimentoAtual.arrivalKm = kmChegada
                atendimentoAtual.statusService = "Finalizado"
                currentUserServices.addUltimoKm(kmChegada)
                atendimentoAtual.kmDriven =
                    (kmChegada.toInt() - atendimentoAtual.departureKm!!.toInt()).toString()

                userLoggedData.lastKm = kmChegada
                userLoggedData.kmBackup = kmChegada

                executar(
                    function = {
                        viewModelScope.launch(Dispatchers.IO) {
                            update(atendimentoAtual)
                            currentUserViewModel.update(userLoggedData)
                            currentUserServices.addUltimoKm(kmChegada)
                            currentUserServices.addKmBackup(kmChegada)
                            countContent.value = 1
                        }
                    },
                    onExecuted = {
                        if (it) { _loading.value = false}
                    },
                    onError = {}
                )
            }
        }
    }

    fun iniciarRetorno(
        atendimento: Service,
        localRetorno: String,
        resumoAtendimento: String,
        data: String,
        hora: String,
    ) {
        atendimento.dateCompletion = data
        atendimento.CompletionTime = hora

        atendimento.description = resumoAtendimento
        atendimento.departureDateToReturn = data
        atendimento.startTimeReturn = hora
        atendimento.addressReturn = localRetorno
        atendimento.statusService = "Em rota, retornando"

        executar(
            function = {
                viewModelScope.launch(Dispatchers.IO) {
                    update(atendimento)
                }
                homeCountContent()
            },
            onExecuted = {
                if(it) {
                    countContent.value = 2
                    _loading.value = false
                }
            },
            onError = {}
        )
    }

    fun cancelar(
        currentUserViewModel: CurrentUserViewModel,
        currentUserServices: CurrentUserServices,
        userLoggedData: CurrentUser,
        atendimentoAtual: Service,
    ) {
        var count: MutableStateFlow<Int> = MutableStateFlow(0)
        executar(
            function = {
                if (atendimentoAtual.statusService.equals("Em rota, retornando")) {
                    // CANCELA VIAGEM DE RETORNO
                    atendimentoAtual.departureDateToReturn = ""
                    atendimentoAtual.startTimeReturn = ""
                    atendimentoAtual.addressReturn = ""
                    atendimentoAtual.statusService = "Em andamento"

                    userLoggedData.lastKm = userLoggedData?.kmBackup.toString()

                    viewModelScope.launch(Dispatchers.IO) {
                        update(atendimentoAtual)
                    }
                    count.value = 3
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        // NO ROOM
                        // DELETA O ATENDIMENTO ATUAL DA TABELA
                        delete(Service(atendimentoAtual.id))

                        // NO ROOM
                        // DEFINE O VALOR DO (ULTIMO KM) DO USUÁRIO PARA O ULTIMO INFORMADO AO CONCLUIR A ULTIMA VIAGEM
                        currentUserViewModel.update(userLoggedData)
                        // NO FIREBASE
                        // DEFINE O VALOR DO (ULTIMO KM) DO USUÁRIO PARA O ULTIMO INFORMADO AO CONCLUIR A ULTIMA VIAGEM
                        currentUserServices.addUltimoKm(userLoggedData.kmBackup.toString())
                    }
                    count.value = 1
                }
            },
            onExecuted = {
                if(it) {
                    countContent.value = count.value
                    _loading.value = false
                }
            },
            onError = {}
        )
    }

    fun novoAtendimento(
        currentUserViewModel: CurrentUserViewModel,
        currentUserServices: CurrentUserServices,
        userLoggedData: CurrentUser?,
        novoAtendimento: Service,
        localSaida: String,
        localAtendimento: String,
        kmSaida: String,
        data: String,
        hora: String,
        atendimentoAtual: Service,
        resumoAtendimento: String,
    ) {
        // VERIFICA SE ALGUM CAMPO ESTA VAZIO
        if (localSaida.equals(null) || localAtendimento.equals(null) || kmSaida.equals(null)) {
            println("Preencha todos os campos para continuar")
            // VERIFICA SE O LOCAL DE SAIDA É IGUAL AO LOCAL DE ATENDIMENTO
        } else if (localSaida.equals(localAtendimento)) {
            println("O local de saida não pode ser o mesmo local do atendimento")
            // VERIFICA SE O KM INFORMADO É VALIDO DE ACORDO COM O ULTIMO KM INFORMADO
        } else if (kmSaida.toInt() < userLoggedData?.lastKm!!.toInt()) {
            println("O KM não pode ser inferior ao último informado")
        } else {
            novoAtendimento.departureDate = data
            novoAtendimento.departureTime = hora
            novoAtendimento.departureAddress = localSaida
            novoAtendimento.serviceAddress = localAtendimento
            novoAtendimento.departureKm = kmSaida
            novoAtendimento.technicianId = userLoggedData.id
            novoAtendimento.technicianName = "${userLoggedData.name} ${userLoggedData.lastName}"
            novoAtendimento.profileImgTechnician = userLoggedData.image.toString()
            novoAtendimento.statusService = "Em rota"

            // FINALIZA O ATENDIMENTO ATUAL
            atendimentoAtual.dateCompletion = data
            atendimentoAtual.CompletionTime = hora
            atendimentoAtual.description = resumoAtendimento
            atendimentoAtual.statusService = "Finalizado"

            executar(
                function = {
                    userLoggedData.kmBackup = userLoggedData.lastKm.toString()
                    // FINALIZA O ATENDIMENTO ATUAL
                    viewModelScope.launch(Dispatchers.IO) {
                        update(atendimentoAtual)
                        currentUserViewModel.update(userLoggedData)
                        currentUserServices.addKmBackup(userLoggedData.lastKm.toString())
                    }
                },
                onExecuted = {
                    if(it) {
                        executar(
                            function = {
                                userLoggedData.lastKm = kmSaida
                                //INICIA UM NOVO ATENDIMENTO
                                viewModelScope.launch(Dispatchers.IO) {
                                    insert(novoAtendimento)
                                    currentUserViewModel.update(userLoggedData)
                                    currentUserServices.addUltimoKm(kmSaida)
                                }
                            },
                            onExecuted = {
                                if(it) {
                                    countContent.value = 2
                                    _loading.value = false
                                }
                            },
                            onError = {}
                        )
                    }
                },
                onError = {}
            )
        }
    }

    fun executar(function: () -> Unit, onExecuted: (Boolean) -> Unit, onError: () -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Simular uma função assíncrona real
                val result = withContext(Dispatchers.IO) {
                    delay(1000)
                    function()
                    true
                }
                // Chamar a função de retorno de sucesso
                onExecuted(result)
            } catch (e: Exception) {
                println("Erro desconhecido, não foi possivél executar essa ação")
                onError()
            } finally {
                // Finalizar o carregamento, mesmo em caso de erro
                _loading.value = false
            }
        }
    }

    fun getViagensCurrentUser() {
        allTripsCurrentUser =
            repository.getViagensCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    fun homeCountContent() {
        var count: MutableStateFlow<Int> = MutableStateFlow(1)

        viewModelScope.launch {
            repository.getViagensCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid).collect {
                it.forEach {
                    when {
                        it.statusService?.contains("Em rota") == true ||
                                it.statusService?.contains("Em rota, retornando") == true -> {
                            currentService.value = it
                            count = MutableStateFlow(2)
                        }

                        it.statusService?.contains("Em andamento") == true -> {
                            currentService.value = it
                            count = MutableStateFlow(3)
                        }
                    }
                }
                countContent = count
            }
        }
    }

    fun getCurrentWeekData(firstDayWeek: Long, lastDayWeek: Long) {
        viewModelScope.launch {
            repository.getCurrentWeekData(firstDayWeek, lastDayWeek).collect {
                currentWeekData.value = it
            }
        }
    }

    suspend fun insert(service: Service) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(service)
            firebaseServiceRepository.insert(service)
        }
    }

    suspend fun update(service: Service) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(service)
            firebaseServiceRepository.update(service)
        }
    }

    suspend fun delete(service: Service) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(service)
            firebaseServiceRepository.delete(service)
        }
    }
}

