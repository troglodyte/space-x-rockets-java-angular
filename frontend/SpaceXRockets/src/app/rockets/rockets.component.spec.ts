import { TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { RocketsComponent } from './rockets.component';

describe('RocketsComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RocketsComponent],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(RocketsComponent);
    // Do not trigger ngOnInit HTTP yet
    const comp = fixture.componentInstance;
    expect(comp).toBeTruthy();
  });

  it('should map rocket headers and aria-sort correctly', () => {
    const fixture = TestBed.createComponent(RocketsComponent);
    const comp = fixture.componentInstance;

    // No change detection – avoid ngOnInit HTTP
    expect(comp.rocketHeaderMap('id')).toBe('Rocket ID');
    expect(comp.rocketHeaderMap('name')).toBe('Rocket Name');
    expect(comp.rocketHeaderMap('active')).toBe('Active');
    expect(comp.rocketHeaderMap('successRatePct')).toBe('Success Rate (%)');
    expect(comp.rocketHeaderMap('showLaunch')).toBe('Show Launch Details');

    // Initial aria sort should be none
    expect(comp.ariaSort('name')).toBe('none');

    // Click header to set sort column
    comp.onHeaderClick('name');
    expect(comp.ariaSort('name')).toBe('ascending');
    expect(comp.sortIndicator('name')).toContain('▲');

    // Toggle same column should flip direction
    comp.onHeaderClick('name');
    expect(comp.ariaSort('name')).toBe('descending');
    expect(comp.sortIndicator('name')).toContain('▼');
  });

  it('should sort rockets stably by selected column and direction', () => {
    const fixture = TestBed.createComponent(RocketsComponent);
    const comp = fixture.componentInstance as unknown as {
      sortedRockets: () => Array<Record<string, unknown>>;
      onHeaderClick: (c: string) => void;
      // access private signal for test setup
      rocketsSig: { set: (v: Array<Record<string, unknown>>) => void };
    } as any;

    // Inject sample data with ties to test stability
    comp.rocketsSig.set([
      { id: '2', name: 'Falcon 9', active: true, successRatePct: 98 },
      { id: '1', name: 'Falcon 9', active: false, successRatePct: 97 },
      { id: '3', name: 'Starship', active: false, successRatePct: 50 },
    ]);

    // Sort by name asc – two rows share name "Falcon 9", original order by id should be preserved among ties
    comp.onHeaderClick('name');
    let sorted = comp.sortedRockets();
    expect(sorted.map((r: Record<string, unknown>) => r['name']))
      .toEqual(['Falcon 9', 'Falcon 9', 'Starship']);
    expect(sorted.map((r: Record<string, unknown>) => r['id']))
      .toEqual(['2', '1', '3']); // stability check

    // Now toggle to desc
    comp.onHeaderClick('name');
    sorted = comp.sortedRockets();
    expect(sorted.map((r: Record<string, unknown>) => r['name']))
      .toEqual(['Starship', 'Falcon 9', 'Falcon 9']);

    // Sort by successRatePct asc to test numeric comparison
    comp.onHeaderClick('successRatePct');
    sorted = comp.sortedRockets();
    expect(sorted.map((r: Record<string, unknown>) => r['successRatePct']))
      .toEqual([50, 97, 98]);
  });

  it('should process launches and sort them', () => {
    const fixture = TestBed.createComponent(RocketsComponent);
    const comp = fixture.componentInstance as unknown as {
      processedLaunches: () => Array<Record<string, unknown>>;
      sortedLaunches: () => Array<Record<string, unknown>>;
      onLaunchHeaderClick: (c: string) => void;
      launchesSig: { set: (v: Array<Record<string, unknown>>) => void };
    } as any;

    comp.launchesSig.set([
      { id: 'a', name: 'L-1', date_utc: '2022-01-02T00:00:00Z', rocket_name: 'Falcon 9' },
      { id: 'b', name: 'L-2', date: '2021-12-31T00:00:00Z', rocket_name: 'Starship' },
    ]);

    const processed = comp.processedLaunches();
    expect(processed[0]['date']).toBe('2022-01-02T00:00:00Z');
    expect(processed[1]['date']).toBe('2021-12-31T00:00:00Z');
    expect(processed.map((l: Record<string, unknown>) => l['rocket']))
      .toEqual(['Falcon 9', 'Starship']);

    comp.onLaunchHeaderClick('name');
    let sorted = comp.sortedLaunches();
    expect(sorted.map((l: Record<string, unknown>) => l['name']))
      .toEqual(['L-1', 'L-2']);

    comp.onLaunchHeaderClick('name');
    sorted = comp.sortedLaunches();
    expect(sorted.map((l: Record<string, unknown>) => l['name']))
      .toEqual(['L-2', 'L-1']);
  });

  it('ngOnInit should request rockets and set state on success', () => {
    const fixture = TestBed.createComponent(RocketsComponent);
    const comp = fixture.componentInstance as any;

    // Trigger ngOnInit + subscription
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/rockets/all');
    expect(req.request.method).toBe('GET');
    req.flush([
      { id: 'r1', name: 'Rocket 1', active: true, successRatePct: 80 },
    ]);

    // After flush, loading should be false and rockets available
    expect(comp.loading()).toBe(false);
    expect(comp.rockets().length).toBe(1);
  });

  it('ngOnInit should handle HTTP error and set error message', () => {
    const fixture = TestBed.createComponent(RocketsComponent);
    const comp = fixture.componentInstance as any;

    fixture.detectChanges();

    const req = httpMock.expectOne('/api/rockets/all');
    req.flush('Server down', { status: 500, statusText: 'Server Error' });

    expect(comp.loading()).toBe(false);
    expect(comp.error()).toContain('Failed to load rockets');
  });
});
