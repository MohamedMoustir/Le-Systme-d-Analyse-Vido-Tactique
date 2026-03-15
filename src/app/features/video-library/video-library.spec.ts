import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VideoLibraryComponent } from './video-library';

describe('VideoLibraryComponent', () => {
  let component: VideoLibraryComponent;
  let fixture: ComponentFixture<VideoLibraryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VideoLibraryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VideoLibraryComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
