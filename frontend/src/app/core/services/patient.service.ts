import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';

export interface PatientInfo {
  id?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  cnp: string;
  age: number;
  gender: string;
  height: number;
  weight: number;

}

@Injectable({ providedIn: 'root' })
export class PatientService {
  private apiUrl = 'http://localhost:8080'; // sau URL-ul tău de backend

  constructor(private http: HttpClient) {}

  getMyInfo(): Observable<PatientInfo> {
    return this.http.get<PatientInfo>(`${environment.apiUrl}/patient/me`);
  }

  getAllPatients(): Observable<PatientInfo[]> {
    return this.http.get<PatientInfo[]>(`${this.apiUrl}/api/patient`);
  }
}
