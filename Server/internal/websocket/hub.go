package websocket

import (
	"log"
	"sync"

	"github.com/gorilla/websocket"
)

// Client WebSocket 客户端
type Client struct {
	Hub    *Hub
	Conn   *websocket.Conn
	Send   chan []byte
	ID     string
	UserID int64
	RobotID int64
}

// Hub WebSocket 连接中心
type Hub struct {
	clients    map[*Client]bool
	broadcast  chan []byte
	register   chan *Client
	unregister chan *Client
	mu         sync.RWMutex
}

// NewHub 创建 Hub
func NewHub() *Hub {
	return &Hub{
		clients:    make(map[*Client]bool),
		broadcast:  make(chan []byte),
		register:   make(chan *Client),
		unregister: make(chan *Client),
	}
}

// Run 运行 Hub
func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.mu.Lock()
			h.clients[client] = true
			h.mu.Unlock()
			log.Printf("Client registered: %s, UserID: %d, total clients: %d", client.ID, client.UserID, len(h.clients))

		case client := <-h.unregister:
			h.mu.Lock()
			if _, ok := h.clients[client]; ok {
				delete(h.clients, client)
				close(client.Send)
			}
			h.mu.Unlock()
			log.Printf("Client unregistered: %s, UserID: %d, total clients: %d", client.ID, client.UserID, len(h.clients))

		case message := <-h.broadcast:
			h.mu.RLock()
			for client := range h.clients {
				select {
				case client.Send <- message:
				default:
					close(client.Send)
					delete(h.clients, client)
				}
			}
			h.mu.RUnlock()
		}
	}
}

// Broadcast 广播消息
func (h *Hub) Broadcast(message []byte) {
	h.broadcast <- message
}

// SendToUser 发送消息给特定用户（别名）
func (h *Hub) SendToUser(userID int64, message []byte) {
	h.BroadcastToUser(userID, message)
}

// RegisterClient 注册客户端
func (h *Hub) RegisterClient(client *Client) {
	h.register <- client
}

// UnregisterClient 注销客户端
func (h *Hub) UnregisterClient(client *Client) {
	h.unregister <- client
}

// BroadcastToUser 发送消息给特定用户
func (h *Hub) BroadcastToUser(userID int64, message []byte) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	for client := range h.clients {
		if client.UserID == userID {
			select {
			case client.Send <- message:
			default:
				close(client.Send)
			}
		}
	}
}

// ClientCount 客户端数量
func (h *Hub) ClientCount() int {
	h.mu.RLock()
	defer h.mu.RUnlock()
	return len(h.clients)
}

// GetClient 获取客户端
func (h *Hub) GetClient(userID int64) *Client {
	h.mu.RLock()
	defer h.mu.RUnlock()

	for client := range h.clients {
		if client.UserID == userID {
			return client
		}
	}
	return nil
}
