package service

import (
	"database/sql"
	"errors"
	"time"

	"github.com/clawchannel/server/internal/model"
)

// RobotService 机器人服务
type RobotService struct {
	db *sql.DB
}

// NewRobotService 创建机器人服务
func NewRobotService(db *sql.DB) *RobotService {
	return &RobotService{
		db: db,
	}
}

// CreateRobot 创建机器人
func (s *RobotService) CreateRobot(name, appKey, secret, openclawURL string) (*model.Robot, error) {
	// 加密 Secret
	secretHash, err := HashPassword(secret)
	if err != nil {
		return nil, err
	}

	now := time.Now()
	result, err := s.db.Exec(`
		INSERT INTO robots (name, app_key, secret_hash, openclaw_url, is_active, created_at, updated_at) 
		VALUES (?, ?, ?, ?, 1, ?, ?)
	`, name, appKey, secretHash, openclawURL, now, now)

	if err != nil {
		return nil, err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return nil, err
	}

	return &model.Robot{
		ID:          id,
		Name:        name,
		AppKey:      appKey,
		SecretHash:  secretHash,
		OpenClawURL: openclawURL,
		IsActive:    true,
		CreatedAt:   now,
		UpdatedAt:   now,
	}, nil
}

// GetRobotByID 根据 ID 获取机器人
func (s *RobotService) GetRobotByID(id int64) (*model.Robot, error) {
	robot := &model.Robot{}
	err := s.db.QueryRow(`
		SELECT id, name, app_key, secret_hash, openclaw_url, is_active, created_at, updated_at 
		FROM robots WHERE id = ?
	`, id).Scan(
		&robot.ID, &robot.Name, &robot.AppKey, &robot.SecretHash,
		&robot.OpenClawURL, &robot.IsActive, &robot.CreatedAt, &robot.UpdatedAt,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("robot not found")
		}
		return nil, err
	}

	return robot, nil
}

// GetAllRobots 获取所有机器人
func (s *RobotService) GetAllRobots() ([]*model.Robot, error) {
	rows, err := s.db.Query(`
		SELECT id, name, app_key, secret_hash, openclaw_url, is_active, created_at, updated_at 
		FROM robots ORDER BY created_at DESC
	`)

	if err != nil {
		return nil, err
	}
	defer rows.Close()

	robots := make([]*model.Robot, 0)
	for rows.Next() {
		robot := &model.Robot{}
		err := rows.Scan(
			&robot.ID, &robot.Name, &robot.AppKey, &robot.SecretHash,
			&robot.OpenClawURL, &robot.IsActive, &robot.CreatedAt, &robot.UpdatedAt,
		)

		if err != nil {
			return nil, err
		}

		robots = append(robots, robot)
	}

	return robots, nil
}

// UpdateRobot 更新机器人
func (s *RobotService) UpdateRobot(id int64, name, appKey, secret, openclawURL string) (*model.Robot, error) {
	now := time.Now()

	var err error
	if secret != "" {
		// 如果提供了新 Secret，重新加密
		secretHash, err := HashPassword(secret)
		if err != nil {
			return nil, err
		}

		_, err = s.db.Exec(`
			UPDATE robots 
			SET name = ?, app_key = ?, secret_hash = ?, openclaw_url = ?, updated_at = ? 
			WHERE id = ?
		`, name, appKey, secretHash, openclawURL, now, id)
	} else {
		// 不更新 Secret
		_, err = s.db.Exec(`
			UPDATE robots 
			SET name = ?, app_key = ?, openclaw_url = ?, updated_at = ? 
			WHERE id = ?
		`, name, appKey, openclawURL, now, id)
	}

	if err != nil {
		return nil, err
	}

	return s.GetRobotByID(id)
}

// DeleteRobot 删除机器人
func (s *RobotService) DeleteRobot(id int64) error {
	_, err := s.db.Exec(`DELETE FROM robots WHERE id = ?`, id)
	return err
}

// VerifyRobot 验证机器人凭证
func (s *RobotService) VerifyRobot(appKey, secret string) (*model.Robot, error) {
	robot := &model.Robot{}
	err := s.db.QueryRow(`
		SELECT id, name, app_key, secret_hash, openclaw_url, is_active, created_at, updated_at 
		FROM robots WHERE app_key = ? AND is_active = 1
	`, appKey).Scan(
		&robot.ID, &robot.Name, &robot.AppKey, &robot.SecretHash,
		&robot.OpenClawURL, &robot.IsActive, &robot.CreatedAt, &robot.UpdatedAt,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("robot not found")
		}
		return nil, err
	}

	// 验证 Secret
	if !CheckPassword(secret, robot.SecretHash) {
		return nil, errors.New("invalid secret")
	}

	return robot, nil
}

// CreateRecommendationCode 创建推荐码
func (s *RobotService) CreateRecommendationCode() (*model.RecommendationCode, error) {
	code := GenerateRecommendationCode()
	now := time.Now()

	result, err := s.db.Exec(`
		INSERT INTO recommendation_codes (code, is_used, created_at) 
		VALUES (?, 0, ?)
	`, code, now)

	if err != nil {
		return nil, err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return nil, err
	}

	return &model.RecommendationCode{
		ID:        id,
		Code:      code,
		IsUsed:    false,
		CreatedAt: now,
	}, nil
}

// GetAllRecommendationCodes 获取所有推荐码
func (s *RobotService) GetAllRecommendationCodes() ([]*model.RecommendationCode, error) {
	rows, err := s.db.Query(`
		SELECT id, code, is_used, used_by, created_at, used_at 
		FROM recommendation_codes ORDER BY created_at DESC
	`)

	if err != nil {
		return nil, err
	}
	defer rows.Close()

	codes := make([]*model.RecommendationCode, 0)
	for rows.Next() {
		code := &model.RecommendationCode{}
		var usedBy sql.NullInt64
		var usedAt sql.NullTime

		err := rows.Scan(&code.ID, &code.Code, &code.IsUsed, &usedBy, &code.CreatedAt, &usedAt)
		if err != nil {
			return nil, err
		}

		if usedBy.Valid {
			code.UsedBy = &usedBy.Int64
		}
		if usedAt.Valid {
			code.UsedAt = &usedAt.Time
		}

		codes = append(codes, code)
	}

	return codes, nil
}

// ValidateAdmin 验证管理员密码
func (s *RobotService) ValidateAdmin(password string) bool {
	// 从环境变量获取管理员密码（在 config 中）
	// 这里简化处理，实际应该从数据库或配置读取
	return password == "admin123" // 需要替换为实际配置
}

// GetStats 获取统计信息
func (s *RobotService) GetStats() (map[string]interface{}, error) {
	stats := make(map[string]interface{})

	// 在线用户数（WebSocket 连接数，由 WebSocket Hub 提供）
	// 这里只返回数据库统计

	// 用户总数
	var userCount int64
	err := s.db.QueryRow(`SELECT COUNT(*) FROM users`).Scan(&userCount)
	if err != nil {
		return nil, err
	}
	stats["total_users"] = userCount

	// 机器人总数
	var robotCount int64
	err = s.db.QueryRow(`SELECT COUNT(*) FROM robots`).Scan(&robotCount)
	if err != nil {
		return nil, err
	}
	stats["total_robots"] = robotCount

	// 推荐码总数
	var codeCount int64
	err = s.db.QueryRow(`SELECT COUNT(*) FROM recommendation_codes`).Scan(&codeCount)
	if err != nil {
		return nil, err
	}
	stats["total_codes"] = codeCount

	// 已使用推荐码数
	var usedCodeCount int64
	err = s.db.QueryRow(`SELECT COUNT(*) FROM recommendation_codes WHERE is_used = 1`).Scan(&usedCodeCount)
	if err != nil {
		return nil, err
	}
	stats["used_codes"] = usedCodeCount

	return stats, nil
}
