import {Component, OnInit,  signal} from '@angular/core';
import { PatientService, PatientInfo } from '../../core/services/patient.service';
import { DoctorService, DoctorInfo } from '../../core/services/doctor.service';
import { Observable, BehaviorSubject, switchMap } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { JwtService } from '../../core/services/jwt.service';
import { Router } from '@angular/router';
import { AsyncPipe, CommonModule } from '@angular/common';
import {XrayList} from '../xray-list/xray-list';
import {PatientEditDialog} from '../patient-edit/patient-edit';
import {MatDialog} from '@angular/material/dialog';
import { EditDoctor } from '../edit-doctor/edit-doctor';
import { DiagnosisDetail } from '../diagnosis-detail/diagnosis-detail';
import { AddDoctor } from '../add-doctor/add-doctor';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AsyncPipe, XrayList, DiagnosisDetail],
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
  allPatients = signal<PatientInfo[]>([]);
  allDoctors  = signal<DoctorInfo[]>([]);
  currentDoctorId: number = 0;
  showAssignPanel = false;

  constructor(
    private auth: AuthService,
    private jwt: JwtService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private router: Router,
    private dialog: MatDialog
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
        next: (patients) => this.allPatients.set(patients)
      });
    }

    if (this.role === 'ADMIN') {
      this.patientService.getAllPatients().subscribe({
          next: (patients) => {
            this.allPatients.set(patients);   // ← signal

        },
        error: (err) => console.error('Eroare patients:', err)
      });
      this.doctorService.getAllDoctors().subscribe({
          next:  (doctors) => this.allDoctors.set(doctors),   // ← fix
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

  openEditPatientDialog(patientId: number): void {
    this.dialog.open(PatientEditDialog, {
      width: '480px',
      data: { patientId }
    })
      .afterClosed()
      .subscribe((updated: PatientInfo) => {
        if (!updated) return;
        this.patientService.getAllPatients().subscribe({
          next: (patients) => this.allPatients.set(patients),
          error: (err) => console.error('Refresh eșuat:', err)
        });

        this.refreshDoctorInfo();
      });
  }

  openAddDoctorDialog(): void {
    this.dialog.open(AddDoctor, { width: '480px' })
      .afterClosed()
      .subscribe((created: DoctorInfo) => {
        if (!created) return;
        this.doctorService.getAllDoctors().subscribe({
          next: (doctors) => this.allDoctors.set(doctors)
        });
      });
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


  openEditDoctorDialog(doctorId: number): void {
    this.dialog.open(EditDoctor, { width: '480px', data: { doctorId } })
      .afterClosed()
      .subscribe((updated: DoctorInfo) => {
        if (!updated) return;
        this.doctorService.getAllDoctors().subscribe({
          next: (doctors) => this.allDoctors.set(doctors)
        });
      });
  }

  deleteDoctor(doctorId: number): void {
    if (!confirm('Ești sigur că vrei să ștergi acest doctor?')) return;
    this.doctorService.deleteDoctor(doctorId).subscribe({
      next:  () => this.allDoctors.update(list => list.filter(d => d.id !== doctorId)),
      error: (err) => console.error('Eroare la ștergere doctor:', err)
    });
  }

  deletePatient(patientId: number): void {
    if (!confirm('Ești sigur că vrei să ștergi acest pacient?')) return;
    this.patientService.deletePatient(patientId).subscribe({
      next:  () => this.allPatients.update(list => list.filter(p => p.id !== patientId)),
      error: (err) => console.error('Eroare la ștergere pacient:', err)
    });
  }
}
