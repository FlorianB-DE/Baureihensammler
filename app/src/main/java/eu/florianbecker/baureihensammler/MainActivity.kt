package eu.florianbecker.baureihensammler

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import eu.florianbecker.baureihensammler.ui.TrainSeriesScreen
import eu.florianbecker.baureihensammler.ui.theme.BaureihensammlerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            BaureihensammlerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TrainSeriesScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
