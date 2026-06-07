import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, RegisterRequest } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  form: RegisterRequest = { firstName: '', lastName: '', email: '', password: '' };
  success = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit(): void {
    this.error = '';
    this.success = '';
    this.loading = true;

    this.auth.register(this.form).subscribe({
      next: () => {
        this.success = 'Cont creat!';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/auth/login']), 2500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Eroare la înregistrare.';
        this.loading = false;
      }
    });
  }
}
