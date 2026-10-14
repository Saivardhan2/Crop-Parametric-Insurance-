import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { loadMyClaims } from '../../state/claims.actions';
import { myClaimsSelector } from '../../state/claims.selector';
import { ClaimsService } from '../../services/claims-service';
import { ClaimResponseDTO, ClaimFeedbackReqDTO } from '../../models/ClaimResponseDTO';

@Component({
  selector: 'app-claims-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './claims-view-component.html',
  styleUrl: './claims-view-component.css'
})
export class ClaimsViewComponent implements OnInit {
  private store = inject(Store);
  private claimsService = inject(ClaimsService);
  private cdr = inject(ChangeDetectorRef);

  claims$ = this.store.select(myClaimsSelector);
  claimsList: ClaimResponseDTO[] = [];
  userRole: string = localStorage.getItem('role') || 'FARMER';
  activeFilter: 'ALL' | 'PENDING' | 'SETTLED' | 'REJECTED' = 'ALL';

  officerRemarks: { [key: number]: string } = {};
  officerCompensation: { [key: number]: number } = {};
  activeFeedbackForms: { [key: number]: ClaimFeedbackReqDTO } = {};
  actionLoading: { [key: number]: boolean } = {};
  actionMessage: { [key: number]: { type: 'success' | 'error'; text: string } } = {};

  ngOnInit(): void {
    this.userRole = localStorage.getItem('role') || 'FARMER';
    this.refreshClaims();
  }

  isOfficer(): boolean {
    return this.userRole === 'CLAIMS_OFFICER' || this.userRole === 'ADMIN';
  }

  refreshClaims(): void {
    if (this.isOfficer()) {
      this.claimsService.getAllClaims().subscribe({
        next: (list) => {
          this.claimsList = list || [];
          this.initOfficerCompensation();
          this.cdr.detectChanges();
        },
        error: () => {
          this.cdr.detectChanges();
        }
      });
    } else {
      this.store.dispatch(loadMyClaims());
      this.claimsService.getMyClaims().subscribe({
        next: (list) => {
          this.claimsList = list || [];
          this.initOfficerCompensation();
          this.cdr.detectChanges();
        },
        error: () => {
          this.cdr.detectChanges();
        }
      });
    }
  }

  get filteredClaims(): ClaimResponseDTO[] {
    if (this.activeFilter === 'ALL') return this.claimsList;
    if (this.activeFilter === 'PENDING') {
      return this.claimsList.filter(c => c.claimStatus === 'PENDING_APPROVAL');
    }
    if (this.activeFilter === 'SETTLED') {
      return this.claimsList.filter(c => c.claimStatus === 'SETTLED');
    }
    if (this.activeFilter === 'REJECTED') {
      return this.claimsList.filter(c => c.claimStatus === 'REJECTED');
    }
    return this.claimsList;
  }

  get pendingCount(): number {
    return this.claimsList.filter(c => c.claimStatus === 'PENDING_APPROVAL').length;
  }

  private initOfficerCompensation(): void {
    this.claimsList.forEach(c => {
      if (c.claimId && c.compensationAmount != null && this.officerCompensation[c.claimId] === undefined) {
        this.officerCompensation[c.claimId] = c.compensationAmount;
      }
    });
  }

  onApprove(claimId: number): void {
    const remarks = this.officerRemarks[claimId] || '';
    const adjustedAmount = this.officerCompensation[claimId];
    this.actionLoading[claimId] = true;
    this.claimsService.approveClaim(claimId, { remarks, adjustedAmount }).subscribe({
      next: (updated) => {
        this.actionLoading[claimId] = false;
        this.actionMessage[claimId] = { type: 'success', text: 'Claim approved and settled successfully!' };
        this.updateClaimInList(updated);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.actionLoading[claimId] = false;
        this.actionMessage[claimId] = { type: 'error', text: err.error?.message || 'Failed to approve claim.' };
        this.cdr.detectChanges();
      }
    });
  }

  onReject(claimId: number): void {
    const remarks = this.officerRemarks[claimId] || '';
    this.actionLoading[claimId] = true;
    this.claimsService.rejectClaim(claimId, remarks).subscribe({
      next: (updated) => {
        this.actionLoading[claimId] = false;
        this.actionMessage[claimId] = { type: 'success', text: 'Claim rejected.' };
        this.updateClaimInList(updated);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.actionLoading[claimId] = false;
        this.actionMessage[claimId] = { type: 'error', text: err.error?.message || 'Failed to reject claim.' };
        this.cdr.detectChanges();
      }
    });
  }

  openFeedbackForm(claimId: number): void {
    this.activeFeedbackForms[claimId] = {
      feedbackStatus: 'SATISFIED',
      feedbackRating: 5,
      feedbackComments: '',
      requestedAdditionalAmount: 0
    };
    this.cdr.detectChanges();
  }

  cancelFeedbackForm(claimId: number): void {
    delete this.activeFeedbackForms[claimId];
    this.cdr.detectChanges();
  }

  onSubmitFeedback(claimId: number): void {
    const form = this.activeFeedbackForms[claimId];
    if (!form) return;
    this.actionLoading[claimId] = true;
    this.claimsService.submitClaimFeedback(claimId, form).subscribe({
      next: (updated) => {
        this.actionLoading[claimId] = false;
        delete this.activeFeedbackForms[claimId];
        this.actionMessage[claimId] = { type: 'success', text: 'Thank you! Your feedback has been recorded.' };
        this.updateClaimInList(updated);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.actionLoading[claimId] = false;
        this.actionMessage[claimId] = { type: 'error', text: err.error?.message || 'Failed to submit feedback.' };
        this.cdr.detectChanges();
      }
    });
  }

  private updateClaimInList(updated: ClaimResponseDTO): void {
    const idx = this.claimsList.findIndex(c => c.claimId === updated.claimId);
    if (idx !== -1) {
      this.claimsList[idx] = updated;
    } else {
      this.claimsList.unshift(updated);
    }
  }
}
