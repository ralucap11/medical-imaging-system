import { ComponentFixture, TestBed } from '@angular/core/testing';

import { XrayList } from './xray-list';

describe('XrayList', () => {
  let component: XrayList;
  let fixture: ComponentFixture<XrayList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [XrayList],
    }).compileComponents();

    fixture = TestBed.createComponent(XrayList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
