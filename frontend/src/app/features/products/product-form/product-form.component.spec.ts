import { TestBed } from '@angular/core/testing';
import { ProductFormComponent } from './product-form.component';

describe('ProductFormComponent', () => {
  beforeEach(async () => {
    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [ProductFormComponent],
    }).compileComponents();
  });

  it('is invalid when name is empty', () => {
    const fixture = TestBed.createComponent(ProductFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.form.controls.name.setValue('');
    component.form.controls.price.setValue(10);
    expect(component.form.valid).toBe(false);
    expect(component.form.controls.name.errors?.['required']).toBeTruthy();
  });

  it('rejects names longer than 150 characters', () => {
    const fixture = TestBed.createComponent(ProductFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.form.controls.name.setValue('a'.repeat(151));
    component.form.controls.price.setValue(10);
    expect(component.form.controls.name.errors?.['maxlength']).toBeTruthy();
  });

  it('rejects negative prices', () => {
    const fixture = TestBed.createComponent(ProductFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.form.controls.name.setValue('Teclado');
    component.form.controls.price.setValue(-1);
    expect(component.form.controls.price.errors?.['min']).toBeTruthy();
  });

  it('emits a create payload with active=null when not editing', () => {
    const fixture = TestBed.createComponent(ProductFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    let emitted: unknown = null;
    component.submit.subscribe((payload) => (emitted = payload));

    component.form.controls.name.setValue('  Teclado  ');
    component.form.controls.description.setValue('  ');
    component.form.controls.price.setValue(99.99);
    component.onSubmit();

    expect(emitted).toEqual({ name: 'Teclado', description: null, brand: null, category: null, imageUrl: null, price: 99.99, active: null });
  });

  it('emits an update payload with the active flag when editing', () => {
    const fixture = TestBed.createComponent(ProductFormComponent);
    fixture.componentRef.setInput('editing', true);
    fixture.componentRef.setInput('product', {
      id: 1,
      name: 'Teclado',
      description: 'Mecánico',
      brand: null, category: null, imageUrl: null,
      price: 99.99,
      active: true,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    });
    fixture.detectChanges();
    const component = fixture.componentInstance;

    let emitted: unknown = null;
    component.submit.subscribe((payload) => (emitted = payload));

    component.form.controls.name.setValue('Teclado RGB');
    component.form.controls.active.setValue(false);
    component.onSubmit();

    expect(emitted).toEqual({
      name: 'Teclado RGB',
      description: 'Mecánico',
      brand: null, category: null, imageUrl: null,
      price: 99.99,
      active: false,
    });
  });
});
