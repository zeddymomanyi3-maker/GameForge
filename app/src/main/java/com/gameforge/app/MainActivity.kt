package com.gameforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.gameforge.app.core.db.GameForgeDatabase
import com.gameforge.app.core.engine.adapters.LocalJsonGameAdapter
import com.gameforge.app.core.model.GameManifest
import com.gameforge.app.ui.GameForgeMainScreen
import com.gameforge.app.ui.GameForgeViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    private var database: GameForgeDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            database = Room.databaseBuilder(
                applicationContext,
                GameForgeDatabase::class.java,
                "gameforge_db"
            ).fallbackToDestructiveMigration().build()

            val testConfigFile = File(filesDir, "sample_game_config.json")
            if (!testConfigFile.exists()) {
                createSampleGameConfigFile(testConfigFile)
            }

            val sampleManifest = GameManifest(
                id = "test_adventure_01",
                title = "Test Adventure Build",
                developer = "Indie Studio",
                gameVersion = "v1.0.4-debug",
                adapterVersion = "1.0",
                engine = "Unity 2023.2",
                connectionType = "JSON_CONFIG"
            )

            val adapter = LocalJsonGameAdapter(sampleManifest, testConfigFile)

            val viewModelFactory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameForgeViewModel(adapter) as T
                }
            }
            val viewModel = ViewModelProvider(this, viewModelFactory)[GameForgeViewModel::class.java]

            setContent {
                MaterialTheme {
                    Surface {
                        GameForgeMainScreen(viewModel = viewModel)
                    }
                }
            }
        } catch (e: Exception) {
            setContent {
                MaterialTheme {
                    Surface {
                        Text(text = "Error starting GameForge:\n${e.localizedMessage}")
                    }
                }
            }
        }
    }

    private fun createSampleGameConfigFile(file: File) {
        val sampleJson = """
            {
              "progression": {
                "level": 12,
                "xp": 4500
              },
              "economy": {
                "coins": 2500,
                "gems": 50
              },
              "stats": {
                "maxHealth": 150.0
              },
              "gameplay": {
                "moveSpeed": 1.5
              }
            }
        """.trimIndent()
        file.writeText(sampleJson)
    }
}
