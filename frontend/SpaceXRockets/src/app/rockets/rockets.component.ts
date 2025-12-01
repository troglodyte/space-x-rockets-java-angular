import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-rockets',
  imports: [CommonModule],
  templateUrl: './rockets.component.html',
  styleUrl: './rockets.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
/**
 * Component responsible for displaying and managing SpaceX rocket and launch data.
 * Handles data fetching, sorting, and display of rocket information and their associated launches.
 */
export class RocketsComponent implements OnInit {
  loading = signal(true);
  error = signal<string | null>(null);
  private readonly rocketsSig = signal<Record<string, unknown>[]>([]);
  private readonly launchesSig = signal<Record<string, unknown>[]>([]);
  readonly rockets = computed(() => this.rocketsSig());
  readonly launches = computed(() => this.launchesSig());
  readonly columns = signal<string[]>(['name', 'id', 'active', 'successRatePct', 'showLaunch']);
  readonly launchColumns = signal<string[]>(['rocket', 'name', 'id', 'date']);
  private readonly http = inject(HttpClient);
  readonly sortColumn = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc'>('asc');

  /**
   * Computed property that returns rockets sorted by the current sort column and direction.
   * Updates automatically when sort parameters or rocket data changes.
   */
  readonly sortedRockets = computed(() => this.sortRows(this.rocketsSig(), this.sortColumn(), this.sortDir()));
  readonly launchSortColumn = signal<string | null>(null);
  readonly launchSortDir = signal<'asc' | 'desc'>('asc');
  readonly processedLaunches = computed(() =>
    this.launchesSig().map(l => ({
      ...l,
      date: (l as any)['date'] ?? (l as any)['date_utc'],
      rocket: l['rocket_name'] ?? '',
    }))
  );
  readonly sortedLaunches = computed(() => this.sortRows(this.processedLaunches(), this.launchSortColumn(), this.launchSortDir()));


  ngOnInit(): void {
    const url = '/api/rockets/all';
    this.http.get<Record<string, unknown>[]>(url).subscribe({
      next: (data) => {
        this.rocketsSig.set(Array.isArray(data) ? data : []);
        this.loading.set(false);
      },
      error: (err) => {
        const status = (err?.status as number | undefined) ?? 0;
        const statusText = (err?.statusText as string | undefined) ?? 'Unknown Error';
        const detail = typeof err?.error === 'string'
          ? err.error
          : (err?.message as string | undefined) ?? '';
        console.error('[RocketsComponent] HTTP error when fetching rockets', err);
        this.error.set(`Failed to load rockets from ${url} (${status} ${statusText})${detail ? ': ' + detail : ''}`);
        this.loading.set(false);
      }
    });
  }

  /**
   * Fetches and displays launch data for a specific rocket.
   * @param id - The ID of the rocket to fetch launches for
   * @param rocket_name - The name of the rocket to associate with launches
   */
  showLaunchData(id: any, rocket_name: any): void {
    const url = `/api/launches/id/${id}`;
    console.log(rocket_name);
    this.http.get<Record<string, unknown>[]>(url).subscribe({

      next: (data) => {
        const launchesWithRocketName = Array.isArray(data)
          ? data.map(launch => ({...launch, rocket_name}))
          : [];
        this.launchesSig.set(launchesWithRocketName);
        this.loading.set(false);
      },
      error: (err) => {
        const status = (err?.status as number | undefined) ?? 0;
        const statusText = (err?.statusText as string | undefined) ?? 'Unknown Error';
        const detail = typeof err?.error === 'string'
          ? err.error
          : (err?.message as string | undefined) ?? '';
        console.error('[RocketsComponent] HTTP error when fetching launches', err);
        this.error.set(`Failed to load launces from ${url} (${status} ${statusText})${detail ? ': ' + detail : ''}`);
        this.loading.set(false);
      }
    });
  }

  /**
   * Handles click events on rocket table headers for sorting.
   * @param col - The column name to sort by
   */
  onHeaderClick(col: string): void {
    const current = this.sortColumn();
    if (current === col) {
      this.sortDir.update(d => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortColumn.set(col);
      this.sortDir.set('asc');
    }
  }

  /**
   * Handles click events on launch table headers for sorting.
   * @param col - The column name to sort by
   */
  onLaunchHeaderClick(col: string): void {
    const current = this.launchSortColumn();
    if (current === col) {
      this.launchSortDir.update(d => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.launchSortColumn.set(col);
      this.launchSortDir.set('asc');
    }
  }

  /**
   * Determines the ARIA sort attribute value for rocket table columns.
   * @param col - The column to get the sort state for
   * @returns The ARIA sort state: 'none', 'ascending', or 'descending'
   */
  ariaSort(col: string): 'none' | 'ascending' | 'descending' {
    if (this.sortColumn() !== col) return 'none';
    return this.sortDir() === 'asc' ? 'ascending' : 'descending';
  }

  ariaSortLaunch(col: string): 'none' | 'ascending' | 'descending' {
    if (this.launchSortColumn() !== col) return 'none';
    return this.launchSortDir() === 'asc' ? 'ascending' : 'descending';
  }

  /**
   * Mapping the rocket table column names to their display names.
   * 'id', 'name', 'active', 'successRatePct', 'showLaunch'
   */
  rocketHeaderMap(col: string): string {
    return {
      'id': 'Rocket ID',
      'name': 'Rocket Name',
      'active': 'Active',
      'successRatePct': 'Success Rate (%)',
      'showLaunch': 'Show Launch Details'
    }[col] ?? col;
  }

  sortIndicator(col: string): string {
    if (this.sortColumn() !== col) return '';
    return this.sortDir() === 'asc' ? ' ▲' : ' ▼';
  }

  sortIndicatorLaunch(col: string): string {
    if (this.launchSortColumn() !== col) return '';
    return this.launchSortDir() === 'asc' ? ' ▲' : ' ▼';
  }

  /**
   * Sorts an array of records based on the specified column and direction.
   * Maintains sort stability by using the original index as a secondary sort key.
   * @param rows - The array of records to sort
   * @param col - The column to sort by
   * @param dir - The sort direction ('asc' or 'desc')
   * @returns The sorted array of records
   */
  private sortRows(rows: Record<string, unknown>[], col: string | null, dir: 'asc' | 'desc') {
    if (!col) return rows;
    const factor = dir === 'asc' ? 1 : -1;
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

  /**
   * Compares two values for sorting, handling different data types appropriately.
   * Supports undefined/null, numbers, booleans, and strings with numeric content.
   * @param a - First value to compare
   * @param b - Second value to compare
   * @returns Negative if a < b, positive if a > b, zero if equal
   */
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
