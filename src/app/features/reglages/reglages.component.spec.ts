import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReglagesComponent } from './reglages.component';

describe('ReglagesComponent', () => {
  let component: ReglagesComponent;
  let fixture: ComponentFixture<ReglagesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglagesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReglagesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
