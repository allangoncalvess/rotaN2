package com.ags.controlekm.ui.views.login

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ags.controlekm.ui.components.dialog.AlertDialogSelectImage
import com.ags.controlekm.ui.components.textField.FormularioTextField
import com.ags.controlekm.ui.components.textField.FormularioTextFieldMenu
import com.ags.controlekm.ui.views.userManager.ViewModel.UserViewModel
import com.ags.controlekm.maskTransformations.MaskVisualTransformation
import com.ags.controlekm.functions.validete_format.validateContainsOnlyNumbers
import com.ags.controlekm.functions.validete_format.validateContainsOnlyText
import com.ags.controlekm.functions.validete_format.validateCpfFormat
import com.ags.controlekm.functions.validete_format.validateEmailFormat
import com.ags.controlekm.functions.validete_format.validatePasswordFormat
import com.ags.controlekm.navigation.navigateSingleTopTo
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RegisterUserView(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    //VARIAVEL CONTROLADORA DE CONTEUDO
    var countContent by remember { mutableStateOf(0) }

    // CONTROLE DO TAMANHO DO FORMULARIO
    val SIZE_FORM = 14

    // CONFIGURAÇÕES DE MASCARAS DE TEXTO DO CAMPO NASCIMENTO
    val BIRTH_MASK = "##/##/####"
    val BIRTH_INPUT_LENGTH = 8

    // CONFIGURAÇÕES DE MASCARAS DE TEXTO DO CAMPO TELEFONE
    val PHONE_NUMBER_MASK = "(##)# ####-####"
    val PHONE_NUMBER_INPUT_LENGTH = 11

    // CONFIGURAÇÕES DE MASCARAS DE TEXTO DO CAMPO CPF
    val CPF_MASK = "###.###.###-##"
    val CPF_INPUT_LENGTH = 11

    val SPACE_DEFAULT = 6.dp

    // INDICADOR DE PROGRESSO / LOADING
    val progressIndicator = remember { mutableStateOf(false) }

    // upload de imagem
    val context = LocalContext.current
    val img: Bitmap = BitmapFactory.decodeResource(
        Resources.getSystem(),
        android.R.drawable.ic_menu_report_image
    )
    val bitmap = remember { (mutableStateOf(img)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) {
        if (it != null) {
            bitmap.value = it
        }
    }

    val launcherImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (Build.VERSION.SDK_INT < 28) {
            bitmap.value = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
        } else if (it != null) {
            val source = it?.let { it1 ->
                ImageDecoder.createSource(context.contentResolver, it1)
            }
            bitmap.value = source?.let { it1 ->
                ImageDecoder.decodeBitmap(it1)
            }!!
        }
    }

    //ImagemPerfilAlertDialog
    var AlertDialogSelectImage by remember { mutableStateOf(false) }

    if (AlertDialogSelectImage) {
        AlertDialogSelectImage(
            onDismissRequest = { AlertDialogSelectImage = false },
            abrirCamera = {
                AlertDialogSelectImage = false
                launcher.launch()
            },
            abrirGallery = {
                AlertDialogSelectImage = false
                launcherImage.launch("image/*")
            }
        )
    }


    var title by remember { mutableStateOf("Criar conta") }
    var subTitle by remember { mutableStateOf("Informe seu nome") }

    var nome by remember { mutableStateOf("") }
    var nomeError by remember { mutableStateOf(true) }
    val nomeErrorMessage by remember { mutableStateOf("") }

    var sobrenome by remember { mutableStateOf("") }
    var sobrenomeError by remember { mutableStateOf(true) }
    val sobrenomeErrorMessage by remember { mutableStateOf("") }

    var nascimento by remember { mutableStateOf("") }
    var nascimentoError by remember { mutableStateOf(true) }
    var nascimentoErrorMessage by remember { mutableStateOf("") }

    // DROP-DOWN MENU GENERO
    var generoExpanded by remember { mutableStateOf(false) }
    var generoSelected by remember { mutableStateOf("Selecionar") }
    val generoOptions = listOf("Masculino", "Feminino", "Outro")
    var generoError by remember { mutableStateOf(true) }
    val generoErrorMessage by remember { mutableStateOf("") }

    // DROP-DOWN MENU PERMISSÕES / NIVEL DE ACESSO
    var nivelAcessoExpanded by remember { mutableStateOf(false) }
    var nivelAcessoSelected by remember { mutableStateOf("Selecionar") }
    val nivelAcessoOptions = listOf("Colaborador", "Coordenador", "Master")
    var nivelAcessoError by remember { mutableStateOf(true) }
    val nivelAcessoErrorMessage by remember { mutableStateOf("") }

    var matricula by remember { mutableStateOf("") }
    var matriculaError by remember { mutableStateOf(true) }
    var matriculaErrorMessage by remember { mutableStateOf("") }

    var cpf by remember { mutableStateOf("") }
    var cpfError by remember { mutableStateOf(true) }
    var cpfErrorMessage by remember { mutableStateOf("") }

    var senha by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var senhaError by remember { mutableStateOf(true) }
    var senhaErrorMessage by remember { mutableStateOf("") }

    var telefone by remember { mutableStateOf("") }
    var telefoneError by remember { mutableStateOf(true) }
    var telefoneErrorMessage by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(true) }
    var emailErrorMessage by remember { mutableStateOf("") }

    var cargo by remember { mutableStateOf("") }
    var cargoError by remember { mutableStateOf(true) }
    val cargoErrorMessage by remember { mutableStateOf("") }

    var setor by remember { mutableStateOf("") }
    var setorError by remember { mutableStateOf(true) }
    val setorErrorMessage by remember { mutableStateOf("") }

    val emailList = mutableListOf<String>()
    val cpfList = mutableListOf<String>()
    val telefoneList = mutableListOf<String>()
    val matriculaList = mutableListOf<String>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        //.size(width = 90.dp, height = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                AnimatedContent(
                    targetState = countContent,
                    label = "",
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally(animationSpec = tween(600),
                            initialOffsetX = { fullHeight -> fullHeight })).togetherWith(
                            fadeOut(
                                animationSpec = tween(400)
                            )
                        )
                    }
                ) { targetCount ->
                    when (targetCount) {
                        0 -> {
                            subTitle = "Informe seu nome"
                            Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // NOME
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = nome,
                                    onValueChange = { nome = it.take(20) },
                                    label = "Nome",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words,
                                    erro = nomeError,
                                    erroMensagem = nomeErrorMessage
                                )
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                // SOBRENOME
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = sobrenome,
                                    onValueChange = { sobrenome = it.take(20) },
                                    label = "Sobrenome",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words,
                                    erro = sobrenomeError,
                                    erroMensagem = sobrenomeErrorMessage
                                )
                            }
                        }

                        1 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                            }

                            nomeError = validateContainsOnlyText(nome)
                            sobrenomeError = validateContainsOnlyText(sobrenome)
                            if (!nomeError || !sobrenomeError) {
                                countContent = 0
                            } else {
                                countContent = 2
                            }

                        }

                        2 -> {
                            subTitle = "Dados pessoais"
                            Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                // NASCIMENTO
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = nascimento,
                                    onValueChange = {
                                        nascimento = it.take(BIRTH_INPUT_LENGTH)
                                    },
                                    label = "Nascimento",
                                    visualTransformation = MaskVisualTransformation(
                                        BIRTH_MASK
                                    ),
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.None,
                                    erro = nascimentoError,
                                    erroMensagem = nascimentoErrorMessage
                                )
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                //GENERO INICIO
                                Box(
                                    modifier = Modifier,
                                    contentAlignment = Alignment.BottomEnd
                                )
                                {
                                    // GENERO OutlinedTextField
                                    FormularioTextFieldMenu(
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true,
                                        trailingIconVector = Icons.Filled.ArrowDropDown,
                                        trailingOnClick = { generoExpanded = !generoExpanded },
                                        value = generoSelected,
                                        onValueChange = { generoSelected = it },
                                        label = "Gênero",
                                        visualTransformation = VisualTransformation.None,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next,
                                        capitalization = KeyboardCapitalization.Words,
                                        erro = generoError,
                                        erroMensagem = generoErrorMessage
                                    )
                                    DropdownMenu(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        expanded = generoExpanded,
                                        onDismissRequest = { generoExpanded = false }
                                    ) {
                                        generoOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(text = option) },
                                                onClick = {
                                                    generoSelected = option
                                                    generoExpanded = false
                                                })
                                        }
                                    }

                                }
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                //CPF
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = cpf,
                                    onValueChange = {
                                        cpf = it.take(CPF_INPUT_LENGTH)
                                        validateCpfFormat(cpf)
                                    },
                                    label = "CPF",
                                    visualTransformation = MaskVisualTransformation(CPF_MASK),
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.None,
                                    erro = cpfError,
                                    erroMensagem = cpfErrorMessage
                                )
                            }
                        }

                        3 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                                generoError = validateContainsOnlyText(generoSelected)
                                if (!validateContainsOnlyNumbers(nascimento) || nascimento.length < 8) {
                                    nascimentoError = false
                                    nascimentoErrorMessage = "informe a data completa"
                                } else {
                                    nascimentoError = true
                                }
                                if (cpfList.contains(cpf)) {
                                    cpfError = false
                                    cpfErrorMessage = "CPF vinculado a outra conta"
                                } else {
                                    if (!validateCpfFormat(cpf)) {
                                        cpfError = false
                                        cpfErrorMessage = "Informe um CPF válido"
                                    } else {
                                        cpfError = true
                                    }
                                }
                                if (
                                    !nascimentoError || !generoError || !cpfError
                                ) {
                                    countContent = 2
                                } else {
                                    countContent = 4
                                }
                            }
                        }

                        4 -> {
                            subTitle = "Dados de contato"
                            Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                //TELEFONE
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = telefone,
                                    onValueChange = { telefone = it.take(PHONE_NUMBER_INPUT_LENGTH) },
                                    label = "Telefone",
                                    visualTransformation = MaskVisualTransformation(PHONE_NUMBER_MASK),
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.None,
                                    erro = telefoneError,
                                    erroMensagem = telefoneErrorMessage
                                )
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                //E-MAIL
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = email,
                                    onValueChange = { email = it },
                                    label = "E-mail",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.None,
                                    erro = emailError,
                                    erroMensagem = emailErrorMessage
                                )
                            }

                        }

                        5 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                                if (telefoneList.contains(telefone)) {
                                    telefoneError = false
                                    telefoneErrorMessage = "Número vinculado a outra conta"
                                } else {
                                    if (!validateContainsOnlyNumbers(telefone) || telefone.length < 11) {
                                        telefoneErrorMessage = "Informe um número válido"
                                        telefoneError = false
                                    } else {
                                        telefoneError = true
                                    }
                                }
                                if (emailList.contains(email)) {
                                    emailError = false
                                    emailErrorMessage = "Email vinculado a outra conta"
                                } else {
                                    if (!validateEmailFormat(email)) {
                                        emailErrorMessage = "Informe um Email válido"
                                        emailError = false
                                    } else {
                                        emailError = true
                                    }
                                }
                                if (
                                    !telefoneError || !emailError
                                ) {
                                    countContent = 4
                                } else {
                                    countContent = 6
                                }
                            }
                        }

                        6 -> {
                            subTitle = "Dados funcionais"
                            Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                //MATRICULA
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = matricula,
                                    onValueChange = { matricula = it.take(15) },
                                    label = "Matrícula",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.None,
                                    erro = matriculaError,
                                    erroMensagem = matriculaErrorMessage
                                )
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                // CARGO
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = cargo,
                                    onValueChange = { cargo = it.take(20) },
                                    label = "Cargo",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words,
                                    erro = cargoError,
                                    erroMensagem = cargoErrorMessage
                                )
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                // SETOR
                                FormularioTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = false,
                                    value = setor,
                                    onValueChange = { setor = it.take(20) },
                                    label = "Setor",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words,
                                    erro = setorError,
                                    erroMensagem = setorErrorMessage
                                )
                            }
                        }

                        7 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                                cargoError = cargo.isNotEmpty()
                                setorError = validateContainsOnlyText(setor)
                                if (matriculaList.contains(matricula)) {
                                    matriculaError = false
                                    matriculaErrorMessage = "Matricula vinculado a outra conta"
                                } else {
                                    if (!validateContainsOnlyNumbers(matricula) || matricula.length < 3) {
                                        matriculaErrorMessage = "Informe uma matrícula válida"
                                        matriculaError = false
                                    } else {
                                        matriculaError = true
                                    }
                                }
                                if (
                                    !matriculaError || !cargoError || !setorError
                                ) {
                                    countContent = 6
                                } else {
                                    countContent = 8
                                }
                            }
                        }

                        8 -> {
                            subTitle = "Defina o tipo de acesso do colaborador"
                            Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                            //PERMISSÕES / NIVEL DE ACESSO DO USUÁRIO
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.BottomEnd
                            )
                            {
                                FormularioTextFieldMenu(
                                    modifier = Modifier,
                                    readOnly = true,
                                    trailingIconVector = Icons.Filled.ArrowDropDown,
                                    trailingOnClick = {
                                        nivelAcessoExpanded = !nivelAcessoExpanded
                                    },
                                    value = nivelAcessoSelected,
                                    onValueChange = { nivelAcessoSelected = it },
                                    label = "Nível de acesso",
                                    visualTransformation = VisualTransformation.None,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words,
                                    erro = nivelAcessoError,
                                    erroMensagem = nivelAcessoErrorMessage
                                )
                                DropdownMenu(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    expanded = nivelAcessoExpanded,
                                    onDismissRequest = { nivelAcessoExpanded = false }
                                ) {
                                    nivelAcessoOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option) },
                                            onClick = {
                                                nivelAcessoSelected = option
                                                nivelAcessoExpanded = false
                                            })
                                    }
                                }

                            }
                        }

                        9 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                                nivelAcessoError = validateContainsOnlyText(nivelAcessoSelected)
                                if (
                                    !nivelAcessoError
                                ) {
                                    countContent = 8
                                } else {
                                    countContent = 10
                                }
                            }
                        }

                        10 -> {
                            subTitle = "Defina uma senha de acesso"
                            Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                            //SENHA
                            FormularioTextFieldMenu(
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = false,
                                value = senha,
                                onValueChange = { senha = it },
                                trailingIconVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                trailingOnClick = { passwordVisibility = !passwordVisibility },
                                label = "Senha",
                                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                                capitalization = KeyboardCapitalization.None,
                                erro = senhaError,
                                erroMensagem = ""
                            )
                        }

                        11 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                                senhaError = validatePasswordFormat(senha)
                                if (!senhaError) {
                                    countContent = 10
                                } else {
                                    countContent = 12
                                }
                            }
                        }

                        12 -> {
                            subTitle = "Imagem do perfil"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(top = SPACE_DEFAULT),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Image(
                                        bitmap = bitmap.value.asImageBitmap(),
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .border(
                                                BorderStroke(2.dp, Color.Gray),
                                                CircleShape,
                                            )
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                    )

                                    IconButton(
                                        modifier = Modifier
                                            .background(Color.White, CircleShape)
                                            .size(40.dp)
                                            .padding(10.dp),
                                        onClick = {
                                            AlertDialogSelectImage = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.AddPhotoAlternate,
                                            contentDescription = "",
                                        )
                                    }

                                }
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                TextButton(
                                    onClick = { AlertDialogSelectImage = true }
                                ) {
                                    Text(text = "Selecionar imagem")
                                }
                            }
                        }

                        13 -> {
                            progressIndicator.value = true
                            title = "Login"
                            subTitle = ""
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(SPACE_DEFAULT))
                                if (progressIndicator.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                                countContent = 14
                            }
                        }

                        14 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                progressIndicator.value = true
                                title = "Salvando"
                                subTitle = ""
                                if (progressIndicator.value) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 15.dp, end = 15.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }

                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha).addOnSuccessListener { AuthResult ->
                                                    if (AuthResult.user != null) {
                                                        // TRATAMENTO PARA SALVAR NOVO USUÁRIO
                                                        bitmap.value.let { bitmap ->
                                                            // Chama a função UploadImage dentro de uma coroutine
                                                            userViewModel.uploadImage(
                                                                bitmap = bitmap,
                                                                context = context as ComponentActivity,
                                                                imgName = AuthResult.user!!.uid,
                                                                callback = { success ->
                                                                    if (success) {
                                                                        navController.navigateSingleTopTo(
                                                                            "login"
                                                                        )
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Novo perfil criado com sucesso!",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                    } else {
                                                                        navController.navigateSingleTopTo(
                                                                            "Login"
                                                                        )
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Erro ao tentar criar conta!",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                    }
                                                                }
                                                            ) { imageUrl ->
                                                                // SALVA DADOS DO USUÁRIO

                                                            }
                                                            navController.navigateSingleTopTo("login")
                                                        }

                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
            if (countContent < SIZE_FORM) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, end = 35.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    var visible by rememberSaveable { mutableStateOf(false) }
                    TextButton(
                        enabled = visible,
                        onClick = {
                            if (countContent > 0) countContent--
                        }) {

                        if (countContent > 0) visible = true else visible = false

                        AnimatedVisibility(visible = visible) {
                            Text("Voltar")
                        }

                    }
                    //AVAN
                    TextButton(
                        onClick = {
                            countContent++
                        }) {
                        if (countContent == SIZE_FORM - 1) Text("Finalizar")
                        else Text("Próximo")
                    }
                }
            }
        }
    }
}
