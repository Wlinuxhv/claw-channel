package database

import (
	"database/sql"
	"log"

	"github.com/clawchannel/server/internal/model"
	_ "github.com/mattn/go-sqlite3"
)

// Database 数据库结构
type Database struct {
	DB *sql.DB
}

// NewDatabase 创建数据库连接
func NewDatabase(dbPath string) (*Database, error) {
	db, err := sql.Open("sqlite3", dbPath)
	if err != nil {
		return nil, err
	}

	// 测试连接
	if err := db.Ping(); err != nil {
		return nil, err
	}

	log.Println("Database connected successfully")

	database := &Database{DB: db}

	// 初始化表
	if err := database.initTables(); err != nil {
		return nil, err
	}

	return database, nil
}

// initTables 初始化数据库表
func (d *Database) initTables() error {
	// 用户表
	_, err := d.DB.Exec(`
		CREATE TABLE IF NOT EXISTS users (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			recommendation_code TEXT NOT NULL UNIQUE,
			is_active BOOLEAN DEFAULT 1,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		return err
	}

	// 机器人表
	_, err = d.DB.Exec(`
		CREATE TABLE IF NOT EXISTS robots (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			name TEXT NOT NULL,
			app_key TEXT NOT NULL UNIQUE,
			secret_hash TEXT NOT NULL,
			openclaw_url TEXT NOT NULL,
			is_active BOOLEAN DEFAULT 1,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		return err
	}

	// Token 表
	_, err = d.DB.Exec(`
		CREATE TABLE IF NOT EXISTS tokens (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			user_id INTEGER REFERENCES users(id),
			access_token_hash TEXT NOT NULL UNIQUE,
			refresh_token_hash TEXT NOT NULL UNIQUE,
			expires_at DATETIME NOT NULL,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		return err
	}

	// 推荐码表
	_, err = d.DB.Exec(`
		CREATE TABLE IF NOT EXISTS recommendation_codes (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			code TEXT NOT NULL UNIQUE,
			is_used BOOLEAN DEFAULT 0,
			used_by INTEGER REFERENCES users(id),
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			used_at DATETIME
		)
	`)
	if err != nil {
		return err
	}

	// 消息日志表
	_, err = d.DB.Exec(`
		CREATE TABLE IF NOT EXISTS message_logs (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			user_id INTEGER REFERENCES users(id),
			robot_id INTEGER REFERENCES robots(id),
			session_id TEXT NOT NULL,
			message_id TEXT NOT NULL,
			content TEXT NOT NULL,
			direction TEXT NOT NULL,
			status TEXT DEFAULT 'sent',
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		return err
	}

	// 创建索引
	d.DB.Exec(`CREATE INDEX IF NOT EXISTS idx_tokens_access ON tokens(access_token_hash)`)
	d.DB.Exec(`CREATE INDEX IF NOT EXISTS idx_tokens_refresh ON tokens(refresh_token_hash)`)
	d.DB.Exec(`CREATE INDEX IF NOT EXISTS idx_message_logs_user ON message_logs(user_id)`)
	d.DB.Exec(`CREATE INDEX IF NOT EXISTS idx_message_logs_robot ON message_logs(robot_id)`)

	log.Println("Database tables initialized successfully")

	return nil
}

// Close 关闭数据库连接
func (d *Database) Close() error {
	if d.DB != nil {
		return d.DB.Close()
	}
	return nil
}

// SaveMessage 保存消息
func (d *Database) SaveMessage(msg *model.Message) error {
	_, err := d.DB.Exec(`
		INSERT INTO message_logs (user_id, robot_id, session_id, message_id, content, direction, status)
		VALUES (?, ?, ?, ?, ?, ?, ?)
	`, msg.UserID, msg.RobotID, msg.SessionID, msg.MessageID, msg.Content, msg.FromUser, msg.Status)
	return err
}

// GetUserMessages 获取用户消息
func (d *Database) GetUserMessages(userID int64, limit int) ([]model.Message, error) {
	rows, err := d.DB.Query(`
		SELECT id, user_id, robot_id, session_id, message_id, content, status, created_at
		FROM message_logs
		WHERE user_id = ?
		ORDER BY created_at DESC
		LIMIT ?
	`, userID, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var messages []model.Message
	for rows.Next() {
		var msg model.Message
		err := rows.Scan(&msg.ID, &msg.UserID, &msg.RobotID, &msg.SessionID, &msg.MessageID, &msg.Content, &msg.Status, &msg.CreatedAt)
		if err != nil {
			return nil, err
		}
		messages = append(messages, msg)
	}
	return messages, nil
}
