package service

import (
	"encoding/json"
	"log"
	"sync"
	"time"

	"github.com/clawchannel/server/internal/model"
	"github.com/clawchannel/server/internal/websocket"
)

// MessageForwarder 消息转发器
type MessageForwarder struct {
	hub            *websocket.Hub
	openclawClient *OpenClawClient
	db             model.Database
	userSessions   map[uint64]string // userID -> sessionID
	mu             sync.RWMutex
}

// NewMessageForwarder 创建消息转发器
func NewMessageForwarder(hub *websocket.Hub, openclawClient *OpenClawClient, db model.Database) *MessageForwarder {
	return &MessageForwarder{
		hub:            hub,
		openclawClient: openclawClient,
		db:             db,
		userSessions:   make(map[uint64]string),
	}
}

// Start 启动消息转发
func (f *MessageForwarder) Start() {
	log.Println("MessageForwarder started")
	// TODO: 实现从 Hub 接收消息并转发到 OpenClaw
}

// ForwardMessage 转发消息到 OpenClaw
func (f *MessageForwarder) ForwardMessage(userID uint64, content string) error {
	// 获取或创建会话
	sessionID := f.getSessionID(userID)

	// 保存消息到数据库
	msg := model.Message{
		UserID:    int64(userID),
		Content:   content,
		SessionID: sessionID,
		Status:    "sending",
		CreatedAt: time.Now(),
	}

	if err := f.db.SaveMessage(&msg); err != nil {
		log.Printf("Failed to save message: %v", err)
	}

	// 发送消息到 OpenClaw
	if err := f.openclawClient.SendMessage(content); err != nil {
		msg.Status = "failed"
		f.db.SaveMessage(&msg)
		return err
	}

	// 更新消息状态
	msg.Status = "sent"
	f.db.SaveMessage(&msg)

	// 发送确认给客户端
	f.sendToClient(userID, "message_sent", map[string]interface{}{
		"message_id": msg.ID,
		"status":     "sent",
	})

	return nil
}

// ForwardToOpenClaw 转发消息到 OpenClaw（从 WebSocket 客户端）
func (f *MessageForwarder) forwardToOpenClaw() {
	// 监听 Hub 的广播消息
	// 实际实现需要根据 Hub 的设计调整
}

// HandleOpenClawMessage 处理来自 OpenClaw 的消息
func (f *MessageForwarder) HandleOpenClawMessage(sessionID string, content string) {
	// 查找对应的用户
	userID := f.getUserBySession(sessionID)
	if userID == 0 {
		log.Printf("No user found for session: %s", sessionID)
		return
	}

	// 保存消息到数据库
	msg := model.Message{
		UserID:    int64(userID),
		Content:   content,
		SessionID: sessionID,
		IsFromAI:  true,
		Status:    "delivered",
		CreatedAt: time.Now(),
	}

	if err := f.db.SaveMessage(&msg); err != nil {
		log.Printf("Failed to save message: %v", err)
	}

	// 发送消息给客户端
	f.sendToClient(userID, "new_message", map[string]interface{}{
		"id":         msg.ID,
		"content":    content,
		"type":       "text",
		"from_ai":    true,
		"status":     "delivered",
		"created_at": msg.CreatedAt,
	})
}

// sendToClient 发送消息给客户端
func (f *MessageForwarder) sendToClient(userID uint64, msgType string, data map[string]interface{}) {
	message := map[string]interface{}{
		"type": msgType,
		"data": data,
	}

	jsonData, err := json.Marshal(message)
	if err != nil {
		log.Printf("Failed to marshal message: %v", err)
		return
	}

	// 通过 Hub 发送给用户
	f.hub.BroadcastToUser(int64(userID), jsonData)
}

// getSessionID 获取或创建会话 ID
func (f *MessageForwarder) getSessionID(userID uint64) string {
	f.mu.RLock()
	if sessionID, ok := f.userSessions[userID]; ok {
		f.mu.RUnlock()
		return sessionID
	}
	f.mu.RUnlock()

	// 创建新会话
	sessionID := generateSessionID()
	f.mu.Lock()
	f.userSessions[userID] = sessionID
	f.mu.Unlock()

	return sessionID
}

// getUserBySession 根据会话 ID 获取用户 ID
func (f *MessageForwarder) getUserBySession(sessionID string) uint64 {
	f.mu.RLock()
	defer f.mu.RUnlock()

	for userID, sid := range f.userSessions {
		if sid == sessionID {
			return userID
		}
	}

	return 0
}

// GetSessionStats 获取会话统计
func (f *MessageForwarder) GetSessionStats() map[string]interface{} {
	f.mu.RLock()
	defer f.mu.RUnlock()

	return map[string]interface{}{
		"total_sessions": len(f.userSessions),
		"openclaw_connected": f.openclawClient.IsConnected(),
	}
}
