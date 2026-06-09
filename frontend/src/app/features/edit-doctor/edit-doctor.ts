import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { DoctorService, DoctorInfo } from '../../core/services/doctor.service';

@Component({
  selector: 'app-edit-doctor-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule],
  templateUrl: './edit-doctor.html',
  styleUrls: ['./edit-doctor.scss']
})
export class EditDoctor implements OnInit {
  form!: FormGroup;
  isLoading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private dialogRef: MatDialogRef<EditDoctor>,
    @Inject(MAT_DIALOG_DATA) public data: { doctorId: number }
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: [''],
      lastName:  [''],
      email:     ['', Validators.email],
      specialty: ['', Validators.required]
    });

    this.doctorService.getDoctorById(this.data.doctorId).subscribe({
      next:  (d) => this.form.patchValue(d),
      error: ()  => this.error = 'Nu s-au putut încărca datele doctorului.'
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.isLoading = true;
    this.doctorService.updateDoctor(this.data.doctorId, this.form.value).subscribe({
      next:  (updated: DoctorInfo) => this.dialogRef.close(updated),
      error: () => {
        this.isLoading = false;
        this.error = 'Actualizarea a eșuat.';
      }
    });
  }

  cancel(): void { this.dialogRef.close(); }
}
