import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { DoctorService, DoctorInfo } from '../../core/services/doctor.service';

@Component({
  selector: 'app-add-doctor-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule],
  templateUrl: './add-doctor.html',
  styleUrls: ['./add-doctor.scss']
})
export class AddDoctor implements OnInit {
  form!: FormGroup;
  isLoading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private dialogRef: MatDialogRef<AddDoctor>
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName:  ['', Validators.required],
      email:     ['', [Validators.required, Validators.email]],
      password:  ['', [Validators.required, Validators.minLength(6)]],
      specialty: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.isLoading = true;
    this.doctorService.createDoctor({ ...this.form.value, role: 'DOCTOR' }).subscribe({
      next:  (created: DoctorInfo) => this.dialogRef.close(created),
      error: () => {
        this.isLoading = false;
        this.error = 'Crearea a eșuat.';
      }
    });
  }

  cancel(): void { this.dialogRef.close(); }
}
