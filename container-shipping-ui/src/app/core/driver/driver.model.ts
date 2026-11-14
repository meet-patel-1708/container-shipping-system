export type DriverType = 'VEHICLE_DRIVER' | 'SHIP_DRIVER';
export type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';
export type DriverDocType =
  | 'DRIVING_LICENSE' | 'ID_PROOF' | 'VEHICLE_RC' | 'SEAMAN_BOOK' | 'PROFILE_PHOTO';

export interface DriverDocument {
  documentId: number;
  documentType: DriverDocType;
  fileName: string;
  sizeBytes: number;
  createdAt: string;
}

export interface DriverApplication {
  applicationId: number;
  userId: number;
  userName: string;
  userEmail: string;
  userPhone: string;
  driverType: DriverType;
  licenseNumber: string | null;
  experienceYears: number | null;
  vehicleNumber: string | null;
  status: ApplicationStatus;
  rejectionReason: string | null;
  submittedAt: string | null;
  reviewedAt: string | null;
  documents: DriverDocument[];
  missingDocuments: DriverDocType[];
}

export interface DriverApplicationRequest {
  driverType: DriverType;
  licenseNumber: string;
  experienceYears: number;
  vehicleNumber: string;
}

export const DOC_LABELS: Record<DriverDocType, string> = {
  DRIVING_LICENSE: 'Driving licence',
  ID_PROOF: 'ID proof (Aadhaar / PAN / Voter ID)',
  VEHICLE_RC: 'Vehicle RC',
  SEAMAN_BOOK: 'Seaman book / certificate',
  PROFILE_PHOTO: 'Profile photo',
};

export const REQUIRED_DOCS: Record<DriverType, DriverDocType[]> = {
  VEHICLE_DRIVER: ['DRIVING_LICENSE', 'ID_PROOF', 'VEHICLE_RC', 'PROFILE_PHOTO'],
  SHIP_DRIVER: ['SEAMAN_BOOK', 'ID_PROOF', 'PROFILE_PHOTO'],
};