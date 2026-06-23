import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color?: string;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color = '#1976d2' }) => {
  return (
    <Card sx={{ minWidth: 200, display: 'flex', alignItems: 'center', p: 2, height: '100%' }}>
      <Box
        sx={{
          backgroundColor: `${color}22`,
          color: color,
          borderRadius: '50%',
          p: 2,
          mr: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {icon}
      </Box>
      <CardContent sx={{ flex: '1 0 auto', padding: 0, '&:last-child': { paddingBottom: 0 } }}>
        <Typography variant="body2" color="text.secondary" component="div">
          {title}
        </Typography>
        <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
};

export default StatCard;
