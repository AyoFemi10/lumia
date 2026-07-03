package com.lumora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lumora.app.ui.nav.LumiaNavGraph
import com.lumora.app.ui.theme.LumiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumiaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LumiaNavGraph()
                }
            }
        }
    }
}
