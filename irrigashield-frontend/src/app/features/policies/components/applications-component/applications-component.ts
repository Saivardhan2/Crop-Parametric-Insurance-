import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { loadMyApplications, loadMyPolicies } from '../../state/policies.actions';
import { myApplicationsSelector } from '../../state/policies.selector';
import { PoliciesService } from '../../services/policies-service';
import { PolicyApplicationDTO } from '../../models/PolicyApplicationDTO';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './applications-component.html',
  styleUrl: './applications-component.css'
})
export class ApplicationsComponent implements OnInit {
  private store = inject(Store);
  private policiesService = inject(PoliciesService);
  private cdr = inject(ChangeDetectorRef);

  applications$ = this.store.select(myApplicationsSelector);
  myAppsList: PolicyApplicationDTO[] = [];

  ngOnInit(): void {
    this.refreshApplications();
    this.applications$.subscribe(apps => {
      if (apps && apps.length > this.myAppsList.length) {
        this.myAppsList = apps;
        this.cdr.detectChanges();
      }
    });
    setTimeout(() => this.refreshApplications(), 500);
  }

  refreshApplications(): void {
    this.store.dispatch(loadMyApplications());
    this.policiesService.getMyApplications().subscribe({
      next: (apps) => {
        this.myAppsList = apps || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });
  }

  onAcceptQuote(applicationId?: number): void {
    if (applicationId) {
      this.policiesService.acceptApprovedQuote(applicationId).subscribe({
        next: () => {
          this.refreshApplications();
          this.store.dispatch(loadMyPolicies());
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Accept quote error', err);
          this.refreshApplications();
          this.cdr.detectChanges();
        }
      });
    }
  }

  onSubmitApplication(applicationId?: number): void {
    if (applicationId) {
      this.policiesService.submitApplication(applicationId).subscribe({
        next: () => {
          this.refreshApplications();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Submit application error', err);
          this.cdr.detectChanges();
        }
      });
    }
  }
}
