package com.ags.controlekm.ui.views.app.fragments

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ags.controlekm.R
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.ags.controlekm.navigation.navigateSingleTopTo
import com.ags.controlekm.ui.views.app.viewModel.AppViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavigationDrawer(
    navController: NavHostController,
    drawerState: DrawerState,
    appViewModel: AppViewModel = hiltViewModel<AppViewModel>()
) {
    val scope = rememberCoroutineScope()
    val currentUser by appViewModel.currentUser.collectAsStateWithLifecycle()

    val currentUserImage = rememberImagePainter(
        data = currentUser.image,
        builder = {
            transformations(CircleCropTransformation())
            placeholder(R.drawable.perfil)
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    )

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.7f),
        drawerTonalElevation = 6.dp,
        drawerShape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = currentUserImage,
                contentDescription = "user imagem",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { scope.launch { drawerState.open() } },
            )
            if (currentUser.emailVerification == true) {
                Icon(
                    modifier = Modifier
                        .size(16.dp),
                    imageVector = Icons.Filled.Verified,
                    tint = Color(0xFF228B22),
                    contentDescription = ""
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(16.dp),
                    imageVector = Icons.Filled.Verified,
                    tint = Color(0xFFeb0b0b),
                    contentDescription = ""
                )
            }
            Text(
                text = currentUser.email,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${currentUser.name} ${currentUser.lastName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = currentUser.position,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = currentUser.sector,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        scope.launch {
                            drawerState.close()
                            FirebaseAuth
                                .getInstance()
                                .signOut()
                            //currentUserViewModel.deleteCurrentUser()
                            navController.popBackStack()
                            navController.navigateSingleTopTo("login")
                        }
                    },
                imageVector = Icons.Filled.ExitToApp,
                contentDescription = ""
            )
            Divider(
                modifier = Modifier
                    .padding(top = 8.dp, start = 6.dp, end = 6.dp),
            )
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(32.dp),
                    text = "LOGO"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}