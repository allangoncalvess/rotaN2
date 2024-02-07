package com.ags.controlekm.components.Dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ags.controlekm.components.Buttons.ButtonPadrao
import com.ags.controlekm.components.DropDownMenu.DropDownMenuAtendimento
import com.ags.controlekm.components.Progress.LoadingCircular
import com.ags.controlekm.components.Text.TitleText
import com.ags.controlekm.database.FirebaseServices.CurrentUserServices
import com.ags.controlekm.database.Models.CurrentUser
import com.ags.controlekm.database.Models.ViagemSuporteTecnico
import com.ags.controlekm.database.ViewModels.CurrentUserViewModel
import com.ags.controlekm.database.ViewModels.ViagemSuporteTecnicoViewModel
import com.ags.controlekm.database.ViewModels.ExecutarFuncaoViewModel

@Composable
fun HomeAtendimentoDialog(
    viagemSuporteTecnicoViewModel: ViagemSuporteTecnicoViewModel = viewModel(),
    currentUserViewModel: CurrentUserViewModel = viewModel(),
    executarFuncaoViewModel: ExecutarFuncaoViewModel = viewModel(),
    currentUserServices: CurrentUserServices,
    userLoggedData: CurrentUser?,
    atendimentoAtual: ViagemSuporteTecnico,
    novoAtendimento: ViagemSuporteTecnico,
    resumoAtendimento: String,
    data: String,
    hora: String,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var visibleOpcoes by remember { mutableStateOf(true) }
    var visibleRetornar by remember { mutableStateOf(false) }
    var visibleNovoAtendimento by remember { mutableStateOf(false) }
    var visibleFinalizarAtendimento by remember { mutableStateOf(false) }

    var titleDialog by remember { mutableStateOf("Finalizar atendimento") }

    var local by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }

    val paddingButton = 3.dp

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier.wrapContentSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // EXIBIR CARREGAMENTO
                if (executarFuncaoViewModel.carregando.value) {
                    visibleRetornar = false
                    visibleNovoAtendimento = false
                    LoadingCircular()
                }
                // OPÇÕES FINALIZAR ATENDIMENTO
                AnimatedVisibility(visibleOpcoes) {
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleText(titleDialog)
                        ButtonPadrao(
                            "Retornar",
                            padding = paddingButton
                        ) {
                            titleDialog = "Para onde vai retornar?"
                            visibleRetornar = true
                            visibleNovoAtendimento = false
                            visibleOpcoes = false
                        }
                        ButtonPadrao(
                            "Novo atendimento",
                            padding = paddingButton
                        ) {
                            titleDialog = "Qual o local do novo atendimento?"
                            visibleNovoAtendimento = true
                            visibleRetornar = false
                            visibleOpcoes = false
                        }
                    }
                }
                // INICIAR UM NOVO ATENDIMENTO
                AnimatedVisibility(visible = visibleNovoAtendimento) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleText(titleDialog)
                        DropDownMenuAtendimento(
                            labelLocal = "Local do atendimento",
                            labelKm = "KM de saída",
                            data = data,
                            hora = hora,
                            visibleLocal = true,
                            visibleKm = true,
                            localSelecionado = { localSelecionado -> local = localSelecionado },
                            kmInformado = { kmInformado -> km = kmInformado }
                        )
                        ButtonPadrao(
                            "Iniciar percurso",
                            padding = paddingButton
                        ) {
                            viagemSuporteTecnicoViewModel.novoAtendimento(
                                currentUserViewModel = currentUserViewModel,
                                currentUserServices = currentUserServices,
                                userLoggedData = userLoggedData,
                                novoAtendimento = novoAtendimento,
                                localSaida = atendimentoAtual.localAtendimento.toString(),
                                localAtendimento = local,
                                kmSaida = km,
                                data = data,
                                hora = hora,
                                atendimentoAtual = atendimentoAtual,
                                resumoAtendimento = resumoAtendimento,
                            )
                        }
                    }
                }
                // SELECIONAR CIDADE DE RETORNO
                AnimatedVisibility(visible = visibleRetornar) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(20.dp),
                            text = titleDialog,
                            fontWeight = FontWeight.SemiBold,
                        )
                        DropDownMenuAtendimento(
                            labelLocal = "Retornar para",
                            data = data,
                            hora = hora,
                            visibleLocal = true,
                            localSelecionado = { localSelecionado -> local = localSelecionado },
                        )
                        ButtonPadrao(
                            "Iniciar percurso",
                            padding = paddingButton
                        ) {
                            viagemSuporteTecnicoViewModel.iniciarRetorno(
                                atendimento = atendimentoAtual,
                                localRetorno = local,
                                resumoAtendimento = resumoAtendimento,
                                data = data,
                                hora = hora,
                            )
                        }
                    }
                }
            }
        }
    }
}