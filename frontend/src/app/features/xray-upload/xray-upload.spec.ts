import { ComponentFixture, TestBed } from '@angular/core/testing';

import { XrayUpload } from './xray-upload';

describe('XrayUpload', () => {
  let component: XrayUpload;
  let fixture: ComponentFixture<XrayUpload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [XrayUpload],
    }).compileComponents();

    fixture = TestBed.createComponent(XrayUpload);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
