import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebtComponent } from './debt.component';

describe('DebtComponent', () => {
  let component: DebtComponent;
  let fixture: ComponentFixture<DebtComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DebtComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DebtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the Debt component', () => {
    expect(component).toBeTruthy();
  });
});
