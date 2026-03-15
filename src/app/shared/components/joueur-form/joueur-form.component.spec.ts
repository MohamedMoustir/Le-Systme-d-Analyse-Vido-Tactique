import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JoueurForm } from './joueur-form.component';

describe('JoueurForm', () => {
  let component: JoueurForm;
  let fixture: ComponentFixture<JoueurForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JoueurForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JoueurForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
