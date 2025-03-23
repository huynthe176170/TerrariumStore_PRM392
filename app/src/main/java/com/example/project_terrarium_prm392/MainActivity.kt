package com.example.project_terrarium_prm392

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project_terrarium_prm392.ui.ProductListActivity
import com.example.project_terrarium_prm392.ui.theme.Project_terrarium_prm392Theme

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
    }
}

@Preview(showBackground = true)
@Composable
fun TerrariumAppPreview() {
    Project_terrarium_prm392Theme {
        TerrariumApp()
    }
}