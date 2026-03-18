package config

import (
	"os"
)

// Config 服务器配置
type Config struct {
	ServerPort        string
	JWTSecret         string
	DatabaseURL       string
	AdminPassword     string
	ZeroTierNetworkID string
	OpenClawWSURL     string
}

// LoadConfig 加载配置
func LoadConfig() (*Config, error) {
	cfg := &Config{
		ServerPort:        getEnv("SERVER_PORT", "8080"),
		JWTSecret:         getEnv("JWT_SECRET", "claw-channel-secret-key-change-in-production"),
		DatabaseURL:       getEnv("DATABASE_URL", "sqlite:///data/claw.db"),
		AdminPassword:     getEnv("ADMIN_PASSWORD", "admin123"), // 生产环境必须修改
		ZeroTierNetworkID: getEnv("ZEROTIER_NETWORK_ID", ""),
		OpenClawWSURL:     getEnv("OPENCLAW_WS_URL", "ws://localhost:18789/ws"),
	}

	return cfg, nil
}

// getEnv 获取环境变量
func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}
