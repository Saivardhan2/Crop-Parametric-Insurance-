import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { loadMyClaims } from '../../state/claims.actions';
import { myClaimsSelector } from '../../state/claims.selector';
import { ClaimsService } from '../../services/claims-service';
import { ClaimResponseDTO } from '../../models/ClaimResponseDTO';

@Component({
  selector: 'app-claims-view',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './claims-view-component.html',
  styleUrl: './claims-view-component.css'
})
export class ClaimsViewComponent implements OnInit {
  private store = inject(Store);
  private claimsService = inject(ClaimsService);
  private cdr = inject(ChangeDetectorRef);

  claims$ = this.store.select(myClaimsSelector);
  claimsList: ClaimResponseDTO[] = [];

  ngOnInit(): void {
    this.refreshClaims();
  }

  refreshClaims(): void {
    this.store.dispatch(loadMyClaims());
    this.claimsService.getMyClaims().subscribe({
      next: (list) => {
        this.claimsList = list || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });
  }
}
