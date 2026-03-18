package main

import (
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/clawchannel/server/internal/config"
	"github.com/clawchannel/server/internal/database"
	"github.com/clawchannel/server/internal/handler"
	"github.com/clawchannel/server/internal/service"
	ws "github.com/clawchannel/server/internal/websocket"
)

func main() {
	log.Println("🦞 Claw Channel Server starting...")

	// 加载配置
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}
	log.Printf("Config loaded: ServerPort=%s", cfg.ServerPort)

	// 初始化数据库
	dbPath := "data/claw.db"
	if cfg.DatabaseURL != "" {
		// 解析 SQLite 路径
		if len(cfg.DatabaseURL) > 10 && cfg.DatabaseURL[:10] == "sqlite:///" {
			dbPath = cfg.DatabaseURL[10:]
		}
	}

	// 确保数据目录存在
	os.MkdirAll("data", 0755)

	database, err := database.NewDatabase(dbPath)
	if err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}
	defer database.Close()
	log.Println("Database initialized")

	// 初始化 WebSocket Hub
	hub := ws.NewHub()
	go hub.Run()
	log.Println("WebSocket Hub started")

	// 初始化服务
	jwtService := service.NewJWTService(cfg.JWTSecret)
	authService := service.NewAuthService(database.DB, jwtService)
	robotService := service.NewRobotService(database.DB)
	log.Println("Services initialized")

	// 初始化 OpenClaw 客户端
	openclawURL := cfg.OpenClawWSURL
	if openclawURL == "" {
		openclawURL = "ws://localhost:18789/ws"
	}
	openclawClient := service.NewOpenClawClient(openclawURL)
	
	// 连接到 OpenClaw（可选）
	go func() {
		if err := openclawClient.Connect(); err != nil {
			log.Printf("Warning: Failed to connect to OpenClaw: %v", err)
			log.Println("Server will run without OpenClaw connection")
		}
	}()

	// 初始化消息转发器
	messageForwarder := service.NewMessageForwarder(hub, openclawClient, database)
	messageForwarder.Start()
	log.Println("MessageForwarder initialized")

	// 创建并启动 HTTP 服务器
	server := handler.NewServer(cfg, hub, authService, robotService)

	// 优雅关闭
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)

	go func() {
		if err := server.Start(); err != nil {
			log.Fatalf("Server failed: %v", err)
		}
	}()

	log.Println("🦞 Claw Channel Server is running!")
	log.Println("Press Ctrl+C to stop")

	<-quit
	log.Println("Shutting down server...")
	if err := server.Stop(); err != nil {
		log.Printf("Error stopping server: %v", err)
	}
	log.Println("🦞 Claw Channel Server stopped")
}
