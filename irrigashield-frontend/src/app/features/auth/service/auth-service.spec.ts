import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth-service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should authenticate user via POST /api/auth/login', () => {
    const mockRes = {
      tokenType: 'Bearer',
      accessToken: 'jwt-123',
      expiresIn: 3600,
      user: { userId: 1, name: 'Farmer', email: 'farmer@test.com', role: 'FARMER', status: 'ACTIVE' }
    };
    service.login({ email: 'farmer@test.com', password: 'Password@123' }).subscribe(res => {
      expect(res.accessToken).toBe('jwt-123');
      expect(res.user.role).toBe('FARMER');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockRes);
  });

  it('should register user via POST /api/auth/register', () => {
    const mockRes = { userId: 2, email: 'new@test.com', name: 'New User', role: 'FARMER', status: 'ACTIVE' };
    service.register({
      name: 'New User',
      email: 'new@test.com',
      password: 'Password@123',
      role: 'FARMER'
    } as any).subscribe(res => {
      expect(res.email).toBe('new@test.com');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush(mockRes);
  });

  it('should send logout request via POST /api/auth/logout', () => {
    service.logout().subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
