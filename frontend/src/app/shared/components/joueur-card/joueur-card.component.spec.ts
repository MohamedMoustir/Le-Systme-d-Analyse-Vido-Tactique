import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JoueurCard } from './joueur-card.component';

describe('JoueurCard', () => {
  let component: JoueurCard;
  let fixture: ComponentFixture<JoueurCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JoueurCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JoueurCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
