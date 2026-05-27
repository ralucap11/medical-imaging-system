import { Component, Input } from '@angular/core';
import { XrayService, XrayResponse } from '../../core/services/xray.service';

@Component({
  selector: 'app-xray-upload',
  templateUrl: './xray-upload.html',
  styleUrls: ['./xray-upload.css']
})
export class XrayUploadComponent {
  @Input() patientId!: number;

  selectedFile: File | null = null;
  isUploading = false;
  uploadResult: XrayResponse | null = null;
  errorMessage = '';

  constructor(private xrayService: XrayService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.errorMessage = '';
      this.uploadResult = null;
    }
  }

  upload(): void {
    if (!this.selectedFile || !this.patientId) return;

    this.isUploading = true;
    this.errorMessage = '';

    this.xrayService.uploadXray(this.patientId, this.selectedFile).subscribe({
       next: (result: XrayResponse) => {
        this.uploadResult = result;
        this.isUploading = false;
        this.selectedFile = null;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message || 'Eroare la upload. Încearcă din nou.';
        this.isUploading = false;
      }
    });
  }
}
