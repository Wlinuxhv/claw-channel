package com.clawchannel.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.clawchannel.app.data.local.TokenStore
import com.clawchannel.app.data.remote.ApiService
import com.clawchannel.app.data.remote.WebSocketManager
import com.clawchannel.app.data.repository.AuthRepository
import com.clawchannel.app.data.repository.MessageRepository
import com.clawchannel.app.domain.model.Robot
import com.clawchannel.app.domain.model.RobotStatus
import com.clawchannel.app.ui.admin.AdminScreen
import com.clawchannel.app.ui.chat.ChatScreen
import com.clawchannel.app.ui.chat.ChatViewModel
import com.clawchannel.app.ui.login.LoginScreen
import com.clawchannel.app.ui.login.LoginViewModel
import com.clawchannel.app.ui.robots.RobotListScreen
import com.clawchannel.app.ui.settings.SettingsScreen
import com.clawchannel.app.ui.theme.LobsterRed
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClawChannelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// 应用级别的依赖管理
object AppDependencies {
    private var _apiService: ApiService? = null
    private var _webSocketManager: WebSocketManager? = null
    private var _authRepository: AuthRepository? = null
    private var _messageRepository: MessageRepository? = null
    private var _tokenStore: TokenStore? = null
    
    val apiService: ApiService get() = _apiService!!
    val webSocketManager: WebSocketManager get() = _webSocketManager!!
    val authRepository: AuthRepository get() = _authRepository!!
    val messageRepository: MessageRepository get() = _messageRepository!!
    val tokenStore: TokenStore get() = _tokenStore!!
    
    private var currentServerUrl: String = TokenStore.DEFAULT_SERVER_URL
    
    fun initialize(application: Application) {
        _tokenStore = TokenStore(application)
        currentServerUrl = _tokenStore!!.getServerUrl()
        createDependencies(currentServerUrl, application)
    }
    
    fun updateServerUrl(url: String, application: Application) {
        currentServerUrl = url
        _tokenStore?.saveServerUrl(url)
        createDependencies(url, application)
    }
    
    private fun createDependencies(baseUrl: String, application: Application) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        _apiService = retrofit.create(ApiService::class.java)
        _webSocketManager = WebSocketManager()
        _authRepository = AuthRepository(_apiService!!, _webSocketManager!!)
        _messageRepository = MessageRepository(application, _webSocketManager!!)
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    
    // 初始化依赖
    LaunchedEffect(Unit) {
        AppDependencies.initialize(application)
    }
    
    val navController = rememberNavController()
    val tokenStore = AppDependencies.tokenStore
    
    // 检查是否已登录
    val isLoggedIn = remember { mutableStateOf(tokenStore.hasToken()) }
    
    // 当前选中的机器人
    var selectedRobot by remember { mutableStateOf<Robot?>(null) }
    
    // 示例机器人列表（后续从服务器获取）
    val robots = remember {
        mutableStateListOf(
            Robot(
                id = 1,
                name = "小助手",
                avatar = "🦞",
                status = RobotStatus.ONLINE,
                lastMessage = "你好，有什么可以帮你的？"
            ),
            Robot(
                id = 2,
                name = "翻译官",
                avatar = "🌍",
                status = RobotStatus.ONLINE
            ),
            Robot(
                id = 3,
                name = "代码专家",
                avatar = "💻",
                status = RobotStatus.BUSY
            )
        )
    }
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn.value) "robots" else "login"
    ) {
        // 登录界面
        composable("login") {
            val viewModel: LoginViewModel = viewModel {
                LoginViewModel(application, AppDependencies.authRepository)
            }
            
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn.value = true
                    navController.navigate("robots") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
        
        // 机器人列表（主界面）
        composable("robots") {
            RobotListScreen(
                robots = robots,
                selectedRobotId = selectedRobot?.id,
                onRobotSelected = { robot ->
                    selectedRobot = robot
                    navController.navigate("chat/${robot.id}")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onAdminClick = {
                    navController.navigate("admin")
                },
                onLogoutClick = {
                    // 清除本地 token
                    tokenStore.clearTokens()
                    AppDependencies.authRepository.logout()
                    isLoggedIn.value = false
                    navController.navigate("login") {
                        popUpTo("robots") { inclusive = true }
                    }
                },
                isAdmin = tokenStore.isAdmin(),
                username = tokenStore.getUsername()
            )
        }
        
        // 聊天界面
        composable(
            route = "chat/{robotId}",
            arguments = listOf(navArgument("robotId") { type = NavType.LongType })
        ) { backStackEntry ->
            val robotId = backStackEntry.arguments?.getLong("robotId") ?: -1
            val robot = robots.find { it.id == robotId }
            
            val viewModel: ChatViewModel = viewModel {
                ChatViewModel(
                    application = application,
                    messageRepository = AppDependencies.messageRepository,
                    webSocketManager = AppDependencies.webSocketManager
                )
            }
            
            ChatScreen(
                robotName = robot?.name ?: "AI 助手",
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    tokenStore.clearTokens()
                    AppDependencies.authRepository.logout()
                    isLoggedIn.value = false
                    navController.navigate("login") {
                        popUpTo("chat/{robotId}") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
        
        // 设置界面
        composable("settings") {
            SettingsScreen(
                currentServerUrl = tokenStore.getServerUrl(),
                onSaveServerUrl = { url ->
                    tokenStore.saveServerUrl(url)
                    AppDependencies.updateServerUrl(url, application)
                    // 清除登录状态，需要重新登录
                    tokenStore.clearTokens()
                    isLoggedIn.value = false
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 管理员界面
        composable("admin") {
            // 示例推荐码
            val codes = remember {
                mutableStateListOf(
                    com.clawchannel.app.domain.model.RecommendationCode(
                        code = "ABC12345",
                        createdAt = System.currentTimeMillis() - 86400000, // 1天前
                        isUsed = false
                    ),
                    com.clawchannel.app.domain.model.RecommendationCode(
                        code = "XYZ98765",
                        createdAt = System.currentTimeMillis() - 172800000, // 2天前
                        isUsed = true
                    )
                )
            }
            
            AdminScreen(
                recommendationCodes = codes,
                onGenerateCode = {
                    // TODO: 调用 API 生成推荐码
                    val newCode = com.clawchannel.app.domain.model.RecommendationCode(
                        code = kotlin.random.Random.Default.nextBytes(4)
                            .joinToString("") { "%02X".format(it) },
                        createdAt = System.currentTimeMillis(),
                        isUsed = false
                    )
                    codes.add(0, newCode)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun ClawChannelTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = LobsterRed,
            secondary = LobsterRed.copy(alpha = 0.8f),
            tertiary = LobsterRed.copy(alpha = 0.6f)
        )
    } else {
        lightColorScheme(
            primary = LobsterRed,
            secondary = LobsterRed.copy(alpha = 0.8f),
            tertiary = LobsterRed.copy(alpha = 0.6f)
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}