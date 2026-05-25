import { jwtDecode } from 'jwt-decode';
import {Injectable} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class JwtService {
  getDecodedToken(token: string): any {
    try {
      return jwtDecode(token);
    } catch(Error) {
      return null;
    }
  }

  getEmail(token: string): string | null {
    return this.getDecodedToken(token)?.email ?? null;
  }

  getRole(token: string): string | null {
    return this.getDecodedToken(token)?.role ?? null;
  }

  getFirstName(token: string): string | null {
    return this.getDecodedToken(token)?.firstName ?? null;
  }

  getLastName(token: string): string | null {
    return this.getDecodedToken(token)?.lastName ?? null;
  } 


  isTokenExpired(token: string): boolean {
    const decoded = this.getDecodedToken(token);
    if (!decoded) return true;
    return decoded.exp * 1000 < Date.now();
  }
}
