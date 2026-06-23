import React from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Card, CardContent, Typography } from '@mui/material';

const data = [
  { name: 'Low Risk', value: 400, color: '#4caf50' },
  { name: 'Medium Risk', value: 300, color: '#ff9800' },
  { name: 'High Risk', value: 150, color: '#f44336' },
  { name: 'Critical Risk', value: 50, color: '#b71c1c' },
];

const RiskDistributionChart: React.FC = () => {
  return (
    <Card sx={{ height: '100%', minHeight: 350 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Risk Distribution
        </Typography>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={100}
              paddingAngle={5}
              dataKey="value"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};

export default RiskDistributionChart;
