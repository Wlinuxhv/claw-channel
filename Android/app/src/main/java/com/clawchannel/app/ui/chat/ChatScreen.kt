package com.clawchannel.app.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clawchannel.app.data.remote.WebSocketManager.ConnectionState
import com.clawchannel.app.domain.model.Message
import com.clawchannel.app.domain.model.MessageStatus
import com.clawchannel.app.domain.model.MessageType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    robotName: String = "AI 助手",
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle(emptyList())
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 获取文件大小
            val fileSize = context.contentResolver.openFileDescriptor(it, "r")?.statSize ?: 0
            // 保存到本地并发送
            val filePath = saveUriToCache(context, it)
            if (filePath != null) {
                viewModel.sendImageMessage(filePath, fileSize)
            }
        }
    }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileSize = context.contentResolver.openFileDescriptor(it, "r")?.statSize ?: 0
            val fileName = getFileNameFromUri(context, it) ?: "unknown"
            val filePath = saveUriToCache(context, it)
            if (filePath != null) {
                viewModel.sendFileMessage(filePath, fileName, fileSize)
            }
        }
    }
    
    // 滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("🦞 $robotName")
                        Text(
                            text = when (val state = uiState.connectionState) {
                                is ConnectionState.Connected -> "● 在线"
                                is ConnectionState.Connecting -> "● 连接中..."
                                is ConnectionState.Error -> "● 已断开"
                                is ConnectionState.Disconnected -> "● 未连接"
                            },
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = when (uiState.connectionState) {
                                is ConnectionState.Connected -> Color(0xFF4CAF50)
                                is ConnectionState.Connecting -> Color.Gray
                                is ConnectionState.Error -> Color(0xFFF44336)
                                is ConnectionState.Disconnected -> Color.Gray
                            }
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "退出登录")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageItem(message)
                }
                
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "开始与 AI 对话吧！",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            // 输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 附件按钮
                IconButton(
                    onClick = { showAttachmentDialog = true }
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "添加附件",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    placeholder = { Text("输入消息...") },
                    maxLines = 4
                )
                
                IconButton(
                    onClick = {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank() && uiState.connectionState is ConnectionState.Connected
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (messageText.isNotBlank() && uiState.connectionState is ConnectionState.Connected)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray
                    )
                }
            }
        }
        
        // 附件选择对话框
        if (showAttachmentDialog) {
            AttachmentDialog(
                onDismiss = { showAttachmentDialog = false },
                onChooseImage = {
                    showAttachmentDialog = false
                    imagePickerLauncher.launch("*/*")
                },
                onChooseFile = {
                    showAttachmentDialog = false
                    filePickerLauncher.launch("*/*")
                }
            )
        }
    }
}

@Composable
fun AttachmentDialog(
    onDismiss: () -> Unit,
    onChooseImage: () -> Unit,
    onChooseFile: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "选择附件",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 图片选项
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        IconButton(
                            onClick = onChooseImage,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "选择图片",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("图片", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    // 文件选项
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        IconButton(
                            onClick = onChooseFile,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "选择文件",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("文件", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (message.isFromMe)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            // 根据消息类型显示内容
            when (message.type) {
                MessageType.IMAGE -> {
                    Text(
                        text = "📷 图片",
                        color = if (message.isFromMe)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (message.filePath != null) {
                        Text(
                            text = "已保存：${message.filePath}",
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = if (message.isFromMe)
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
                MessageType.FILE -> {
                    Text(
                        text = "📁 文件",
                        color = if (message.isFromMe)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = message.content,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (message.isFromMe)
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                else -> {
                    Text(
                        text = message.content,
                        color = if (message.isFromMe)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTime(message.timestamp),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = if (message.isFromMe)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                if (message.isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = when (message.status) {
                            MessageStatus.SENDING -> Icons.Default.Schedule
                            MessageStatus.SENT -> Icons.Default.Check
                            MessageStatus.DELIVERED -> Icons.Default.DoneAll
                            MessageStatus.FAILED -> Icons.Default.Error
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (message.isFromMe)
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

// 辅助函数：保存 URI 到缓存
private fun saveUriToCache(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val cacheFile = File(context.cacheDir, "attachment_${System.currentTimeMillis()}")
        inputStream?.use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        inputStream?.close()
        cacheFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// 辅助函数：从 URI 获取文件名
private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        fileName
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
