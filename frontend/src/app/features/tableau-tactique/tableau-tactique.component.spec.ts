import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableauTactiqueComponent } from './tableau-tactique.component';

describe('TableauTactiqueComponent', () => {
  let component: TableauTactiqueComponent;
  let fixture: ComponentFixture<TableauTactiqueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableauTactiqueComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableauTactiqueComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
