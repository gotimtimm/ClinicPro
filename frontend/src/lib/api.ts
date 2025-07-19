import { Patient, Staff, Appointment, Inventory } from './types';
import { MedicalRecord, Billing, Feedback } from './types';

export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

// Generic request helpers
async function request<T>(url: string, options?: RequestInit): Promise<T> {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || `HTTP ${response.status}: ${response.statusText}`);
    }
    
    // For DELETE operations (204 No Content), return undefined instead of trying to parse JSON
    if (response.status === 204) {
      return undefined as T;
    }
    
    // Check if response is HTML (error page) instead of JSON
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      throw new Error(`Server returned ${contentType || 'non-JSON'} response. Is the backend server running?`);
    }
    
    return response.json();
  } catch (error) {
    if (error instanceof TypeError && error.message.includes('fetch')) {
      throw new Error('Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
    }
    throw error;
  }
}

export function get<T>(path: string) {
  return request<T>(`${API_BASE_URL}${path}`);
}

export function post<T>(path: string, body: any) {
  return request<T>(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

export function put<T>(path: string, body: any) {
  return request<T>(`${API_BASE_URL}${path}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

export function del<T>(path: string) {
  return request<T>(`${API_BASE_URL}${path}`, {
    method: "DELETE"
  });
}

// Resource-specific API functions with proper typing
export const api = {
  patients: {
    list: () => get<Patient[]>("/api/patients"),
    get: (id: string | number) => get<Patient>(`/api/patients/${id}`),
    create: (data: Omit<Patient, 'patientID'>) => post<Patient>("/api/patients", data),
    update: (id: string | number, data: Patient) => put<Patient>(`/api/patients/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/patients/${id}`),
    searchByName: (name: string) => get<Patient[]>(`/api/patients/search/${encodeURIComponent(name)}`),
    getIdByName: (name: string) => get<number>(`/api/patients/id/${encodeURIComponent(name)}`),
  },
  staff: {
    list: () => get<Staff[]>("/api/staff"),
    get: (id: string | number) => get<Staff>(`/api/staff/${id}`),
    create: (data: Omit<Staff, 'staffID'>) => post<Staff>("/api/staff", data),
    update: (id: string | number, data: Staff) => put<Staff>(`/api/staff/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/staff/${id}`),
    searchByName: (name: string) => get<Staff[]>(`/api/staff/search/${encodeURIComponent(name)}`),
    getIdByName: (name: string) => get<number>(`/api/staff/id/${encodeURIComponent(name)}`),
  },
  appointments: {
    list: () => get<Appointment[]>("/api/appointments"),
    get: (id: string | number) => get<Appointment>(`/api/appointments/${id}`),
    create: (data: Omit<Appointment, 'appointmentID'>) => post<Appointment>("/api/appointments", data),
    update: (id: string | number, data: Appointment) => put<Appointment>(`/api/appointments/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/appointments/${id}`),
    listWithNames: () => get<any[]>("/api/appointments/with-names"),
    createWithNames: (data: { patientName: string; doctorName: string; date: string; time: string; duration: number; visitType: string; status: string; notes: string }) => 
      post<any>("/api/appointments/with-names", data),
  },
  inventory: {
    list: () => get<Inventory[]>("/api/inventory"),
    get: (id: string | number) => get<Inventory>(`/api/inventory/${id}`),
    create: (data: Omit<Inventory, 'itemID'>) => post<Inventory>("/api/inventory", data),
    update: (id: string | number, data: Inventory) => put<Inventory>(`/api/inventory/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/inventory/${id}`),
  },
  medicalRecords: {
    list: (patientID?: number, doctorID?: number, recordType?: string) => {
      const params = new URLSearchParams();
      if (patientID) params.append('patientID', patientID.toString());
      if (doctorID) params.append('doctorID', doctorID.toString());
      if (recordType) params.append('recordType', recordType);
      const query = params.toString();
      return get<MedicalRecord[]>(`/api/medical-records${query ? '?' + query : ''}`);
    },
    listWithNames: (patientID?: number, doctorID?: number, recordType?: string) => {
      const params = new URLSearchParams();
      if (patientID) params.append('patientID', patientID.toString());
      if (doctorID) params.append('doctorID', doctorID.toString());
      if (recordType) params.append('recordType', recordType);
      const query = params.toString();
      return get<any[]>(`/api/medical-records/with-names${query ? '?' + query : ''}`);
    },
    get: (id: string | number) => get<MedicalRecord>(`/api/medical-records/${id}`),
    getWithDetails: (id: string | number) => get<any>(`/api/medical-records/${id}/details`),
    create: (data: Omit<MedicalRecord, 'recordID'>) => post<MedicalRecord>("/api/medical-records", data),
    update: (id: string | number, data: MedicalRecord) => put<MedicalRecord>(`/api/medical-records/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/medical-records/${id}`),
  },
  billing: {
    list: () => get<Billing[]>("/api/billing"),
    get: (id: string | number) => get<Billing>(`/api/billing/${id}`),
    create: (data: Omit<Billing, 'billingID'>) => post<Billing>("/api/billing", data),
    update: (id: string | number, data: Billing) => put<Billing>(`/api/billing/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/billing/${id}`),
  },
  feedback: {
    list: () => get<Feedback[]>("/api/feedback"),
    get: (id: string | number) => get<Feedback>(`/api/feedback/${id}`),
    create: (data: Omit<Feedback, 'feedbackID'>) => post<Feedback>("/api/feedback", data),
    update: (id: string | number, data: Feedback) => put<Feedback>(`/api/feedback/${id}`, data),
    delete: (id: string | number) => del<void>(`/api/feedback/${id}`),
  },
  appointmentInventory: {
    list: () => get<any[]>("/api/appointment-inventory"),
    get: (appointmentId: string | number, itemId: string | number) => get<any>(`/api/appointment-inventory/${appointmentId}/${itemId}`),
    create: (data: any) => post<any>("/api/appointment-inventory", data),
    update: (appointmentId: string | number, itemId: string | number, data: any) => put<any>(`/api/appointment-inventory/${appointmentId}/${itemId}`, data),
    delete: (appointmentId: string | number, itemId: string | number) => del<void>(`/api/appointment-inventory/${appointmentId}/${itemId}`),
  },
};
