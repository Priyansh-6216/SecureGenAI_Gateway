import http from 'k6/http';
import { check, sleep } from 'k6';

// Load testing script for SecureGenAI Gateway
export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up to 50 users
    { duration: '1m', target: 50 },   // Stay at 50 users
    { duration: '30s', target: 100 }, // Spike to 100 users
    { duration: '1m', target: 100 },  // Stay at 100 users
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.01'],   // Error rate must be less than 1%
  },
};

export default function () {
  const url = 'http://localhost:8080/api/v1/gateway/process'; // Adjust URL to ALB or local environment
  const payload = JSON.stringify({
    userId: "test-user-123",
    prompt: "Hello, what is the capital of France?",
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer placeholder-jwt-token'
    },
  };

  const res = http.post(url, payload, params);
  
  check(res, {
    'is status 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
