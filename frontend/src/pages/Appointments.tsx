import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { Plus, Search, Calendar, Clock, User, UserCheck, MoreHorizontal, Eye, Edit, RotateCcw, CheckCircle, XCircle } from "lucide-react";
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
import { Appointment } from "@/lib/types";

const Appointments = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState("");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
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

  // Action button states
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [isViewDetailsOpen, setIsViewDetailsOpen] = useState(false);
  const [isEditAppointmentOpen, setIsEditAppointmentOpen] = useState(false);
  const [isRescheduleOpen, setIsRescheduleOpen] = useState(false);
  const [isCancelAppointmentOpen, setIsCancelAppointmentOpen] = useState(false);
  const [editingAppointment, setEditingAppointment] = useState<Appointment | null>(null);
  const [rescheduleData, setRescheduleData] = useState({
    date: new Date().toISOString().split('T')[0],
    time: "09:00"
  });

  // Fetch appointments from API
  const { data: appointments = [], isLoading, error } = useQuery({
    queryKey: ['appointments'],
    queryFn: api.appointments.list,
  });

  // Fetch patients and staff for dropdowns
  const { data: patients = [] } = useQuery({
    queryKey: ['patients'],
    queryFn: api.patients.list,
  });

  const { data: staff = [] } = useQuery({
    queryKey: ['staff'],
    queryFn: api.staff.list,
  });

  // Create appointment mutation
  const createAppointmentMutation = useMutation({
    mutationFn: api.appointments.createWithNames,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      toast({
        title: "Appointment Scheduled",
        description: `Appointment has been scheduled successfully.`,
      });
      setIsDialogOpen(false);
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

  // Update appointment mutation
  const updateAppointmentMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: Appointment }) => api.appointments.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      toast({
        title: "Appointment Updated",
        description: "Appointment has been updated successfully.",
      });
      setIsEditAppointmentOpen(false);
      setEditingAppointment(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to update appointment",
        variant: "destructive",
      });
    }
  });

  // Delete appointment mutation
  const deleteAppointmentMutation = useMutation({
    mutationFn: (id: number) => api.appointments.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      toast({
        title: "Appointment Cancelled",
        description: "Appointment has been cancelled successfully.",
      });
      setIsCancelAppointmentOpen(false);
      setSelectedAppointment(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to cancel appointment",
        variant: "destructive",
      });
    }
  });

  const handleScheduleAppointment = () => {
    createAppointmentMutation.mutate(newAppointment);
  };

  const handleViewDetails = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setIsViewDetailsOpen(true);
  };

  const handleEditAppointment = (appointment: Appointment) => {
    setEditingAppointment(appointment);
    setIsEditAppointmentOpen(true);
  };

  const handleReschedule = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setRescheduleData({
      date: appointment.date,
      time: appointment.time
    });
    setIsRescheduleOpen(true);
  };

  const handleMarkAsCompleted = (appointment: Appointment) => {
    const updatedAppointment = { ...appointment, status: 'Done' as const };
    updateAppointmentMutation.mutate({ id: appointment.appointmentID!, data: updatedAppointment });
    
    // Create billing record for the completed appointment
    const billingAmount = getBillingAmount(appointment.visitType);
    const billingData = {
      appointmentID: appointment.appointmentID!,
      amount: billingAmount,
      paid: false
    };
    
    // Create billing record
    api.billing.create(billingData).then(() => {
      toast({
        title: "Billing Created",
        description: `Billing record created for $${billingAmount.toFixed(2)}`,
      });
    }).catch((error: any) => {
      toast({
        title: "Billing Error",
        description: error.message || "Failed to create billing record",
        variant: "destructive",
      });
    });
  };

  const getBillingAmount = (visitType: string) => {
    switch (visitType) {
      case "Check-up":
        return 75.00;
      case "Procedure":
        return 150.00;
      case "Emergency":
        return 200.00;
      default:
        return 100.00;
    }
  };

  const handleCancelAppointment = (appointment: Appointment) => {
    setSelectedAppointment(appointment);
    setIsCancelAppointmentOpen(true);
  };

  const handleUpdateAppointment = () => {
    if (editingAppointment) {
      updateAppointmentMutation.mutate({ id: editingAppointment.appointmentID!, data: editingAppointment });
    }
  };

  const handleRescheduleAppointment = () => {
    if (selectedAppointment) {
      const updatedAppointment = { 
        ...selectedAppointment, 
        date: rescheduleData.date,
        time: rescheduleData.time
      };
      updateAppointmentMutation.mutate({ id: selectedAppointment.appointmentID!, data: updatedAppointment });
      setIsRescheduleOpen(false);
    }
  };

  const handleDeleteAppointment = () => {
    if (selectedAppointment) {
      deleteAppointmentMutation.mutate(selectedAppointment.appointmentID!);
    }
  };

  const getPatientName = (patientID: number) => {
    const patient = patients.find(p => p.patientID === patientID);
    return patient?.name || `Patient ID: ${patientID}`;
  };

  const getDoctorName = (doctorID: number) => {
    const doctor = staff.find(s => s.staffID === doctorID);
    return doctor?.name || `Doctor ID: ${doctorID}`;
  };

  const filteredAppointments = appointments.filter(appointment => {
    const patientName = getPatientName(appointment.patientID);
    const doctorName = getDoctorName(appointment.doctorID);
    return (
      patientName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      doctorName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      appointment.visitType?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      appointment.notes?.toLowerCase().includes(searchTerm.toLowerCase())
    );
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Done": return "bg-green-100 text-green-800";
      case "Not Done": return "bg-yellow-100 text-yellow-800";
      case "Canceled": return "bg-red-100 text-red-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case "Check-up": return "bg-green-100 text-green-800";
      case "Procedure": return "bg-purple-100 text-purple-800";
      case "Emergency": return "bg-red-100 text-red-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-muted-foreground">Loading appointments...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-destructive">Error loading appointments: {error.message}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Appointments</h1>
          <p className="text-muted-foreground">Manage patient appointments and schedules</p>
        </div>
        <Dialog>
          <DialogTrigger asChild>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Schedule Appointment
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Schedule New Appointment</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="patientName">Patient</Label>
                <Select value={newAppointment.patientName} onValueChange={(value) => setNewAppointment({ ...newAppointment, patientName: value })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select patient" />
                  </SelectTrigger>
                  <SelectContent>
                    {patients.map(patient => (
                      <SelectItem key={patient.patientID} value={patient.name}>
                        {patient.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="doctorName">Doctor</Label>
                <Select value={newAppointment.doctorName} onValueChange={(value) => setNewAppointment({ ...newAppointment, doctorName: value })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select doctor" />
                  </SelectTrigger>
                  <SelectContent>
                    {staff.filter(s => s.jobType === 'Doctor').map(doctor => (
                      <SelectItem key={doctor.staffID} value={doctor.name}>
                        {doctor.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="appointmentDate">Date</Label>
                  <Input
                    id="appointmentDate"
                    type="date"
                    value={newAppointment.date}
                    onChange={(e) => setNewAppointment({ ...newAppointment, date: e.target.value })}
                  />
                </div>
                <div>
                  <Label htmlFor="appointmentTime">Time</Label>
                  <Input
                    id="appointmentTime"
                    type="time"
                    value={newAppointment.time}
                    onChange={(e) => setNewAppointment({ ...newAppointment, time: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <Label htmlFor="visitType">Visit Type</Label>
                <Select value={newAppointment.visitType} onValueChange={(value) => setNewAppointment({ ...newAppointment, visitType: value as 'Check-up' | 'Procedure' | 'Emergency' })}>
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
                <Label htmlFor="duration">Duration (minutes)</Label>
                <Input
                  id="duration"
                  type="number"
                  value={newAppointment.duration}
                  onChange={(e) => setNewAppointment({ ...newAppointment, duration: parseInt(e.target.value) })}
                  placeholder="Duration in minutes"
                />
              </div>
              <div>
                <Label htmlFor="notes">Notes</Label>
                <Input
                  id="notes"
                  value={newAppointment.notes}
                  onChange={(e) => setNewAppointment({ ...newAppointment, notes: e.target.value })}
                  placeholder="Additional notes"
                />
              </div>
              <Button onClick={handleScheduleAppointment} className="w-full">
                <Plus className="h-4 w-4 mr-2" />
                Schedule Appointment
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
              placeholder="Search by patient name, doctor name, visit type, or notes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      {/* Appointments List */}
      <div className="space-y-4">
        {filteredAppointments.map((appointment) => (
          <Card key={appointment.appointmentID} className="bg-gradient-card border-0 shadow-sm hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 flex-1">
                  {/* Patient & Doctor Info */}
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <User className="h-4 w-4 text-muted-foreground" />
                      <span className="font-medium text-foreground">{getPatientName(appointment.patientID)}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <UserCheck className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm text-muted-foreground">{getDoctorName(appointment.doctorID)}</span>
                    </div>
                  </div>

                  {/* Date & Time */}
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm text-foreground">{appointment.date}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Clock className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm text-foreground">{appointment.time}</span>
                      <span className="text-xs text-muted-foreground">({appointment.duration} min)</span>
                    </div>
                  </div>

                  {/* Status & Type */}
                  <div className="space-y-2">
                    <Badge className={getStatusColor(appointment.status || 'Not Done')}>
                      {appointment.status || 'Not Done'}
                    </Badge>
                    <div>
                      <Badge className={getTypeColor(appointment.visitType)}>
                        {appointment.visitType}
                      </Badge>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm" onClick={() => handleViewDetails(appointment)}>
                    <Eye className="h-4 w-4 mr-2" />
                    View Details
                  </Button>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent className="bg-popover border shadow-md">
                      <DropdownMenuItem onClick={() => handleEditAppointment(appointment)}>
                        <Edit className="h-4 w-4 mr-2" />
                        Edit Appointment
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => handleReschedule(appointment)}>
                        <RotateCcw className="h-4 w-4 mr-2" />
                        Reschedule
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => handleMarkAsCompleted(appointment)}>
                        <CheckCircle className="h-4 w-4 mr-2" />
                        Mark as Completed
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => handleCancelAppointment(appointment)} className="text-destructive">
                        <XCircle className="h-4 w-4 mr-2" />
                        Cancel Appointment
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredAppointments.length === 0 && (
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-12 text-center">
            <Calendar className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium text-foreground mb-2">No appointments found</h3>
            <p className="text-muted-foreground mb-4">Try adjusting your search criteria or schedule a new appointment.</p>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Schedule First Appointment
            </Button>
          </CardContent>
        </Card>
      )}

             {/* View Details Dialog */}
       <Dialog open={isViewDetailsOpen} onOpenChange={setIsViewDetailsOpen}>
         <DialogContent className="sm:max-w-md">
           <DialogHeader>
             <DialogTitle>Appointment Details</DialogTitle>
           </DialogHeader>
           {selectedAppointment && (
             <div className="space-y-4">
               <div>
                 <Label>Patient</Label>
                 <p className="text-foreground">{getPatientName(selectedAppointment.patientID)}</p>
               </div>
               <div>
                 <Label>Doctor</Label>
                 <p className="text-foreground">{getDoctorName(selectedAppointment.doctorID)}</p>
               </div>
               <div>
                 <Label>Appointment ID</Label>
                 <p className="text-foreground">{selectedAppointment.appointmentID}</p>
               </div>
               <div>
                 <Label>Date</Label>
                 <p className="text-foreground">{selectedAppointment.date}</p>
               </div>
               <div>
                 <Label>Time</Label>
                 <p className="text-foreground">{selectedAppointment.time}</p>
               </div>
               <div>
                 <Label>Duration</Label>
                 <p className="text-foreground">{selectedAppointment.duration} minutes</p>
               </div>
               <div>
                 <Label>Visit Type</Label>
                 <Badge className={getTypeColor(selectedAppointment.visitType)}>
                   {selectedAppointment.visitType}
                 </Badge>
               </div>
               <div>
                 <Label>Status</Label>
                 <Badge className={getStatusColor(selectedAppointment.status || 'Not Done')}>
                   {selectedAppointment.status || 'Not Done'}
                 </Badge>
               </div>
               <div>
                 <Label>Notes</Label>
                 <p className="text-foreground">{selectedAppointment.notes || 'No notes available'}</p>
               </div>
             </div>
           )}
         </DialogContent>
       </Dialog>

             {/* Edit Appointment Dialog */}
       <Dialog open={isEditAppointmentOpen} onOpenChange={setIsEditAppointmentOpen}>
         <DialogContent className="sm:max-w-md">
           <DialogHeader>
             <DialogTitle>Edit Appointment</DialogTitle>
           </DialogHeader>
           {editingAppointment && (
             <div className="space-y-4">
               <div>
                 <Label htmlFor="editPatientID">Patient</Label>
                 <Select value={editingAppointment.patientID.toString()} onValueChange={(value) => setEditingAppointment({ ...editingAppointment, patientID: parseInt(value) })}>
                   <SelectTrigger>
                     <SelectValue placeholder="Select patient" />
                   </SelectTrigger>
                   <SelectContent>
                     {patients.map(patient => (
                       <SelectItem key={patient.patientID} value={patient.patientID?.toString() || ''}>
                         {patient.name}
                       </SelectItem>
                     ))}
                   </SelectContent>
                 </Select>
               </div>
               <div>
                 <Label htmlFor="editDoctorID">Doctor</Label>
                 <Select value={editingAppointment.doctorID.toString()} onValueChange={(value) => setEditingAppointment({ ...editingAppointment, doctorID: parseInt(value) })}>
                   <SelectTrigger>
                     <SelectValue placeholder="Select doctor" />
                   </SelectTrigger>
                   <SelectContent>
                     {staff.filter(s => s.jobType === 'Doctor').map(doctor => (
                       <SelectItem key={doctor.staffID} value={doctor.staffID?.toString() || ''}>
                         {doctor.name}
                       </SelectItem>
                     ))}
                   </SelectContent>
                 </Select>
               </div>
               <div className="grid grid-cols-2 gap-4">
                 <div>
                   <Label htmlFor="editAppointmentDate">Date</Label>
                   <Input
                     id="editAppointmentDate"
                     type="date"
                     value={editingAppointment.date}
                     onChange={(e) => setEditingAppointment({ ...editingAppointment, date: e.target.value })}
                   />
                 </div>
                 <div>
                   <Label htmlFor="editAppointmentTime">Time</Label>
                   <Input
                     id="editAppointmentTime"
                     type="time"
                     value={editingAppointment.time}
                     onChange={(e) => setEditingAppointment({ ...editingAppointment, time: e.target.value })}
                   />
                 </div>
               </div>
               <div>
                 <Label htmlFor="editVisitType">Visit Type</Label>
                 <Select value={editingAppointment.visitType} onValueChange={(value) => setEditingAppointment({ ...editingAppointment, visitType: value as 'Check-up' | 'Procedure' | 'Emergency' })}>
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
                 <Label htmlFor="editDuration">Duration (minutes)</Label>
                 <Input
                   id="editDuration"
                   type="number"
                   value={editingAppointment.duration}
                   onChange={(e) => setEditingAppointment({ ...editingAppointment, duration: parseInt(e.target.value) })}
                   placeholder="Duration in minutes"
                 />
               </div>
               <div>
                 <Label htmlFor="editStatus">Status</Label>
                 <Select value={editingAppointment.status || 'Not Done'} onValueChange={(value) => setEditingAppointment({ ...editingAppointment, status: value as 'Done' | 'Not Done' | 'Canceled' })}>
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
                 <Label htmlFor="editNotes">Notes</Label>
                 <Textarea
                   id="editNotes"
                   value={editingAppointment.notes}
                   onChange={(e) => setEditingAppointment({ ...editingAppointment, notes: e.target.value })}
                   placeholder="Additional notes"
                 />
               </div>
               <Button onClick={handleUpdateAppointment} className="w-full">
                 <Edit className="h-4 w-4 mr-2" />
                 Update Appointment
               </Button>
             </div>
           )}
         </DialogContent>
       </Dialog>

             {/* Reschedule Dialog */}
       <Dialog open={isRescheduleOpen} onOpenChange={setIsRescheduleOpen}>
         <DialogContent className="sm:max-w-md">
           <DialogHeader>
             <DialogTitle>Reschedule Appointment</DialogTitle>
           </DialogHeader>
           {selectedAppointment && (
             <div className="space-y-4">
               <div>
                 <Label>Current Appointment</Label>
                 <p className="text-sm text-muted-foreground">
                   {selectedAppointment.date} at {selectedAppointment.time} with {getPatientName(selectedAppointment.patientID)}
                 </p>
               </div>
               <div>
                 <Label htmlFor="rescheduleDate">New Date</Label>
                 <Input
                   id="rescheduleDate"
                   type="date"
                   value={rescheduleData.date}
                   onChange={(e) => setRescheduleData({ ...rescheduleData, date: e.target.value })}
                 />
               </div>
               <div>
                 <Label htmlFor="rescheduleTime">New Time</Label>
                 <Input
                   id="rescheduleTime"
                   type="time"
                   value={rescheduleData.time}
                   onChange={(e) => setRescheduleData({ ...rescheduleData, time: e.target.value })}
                 />
               </div>
               <Button onClick={handleRescheduleAppointment} className="w-full">
                 <RotateCcw className="h-4 w-4 mr-2" />
                 Reschedule Appointment
               </Button>
             </div>
           )}
         </DialogContent>
       </Dialog>

             {/* Cancel Appointment Dialog */}
       <AlertDialog open={isCancelAppointmentOpen} onOpenChange={setIsCancelAppointmentOpen}>
         <AlertDialogContent>
           <AlertDialogHeader>
             <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
             <AlertDialogDescription>
               This action cannot be undone. This will permanently cancel the appointment for {selectedAppointment && getPatientName(selectedAppointment.patientID)} on {selectedAppointment?.date} at {selectedAppointment?.time}.
             </AlertDialogDescription>
           </AlertDialogHeader>
           <AlertDialogFooter>
             <AlertDialogCancel onClick={() => setIsCancelAppointmentOpen(false)}>Keep Appointment</AlertDialogCancel>
             <AlertDialogAction onClick={handleDeleteAppointment} className="bg-destructive text-destructive-foreground hover:bg-destructive-hover">
               Cancel Appointment
             </AlertDialogAction>
           </AlertDialogFooter>
         </AlertDialogContent>
       </AlertDialog>
    </div>
  );
};

export default Appointments;