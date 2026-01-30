import { Injectable } from '@angular/core';
import { TopCard, Feed, Project, SalesSummary, ChartSeries } from '../models/dashboard.models';

/**
 * Backend Service
 * 
 * This service handles communication with the Java backend via JxBrowser's JS-Java Bridge.
 * When running in JxBrowser, it calls Java methods directly.
 * When running in a browser (development mode), it returns mock data.
 */

// Declare the Java backend interface that will be injected by JxBrowser
declare global {
  interface Window {
    backend?: {
      getTopCards(): string;
      getFeeds(): string;
      getProjects(): string;
      getSalesSummary(): string;
      getChartData(timeRange: string): string;
    };
  }
}

@Injectable({
  providedIn: 'root'
})
export class BackendService {

  /**
   * Check if running inside JxBrowser with Java backend available
   */
  isJxBrowserEnvironment(): boolean {
    return typeof window.backend !== 'undefined';
  }

  /**
   * Get top statistics cards data
   */
  getTopCards(): TopCard[] {
    if (this.isJxBrowserEnvironment()) {
      return JSON.parse(window.backend!.getTopCards());
    }
    return this.getMockTopCards();
  }

  /**
   * Get activity feeds data
   */
  getFeeds(): Feed[] {
    if (this.isJxBrowserEnvironment()) {
      return JSON.parse(window.backend!.getFeeds());
    }
    return this.getMockFeeds();
  }

  /**
   * Get projects table data
   */
  getProjects(): Project[] {
    if (this.isJxBrowserEnvironment()) {
      return JSON.parse(window.backend!.getProjects());
    }
    return this.getMockProjects();
  }

  /**
   * Get sales summary data
   */
  getSalesSummary(): SalesSummary {
    if (this.isJxBrowserEnvironment()) {
      return JSON.parse(window.backend!.getSalesSummary());
    }
    return this.getMockSalesSummary();
  }

  /**
   * Get chart series data
   * @param timeRange - '3m' | '30d' | '7d'
   */
  getChartData(timeRange: string = '3m'): ChartSeries[] {
    if (this.isJxBrowserEnvironment()) {
      return JSON.parse(window.backend!.getChartData(timeRange));
    }
    return this.getMockChartData(timeRange);
  }

  // ============ Mock Data for Development ============

  private getMockTopCards(): TopCard[] {
    return [
      { bgcolor: 'success', icon: 'bi bi-wallet', title: '$21,456', subtitle: 'Total Revenue' },
      { bgcolor: 'danger', icon: 'bi bi-coin', title: '$1,250', subtitle: 'Refund Given' },
      { bgcolor: 'warning', icon: 'bi bi-basket3', title: '456', subtitle: 'Total Projects' },
      { bgcolor: 'info', icon: 'bi bi-bag', title: '210', subtitle: 'Weekly Sales' }
    ];
  }

  private getMockFeeds(): Feed[] {
    return [
      { bgClass: 'bg-info', icon: 'bi bi-bell', task: 'You have 4 pending tasks.', time: 'Just Now' },
      { bgClass: 'bg-success', icon: 'bi bi-hdd', task: 'Server #1 overloaded.', time: '2 Hours ago' },
      { bgClass: 'bg-warning', icon: 'bi bi-bag-check', task: 'New order received.', time: '31 May' },
      { bgClass: 'bg-danger', icon: 'bi bi-person', task: 'New user registered.', time: '30 May' },
      { bgClass: 'bg-primary', icon: 'bi bi-shield-check', task: 'System update completed.', time: '29 May' },
      { bgClass: 'bg-info', icon: 'bi bi-bell', task: 'Payment processed successfully.', time: '28 May' },
      { bgClass: 'bg-success', icon: 'bi bi-hdd', task: 'Database backup completed.', time: '27 May' },
      { bgClass: 'bg-warning', icon: 'bi bi-bag-check', task: 'Low inventory alert.', time: '26 May' }
    ];
  }

  private getMockProjects(): Project[] {
    return [
      { image: 'assets/images/user1.jpg', name: 'John Smith', email: 'john@example.com', project: 'JxBrowser Demo', status: 'success', weeks: 12, budget: '$15K' },
      { image: 'assets/images/user2.jpg', name: 'Sarah Wilson', email: 'sarah@example.com', project: 'Angular Dashboard', status: 'info', weeks: 8, budget: '$12K' },
      { image: 'assets/images/user3.jpg', name: 'Mike Johnson', email: 'mike@example.com', project: 'Java Backend', status: 'warning', weeks: 15, budget: '$20K' },
      { image: 'assets/images/user4.jpg', name: 'Emily Brown', email: 'emily@example.com', project: 'Desktop App', status: 'danger', weeks: 6, budget: '$8K' }
    ];
  }

  private getMockSalesSummary(): SalesSummary {
    return {
      totalSales: '$10,345',
      thisMonth: '$7,545',
      thisWeek: '$1,345'
    };
  }

  private getMockChartData(timeRange: string = '3m'): ChartSeries[] {
    switch (timeRange) {
      case '7d':
        // Last 7 days - daily data
        return [
          { name: 'Desktop', data: [186, 305, 237, 173, 209, 214, 186] },
          { name: 'Mobile', data: [80, 120, 95, 85, 110, 98, 76] }
        ];
      case '30d':
        // Last 30 days - weekly data
        return [
          { name: 'Desktop', data: [1250, 1380, 1520, 1420] },
          { name: 'Mobile', data: [620, 710, 680, 750] }
        ];
      case '3m':
      default:
        // Last 3 months - monthly data
        return [
          { name: 'Desktop', data: [31, 40, 28, 51, 42, 109, 100] },
          { name: 'Mobile', data: [11, 32, 45, 32, 34, 52, 41] }
        ];
    }
  }
}
