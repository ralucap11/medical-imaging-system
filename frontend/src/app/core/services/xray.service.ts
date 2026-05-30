import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface XrayResponse {
  id: number;
  fileName: string;
  xrayName: string;
  format: string;
  description: string | null;
  dateUploaded: string;
  patientId: number;
  patientFirstName: string;
  patientLastName: string;
  aiClassification: string | null;
  aiConfidence: number | null;
  cobbAngle: number | null;
  cobbVisualization: string | null;
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

  deleteXray(xrayId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${xrayId}`,
      { headers: this.getHeaders() }
    );
  }
}
