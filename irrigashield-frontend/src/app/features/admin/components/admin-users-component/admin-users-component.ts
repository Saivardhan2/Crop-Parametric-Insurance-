import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../services/admin-service';
import { UserResDTO } from '../../../user/models/UserResDTO';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './admin-users-component.html',
  styleUrl: './admin-users-component.css'
})
export class AdminUsersComponent implements OnInit {
  private fb = inject(FormBuilder);
  private adminService = inject(AdminService);
  private cdr = inject(ChangeDetectorRef);

  users: UserResDTO[] = [];
  showModal = false;
  message = '';
  errorMessage = '';

  userForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['Password123', [Validators.required, Validators.minLength(8)]],
    role: ['UNDERWRITER', [Validators.required]]
  });

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getUsers().subscribe(users => {
      this.users = users;
      this.cdr.detectChanges();
    });
  }

  get totalUsers(): number {
    return this.users.length;
  }

  get activeUsersCount(): number {
    return this.users.filter(u => u.status === 'ACTIVE').length;
  }

  get inactiveUsersCount(): number {
    return this.users.filter(u => u.status === 'INACTIVE' || u.status === 'BLOCKED').length;
  }

  get roleCounts(): { [key: string]: number } {
    const counts: { [key: string]: number } = {};
    for (const u of this.users) {
      counts[u.role] = (counts[u.role] || 0) + 1;
    }
    return counts;
  }

  toggleUserStatus(user: UserResDTO): void {
    if (user.email === 'admin@irrigashield.com') {
      this.errorMessage = 'Super admin account cannot be deactivated.';
      this.cdr.detectChanges();
      return;
    }

    const nextStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.adminService.updateUserStatus(user.userId, nextStatus).subscribe({
      next: (updated) => {
        user.status = updated.status;
        this.message = 'User ' + user.email + ' status changed to ' + updated.status;
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || ('Failed to update status for ' + user.email);
        this.cdr.detectChanges();
      }
    });
  }

  openModal(): void {
    this.showModal = true;
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showModal = false;
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.adminService.createUser(this.userForm.value).subscribe({
        next: (created) => {
          this.message = 'User ' + created.email + ' created successfully.';
          this.errorMessage = '';
          this.showModal = false;
          this.users = [created, ...this.users.filter(u => u.userId !== created.userId)];
          this.userForm.reset({ password: 'Password123', role: 'UNDERWRITER' });
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to create user.';
          this.cdr.detectChanges();
        }
      });
    }
  }
}