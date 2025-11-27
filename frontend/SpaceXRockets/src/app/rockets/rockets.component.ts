import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-rockets',
  imports: [CommonModule, HttpClientModule],
  templateUrl: './rockets.component.html',
  styleUrl: './rockets.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RocketsComponent implements OnInit {
  loading = signal(true);
  error = signal<string | null>(null);
  private readonly rocketsSig = signal<Record<string, unknown>[]>([]);
  readonly rockets = computed(() => this.rocketsSig());
  readonly columns = computed(() => this.deriveColumns(this.rocketsSig()));
  private readonly http = inject(HttpClient);


  ngOnInit(): void {
    // Use relative URL so Angular dev proxy can forward to backend and avoid CORS during dev
    const url = '/api/rockets/active';
    this.http.get<Record<string, unknown>[]>(url).subscribe({
      next: (data) => {
        this.rocketsSig.set(Array.isArray(data) ? data : []);
        this.loading.set(false);
      },
      error: (err) => {
        // Provide clearer error information + log full error for debugging
        const status = (err?.status as number | undefined) ?? 0;
        const statusText = (err?.statusText as string | undefined) ?? 'Unknown Error';
        // const url = (err?.url as string | undefined) ?? '/api/rockets/all';
        const detail = typeof err?.error === 'string'
          ? err.error
          : (err?.message as string | undefined) ?? '';
        // Log the full error for deeper diagnostics in the console
        // eslint-disable-next-line no-console
        console.error('[RocketsComponent] HTTP error when fetching rockets', err);
        this.error.set(`Failed to load rockets from ${url} (${status} ${statusText})${detail ? ': ' + detail : ''}`);
        this.loading.set(false);
      }
    });
  }

  private deriveColumns(rows: Record<string, unknown>[]): string[] {
    if (!rows || rows.length === 0) return [];
    const keys = new Set<string>();
    // Use keys from the first few rows to avoid overly wide tables
    rows.slice(0, 3).forEach(r => Object.keys(r ?? {}).forEach(k => keys.add(k)));
    // Prefer common readable fields first if they exist
    const preferredOrder = [
      'id', 'name', 'type', 'first_flight', 'firstFlight', 'country', 'company',
      'active', 'cost_per_launch', 'costPerLaunch', 'success_rate_pct', 'successRatePct'
    ];
    const sample = rows.find(r => r && Object.keys(r).length > 0) ?? rows[0];
    const simpleKeys = Array.from(keys).filter(k => this.isSimple((sample as Record<string, unknown>)[k]));
    const ordered: string[] = [];
    preferredOrder.forEach(p => { if (simpleKeys.includes(p)) ordered.push(p); });
    simpleKeys.forEach(k => { if (!ordered.includes(k)) ordered.push(k); });
    // Limit number of columns for readability
    return ordered.slice(0, 12);
  }

  private isSimple(val: unknown): boolean {
    return val === null || ['string', 'number', 'boolean'].includes(typeof val as string);
  }
}
