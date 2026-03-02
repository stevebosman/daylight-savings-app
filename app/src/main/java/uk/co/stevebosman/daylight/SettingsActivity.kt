package uk.co.stevebosman.daylight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import uk.co.stevebosman.daylight.ui.SettingsScreen
import uk.co.stevebosman.daylight.ui.theme.MainActivityTheme

@OptIn(ExperimentalFoundationApi::class)
class SettingsActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainActivityTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("Continuous Daylight Savings")
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { innerPadding ->
                    SettingsScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
