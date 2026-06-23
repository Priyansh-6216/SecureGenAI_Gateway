import React, { useState } from 'react';
import {
  ThemeProvider,
  createTheme,
  CssBaseline,
  Box,
  Container,
  Grid,
  Paper,
  Typography,
  AppBar,
  Toolbar,
  Button,
  TextField,
  Chip,
  LinearProgress,
  Card,
  CardContent,
  Divider,
  Avatar,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Shield as ShieldIcon,
  Policy as PolicyIcon,
  Lock as LockIcon,
  Security as SecurityIcon,
  Speed as SpeedIcon,
  Assessment as AssessmentIcon,
  Notifications as NotificationsIcon,
  Settings as SettingsIcon,
  Send as SendIcon,
  CloudDone as CloudDoneIcon,
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  PlayArrow as PlayArrowIcon,
  Dashboard as DashboardIcon,
  AccountTree as AccountTreeIcon,
} from '@mui/icons-material';

import SecurityDashboard from './components/dashboard/SecurityDashboard';

// Create the premium theme matching our design system
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#a855f7', // Vibrant purple
      light: '#c084fc',
      dark: '#7e22ce',
    },
    secondary: {
      main: '#3b82f6', // Bright blue
      light: '#60a5fa',
      dark: '#1d4ed8',
    },
    background: {
      default: '#0a0b10', // Dark space background
      paper: '#12131a', // Sleek paper background
    },
    text: {
      primary: '#f8fafc',
      secondary: '#94a3b8',
    },
    success: {
      main: '#22c55e',
    },
    warning: {
      main: '#eab308',
    },
    error: {
      main: '#ef4444',
    },
  },
  typography: {
    fontFamily: '"Inter", "Helvetica", "Arial", sans-serif',
    h1: {
      fontFamily: '"Outfit", sans-serif',
      fontWeight: 800,
    },
    h2: {
      fontFamily: '"Outfit", sans-serif',
      fontWeight: 700,
    },
    h3: {
      fontFamily: '"Outfit", sans-serif',
      fontWeight: 700,
    },
    h4: {
      fontFamily: '"Outfit", sans-serif',
      fontWeight: 600,
    },
    h5: {
      fontFamily: '"Outfit", sans-serif',
      fontWeight: 600,
    },
    h6: {
      fontFamily: '"Outfit", sans-serif',
      fontWeight: 600,
    },
    subtitle1: {
      fontFamily: '"Inter", sans-serif',
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 600,
          padding: '8px 20px',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          border: '1px solid rgba(255, 255, 255, 0.06)',
          backgroundImage: 'none',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          border: '1px solid rgba(255, 255, 255, 0.06)',
          backgroundImage: 'none',
        },
      },
    },
  },
});

interface ServiceStatus {
  name: string;
  purpose: string;
  responsibilities: string[];
  status: 'active' | 'planned';
  icon: React.ReactNode;
}

function App() {
  const [promptInput, setPromptInput] = useState(
    "Hi, please check this server logs: user: admin, secret_key: sk-live-28f9a239c819bc2, email: support@securegenai.com. Also, here is our customer's SSN: 666-29-9182 for testing."
  );
  const [simulationResult, setSimulationResult] = useState<any>(null);
  const [isSimulating, setIsSimulating] = useState(false);
  const [activeView, setActiveView] = useState<'simulator' | 'dashboard'>('dashboard');

  const services: ServiceStatus[] = [
    {
      name: 'gateway-service',
      purpose: 'Entry point for all AI requests.',
      responsibilities: ['Request routing', 'Authentication', 'Rate limiting', 'Tenant identification'],
      status: 'active',
      icon: <SecurityIcon color="primary" />,
    },
    {
      name: 'policy-service',
      purpose: 'Policy evaluation.',
      responsibilities: ['Prompt validation', 'Security rule checks', 'Organizational policies'],
      status: 'planned',
      icon: <PolicyIcon color="secondary" />,
    },
    {
      name: 'pii-service',
      purpose: 'Sensitive data detection.',
      responsibilities: ['PII scanning', 'Entity recognition', 'Data classification'],
      status: 'planned',
      icon: <LockIcon color="secondary" />,
    },
    {
      name: 'masking-service',
      purpose: 'Data protection.',
      responsibilities: ['Email & SSN masking', 'Credit card masking', 'Source code masking'],
      status: 'planned',
      icon: <ShieldIcon color="secondary" />,
    },
    {
      name: 'risk-engine-service',
      purpose: 'Threat scoring.',
      responsibilities: ['Risk calculation', 'Security recommendations'],
      status: 'planned',
      icon: <SpeedIcon color="secondary" />,
    },
    {
      name: 'llm-adapter-service',
      purpose: 'Provider abstraction.',
      responsibilities: ['OpenAI Integration', 'AWS Bedrock Integration', 'Claude & Gemini Integration'],
      status: 'planned',
      icon: <CloudDoneIcon color="secondary" />,
    },
    {
      name: 'audit-service',
      purpose: 'Compliance tracking.',
      responsibilities: ['Prompt & Response logging', 'Audit reports', 'Archival logs'],
      status: 'planned',
      icon: <AssessmentIcon color="secondary" />,
    },
    {
      name: 'notification-service',
      purpose: 'Security alerts.',
      responsibilities: ['Slack notifications', 'Email notifications', 'Risk alerts'],
      status: 'planned',
      icon: <NotificationsIcon color="secondary" />,
    },
  ];

  const handleSimulate = () => {
    setIsSimulating(true);
    setSimulationResult(null);

    setTimeout(() => {
      const hasSSN = /\b\d{3}-\d{2}-\d{4}\b/.test(promptInput);
      const hasEmail = /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/.test(promptInput);
      const hasSecret = /(?:secret|key|api|sk-[a-zA-Z0-9]{12,})/i.test(promptInput);
      const lineCount = promptInput.split('\n').length;
      const hasSourceCode = lineCount > 10 || /function|import|class|const\s+\w+\s*=\s*/.test(promptInput);

      let maskedPrompt = promptInput;
      const detectedEntities: string[] = [];

      if (hasSSN) {
        detectedEntities.push('SSN (Social Security Number)');
        maskedPrompt = maskedPrompt.replace(/\b\d{3}-\d{2}-\d{4}\b/g, '***-**-****');
      }
      if (hasEmail) {
        detectedEntities.push('Email Address');
        maskedPrompt = maskedPrompt.replace(/\b([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Z|a-z]{2,})\b/g, (_match, p1, p2) => {
          return p1.charAt(0) + '***@' + p2;
        });
      }
      if (hasSecret) {
        detectedEntities.push('API Credentials / Secrets');
        maskedPrompt = maskedPrompt.replace(/(sk-live-[a-zA-Z0-9]+)/gi, 'sk-live-****************');
        maskedPrompt = maskedPrompt.replace(/(secret_key:\s*[a-zA-Z0-9_-]+)/gi, 'secret_key: ****************');
      }
      if (hasSourceCode) {
        detectedEntities.push('Source Code Block');
      }

      let riskScore = 0;
      if (hasSSN) riskScore += 30;
      if (hasSecret) riskScore += 50;
      if (hasSourceCode) riskScore += 25;
      if (hasEmail) riskScore += 10;

      if (riskScore > 100) riskScore = 100;

      let action: 'ALLOW' | 'BLOCK' | 'WARN' = 'ALLOW';
      let policyTriggered = 'Rule-000 (Generic Allow)';
      let reason = 'All security checks passed. Prompt is safe.';

      if (hasSSN) {
        action = 'BLOCK';
        policyTriggered = 'Rule-001 (Block SSN)';
        reason = 'Prompt blocked: SSN detected in user input.';
      } else if (riskScore > 90) {
        action = 'BLOCK';
        policyTriggered = 'Rule-003 (Deny High Risk)';
        reason = `Prompt blocked: Risk Score (${riskScore}) exceeds threshold of 90.`;
      } else if (hasSecret) {
        action = 'WARN';
        policyTriggered = 'Rule-002 (Mask API Key)';
        reason = 'API credentials detected and masked automatically before forwarding.';
      } else if (lineCount > 200) {
        action = 'WARN';
        policyTriggered = 'Rule-004 (Source Code Review)';
        reason = 'Source code block > 200 lines requires manual security approval.';
      }

      let severity: 'Safe' | 'Medium' | 'High' | 'Critical' = 'Safe';
      if (riskScore > 80) severity = 'Critical';
      else if (riskScore > 60) severity = 'High';
      else if (riskScore > 30) severity = 'Medium';

      setSimulationResult({
        detectedEntities,
        maskedPrompt,
        riskScore,
        severity,
        action,
        policyTriggered,
        reason,
        processingTimeMs: Math.floor(Math.random() * 80) + 40,
      });
      setIsSimulating(false);
    }, 1200);
  };

  const getActionColor = (action: string) => {
    switch (action) {
      case 'BLOCK':
        return 'error';
      case 'WARN':
        return 'warning';
      default:
        return 'success';
    }
  };

  const getRiskColor = (score: number) => {
    if (score > 80) return '#ef4444';
    if (score > 60) return '#f97316';
    if (score > 30) return '#eab308';
    return '#22c55e';
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      
      {/* Top Navbar */}
      <AppBar position="static" color="transparent" elevation={0} sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
        <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Avatar sx={{ bgcolor: 'primary.dark', width: 40, height: 40 }}>
              <SecurityIcon />
            </Avatar>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: -0.5, lineHeight: 1.2 }}>
                SecureGenAI Gateway
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Enterprise AI Security Firewall
              </Typography>
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Chip label="System Live" color="success" size="small" variant="outlined" />
            <Chip label="Phase 1 - Foundation" color="primary" size="small" />
            <Tooltip title="Settings">
              <IconButton color="inherit">
                <SettingsIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Toolbar>
      </AppBar>

      {/* Main Layout Container */}
      <Container maxWidth="xl" sx={{ mt: 5, mb: 8 }} className="animate-fade-in">
        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 4 }}>
          <Box sx={{ bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2, p: 0.5 }}>
            <Button
              variant={activeView === 'dashboard' ? 'contained' : 'text'}
              onClick={() => setActiveView('dashboard')}
              startIcon={<DashboardIcon />}
              sx={{ borderRadius: 1.5 }}
            >
              Security Dashboard
            </Button>
            <Button
              variant={activeView === 'simulator' ? 'contained' : 'text'}
              onClick={() => setActiveView('simulator')}
              startIcon={<AccountTreeIcon />}
              sx={{ borderRadius: 1.5 }}
            >
              Gateway Simulator
            </Button>
          </Box>
        </Box>

        {activeView === 'dashboard' ? (
          <SecurityDashboard />
        ) : (
          <>
            {/* Welcome Section */}
            <Box sx={{ mb: 6, textAlign: 'center' }}>
          <Typography variant="h3" component="h1" gutterBottom sx={{ background: 'linear-gradient(90deg, #c084fc, #60a5fa)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', mb: 2 }}>
            SecureGenAI Gateway Dashboard
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ maxWidth: '800px', mx: 'auto', fontWeight: 400 }}>
            Accidental data leakage prevention for Generative AI tools. Monitor microservices, evaluate custom compliance policies, scan PII, and secure enterprise LLM prompts.
          </Typography>
        </Box>

        <Grid container spacing={4}>
          
          {/* Left Panel: Gateway Info & Microservices Monitor */}
          <Grid size={{ xs: 12, lg: 7 }}>
            <Box sx={{ mb: 4 }}>
              <Typography variant="h5" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                <CheckCircleIcon color="primary" /> Microservices Architecture
              </Typography>
              
              <Grid container spacing={2}>
                {services.map((service, index) => (
                  <Grid size={{ xs: 12, sm: 6 }} key={index}>
                    <Card sx={{ 
                      height: '100%', 
                      transition: 'transform 0.2s, box-shadow 0.2s', 
                      '&:hover': { transform: 'translateY(-4px)', boxShadow: '0 8px 20px rgba(168, 85, 247, 0.15)' },
                      borderColor: service.status === 'active' ? 'primary.light' : 'rgba(255, 255, 255, 0.06)'
                    }}>
                      <CardContent>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {service.icon}
                            <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                              {service.name}
                            </Typography>
                          </Box>
                          <Chip 
                            label={service.status === 'active' ? 'ACTIVE' : 'PLANNED'} 
                            color={service.status === 'active' ? 'primary' : 'default'} 
                            size="small" 
                            variant={service.status === 'active' ? 'filled' : 'outlined'}
                            sx={{ height: 20, fontSize: '0.65rem', fontWeight: 700 }}
                          />
                        </Box>
                        
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2, minHeight: '3em' }}>
                          {service.purpose}
                        </Typography>
                        
                        <Divider sx={{ my: 1, borderColor: 'rgba(255, 255, 255, 0.05)' }} />
                        
                        <Box sx={{ mt: 1 }}>
                          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5, fontWeight: 600 }}>
                            Responsibilities:
                          </Typography>
                          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                            {service.responsibilities.map((resp, i) => (
                              <Chip key={i} label={resp} size="small" sx={{ fontSize: '0.65rem', bgcolor: 'rgba(255, 255, 255, 0.03)' }} />
                            ))}
                          </Box>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </Box>
          </Grid>

          {/* Right Panel: Interactive Security Simulator */}
          <Grid size={{ xs: 12, lg: 5 }}>
            <Paper sx={{ p: 4, height: '100%', background: 'linear-gradient(135deg, rgba(18, 19, 26, 0.9) 0%, rgba(20, 21, 30, 0.9) 100%)', backdropFilter: 'blur(10px)', border: '1px solid rgba(168, 85, 247, 0.15)' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
                <Typography variant="h5" sx={{ fontWeight: 700, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <PlayArrowIcon color="primary" /> Security Gateway Simulator
                </Typography>
                <Chip label="Live Scanner Demo" color="secondary" variant="outlined" size="small" />
              </Box>
              
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Type or paste a sample prompt below to simulate how the gateway evaluates policies, scores risk, and masks PII before hitting an LLM endpoint.
              </Typography>

              <TextField
                fullWidth
                multiline
                rows={5}
                variant="outlined"
                label="Prompt Simulator Input"
                placeholder="Enter prompts containing SSNs, email addresses, or API keys..."
                value={promptInput}
                onChange={(e) => setPromptInput(e.target.value)}
                sx={{
                  mb: 3,
                  '& .MuiOutlinedInput-root': {
                    backgroundColor: 'rgba(0, 0, 0, 0.2)',
                    borderRadius: 3,
                  }
                }}
              />

              <Button
                fullWidth
                variant="contained"
                size="large"
                color="primary"
                onClick={handleSimulate}
                disabled={isSimulating || !promptInput}
                startIcon={isSimulating ? null : <SendIcon />}
                sx={{ py: 1.5 }}
              >
                {isSimulating ? 'Analyzing Prompt Risk...' : 'Run Gateway Guard'}
              </Button>

              {isSimulating && (
                <Box sx={{ mt: 3, textAlign: 'center' }}>
                  <LinearProgress color="primary" sx={{ borderRadius: 2, height: 6 }} />
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
                    Scanning for PII & Evaluating Security Rules...
                  </Typography>
                </Box>
              )}

              {/* Simulation Result Output */}
              {simulationResult && (
                <Box sx={{ mt: 4 }} className="animate-fade-in">
                  <Divider sx={{ my: 3 }} />
                  
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h6" sx={{ fontWeight: 700 }}>
                      Security Evaluation
                    </Typography>
                    <Chip
                      label={simulationResult.action}
                      color={getActionColor(simulationResult.action)}
                      sx={{ fontWeight: 800, px: 1 }}
                    />
                  </Box>

                  {/* Risk gauge */}
                  <Paper sx={{ p: 2, mb: 3, bgcolor: 'rgba(0,0,0,0.15)', display: 'flex', flexDirection: 'column', gap: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        Threat Risk Score
                      </Typography>
                      <Typography variant="h5" sx={{ color: getRiskColor(simulationResult.riskScore), fontWeight: 800 }}>
                        {simulationResult.riskScore} / 100
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={simulationResult.riskScore}
                      sx={{
                        height: 10,
                        borderRadius: 5,
                        backgroundColor: 'rgba(255,255,255,0.05)',
                        '& .MuiLinearProgress-bar': {
                          backgroundColor: getRiskColor(simulationResult.riskScore),
                        }
                      }}
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="caption" color="text.secondary">
                        Severity: <strong>{simulationResult.severity.toUpperCase()}</strong>
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Latency: {simulationResult.processingTimeMs}ms
                      </Typography>
                    </Box>
                  </Paper>

                  {/* Policy and Reason */}
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mb: 3 }}>
                    <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
                      {simulationResult.action === 'BLOCK' ? (
                        <ErrorIcon color="error" sx={{ mt: 0.2 }} />
                      ) : simulationResult.action === 'WARN' ? (
                        <WarningIcon color="warning" sx={{ mt: 0.2 }} />
                      ) : (
                        <CheckCircleIcon color="success" sx={{ mt: 0.2 }} />
                      )}
                      <Box>
                        <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                          {simulationResult.policyTriggered}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {simulationResult.reason}
                        </Typography>
                      </Box>
                    </Box>

                    {simulationResult.detectedEntities.length > 0 && (
                      <Box sx={{ mt: 1 }}>
                        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5, fontWeight: 600 }}>
                          Detected Entities:
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                          {simulationResult.detectedEntities.map((entity: string, i: number) => (
                            <Chip key={i} label={entity} size="small" color="secondary" variant="outlined" sx={{ fontSize: '0.7rem' }} />
                          ))}
                        </Box>
                      </Box>
                    )}
                  </Box>

                  {/* Masked Prompt Output */}
                  <Typography variant="subtitle2" gutterBottom sx={{ fontWeight: 700 }}>
                    Masked Prompt (Passed to LLM)
                  </Typography>
                  <Box
                    sx={{
                      p: 2,
                      bgcolor: 'rgba(0, 0, 0, 0.3)',
                      borderRadius: 3,
                      border: '1px solid rgba(255,255,255,0.05)',
                      fontFamily: 'Courier New, monospace',
                      fontSize: '0.85rem',
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-all',
                      color: simulationResult.action === 'BLOCK' ? '#ef4444' : '#67e8f9',
                    }}
                  >
                    {simulationResult.action === 'BLOCK' 
                      ? '[REQUEST BLOCKED BY GATEWAY SECURITY POLICY]'
                      : simulationResult.maskedPrompt}
                  </Box>
                </Box>
              )}
            </Paper>
          </Grid>
        </Grid>
        </>
        )}
      </Container>
    </ThemeProvider>
  );
}

export default App;
