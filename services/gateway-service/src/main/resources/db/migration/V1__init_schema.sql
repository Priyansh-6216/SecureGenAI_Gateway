CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE policies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    action VARCHAR(50) NOT NULL, -- ALLOW, WARN, BLOCK
    rule_type VARCHAR(100) NOT NULL, -- e.g., SSN, API_KEY
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE risk_assessments (
    id UUID PRIMARY KEY,
    risk_score INT NOT NULL,
    severity VARCHAR(50) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prompts (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    original_text TEXT NOT NULL,
    masked_text TEXT,
    risk_assessment_id UUID REFERENCES risk_assessments(id),
    status VARCHAR(50) NOT NULL, -- PENDING, ALLOWED, BLOCKED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE responses (
    id UUID PRIMARY KEY,
    prompt_id UUID REFERENCES prompts(id),
    provider VARCHAR(50) NOT NULL, -- OPENAI, BEDROCK, CLAUDE, GEMINI
    response_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
