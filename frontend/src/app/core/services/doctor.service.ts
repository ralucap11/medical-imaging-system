import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {PatientInfo} from './patient.service';

export interface PatientSummary {
  id: number;
  firstName: string;
  lastName: string;
  age: number;
  gender: string;
}

export interface DoctorInfo {
  specialty: string;
  patients: PatientSummary[];
  id: number;
}

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getMyInfo(): Observable<DoctorInfo> {
    return this.http.get<DoctorInfo>(`${environment.apiUrl}/doctor/me`);
  }

  assignPatient(doctorId: number, patientId: number): Observable<DoctorInfo> {
    return this.http.post<DoctorInfo>(
      `${this.apiUrl}/api/doctor/${doctorId}/patients/${patientId}`, {}  // ← /api/doctor nu /doctors
    );
  }

  unassignPatient(doctorId: number, patientId: number): Observable<DoctorInfo> {
    return this.http.delete<DoctorInfo>(
      `${this.apiUrl}/api/doctor/${doctorId}/patients/${patientId}`  // ← la fel
    );
  }
}
