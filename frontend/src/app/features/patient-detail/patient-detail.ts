import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService, PatientInfo } from '../../core/services/patient.service';
import {XrayList} from '../xray-list/xray-list';
import {XrayUpload} from '../xray-upload/xray-upload';
import {Component, OnInit, signal, ViewChild} from '@angular/core';
import {XrayResponse} from '../../core/services/xray.service';
import { DiagnosisDetail } from '../diagnosis-detail/diagnosis-detail';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, XrayList, XrayUpload, DiagnosisDetail],
  templateUrl: './patient-detail.html',
  styleUrls: ['./patient-detail.scss']
})
export class PatientDetail implements OnInit {
  @ViewChild(XrayList) xrayListRef!: XrayList;
  patient = signal<PatientInfo | null>(null);
  error = signal<string | null>(null);
  currentPatientId = signal<number>(0);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private patientService: PatientService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentPatientId.set(Number(this.route.snapshot.paramMap.get('id')));
    this.getPatientData();
  }

  getPatientData() {
    this.patientService.getPatientById(this.currentPatientId()).subscribe({
      next: (data) => {
        this.patient.set(data);
      },
      error: (err) => {
        this.error.set(`Eroare ${err.status}: ${err.message}`);
      }
    });
  }

  isDoctor(): boolean {
    return this.authService.getRole() === 'DOCTOR';
  }

  onUploaded(xray: XrayResponse): void {
    this.xrayListRef.loadXrays();
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
