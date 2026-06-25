provider "aws" {
  region = var.aws_region
}

# --- VPC & Networking ---
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "SecureGenAI-VPC"
  }
}

resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 8, count.index)
  availability_zone = element(var.availability_zones, count.index)
  tags = {
    Name = "SecureGenAI-PrivateSubnet-${count.index + 1}"
  }
}

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(aws_vpc.main.cidr_block, 8, count.index + 10)
  availability_zone       = element(var.availability_zones, count.index)
  map_public_ip_on_launch = true
  tags = {
    Name = "SecureGenAI-PublicSubnet-${count.index + 1}"
  }
}

# --- CloudWatch Logs ---
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/securegenai-gateway"
  retention_in_days = 30
}

# --- ECS Fargate ---
resource "aws_ecs_cluster" "main" {
  name = "securegenai-cluster"
}

# Simplified single task definition representing the gateway
resource "aws_ecs_task_definition" "gateway" {
  family                   = "securegenai-gateway-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "1024"
  memory                   = "2048"

  container_definitions = jsonencode([{
    name      = "gateway-service"
    image     = "nginx:latest" # Placeholder for actual ECR image
    essential = true
    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
    }]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "gateway"
      }
    }
  }])
}

# --- RDS PostgreSQL ---
resource "aws_db_instance" "postgresql" {
  identifier           = "securegenai-db"
  allocated_storage    = 20
  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = "db.t3.micro"
  username             = "admin"
  password             = var.db_password # Sourced securely
  skip_final_snapshot  = true
  publicly_accessible  = false
}

# --- ElastiCache Redis ---
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "securegenai-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.0"
  port                 = 6379
}

# --- S3 Bucket for Archival ---
resource "aws_s3_bucket" "audit_archive" {
  bucket = "securegenai-audit-archive-${var.environment}"
}

# --- Secrets Manager ---
resource "aws_secretsmanager_secret" "db_credentials" {
  name = "securegenai/db-credentials-${var.environment}"
}

resource "aws_secretsmanager_secret_version" "db_credentials_version" {
  secret_id     = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = aws_db_instance.postgresql.username
    password = var.db_password
  })
}

# --- WAF Integration (Rate Limiting) ---
resource "aws_wafv2_web_acl" "rate_limiter" {
  name        = "securegenai-rate-limiter"
  scope       = "REGIONAL"
  description = "Rate limiting for SecureGenAI Gateway"

  default_action {
    allow {}
  }

  rule {
    name     = "RateLimitRule"
    priority = 1

    action {
      block {}
    }

    statement {
      rate_based_statement {
        limit              = 2000 # 2000 requests per 5 minutes per IP
        aggregate_key_type = "IP"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "RateLimitRuleMetric"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "SecureGenAIWAFMetric"
    sampled_requests_enabled   = true
  }
}
