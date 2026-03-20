package com.clawchannel.app.ui.robots

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clawchannel.app.domain.model.Robot
import com.clawchannel.app.domain.model.RobotStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RobotListScreen(
    robots: List<Robot>,
    selectedRobotId: Long?,
    onRobotSelected: (Robot) -> Unit,
    onSettingsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isAdmin: Boolean,
    username: String?
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "🦞 Claw Channel",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isAdmin) {
                            DropdownMenuItem(
                                text = { Text("管理员后台") },
                                onClick = {
                                    showMenu = false
                                    onAdminClick()
                                },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("退出登录") },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Logout, null) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 用户信息
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = username ?: "用户",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isAdmin) "管理员" else "普通用户",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 机器人列表标题
            Text(
                text = "我的机器人",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
            
            if (robots.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🦞",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "还没有机器人",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "联系管理员添加机器人",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 机器人列表
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(robots) { robot ->
                        RobotItem(
                            robot = robot,
                            isSelected = robot.id == selectedRobotId,
                            onClick = { onRobotSelected(robot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RobotItem(
    robot: Robot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = robot.avatar ?: "🦞",
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = robot.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 状态指示器
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (robot.status) {
                                    RobotStatus.ONLINE -> MaterialTheme.colorScheme.primary
                                    RobotStatus.BUSY -> MaterialTheme.colorScheme.tertiary
                                    RobotStatus.OFFLINE -> MaterialTheme.colorScheme.outline
                                },
                                shape = MaterialTheme.shapes.extraSmall
                            )
                    )
                }
                
                robot.lastMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // 未读数
            if (robot.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (robot.unreadCount > 99) "99+" else robot.unreadCount.toString()
                    )
                }
            }
        }
    }
}