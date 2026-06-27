import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DiagnosisService, Diagnosis } from '../../core/services/diagnosis.service';

import { finalize } from 'rxjs';

@Component({
  selector: 'app-diagnosis-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './diagnosis-detail.html',
  styleUrls: ['./diagnosis-detail.css']
})
export class DiagnosisDetail implements OnInit {
  @Input({ required: true }) patientId!: number;
  @Input() canEdit = false;

  private diagnosisService = inject(DiagnosisService);

  diagnosis = signal<Diagnosis | null>(null);
  title = signal('');
  description = signal('');
  isLoading = signal(false);
  isSaving = signal(false);
  savedMessage = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadDiagnosis();
  }

  loadDiagnosis(): void {
    this.isLoading.set(true);
    this.diagnosisService.getByPatient(this.patientId)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (d) => {
          this.diagnosis.set(d);

          this.title.set(d?.title ?? '');
          this.description.set(d?.description ?? '');
        },
        error: () => this.errorMessage.set('Nu s-a putut încărca diagnosticul')
      });
  }

  save(): void {
    this.isSaving.set(true);
    this.savedMessage.set(false);
    this.diagnosisService.save(this.patientId, {
      title: this.title(),
      description: this.description()
    })
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: (d) => {
          this.diagnosis.set(d);
          this.savedMessage.set(true);
          setTimeout(() => this.savedMessage.set(false), 3000);
        },
        error: () => this.errorMessage.set('Salvarea a eșuat')
      });
  }
}
