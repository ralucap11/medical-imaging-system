import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

export interface DoctorInfo {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  specialty: string;
  role: string;
  patients: PatientSummary[];
}

export interface PatientSummary {
  id: number;
  firstName: string;
  lastName: string;
  age: number;
  gender: string;
}

@Injectable({ providedIn: 'root' })
export class DoctorService {
  constructor(private http: HttpClient) {}

  getMyInfo(): Observable<DoctorInfo> {
    return this.http.get<DoctorInfo>(`${environment.apiUrl}/doctor/me`);
  }

  getAllDoctors(): Observable<DoctorInfo[]> {
    return this.http.get<DoctorInfo[]>(`${environment.apiUrl}/doctor`);
  }

  assignPatient(doctorId: number, patientId: number): Observable<DoctorInfo> {
    return this.http.post<DoctorInfo>(
      `${environment.apiUrl}/doctor/${doctorId}/patients/${patientId}`, {}
    );
  }

  unassignPatient(doctorId: number, patientId: number): Observable<DoctorInfo> {
    return this.http.delete<DoctorInfo>(
      `${environment.apiUrl}/doctor/${doctorId}/patients/${patientId}`
    );
  }

  getDoctorById(id: number): Observable<DoctorInfo> {
    return this.http.get<DoctorInfo>(`${environment.apiUrl}/doctor/${id}`);
  }

  updateDoctor(id: number, data: Partial<DoctorInfo>): Observable<DoctorInfo> {
    return this.http.put<DoctorInfo>(`${environment.apiUrl}/doctor/${id}`, data);
  }

  deleteDoctor(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/doctor/${id}`);
  }
}
