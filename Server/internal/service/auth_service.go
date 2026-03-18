package service

import (
	"database/sql"
	"errors"
	"time"

	"github.com/clawchannel/server/internal/model"
	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

// AuthService 认证服务
type AuthService struct {
	db         *sql.DB
	jwtService *JWTService
}

// NewAuthService 创建认证服务
func NewAuthService(db *sql.DB, jwtService *JWTService) *AuthService {
	return &AuthService{
		db:         db,
		jwtService: jwtService,
	}
}

// LoginWithRecommendationCode 使用推荐码登录
func (s *AuthService) LoginWithRecommendationCode(code string) (*model.TokenResponse, error) {
	// 开启事务
	tx, err := s.db.Begin()
	if err != nil {
		return nil, err
	}
	defer tx.Rollback()

	// 1. 验证推荐码
	var recCode model.RecommendationCode
	err = tx.QueryRow(`
		SELECT id, code, is_used, used_by, created_at, used_at 
		FROM recommendation_codes 
		WHERE code = ? AND is_used = 0
	`, code).Scan(&recCode.ID, &recCode.Code, &recCode.IsUsed, &recCode.UsedBy, &recCode.CreatedAt, &recCode.UsedAt)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("invalid or used recommendation code")
		}
		return nil, err
	}

	// 2. 创建用户
	now := time.Now()
	result, err := tx.Exec(`
		INSERT INTO users (recommendation_code, is_active, created_at, updated_at) 
		VALUES (?, 1, ?, ?)
	`, code, now, now)

	if err != nil {
		return nil, err
	}

	userID, err := result.LastInsertId()
	if err != nil {
		return nil, err
	}

	// 3. 标记推荐码为已使用
	_, err = tx.Exec(`
		UPDATE recommendation_codes 
		SET is_used = 1, used_by = ?, used_at = ? 
		WHERE id = ?
	`, userID, now, recCode.ID)

	if err != nil {
		return nil, err
	}

	// 4. 生成 Token
	accessToken, refreshToken, err := s.jwtService.GenerateToken(userID)
	if err != nil {
		return nil, err
	}

	// 5. 保存 Token
	expiresAt := time.Now().Add(2 * time.Hour)
	_, err = tx.Exec(`
		INSERT INTO tokens (user_id, access_token_hash, refresh_token_hash, expires_at, created_at) 
		VALUES (?, ?, ?, ?, ?)
	`, userID, accessToken, refreshToken, expiresAt, now)

	if err != nil {
		return nil, err
	}

	// 提交事务
	if err := tx.Commit(); err != nil {
		return nil, err
	}

	return &model.TokenResponse{
		AccessToken:  accessToken,
		TokenType:    "Bearer",
		ExpiresIn:    7200,
		RefreshToken: refreshToken,
	}, nil
}

// RefreshToken 刷新 Token
func (s *AuthService) RefreshToken(refreshToken string) (*model.TokenResponse, error) {
	// 验证 Refresh Token
	claims, err := s.jwtService.ValidateRefreshToken(refreshToken)
	if err != nil {
		return nil, errors.New("invalid refresh token")
	}

	// 检查 Token 是否在数据库中
	var tokenID int64
	err = s.db.QueryRow(`
		SELECT id FROM tokens WHERE refresh_token_hash = ?
	`, refreshToken).Scan(&tokenID)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("token not found")
		}
		return nil, err
	}

	// 生成新的 Access Token
	newAccessToken, _, err := s.jwtService.GenerateToken(claims.UserID)
	if err != nil {
		return nil, err
	}

	return &model.TokenResponse{
		AccessToken: newAccessToken,
		TokenType:   "Bearer",
		ExpiresIn:   7200,
	}, nil
}

// ValidateAccessToken 验证 Access Token
func (s *AuthService) ValidateAccessToken(token string) (int64, error) {
	claims, err := s.jwtService.ValidateToken(token)
	if err != nil {
		return 0, err
	}

	// 检查 Token 是否在数据库中
	var tokenID int64
	err = s.db.QueryRow(`
		SELECT id FROM tokens WHERE access_token_hash = ?
	`, token).Scan(&tokenID)

	if err != nil {
		if err == sql.ErrNoRows {
			return 0, errors.New("token not found")
		}
		return 0, err
	}

	return claims.UserID, nil
}

// HashPassword 密码加密
func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), 12)
	return string(bytes), err
}

// CheckPassword 密码验证
func CheckPassword(password, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}

// GenerateRecommendationCode 生成推荐码
func GenerateRecommendationCode() string {
	return uuid.New().String()[:8] // 8 位 UUID 短码
}
