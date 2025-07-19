import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Plus, Search, User, Phone, Mail, Calendar, MoreHorizontal, Eye, Edit, Clock, Trash2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { api } from "@/lib/api";
import { Patient } from "@/lib/types";

const Patients = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState("");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newPatient, setNewPatient] = useState<Omit<Patient, 'patientID'>>({
    name: "",
    birthDate: "",
    phone: "",
    email: "",
    insuranceInfo: "",
    firstVisitDate: new Date().toISOString().split('T')[0],
    primaryDoctorID: 1,
    activeStatus: true
  });

  // Action button states
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);
  const [isViewDetailsOpen, setIsViewDetailsOpen] = useState(false);
  const [isEditPatientOpen, setIsEditPatientOpen] = useState(false);
  const [isScheduleAppointmentOpen, setIsScheduleAppointmentOpen] = useState(false);
  const [isRemovePatientOpen, setIsRemovePatientOpen] = useState(false);
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null);
  const [newAppointment, setNewAppointment] = useState({
    patientName: "",
    doctorName: "",
    date: new Date().toISOString().split('T')[0],
    time: "09:00",
    duration: 30,
    visitType: "Check-up" as 'Check-up' | 'Procedure' | 'Emergency',
    status: "Not Done" as 'Done' | 'Not Done' | 'Canceled',
    notes: ""
  });

  // Fetch patients from API
  const { data: patients = [], isLoading, error } = useQuery({
    queryKey: ['patients'],
    queryFn: api.patients.list,
  });

  // Fetch staff for appointment scheduling
  const { data: staff = [] } = useQuery({
    queryKey: ['staff'],
    queryFn: api.staff.list,
  });

  // Create patient mutation
  const createPatientMutation = useMutation({
    mutationFn: api.patients.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      toast({
        title: "Patient Added",
        description: `${newPatient.name} has been added successfully.`,
      });
      setIsDialogOpen(false);
      setNewPatient({
        name: "",
        birthDate: "",
        phone: "",
        email: "",
        insuranceInfo: "",
        firstVisitDate: new Date().toISOString().split('T')[0],
        primaryDoctorID: 1,
        activeStatus: true
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to add patient",
        variant: "destructive",
      });
    }
  });

  // Update patient mutation
  const updatePatientMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: Patient }) => api.patients.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      toast({
        title: "Patient Updated",
        description: "Patient information has been updated successfully.",
      });
      setIsEditPatientOpen(false);
      setEditingPatient(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to update patient",
        variant: "destructive",
      });
    }
  });

  // Delete patient mutation
  const deletePatientMutation = useMutation({
    mutationFn: (id: number) => api.patients.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      toast({
        title: "Patient Removed",
        description: "Patient has been removed successfully.",
      });
      setIsRemovePatientOpen(false);
      setSelectedPatient(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to remove patient",
        variant: "destructive",
      });
    }
  });

  // Create appointment mutation
  const createAppointmentMutation = useMutation({
    mutationFn: api.appointments.createWithNames,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      toast({
        title: "Appointment Scheduled",
        description: "Appointment has been scheduled successfully.",
      });
      setIsScheduleAppointmentOpen(false);
      setNewAppointment({
        patientName: "",
        doctorName: "",
        date: new Date().toISOString().split('T')[0],
        time: "09:00",
        duration: 30,
        visitType: "Check-up",
        status: "Not Done",
        notes: ""
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to schedule appointment",
        variant: "destructive",
      });
    }
  });

  const handleAddPatient = () => {
    createPatientMutation.mutate(newPatient);
  };

  const handleViewDetails = (patient: Patient) => {
    setSelectedPatient(patient);
    setIsViewDetailsOpen(true);
  };

  const handleEditPatient = (patient: Patient) => {
    setEditingPatient(patient);
    setIsEditPatientOpen(true);
  };

  const handleScheduleAppointment = (patient: Patient) => {
    setSelectedPatient(patient);
    setNewAppointment(prev => ({ ...prev, patientName: patient.name }));
    setIsScheduleAppointmentOpen(true);
  };

  const handleRemovePatient = (patient: Patient) => {
    setSelectedPatient(patient);
    setIsRemovePatientOpen(true);
  };

  const handleUpdatePatient = () => {
    if (editingPatient) {
      updatePatientMutation.mutate({ id: editingPatient.patientID!, data: editingPatient });
    }
  };

  const handleDeletePatient = () => {
    if (selectedPatient) {
      deletePatientMutation.mutate(selectedPatient.patientID!);
    }
  };

  const handleCreateAppointment = () => {
    createAppointmentMutation.mutate(newAppointment);
  };

  const filteredPatients = patients.filter(patient =>
    patient.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.insuranceInfo?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getStatusColor = (status: string) => {
    return status === "Active" ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-800";
  };

  const calculateAge = (birthDate: string) => {
    if (!birthDate) return 'N/A';
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return `${age} years`;
  };

  if (isLoading) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-muted-foreground">Loading patients...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-destructive">Error loading patients: {error.message}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Patients</h1>
          <p className="text-muted-foreground">Manage your patient database</p>
        </div>
        <Dialog>
          <DialogTrigger asChild>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Add New Patient
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Add New Patient</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="name">Full Name *</Label>
                <Input
                  id="name"
                  value={newPatient.name}
                  onChange={(e) => setNewPatient({ ...newPatient, name: e.target.value })}
                  placeholder="Enter full name"
                  required
                />
              </div>
              <div>
                <Label htmlFor="birthDate">Birth Date</Label>
                <Input
                  id="birthDate"
                  type="date"
                  value={newPatient.birthDate}
                  onChange={(e) => setNewPatient({ ...newPatient, birthDate: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="phone">Phone Number</Label>
                <Input
                  id="phone"
                  value={newPatient.phone}
                  onChange={(e) => setNewPatient({ ...newPatient, phone: e.target.value })}
                  placeholder="Enter phone number"
                />
              </div>
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={newPatient.email}
                  onChange={(e) => setNewPatient({ ...newPatient, email: e.target.value })}
                  placeholder="Enter email address"
                />
              </div>
              <div>
                <Label htmlFor="insuranceInfo">Insurance Information</Label>
                <Input
                  id="insuranceInfo"
                  value={newPatient.insuranceInfo}
                  onChange={(e) => setNewPatient({ ...newPatient, insuranceInfo: e.target.value })}
                  placeholder="Enter insurance details"
                />
              </div>
              <div>
                <Label htmlFor="firstVisitDate">First Visit Date</Label>
                <Input
                  id="firstVisitDate"
                  type="date"
                  value={newPatient.firstVisitDate}
                  onChange={(e) => setNewPatient({ ...newPatient, firstVisitDate: e.target.value })}
                />
              </div>
              <Button onClick={handleAddPatient} className="w-full">
                <Plus className="h-4 w-4 mr-2" />
                Add Patient
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Search and Filters */}
      <Card className="bg-gradient-card border-0 shadow-sm">
        <CardContent className="p-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="Search patients by name, email, or condition..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      {/* Patients Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredPatients.map((patient) => (
          <Card key={patient.patientID} className="bg-gradient-card border-0 shadow-sm hover:shadow-md transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-gradient-primary rounded-full">
                    <User className="h-5 w-5 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-lg text-foreground">{patient.name}</CardTitle>
                    <p className="text-sm text-muted-foreground">
                      {patient.birthDate ? `${new Date().getFullYear() - new Date(patient.birthDate).getFullYear()} years` : 'Age N/A'}
                    </p>
                  </div>
                </div>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon">
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent className="bg-popover border shadow-md">
                    <DropdownMenuItem onClick={() => handleViewDetails(patient)}>View Details</DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleEditPatient(patient)}>Edit Patient</DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleScheduleAppointment(patient)}>Schedule Appointment</DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleRemovePatient(patient)} className="text-destructive">Remove Patient</DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm">
                  <Phone className="h-4 w-4 text-muted-foreground" />
                  <span className="text-foreground">{patient.phone || 'N/A'}</span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <Mail className="h-4 w-4 text-muted-foreground" />
                  <span className="text-foreground">{patient.email || 'N/A'}</span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span className="text-foreground">First visit: {patient.firstVisitDate || 'N/A'}</span>
                </div>
              </div>
              
              <div className="flex items-center justify-between pt-3 border-t border-border">
                <Badge className={getStatusColor(patient.activeStatus ? 'Active' : 'Inactive')}>
                  {patient.activeStatus ? 'Active' : 'Inactive'}
                </Badge>
                <span className="text-sm text-muted-foreground">{patient.insuranceInfo || 'No Insurance'}</span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredPatients.length === 0 && (
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-12 text-center">
            <User className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium text-foreground mb-2">No patients found</h3>
            <p className="text-muted-foreground mb-4">Try adjusting your search criteria or add a new patient.</p>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Add First Patient
            </Button>
          </CardContent>
        </Card>
      )}

      {/* View Details Dialog */}
      <Dialog open={isViewDetailsOpen} onOpenChange={setIsViewDetailsOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Patient Details</DialogTitle>
          </DialogHeader>
          {selectedPatient && (
            <div className="space-y-4">
              <div>
                <Label>Full Name</Label>
                <p>{selectedPatient.name}</p>
              </div>
              <div>
                <Label>Birth Date</Label>
                <p>{selectedPatient.birthDate ? new Date(selectedPatient.birthDate).toLocaleDateString() : 'N/A'}</p>
              </div>
              <div>
                <Label>Age</Label>
                <p>{calculateAge(selectedPatient.birthDate)}</p>
              </div>
              <div>
                <Label>Phone</Label>
                <p>{selectedPatient.phone || 'N/A'}</p>
              </div>
              <div>
                <Label>Email</Label>
                <p>{selectedPatient.email || 'N/A'}</p>
              </div>
              <div>
                <Label>Insurance Info</Label>
                <p>{selectedPatient.insuranceInfo || 'N/A'}</p>
              </div>
              <div>
                <Label>First Visit Date</Label>
                <p>{selectedPatient.firstVisitDate ? new Date(selectedPatient.firstVisitDate).toLocaleDateString() : 'N/A'}</p>
              </div>
              <div>
                <Label>Primary Doctor</Label>
                <p>{selectedPatient.primaryDoctorID ? staff.find(s => s.staffID === selectedPatient.primaryDoctorID)?.name : 'N/A'}</p>
              </div>
              <div>
                <Label>Active Status</Label>
                <Badge className={getStatusColor(selectedPatient.activeStatus ? 'Active' : 'Inactive')}>
                  {selectedPatient.activeStatus ? 'Active' : 'Inactive'}
                </Badge>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Edit Patient Dialog */}
      <Dialog open={isEditPatientOpen} onOpenChange={setIsEditPatientOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Patient</DialogTitle>
          </DialogHeader>
          {editingPatient && (
            <div className="space-y-4">
              <div>
                <Label htmlFor="editName">Full Name *</Label>
                <Input
                  id="editName"
                  value={editingPatient.name}
                  onChange={(e) => setEditingPatient({ ...editingPatient, name: e.target.value })}
                  placeholder="Enter full name"
                  required
                />
              </div>
              <div>
                <Label htmlFor="editBirthDate">Birth Date</Label>
                <Input
                  id="editBirthDate"
                  type="date"
                  value={editingPatient.birthDate}
                  onChange={(e) => setEditingPatient({ ...editingPatient, birthDate: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="editPhone">Phone Number</Label>
                <Input
                  id="editPhone"
                  value={editingPatient.phone}
                  onChange={(e) => setEditingPatient({ ...editingPatient, phone: e.target.value })}
                  placeholder="Enter phone number"
                />
              </div>
              <div>
                <Label htmlFor="editEmail">Email</Label>
                <Input
                  id="editEmail"
                  type="email"
                  value={editingPatient.email}
                  onChange={(e) => setEditingPatient({ ...editingPatient, email: e.target.value })}
                  placeholder="Enter email address"
                />
              </div>
              <div>
                <Label htmlFor="editInsuranceInfo">Insurance Information</Label>
                <Input
                  id="editInsuranceInfo"
                  value={editingPatient.insuranceInfo}
                  onChange={(e) => setEditingPatient({ ...editingPatient, insuranceInfo: e.target.value })}
                  placeholder="Enter insurance details"
                />
              </div>
              <div>
                <Label htmlFor="editFirstVisitDate">First Visit Date</Label>
                <Input
                  id="editFirstVisitDate"
                  type="date"
                  value={editingPatient.firstVisitDate}
                  onChange={(e) => setEditingPatient({ ...editingPatient, firstVisitDate: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="editPrimaryDoctor">Primary Doctor</Label>
                <Select onValueChange={(value) => setEditingPatient({ ...editingPatient, primaryDoctorID: parseInt(value, 10) })} value={editingPatient.primaryDoctorID.toString()}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a doctor" />
                  </SelectTrigger>
                  <SelectContent>
                    {staff.map(s => (
                      <SelectItem key={s.staffID} value={s.staffID.toString()}>{s.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="editActiveStatus">Active Status</Label>
                <Select onValueChange={(value) => setEditingPatient({ ...editingPatient, activeStatus: value === 'true' })} value={editingPatient.activeStatus.toString()}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Active</SelectItem>
                    <SelectItem value="false">Inactive</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button onClick={handleUpdatePatient} className="w-full">
                <Edit className="h-4 w-4 mr-2" />
                Update Patient
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Schedule Appointment Dialog */}
      <Dialog open={isScheduleAppointmentOpen} onOpenChange={setIsScheduleAppointmentOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Schedule Appointment for {selectedPatient?.name}</DialogTitle>
          </DialogHeader>
          {selectedPatient && (
            <div className="space-y-4">
              <div>
                <Label htmlFor="appointmentDoctor">Doctor</Label>
                <Select onValueChange={(value) => setNewAppointment(prev => ({ ...prev, doctorName: value }))} value={newAppointment.doctorName}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a doctor" />
                  </SelectTrigger>
                  <SelectContent>
                    {staff.filter(s => s.jobType === 'Doctor').map(doctor => (
                      <SelectItem key={doctor.staffID} value={doctor.name}>{doctor.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="appointmentDate">Date</Label>
                <Input
                  id="appointmentDate"
                  type="date"
                  value={newAppointment.date}
                  onChange={(e) => setNewAppointment(prev => ({ ...prev, date: e.target.value }))}
                />
              </div>
              <div>
                <Label htmlFor="appointmentTime">Time</Label>
                <Input
                  id="appointmentTime"
                  type="time"
                  value={newAppointment.time}
                  onChange={(e) => setNewAppointment(prev => ({ ...prev, time: e.target.value }))}
                />
              </div>
              <div>
                <Label htmlFor="appointmentDuration">Duration (minutes)</Label>
                <Input
                  id="appointmentDuration"
                  type="number"
                  value={newAppointment.duration}
                  onChange={(e) => setNewAppointment(prev => ({ ...prev, duration: parseInt(e.target.value, 10) }))}
                />
              </div>
              <div>
                <Label htmlFor="appointmentVisitType">Visit Type</Label>
                <Select onValueChange={(value) => setNewAppointment(prev => ({ ...prev, visitType: value as 'Check-up' | 'Procedure' | 'Emergency' }))} value={newAppointment.visitType}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select visit type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Check-up">Check-up</SelectItem>
                    <SelectItem value="Procedure">Procedure</SelectItem>
                    <SelectItem value="Emergency">Emergency</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="appointmentStatus">Status</Label>
                <Select onValueChange={(value) => setNewAppointment(prev => ({ ...prev, status: value as 'Done' | 'Not Done' | 'Canceled' }))} value={newAppointment.status}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Done">Done</SelectItem>
                    <SelectItem value="Not Done">Not Done</SelectItem>
                    <SelectItem value="Canceled">Canceled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="appointmentNotes">Notes</Label>
                <Textarea
                  id="appointmentNotes"
                  value={newAppointment.notes}
                  onChange={(e) => setNewAppointment(prev => ({ ...prev, notes: e.target.value }))}
                  placeholder="Enter appointment notes"
                />
              </div>
              <Button onClick={handleCreateAppointment} className="w-full">
                <Clock className="h-4 w-4 mr-2" />
                Schedule Appointment
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Remove Patient Dialog */}
      <AlertDialog open={isRemovePatientOpen} onOpenChange={setIsRemovePatientOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently remove {selectedPatient?.name} from your patient database.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setIsRemovePatientOpen(false)}>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeletePatient}>Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Patients;