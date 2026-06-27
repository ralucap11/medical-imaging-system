import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Diagnosis {
  id: number;
  title: string | null;
  description: string | null;
  date: string | null;
  patientId: number;
  doctorFirstName: string | null;
  doctorLastName: string | null;
}

export interface DiagnosisRequest {
  title: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class DiagnosisService {
  private http = inject(HttpClient);
  private baseUrl = '/api/diagnosis';

  getByPatient(patientId: number): Observable<Diagnosis | null> {
    return this.http.get<Diagnosis | null>(`${this.baseUrl}/patient/${patientId}`);
  }

  save(patientId: number, request: DiagnosisRequest): Observable<Diagnosis> {
    return this.http.put<Diagnosis>(`${this.baseUrl}/patient/${patientId}`, request);
  }
}
