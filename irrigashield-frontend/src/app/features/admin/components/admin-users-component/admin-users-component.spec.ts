import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AdminUsersComponent } from './admin-users-component';
import { AdminService } from '../../services/admin-service';
import { vi } from 'vitest';

describe('AdminUsersComponent', () => {
  const mockUsers = [
    { userId: 1, name: 'Admin One', email: 'admin@test.com', role: 'ADMIN', status: 'ACTIVE' },
    { userId: 2, name: 'Farmer One', email: 'farmer@test.com', role: 'FARMER', status: 'INACTIVE' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUsersComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
  });

  it('should load users and compute active/inactive counts and roleCounts', () => {
    const adminService = TestBed.inject(AdminService);
    vi.spyOn(adminService, 'getUsers').mockReturnValue(of(mockUsers));

    const fixture = TestBed.createComponent(AdminUsersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.users.length).toBe(2);
    expect(component.activeUsersCount).toBe(1);
    expect(component.inactiveUsersCount).toBe(1);
    expect(component.roleCounts['FARMER']).toBe(1);
  });

  it('should toggle user status via AdminService', () => {
    const adminService = TestBed.inject(AdminService);
    vi.spyOn(adminService, 'getUsers').mockReturnValue(of(mockUsers));
    const statusSpy = vi.spyOn(adminService, 'updateUserStatus').mockReturnValue(of({ ...mockUsers[1], status: 'ACTIVE' }));

    const fixture = TestBed.createComponent(AdminUsersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.toggleUserStatus(component.users[1]);
    expect(statusSpy).toHaveBeenCalledWith(2, 'ACTIVE');
  });
});
