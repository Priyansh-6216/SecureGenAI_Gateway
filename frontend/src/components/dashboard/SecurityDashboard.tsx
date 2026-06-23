import React from 'react';
import { Box, Grid, Typography } from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import BlockIcon from '@mui/icons-material/Block';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';

import StatCard from './StatCard';
import RiskDistributionChart from './RiskDistributionChart';

const SecurityDashboard: React.FC = () => {
  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', mb: 4 }}>
        Security Dashboard
      </Typography>
      
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard 
            title="Total Requests" 
            value="14,231" 
            icon={<SecurityIcon fontSize="large" />} 
            color="#1976d2"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard 
            title="Blocked Requests" 
            value="423" 
            icon={<BlockIcon fontSize="large" />} 
            color="#d32f2f"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard 
            title="Policy Violations" 
            value="156" 
            icon={<WarningAmberIcon fontSize="large" />} 
            color="#ed6c02"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard 
            title="Safe Prompts" 
            value="13,808" 
            icon={<VerifiedUserIcon fontSize="large" />} 
            color="#2e7d32"
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <RiskDistributionChart />
        </Grid>
        <Grid item xs={12} md={6}>
          <Box sx={{ 
            height: '100%', 
            minHeight: 350, 
            backgroundColor: '#f5f5f5', 
            borderRadius: 2, 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            border: '1px dashed #ccc'
          }}>
            <Typography variant="body1" color="text.secondary">
              Recent Alerts Widget (Coming Soon)
            </Typography>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SecurityDashboard;
