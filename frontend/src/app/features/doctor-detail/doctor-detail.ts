import { Component, OnInit, ChangeDetectorRef  } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DoctorService, DoctorInfo } from '../../core/services/doctor.service';

@Component({
  selector: 'app-doctor-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl:'./doctor-detail.html',
  styleUrls: ['./doctor-detail.scss']
})
export class DoctorDetail implements OnInit {
  doctor: DoctorInfo | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.doctorService.getDoctorById(id).subscribe({
      next: (data) => {
        this.doctor = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'the doctor was not found.';
        this.cdr.detectChanges();
      }
    });
  }

  goToPatient(patientId: number): void {
    this.router.navigate(['/patient', patientId]);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

}
