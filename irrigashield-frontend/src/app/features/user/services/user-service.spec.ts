import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserService } from './user-service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch current user via GET /api/users/me', () => {
    const mockUser = { userId: 1, name: 'Ramesh', email: 'ramesh@farm.in', role: 'FARMER', status: 'ACTIVE' };
    service.getCurrentUser().subscribe(user => {
      expect(user.email).toBe('ramesh@farm.in');
      expect(user.role).toBe('FARMER');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/users/me');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });
});
