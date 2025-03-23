package com.example.project_terrarium_prm392

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project_terrarium_prm392.ui.ProductListActivity
import com.example.project_terrarium_prm392.ui.auth.LoginActivity
import com.example.project_terrarium_prm392.ui.theme.Project_terrarium_prm392Theme
import com.example.project_terrarium_prm392.utils.TokenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_terrarium_prm392Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TerrariumApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TerrariumApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val isLoggedIn = remember { mutableStateOf(tokenManager.isLoggedIn()) }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Terrarium Store",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = {
                context.startActivity(Intent(context, ProductListActivity::class.java))
            }
        ) {
            Text("Browse Products")
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoggedIn.value) {
                Button(
                    onClick = {
                        // Đăng xuất
                        tokenManager.clearAll()
                        isLoggedIn.value = false
                    }
                ) {
                    Text("Logout")
                }
            } else {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    }
                ) {
                    Text("Login")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, LoginActivity::class.java).apply {
                            putExtra("SHOW_REGISTER", true)
                        })
                    }
                ) {
                    Text("Register")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TerrariumAppPreview() {
    Project_terrarium_prm392Theme {
        TerrariumApp()
    }
}