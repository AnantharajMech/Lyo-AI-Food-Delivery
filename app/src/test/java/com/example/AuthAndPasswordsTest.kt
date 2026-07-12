package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.data.database.AppDatabase
import com.example.data.repository.LyoRepository
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AuthAndPasswordsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var repository: LyoRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId("com.example.lyofood")
                .setApiKey("mock-api-key")
                .setProjectId("mock-project")
                .build()
            com.google.firebase.FirebaseApp.initializeApp(context, options)
        }

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        repository = LyoRepository(db)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun loginScreen_rendersSuccessfully() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = {},
                    onLoginSuccess = { _, _ -> },
                    onNavigateToAdminLogin = {},
                    onNavigateToDeliveryLogin = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        
        // Verify core elements exist
        composeTestRule.onNodeWithTag("username_input").assertExists()
        composeTestRule.onNodeWithTag("password_input").assertExists()
        composeTestRule.onNodeWithTag("submit_button").assertExists()
    }

    @Test
    fun loginScreen_togglesToAdminMode() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                var currentScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("LOGIN") }
                if (currentScreen == "LOGIN") {
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = {},
                        onLoginSuccess = { _, _ -> },
                        onNavigateToAdminLogin = { currentScreen = "ADMIN_LOGIN" },
                        onNavigateToDeliveryLogin = {}
                    )
                } else {
                    com.example.ui.screens.AdminLoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { _, _ -> },
                        onBackToCustomerLogin = { currentScreen = "LOGIN" }
                    )
                }
            }
        }
        
        composeTestRule.waitForIdle()
        
        // Assert Admin access button/link exists
        composeTestRule.onNodeWithText("👑 Admin Login", useUnmergedTree = true).assertExists()
        
        // Click the admin toggle link
        composeTestRule.onNodeWithText("👑 Admin Login", useUnmergedTree = true).performClick()
        
        composeTestRule.waitForIdle()
        
        // Verify header text changes to reflect Admin Console Login
        composeTestRule.onNodeWithText("🛡️ Lyo Admin Console Login", useUnmergedTree = true).assertExists()
        
        // Assert the Switch mode button text changes
        composeTestRule.onNodeWithText("Switch to Customer / Rider Login", useUnmergedTree = true).assertExists()
    }
}
