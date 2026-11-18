package com.haidarova.headachediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.haidarova.headachediary.ui.AppRoot
import com.haidarova.headachediary.ui.HeadacheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeadacheTheme {
                AppRoot()
            }
        }
    }
}
