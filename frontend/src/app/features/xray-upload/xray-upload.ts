import {Component, EventEmitter, Input, Output, signal} from '@angular/core';
import { XrayService, XrayResponse } from '../../core/services/xray.service';
import {finalize} from 'rxjs';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-xray-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './xray-upload.html',
  styleUrls: ['./xray-upload.css']
})
export class XrayUpload {
  @Input() patientId!: number;
  @Output() uploaded = new EventEmitter<XrayResponse>();

  selectedFile = signal<File | null>(null);
  isUploading = signal(false);
  uploadResult = signal<XrayResponse | null>(null);
  errorMessage = signal<string | null>(null);

  constructor(private xrayService: XrayService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] || null);
  }

  upload(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.isUploading.set(true);
    this.errorMessage.set(null);

    this.xrayService.uploadXray(this.patientId, file)
      .pipe(finalize(() => {
        this.isUploading.set(false);
      }))
      .subscribe({
        next: (result) => {
          this.uploadResult.set(result);
          this.uploaded.emit(result);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.error || 'Upload eșuat');
        }
      });
  }
}
