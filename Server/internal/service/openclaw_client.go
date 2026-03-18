package service

import (
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/gorilla/websocket"
)

// ErrNotConnected 未连接错误
var ErrNotConnected = fmt.Errorf("not connected to OpenClaw")

// OpenClawMessage OpenClaw 消息结构
type OpenClawMessage struct {
	Type      string                 `json:"type"`
	Content   string                 `json:"content,omitempty"`
	SessionID string                 `json:"session_id,omitempty"`
	Metadata  map[string]interface{} `json:"metadata,omitempty"`
	Timestamp int64                  `json:"timestamp,omitempty"`
}

// OpenClawClient OpenClaw WebSocket 客户端
type OpenClawClient struct {
	Conn        *websocket.Conn
	URL         string
	SessionID   string
	mu          sync.RWMutex
	isConnected bool
	reconnectCh chan bool
	closeCh     chan struct{}
}

// NewOpenClawClient 创建 OpenClaw 客户端
func NewOpenClawClient(url string) *OpenClawClient {
	return &OpenClawClient{
		URL:         url,
		SessionID:   generateSessionID(),
		reconnectCh: make(chan bool, 1),
		closeCh:     make(chan struct{}),
	}
}

// Connect 连接到 OpenClaw
func (c *OpenClawClient) Connect() error {
	log.Printf("Connecting to OpenClaw: %s", c.URL)

	dialer := websocket.Dialer{
		HandshakeTimeout: 10 * time.Second,
	}

	conn, _, err := dialer.Dial(c.URL, nil)
	if err != nil {
		log.Printf("Failed to connect to OpenClaw: %v", err)
		return err
	}

	c.mu.Lock()
	c.Conn = conn
	c.isConnected = true
	c.mu.Unlock()

	log.Printf("Connected to OpenClaw successfully")

	// 启动心跳
	go c.startHeartbeat()

	// 启动消息读取
	go c.readMessages()

	return nil
}

// SendMessage 发送消息到 OpenClaw
func (c *OpenClawClient) SendMessage(content string) error {
	c.mu.RLock()
	if !c.isConnected || c.Conn == nil {
		c.mu.RUnlock()
		return ErrNotConnected
	}
	c.mu.RUnlock()

	msg := OpenClawMessage{
		Type:      "chat",
		Content:   content,
		SessionID: c.SessionID,
		Timestamp: time.Now().Unix(),
	}

	data, err := json.Marshal(msg)
	if err != nil {
		return err
	}

	c.mu.RLock()
	err = c.Conn.WriteMessage(websocket.TextMessage, data)
	c.mu.RUnlock()

	if err != nil {
		log.Printf("Failed to send message to OpenClaw: %v", err)
		c.handleDisconnect()
		return err
	}

	log.Printf("Message sent to OpenClaw: %s", content)
	return nil
}
func (c *OpenClawClient) readMessages() {
	for {
		c.mu.RLock()
		if !c.isConnected {
			c.mu.RUnlock()
			return
		}
		c.mu.RUnlock()

		_, message, err := c.Conn.ReadMessage()
		if err != nil {
			log.Printf("Failed to read message from OpenClaw: %v", err)
			c.handleDisconnect()
			return
		}

		log.Printf("Received from OpenClaw: %s", string(message))

		// 解析消息
		var msg OpenClawMessage
		if err := json.Unmarshal(message, &msg); err != nil {
			log.Printf("Failed to parse OpenClaw message: %v", err)
			continue
		}

		// TODO: 将消息转发给对应的客户端
		// 需要通过 Hub 或回调函数实现
	}
}

// startHeartbeat 启动心跳
func (c *OpenClawClient) startHeartbeat() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			if err := c.sendHeartbeat(); err != nil {
				log.Printf("Heartbeat failed: %v", err)
				c.handleDisconnect()
				return
			}
		case <-c.closeCh:
			return
		}
	}
}

// sendHeartbeat 发送心跳
func (c *OpenClawClient) sendHeartbeat() error {
	c.mu.RLock()
	if !c.isConnected || c.Conn == nil {
		c.mu.RUnlock()
		return ErrNotConnected
	}
	c.mu.RUnlock()

	heartbeat := OpenClawMessage{
		Type:      "heartbeat",
		Timestamp: time.Now().Unix(),
	}

	data, err := json.Marshal(heartbeat)
	if err != nil {
		return err
	}

	c.mu.RLock()
	err = c.Conn.WriteMessage(websocket.PingMessage, data)
	c.mu.RUnlock()

	return err
}

// handleDisconnect 处理断开连接
func (c *OpenClawClient) handleDisconnect() {
	c.mu.Lock()
	c.isConnected = false
	if c.Conn != nil {
		c.Conn.Close()
		c.Conn = nil
	}
	c.mu.Unlock()

	log.Printf("Disconnected from OpenClaw, attempting to reconnect...")

	// 尝试重连
	go c.reconnect()
}

// reconnect 重连逻辑
func (c *OpenClawClient) reconnect() {
	maxRetries := 5
	retryDelay := 5 * time.Second

	for i := 0; i < maxRetries; i++ {
		select {
		case <-c.closeCh:
			return
		default:
		}

		log.Printf("Reconnection attempt %d/%d", i+1, maxRetries)
		time.Sleep(retryDelay)

		if err := c.Connect(); err == nil {
			log.Printf("Reconnected to OpenClaw successfully")
			return
		}

		retryDelay *= 2 // 指数退避
	}

	log.Printf("Failed to reconnect to OpenClaw after %d attempts", maxRetries)
}

// Close 关闭连接
func (c *OpenClawClient) Close() error {
	close(c.closeCh)

	c.mu.Lock()
	defer c.mu.Unlock()

	if c.Conn != nil {
		return c.Conn.Close()
	}

	return nil
}

// IsConnected 检查连接状态
func (c *OpenClawClient) IsConnected() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.isConnected
}

// generateSessionID 生成会话 ID
func generateSessionID() string {
	return fmt.Sprintf("session_%d", time.Now().UnixNano())
}
