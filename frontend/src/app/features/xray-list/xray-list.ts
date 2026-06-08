import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { XrayService, XrayResponse } from '../../core/services/xray.service';
import { XrayUpload } from '../xray-upload/xray-upload';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-xray-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './xray-list.html',
  styleUrls: ['./xray-list.css']
})
export class XrayList implements OnInit {
  @Input({ required: true }) patientId!: number;

  xrays = signal<XrayResponse[]>([]);
  isLoading = signal(false);
  loadError = signal<string | null>(null);
  isCollapsed = signal(false);          // ← collapsable
  deletingId = signal<number | null>(null);

  constructor(private xrayService: XrayService) {}

  ngOnInit(): void {
    this.loadXrays();
  }

  loadXrays(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.xrayService.getXraysForPatient(this.patientId)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (xrays) => this.xrays.set(xrays),
        error: (err) => {
          console.error('Failed to load xrays:', err);
          this.loadError.set('Nu s-au putut încărca radiografiile');
        }
      });
  }

  toggleCollapse(): void {
    this.isCollapsed.update(v => !v);
  }

  deleteXray(xrayId: number): void {
    if (!confirm('Ești sigur că vrei să ștergi această radiografie?')) return;

    this.deletingId.set(xrayId);
    this.xrayService.deleteXray(xrayId)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this.xrays.update(list => list.filter(x => x.id !== xrayId));
        },
        error: (err) => {
          console.error('Delete failed:', err);
          alert('Ștergerea a eșuat. Verifică permisiunile.');
        }
      });
  }

  onXrayUploaded(xray: XrayResponse): void {
    this.loadXrays();
  }
}
