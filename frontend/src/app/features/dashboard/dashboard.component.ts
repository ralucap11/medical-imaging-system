import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { PatientService, PatientInfo } from '../../core/services/patient.service';
import { DoctorService, DoctorInfo } from '../../core/services/doctor.service';
import { Observable, BehaviorSubject, switchMap } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { JwtService } from '../../core/services/jwt.service';
import { Router } from '@angular/router';
import { XrayUpload } from '../xray-upload/xray-upload';
import { AsyncPipe, CommonModule } from '@angular/common';
import {XrayList} from '../xray-list/xray-list';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AsyncPipe, XrayList],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  email: string | null = null;
  role: string | null = null;
  fullName: string | null = null;
  patientInfo$: Observable<PatientInfo> | null = null;
  doctorInfo$: Observable<DoctorInfo> | null = null;
  private refreshTrigger$ = new BehaviorSubject<void>(undefined);
  allPatients: PatientInfo[] = [];
  allDoctors: DoctorInfo[] = [];
  currentDoctorId: number = 0;
  showAssignPanel = false;


  constructor(
    private auth: AuthService,
    private jwt: JwtService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    const token = this.auth.getToken();
    if (token) {
      const decoded = this.jwt.getDecodedToken(token);
      this.email    = decoded?.email ?? null;
      this.role     = decoded?.role?.replace('ROLE_', '') ?? null;
      this.fullName = `${decoded?.firstName ?? ''} ${decoded?.lastName ?? ''}`.trim();
    }
  }

  ngOnInit(): void {
    if (this.role === 'PATIENT') {
      this.patientInfo$ = this.patientService.getMyInfo();
    }

    if (this.role === 'DOCTOR') {
      this.doctorInfo$ = this.refreshTrigger$.pipe(
        switchMap(() => this.doctorService.getMyInfo())
      );
      this.doctorService.getMyInfo().subscribe({
        next: (info) => { this.currentDoctorId = info.id ?? 0; }
      });
      this.patientService.getAllPatients().subscribe({
        next: (patients) => { this.allPatients = patients; }
      });
    }

    if (this.role === 'ADMIN') {
      this.patientService.getAllPatients().subscribe({
        next: (patients) => {
          this.allPatients = patients;
          this.cdr.detectChanges();   // ← forțează update
        },
        error: (err) => console.error('Eroare patients:', err)
      });
      this.doctorService.getAllDoctors().subscribe({
        next: (doctors) => {
          this.allDoctors = doctors;
          this.cdr.detectChanges();   // ← forțează update
        },
        error: (err) => console.error('Eroare doctors:', err)
      });
    }
  }

  refreshDoctorInfo(): void {
    this.refreshTrigger$.next();
  }

  assign(patientId: number): void {
    if (!this.currentDoctorId) return;
    this.doctorService.assignPatient(this.currentDoctorId, patientId).subscribe({
      next: () => this.refreshDoctorInfo(),
      error: (err) => { if (err.status === 409) this.refreshDoctorInfo(); }
    });
  }

  unassign(patientId: number): void {
    if (!this.currentDoctorId) return;
    this.doctorService.unassignPatient(this.currentDoctorId, patientId).subscribe({
      next: () => this.refreshDoctorInfo(),
      error: (err) => console.error('Eroare la eliminare:', err)
    });
  }

  isAssigned(doctorInfo: DoctorInfo, patientId: number): boolean {
    return (doctorInfo.patients ?? []).some(p => p.id === patientId);
  }

  goToPatient(patientId: number): void {
    this.router.navigate(['/patient', patientId]);
  }

  goToDoctor(doctorId: number): void {
    this.router.navigate(['/doctor', doctorId]);
  }

  logout(): void {
    this.auth.logout();
  }

  selectedDoctor: DoctorInfo | null = null;

  selectDoctor(doctor: DoctorInfo): void {
    this.selectedDoctor = this.selectedDoctor?.id === doctor.id ? null : doctor;
  }
}
