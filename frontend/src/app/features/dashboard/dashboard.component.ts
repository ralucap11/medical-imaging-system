import { Component, OnInit } from '@angular/core';
import { PatientService, PatientInfo } from '../../core/services/patient.service';
import { DoctorService, DoctorInfo } from '../../core/services/doctor.service';
import {Observable} from 'rxjs';
import {AuthService} from '../../core/services/auth.service';
import {JwtService} from '../../core/services/jwt.service';
import {Router} from '@angular/router';
import {XrayUpload} from '../xray-upload/xray-upload';
import {AsyncPipe, CommonModule} from '@angular/common';
import { BehaviorSubject, switchMap } from 'rxjs';



@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AsyncPipe, XrayUpload],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})

export class DashboardComponent implements OnInit {
  email: string | null = null;
  role: string | null = null;
  fullName: string | null = null;
  patientInfo$: Observable<PatientInfo> | null = null;
  doctorInfo$: Observable<DoctorInfo> | null = null;
  private refreshTrigger$ = new BehaviorSubject<void>(undefined); // ← adaugă aici



  allPatients: PatientInfo[] = [];
  currentDoctorPatients: PatientInfo[] = [];
  currentDoctorId: number = 0;
  showAssignPanel = false;

  constructor(
    private auth: AuthService,
    private jwt: JwtService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private router: Router
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
        next: (info) => {
          this.currentDoctorId = info.id ?? null;
        }
      });


      this.patientService.getAllPatients().subscribe({
        next: (patients) => {
          this.allPatients = patients;
        }
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
      error: (err) => {
        if (err.status === 409) {
          this.refreshDoctorInfo();
        }
      }
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

  logout(): void {
    this.auth.logout();
  }
}
