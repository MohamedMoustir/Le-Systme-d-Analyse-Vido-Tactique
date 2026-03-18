import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RadarTactique } from './radar-tactique';

describe('RadarTactique', () => {
  let component: RadarTactique;
  let fixture: ComponentFixture<RadarTactique>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RadarTactique]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RadarTactique);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
