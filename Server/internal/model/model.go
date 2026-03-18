package model

import "time"

// User 用户
type User struct {
	ID                 int64     `json:"id"`
	RecommendationCode string    `json:"recommendation_code"`
	IsActive           bool      `json:"is_active"`
	CreatedAt          time.Time `json:"created_at"`
	UpdatedAt          time.Time `json:"updated_at"`
}

// Robot 机器人
type Robot struct {
	ID           int64     `json:"id"`
	Name         string    `json:"name"`
	AppKey       string    `json:"app_key"`
	SecretHash   string    `json:"-"` // 不返回给前端
	OpenClawURL  string    `json:"openclaw_url"`
	IsActive     bool      `json:"is_active"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

// RecommendationCode 推荐码
type RecommendationCode struct {
	ID        int64     `json:"id"`
	Code      string    `json:"code"`
	IsUsed    bool      `json:"is_used"`
	UsedBy    *int64    `json:"used_by,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	UsedAt    *time.Time `json:"used_at,omitempty"`
}

// Token Token
type Token struct {
	ID             int64     `json:"id"`
	UserID         int64     `json:"user_id"`
	AccessToken    string    `json:"-"`
	RefreshToken   string    `json:"-"`
	ExpiresAt      time.Time `json:"expires_at"`
	CreatedAt      time.Time `json:"created_at"`
}

// Message 消息
type Message struct {
	ID          int64      `json:"id"`
	UserID      int64      `json:"user_id"`
	RobotID     int64      `json:"robot_id"`
	SessionID   string     `json:"session_id"`
	MessageID   string     `json:"message_id"`
	FromUser    string     `json:"from_user"`
	Content     string     `json:"content"`
	ContentType string     `json:"content_type"`
	FilePath    *string    `json:"file_path,omitempty"`
	FileSize    *int64     `json:"file_size,omitempty"`
	Timestamp   int64      `json:"timestamp"`
	Status      string     `json:"status"`
	ReplyTo     *string    `json:"reply_to,omitempty"`
	IsFromAI    bool       `json:"is_from_ai"`
	CreatedAt   time.Time  `json:"created_at"`
}

// Database 数据库接口
type Database interface {
	SaveMessage(msg *Message) error
	GetUserMessages(userID int64, limit int) ([]Message, error)
}

// AdminLoginRequest 管理员登录请求
type AdminLoginRequest struct {
	Password string `json:"password"`
}

// UserLoginRequest 用户登录请求
type UserLoginRequest struct {
	RecommendationCode string `json:"recommendation_code"`
}

// TokenResponse Token 响应
type TokenResponse struct {
	AccessToken  string `json:"access_token"`
	TokenType    string `json:"token_type"`
	ExpiresIn    int64  `json:"expires_in"`
	RefreshToken string `json:"refresh_token"`
}

// APIResponse 通用 API 响应
type APIResponse struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}
