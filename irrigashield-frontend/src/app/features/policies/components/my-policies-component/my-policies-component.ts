import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { loadMyPolicies } from '../../state/policies.actions';
import { myPoliciesSelector } from '../../state/policies.selector';
import { PoliciesService } from '../../services/policies-service';
import { PolicyDTO } from '../../models/PolicyDTO';

@Component({
  selector: 'app-my-policies',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './my-policies-component.html',
  styleUrl: './my-policies-component.css'
})
export class MyPoliciesComponent implements OnInit {
  private store = inject(Store);
  private policiesService = inject(PoliciesService);
  private cdr = inject(ChangeDetectorRef);

  policies$ = this.store.select(myPoliciesSelector);
  myPoliciesList: PolicyDTO[] = [];

  ngOnInit(): void {
    this.refreshPolicies();
  }

  refreshPolicies(): void {
    this.store.dispatch(loadMyPolicies());
    this.policiesService.getMyPolicies().subscribe({
      next: (list) => {
        this.myPoliciesList = list || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });
  }
}
