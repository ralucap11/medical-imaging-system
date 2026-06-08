import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { PatientService, PatientInfo } from '../../core/services/patient.service';

@Component({
  selector: 'app-edit-patient-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule
  ],
  templateUrl: './patient-edit.html',
  styleUrls: ['./patient-edit.scss']
})
export class PatientEditDialog implements OnInit {
  form!: FormGroup;
  isLoading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private dialogRef: MatDialogRef<PatientEditDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { patientId: number }
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: [''],
      lastName:  [''],
      email:     ['', Validators.email],
      cnp:       ['', Validators.required],
      age:       [null, [Validators.required, Validators.min(0)]],
      gender:    ['', Validators.required],
      height:    [null, [Validators.required, Validators.min(0)]],
      weight:    [null, [Validators.required, Validators.min(0)]]
    });

    this.patientService.getPatientById(this.data.patientId).subscribe({
      next:  (p) => this.form.patchValue(p),
      error: ()  => this.error = 'Nu s-au putut încărca datele pacientului.'
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.isLoading = true;
    this.patientService.updatePatient(this.data.patientId, this.form.value).subscribe({
      next:  (updated) => this.dialogRef.close(updated),
      error: () => {
        this.isLoading = false;
        this.error = 'Actualizarea a eșuat.';
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
