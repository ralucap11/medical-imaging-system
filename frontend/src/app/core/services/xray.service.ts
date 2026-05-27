import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface XrayResponse {
  id: number;
  fileName: string;
  format: string;
  patientId: number;
  patientFirstName: string;
  patientLastName: string;
}

@Injectable({ providedIn: 'root' })
export class XrayService {
  private baseUrl = 'http://localhost:8080/api/xray';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const credentials = btoa('patient:patient');
    return new HttpHeaders({ Authorization: `Basic ${credentials}` });
  }

  uploadXray(patientId: number, file: File): Observable<XrayResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<XrayResponse>(
      `${this.baseUrl}/upload/${patientId}`,
      formData,
      { headers: this.getHeaders() }
    );
  }

  getXraysForPatient(patientId: number): Observable<XrayResponse[]> {
    return this.http.get<XrayResponse[]>(
      `${this.baseUrl}/patient/${patientId}`,
      { headers: this.getHeaders() }
    );
  }
}
