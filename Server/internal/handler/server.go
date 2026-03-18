package handler

import (
	"encoding/json"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/clawchannel/server/internal/config"
	"github.com/clawchannel/server/internal/model"
	"github.com/clawchannel/server/internal/service"
	ws "github.com/clawchannel/server/internal/websocket"
	"github.com/gorilla/websocket"
)

// Server HTTP 服务器
type Server struct {
	httpServer *http.Server
	config     *config.Config
	hub        *ws.Hub
	mux        *http.ServeMux
	authService *service.AuthService
	robotService *service.RobotService
}

// NewServer 创建服务器
func NewServer(cfg *config.Config, hub *ws.Hub, authService *service.AuthService, robotService *service.RobotService) *Server {
	s := &Server{
		config:       cfg,
		hub:          hub,
		authService:  authService,
		robotService: robotService,
		mux:          http.NewServeMux(),
	}

	s.setupRoutes()

	s.httpServer = &http.Server{
		Addr:         ":" + cfg.ServerPort,
		Handler:      s.mux,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
	}

	return s
}

// Start 启动服务器
func (s *Server) Start() error {
	log.Printf("Server starting on port %s", s.config.ServerPort)
	return s.httpServer.ListenAndServe()
}

// Stop 停止服务器
func (s *Server) Stop() error {
	return s.httpServer.Close()
}

// setupRoutes 设置路由
func (s *Server) setupRoutes() {
	// 健康检查
	s.mux.HandleFunc("/api/health", s.healthHandler)

	// 认证接口
	s.mux.HandleFunc("/api/auth/login", s.loginHandler)
	s.mux.HandleFunc("/api/auth/refresh", s.refreshHandler)

	// WebSocket 接口
	s.mux.HandleFunc("/ws/chat", s.websocketHandler)

	// 管理员接口
	s.mux.HandleFunc("/api/admin/login", s.adminLoginHandler)
	s.mux.HandleFunc("/api/admin/robots", s.adminRobotsHandler)
	s.mux.HandleFunc("/api/admin/robots/", s.adminRobotHandler)
	s.mux.HandleFunc("/api/admin/recommendation-codes", s.adminRecommendationCodesHandler)
	s.mux.HandleFunc("/api/admin/stats", s.adminStatsHandler)
}

// healthHandler 健康检查
func (s *Server) healthHandler(w http.ResponseWriter, r *http.Request) {
	response := model.APIResponse{
		Code:    0,
		Message: "ok",
		Data: map[string]interface{}{
			"status":    "ok",
			"timestamp": time.Now().Unix(),
			"clients":   s.hub.ClientCount(),
		},
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// loginHandler 用户登录
func (s *Server) loginHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req model.UserLoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response := model.APIResponse{
			Code:    400,
			Message: "Invalid request body",
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(response)
		return
	}

	// 使用推荐码登录
	tokenResp, err := s.authService.LoginWithRecommendationCode(req.RecommendationCode)
	if err != nil {
		response := model.APIResponse{
			Code:    401,
			Message: err.Error(),
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusUnauthorized)
		json.NewEncoder(w).Encode(response)
		return
	}

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data:    tokenResp,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
	log.Printf("User logged in with recommendation code: %s", req.RecommendationCode)
}

// refreshHandler 刷新 Token
func (s *Server) refreshHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req struct {
		RefreshToken string `json:"refresh_token"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response := model.APIResponse{
			Code:    400,
			Message: "Invalid request body",
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(response)
		return
	}

	tokenResp, err := s.authService.RefreshToken(req.RefreshToken)
	if err != nil {
		response := model.APIResponse{
			Code:    401,
			Message: err.Error(),
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusUnauthorized)
		json.NewEncoder(w).Encode(response)
		return
	}

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data:    tokenResp,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// websocketHandler WebSocket 连接
func (s *Server) websocketHandler(w http.ResponseWriter, r *http.Request) {
	// 获取 Token
	token := r.URL.Query().Get("token")
	if token == "" {
		// 尝试从 Authorization header 获取
		authHeader := r.Header.Get("Authorization")
		if authHeader != "" && strings.HasPrefix(authHeader, "Bearer ") {
			token = strings.TrimPrefix(authHeader, "Bearer ")
		}
	}

	if token == "" {
		http.Error(w, "Missing token", http.StatusUnauthorized)
		return
	}

	// 验证 Token
	userID, err := s.authService.ValidateAccessToken(token)
	if err != nil {
		http.Error(w, "Invalid token", http.StatusUnauthorized)
		return
	}

	var upgrader = websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
		CheckOrigin: func(r *http.Request) bool {
			return true // 允许所有来源
		},
	}

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("WebSocket upgrade error: %v", err)
		return
	}

	// 创建客户端
	client := &ws.Client{
		Hub:    s.hub,
		Conn:   conn,
		Send:   make(chan []byte, 256),
		ID:     "client_" + time.Now().Format("150405") + "_" + string(rune(userID)),
		UserID: userID,
	}

	// 注册客户端
	client.Hub.RegisterClient(client)

	// 启动客户端协程
	go client.WritePump()
	go client.ReadPump()

	log.Printf("WebSocket connection established: %s, UserID: %d", client.ID, client.UserID)
}

// adminLoginHandler 管理员登录
func (s *Server) adminLoginHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req model.AdminLoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response := model.APIResponse{
			Code:    400,
			Message: "Invalid request body",
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(response)
		return
	}

	// 验证管理员密码
	if !s.robotService.ValidateAdmin(req.Password) {
		response := model.APIResponse{
			Code:    401,
			Message: "Invalid admin password",
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusUnauthorized)
		json.NewEncoder(w).Encode(response)
		return
	}

	// 生成管理员 Token（简化处理，实际应该使用 JWT）
	adminToken := "admin_" + time.Now().Format("20060102150405")

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data: map[string]interface{}{
			"admin_token": adminToken,
			"expires_in":  7200,
		},
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// adminRobotsHandler 管理员机器人管理
func (s *Server) adminRobotsHandler(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodGet:
		s.getRobots(w, r)
	case http.MethodPost:
		s.createRobot(w, r)
	default:
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
	}
}

// getRobots 获取机器人列表
func (s *Server) getRobots(w http.ResponseWriter, r *http.Request) {
	robots, err := s.robotService.GetAllRobots()
	if err != nil {
		response := model.APIResponse{
			Code:    500,
			Message: err.Error(),
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(response)
		return
	}

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data:    robots,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// createRobot 创建机器人
func (s *Server) createRobot(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Name        string `json:"name"`
		AppKey      string `json:"app_key"`
		Secret      string `json:"secret"`
		OpenClawURL string `json:"openclaw_url"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response := model.APIResponse{
			Code:    400,
			Message: "Invalid request body",
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(response)
		return
	}

	robot, err := s.robotService.CreateRobot(req.Name, req.AppKey, req.Secret, req.OpenClawURL)
	if err != nil {
		response := model.APIResponse{
			Code:    500,
			Message: err.Error(),
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(response)
		return
	}

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data:    robot,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
	log.Printf("Robot created: %s (%s)", robot.Name, robot.AppKey)
}

// adminRobotHandler 单个机器人管理
func (s *Server) adminRobotHandler(w http.ResponseWriter, r *http.Request) {
	// 从 URL 获取机器人 ID
	path := strings.TrimPrefix(r.URL.Path, "/api/admin/robots/")
	if path == "" {
		http.Error(w, "Robot ID required", http.StatusBadRequest)
		return
	}

	// 简化处理，实际应该解析 ID
	_ = path

	switch r.Method {
	case http.MethodPut:
		// 更新机器人
		http.Error(w, "Not implemented", http.StatusNotImplemented)
	case http.MethodDelete:
		// 删除机器人
		http.Error(w, "Not implemented", http.StatusNotImplemented)
	default:
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
	}
}

// adminRecommendationCodesHandler 推荐码管理
func (s *Server) adminRecommendationCodesHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	// 生成推荐码
	code, err := s.robotService.CreateRecommendationCode()
	if err != nil {
		response := model.APIResponse{
			Code:    500,
			Message: err.Error(),
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(response)
		return
	}

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data:    code,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
	log.Printf("Recommendation code generated: %s", code.Code)
}

// adminStatsHandler 统计信息
func (s *Server) adminStatsHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	stats, err := s.robotService.GetStats()
	if err != nil {
		response := model.APIResponse{
			Code:    500,
			Message: err.Error(),
		}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(response)
		return
	}

	// 添加在线用户数
	stats["online_users"] = s.hub.ClientCount()

	response := model.APIResponse{
		Code:    0,
		Message: "success",
		Data:    stats,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}
