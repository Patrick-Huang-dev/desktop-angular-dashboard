/**
 * Dashboard data models
 * These interfaces define the data structures used in the dashboard
 */

// Top statistics card
export interface TopCard {
  bgcolor: string;
  icon: string;
  title: string;
  subtitle: string;
}

// Activity feed item
export interface Feed {
  bgClass: string;
  icon: string;
  task: string;
  time: string;
}

// Project/Product item for table
export interface Project {
  image: string;
  name: string;
  email: string;
  project: string;
  status: 'success' | 'warning' | 'danger' | 'info';
  weeks: number;
  budget: string;
}

// Sales summary data
export interface SalesSummary {
  totalSales: string;
  thisMonth: string;
  thisWeek: string;
}

// Chart data point
export interface ChartSeries {
  name: string;
  data: number[];
}











