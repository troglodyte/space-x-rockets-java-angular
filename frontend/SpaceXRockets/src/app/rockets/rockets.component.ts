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
  // Sorting state
  readonly sortColumn = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly sortedRockets = computed(() => this.sortRows(this.rocketsSig(), this.sortColumn(), this.sortDir()));


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

  onHeaderClick(col: string): void {
    const current = this.sortColumn();
    if (current === col) {
      this.sortDir.update(d => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortColumn.set(col);
      this.sortDir.set('asc');
    }
  }

  ariaSort(col: string): 'none' | 'ascending' | 'descending' {
    if (this.sortColumn() !== col) return 'none';
    return this.sortDir() === 'asc' ? 'ascending' : 'descending';
  }

  sortIndicator(col: string): string {
    if (this.sortColumn() !== col) return '';
    return this.sortDir() === 'asc' ? ' ▲' : ' ▼';
  }

  private sortRows(rows: Record<string, unknown>[], col: string | null, dir: 'asc' | 'desc') {
    if (!col) return rows;
    const factor = dir === 'asc' ? 1 : -1;
    // stable sort by pairing with original index
    const paired = rows.map((r, i) => ({ r, i }));
    paired.sort((a, b) => {
      const va = a.r[col];
      const vb = b.r[col];
      const cmp = this.compareValues(va, vb);
      if (cmp !== 0) return cmp * factor;
      return a.i - b.i; // stability
    });
    return paired.map(p => p.r);
  }

  private compareValues(a: unknown, b: unknown): number {
    // Handle undefined/null
    const aU = a === undefined || a === null;
    const bU = b === undefined || b === null;
    if (aU && bU) return 0;
    if (aU) return -1;
    if (bU) return 1;

    // Numbers
    if (typeof a === 'number' && typeof b === 'number') return a - b;

    // Booleans: false < true
    if (typeof a === 'boolean' && typeof b === 'boolean') return (a === b) ? 0 : (a ? 1 : -1);

    // Numeric strings
    const an = this.parseMaybeNumber(a);
    const bn = this.parseMaybeNumber(b);
    if (an.parsed && bn.parsed) return an.value - bn.value;

    // Fallback to string compare
    const sa = String(a);
    const sb = String(b);
    return sa.localeCompare(sb, undefined, { numeric: true, sensitivity: 'base' });
  }

  private parseMaybeNumber(v: unknown): { parsed: boolean; value: number } {
    if (typeof v === 'number') return { parsed: true, value: v };
    if (typeof v === 'string') {
      const trimmed = v.trim();
      if (trimmed === '') return { parsed: false, value: NaN };
      const num = Number(trimmed.replace(/[,\s]/g, ''));
      if (!Number.isNaN(num)) return { parsed: true, value: num };
    }
    return { parsed: false, value: NaN };
  }
}
