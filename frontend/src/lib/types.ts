// Frontend types that match backend models

export interface Patient {
  patientID?: number;
  name: string;
  birthDate?: string;
  phone?: string;
  email?: string;
  insuranceInfo?: string;
  firstVisitDate?: string;
  primaryDoctorID?: number;
  activeStatus?: boolean;
}

export interface Staff {
  staffID?: number;
  name: string;
  jobType: 'Doctor' | 'Nurse' | 'Admin';
  specialization?: string;
  licenseNumber?: string;
  phone?: string;
  email?: string;
  hireDate?: string;
  workingDays?: string;
  activeStatus?: boolean;
}

export interface Appointment {
  appointmentID?: number;
  patientID: number;
  doctorID: number;
  date: string;
  time: string;
  duration?: number;
  visitType: 'Check-up' | 'Procedure' | 'Emergency';
  status?: 'Done' | 'Not Done' | 'Canceled';
  notes?: string;
}

export interface Inventory {
  itemID?: number;
  name: string;
  type: 'Medicine' | 'Equipment';
  purpose?: string;
  stockQuantity?: number;
  reorderThreshold?: number;
  unitPrice?: number;
  supplierInfo?: string;
  expiryDate?: string;
  activeStatus?: boolean;
}

export interface MedicalRecord {
  recordID?: number;
  patientID: number;
  doctorID: number;
  appointmentID?: number;
  recordType: 'Consultation' | 'Lab Results' | 'Surgery' | 'Checkup' | 'X-Ray' | 'Prescription';
  diagnosis?: string;
  treatment?: string;
  vitalSigns?: string;
  medications?: string;
  followUpDate?: string;
  recordDate?: string;
  notes?: string;
  activeStatus?: boolean;
}

export interface Billing {
  billingID?: number;
  appointmentID: number;
  amount: number;
  paid?: boolean;
  paymentDate?: string;
}

export interface Feedback {
  feedbackID?: number;
  appointmentID: number;
  doctorID: number;
  patientID: number;
  rating: number; // 1-5
  comments?: string;
}

export interface StaffSchedule {
  scheduleID?: number;
  staffID: number;
  date: string;
  startTime: string;
  endTime: string;
  shiftType: 'Morning' | 'Afternoon' | 'Evening' | 'Night';
  status?: 'Scheduled' | 'Completed' | 'Canceled';
  notes?: string;
}

export interface TimeOffRequest {
  requestID?: number;
  staffID: number;
  startDate: string;
  endDate: string;
  reason?: string;
  status?: 'Pending' | 'Approved' | 'Rejected';
  requestDate?: string;
  approvedBy?: number;
  approvalDate?: string;
  notes?: string;
}

export interface InventoryTransaction {
  transactionID?: number;
  itemID: number;
  transactionType: 'Purchase' | 'Usage' | 'Adjustment' | 'Expired';
  quantity: number;
  unitPrice?: number;
  totalAmount?: number;
  transactionDate?: string;
  referenceID?: number;
  notes?: string;
}

export interface AppointmentInventory {
  appointmentID: number;
  itemID: number;
  quantityUsed: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

// Extended types for frontend display
export interface PatientWithDetails extends Patient {
  doctorName?: string;
  appointmentCount?: number;
  lastVisit?: string;
}

export interface AppointmentWithDetails extends Appointment {
  patientName?: string;
  doctorName?: string;
  patientPhone?: string;
  doctorSpecialization?: string;
}

export interface StaffWithDetails extends Staff {
  appointmentCount?: number;
  nextAppointment?: string;
  averageRating?: number;
}