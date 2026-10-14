export interface ClaimResponseDTO {
  readingId?: number;
  claimId?: number;
  policyId: number;
  farmId?: number;
  availabilityPercentage: number;
  thresholdBreached: boolean;
  payoutPercentage: number;
  compensationAmount: number;
  payoutStatus: string;
  claimStatus: string;
  recordedAt?: string;
  claimsOfficerId?: number;
  claimsOfficerRemarks?: string;
  feedbackStatus?: 'SATISFIED' | 'REQUEST_MORE_AMOUNT' | null;
  feedbackRating?: number;
  feedbackComments?: string;
  requestedAdditionalAmount?: number;
  feedbackSubmittedAt?: string;
  settledAt?: string;
}

export interface ClaimDecisionReqDTO {
  remarks?: string;
}

export interface ClaimFeedbackReqDTO {
  feedbackStatus: 'SATISFIED' | 'REQUEST_MORE_AMOUNT';
  feedbackRating?: number;
  feedbackComments?: string;
  requestedAdditionalAmount?: number;
}
