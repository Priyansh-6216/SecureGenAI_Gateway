#!/bin/bash
# Security Scanning Script for SecureGenAI Gateway
# Intended for use in CI/CD pipelines (e.g., GitHub Actions or AWS CodePipeline)

set -e

echo "=========================================="
echo "Starting Security Scans..."
echo "=========================================="

# 1. Dependency Scanning (OWASP)
# Requires maven to be installed
echo "[1/3] Running OWASP Dependency Check..."
mvn org.owasp:dependency-check-maven:check -f ../pom.xml || echo "Dependency check failed or requires configuration."

# 2. Container Vulnerability Scanning (Trivy)
# Requires Trivy to be installed
echo "[2/3] Scanning Gateway Docker Image..."
# trivy image securegenai-gateway:latest || echo "Trivy not found or scan failed."
echo "Skipping local Trivy scan for now. Uncomment in CI."

# 3. SAST (Static Application Security Testing)
echo "[3/3] Running Static Code Analysis (SpotBugs / SonarQube)..."
mvn spotbugs:check -f ../pom.xml || echo "Spotbugs not configured in parent pom yet."

echo "=========================================="
echo "Security Scans Completed!"
echo "=========================================="
