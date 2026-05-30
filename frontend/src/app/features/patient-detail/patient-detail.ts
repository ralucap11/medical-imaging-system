import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService, PatientInfo } from '../../core/services/patient.service';
import { XrayUpload } from '../xray-upload/xray-upload';
import {XrayList} from '../xray-list/xray-list';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, XrayList],
  templateUrl: './patient-detail.html',
  styleUrls: ['./patient-detail.scss']
})
export class PatientDetail implements OnInit {
  patient: PatientInfo | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private patientService: PatientService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.patientService.getPatientById(id).subscribe({
      next: (data) => {
        this.patient = data;
        this.cdr.detectChanges();  // ← forțează re-render
      },
      error: (err) => {
        this.error = `Eroare ${err.status}: ${err.message}`;
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
