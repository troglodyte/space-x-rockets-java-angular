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
  // Sorting state
  readonly sortColumn = signal<string | null>(null);
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  // Sorted view of rockets based on current sort state
  readonly sortedRockets = computed(() => {
    const data = this.rocketsSig();
    const col = this.sortColumn();
    const dir = this.sortDirection();
    if (!col) return data;

    // Create a copy to avoid mutating original signal data
    const mapped = data.map((item, index) => ({ item, index }));

    const factor = dir === 'asc' ? 1 : -1;
    mapped.sort((a, b) => factor * this.compareValues((a.item as Record<string, unknown>)[col], (b.item as Record<string, unknown>)[col])
      // Ensure stable sort by falling back to original index
      || (a.index - b.index));

    return mapped.map(m => m.item);
  });
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

  // Toggle sort state for a given column
  setSort(col: string): void {
    const current = this.sortColumn();
    if (current === col) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortColumn.set(col);
      this.sortDirection.set('asc');
    }
  }

  // Compare heterogeneous values in a predictable way
  private compareValues(a: unknown, b: unknown): number {
    // Normalize undefined to null for ordering purposes
    const va = a === undefined ? null : a as unknown;
    const vb = b === undefined ? null : b as unknown;

    // Nulls last
    if (va === null && vb === null) return 0;
    if (va === null) return 1;
    if (vb === null) return -1;

    const ta = typeof va;
    const tb = typeof vb;

    // If types differ, order by type name to keep ordering deterministic
    if (ta !== tb) return ta < tb ? -1 : 1;

    // Numbers
    if (ta === 'number') {
      const na = va as number;
      const nb = vb as number;
      if (Number.isNaN(na) && Number.isNaN(nb)) return 0;
      if (Number.isNaN(na)) return 1;
      if (Number.isNaN(nb)) return -1;
      return na - nb;
    }

    // Booleans: false < true
    if (ta === 'boolean') {
      return (va === vb) ? 0 : (va === false ? -1 : 1);
    }

    // Strings: case-insensitive, locale-aware
    if (ta === 'string') {
      return (va as string).localeCompare(vb as string, undefined, { sensitivity: 'base' });
    }

    // Fallback to stringified comparison
    return String(va).localeCompare(String(vb));
  }
}
