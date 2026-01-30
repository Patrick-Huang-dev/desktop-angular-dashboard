import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { 
  lucideTrendingUp,
  lucideTrendingDown
} from '@ng-icons/lucide';
import { TopCard } from '../../models/dashboard.models';
import { BackendService } from '../../services/backend.service';

@Component({
  selector: 'app-top-cards',
  standalone: true,
  imports: [CommonModule, NgIconComponent],
  viewProviders: [provideIcons({ 
    lucideTrendingUp,
    lucideTrendingDown
  })],
  template: `
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <div *ngFor="let card of topCards; let i = index" 
           class="rounded-xl border border-border bg-card p-6">
        <div class="flex flex-row items-center justify-between space-y-0 pb-2">
          <h3 class="text-sm font-medium text-muted-foreground">{{ card.subtitle }}</h3>
          <div class="flex items-center gap-1.5 rounded-lg bg-secondary/50 px-2.5 py-1 text-xs font-medium">
            <ng-icon 
              [name]="getTrendDirection(i) ? 'lucideTrendingUp' : 'lucideTrendingDown'" 
              class="h-3 w-3"
              [ngClass]="getTrendDirection(i) ? 'text-foreground' : 'text-foreground'" 
            />
            <span class="text-foreground">
              {{ getTrendValue(i) }}
            </span>
          </div>
        </div>
        <div class="pt-2">
          <div class="text-2xl font-bold text-foreground">{{ card.title }}</div>
          <p class="text-xs text-muted-foreground mt-1 flex items-center gap-1">
            <span>{{ getTrendDescription(i) }}</span>
            <ng-icon 
              [name]="getTrendDirection(i) ? 'lucideTrendingUp' : 'lucideTrendingDown'" 
              class="h-3 w-3 text-muted-foreground" 
            />
          </p>
        </div>
      </div>
    </div>
  `
})
export class TopCardsComponent implements OnInit {
  topCards: TopCard[] = [];

  // 模拟趋势数据
  private trendData = [
    { direction: true, value: '+12.5%', description: 'Trending up this month' },
    { direction: false, value: '-20%', description: 'Down 20% this period' },
    { direction: true, value: '+12.5%', description: 'Strong user retention' },
    { direction: true, value: '+4.5%', description: 'Steady performance increase' }
  ];

  constructor(private backendService: BackendService) {}

  ngOnInit(): void {
    this.topCards = this.backendService.getTopCards();
  }

  getTrendDirection(index: number): boolean {
    return this.trendData[index % this.trendData.length].direction;
  }

  getTrendValue(index: number): string {
    return this.trendData[index % this.trendData.length].value;
  }

  getTrendDescription(index: number): string {
    return this.trendData[index % this.trendData.length].description;
  }
}
