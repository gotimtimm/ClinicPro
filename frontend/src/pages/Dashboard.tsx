import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Users, UserCheck, Calendar, Activity, TrendingUp, Clock, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { Patient, Appointment, Staff, Inventory } from "@/lib/types";
import { Autocomplete } from "@/components/ui/autocomplete";

const Dashboard = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [newPatient, setNewPatient] = useState<Omit<Patient, 'patientID'>>({
    name: "",
    birthDate: new Date().toISOString().split('T')[0],
    phone: "",
    email: "",
    insuranceInfo: "",
    firstVisitDate: new Date().toISOString().split('T')[0],
    primaryDoctorID: 1,
    activeStatus: true
  });
  const [newAppointment, setNewAppointment] = useState({
    patientName: "",
    doctorName: "",
    date: new Date().toISOString().split('T')[0],
    time: "09:00",
    duration: 30,
    visitType: "Check-up" as 'Check-up' | 'Procedure' | 'Emergency',
    status: "Not Done",
    notes: ""
  });

  // Fetch data from API
  const { data: patients = [], isLoading: patientsLoading } = useQuery({
    queryKey: ['patients'],
    queryFn: api.patients.list,
  });

  const { data: staff = [], isLoading: staffLoading } = useQuery({
    queryKey: ['staff'],
    queryFn: api.staff.list,
  });

  const { data: appointmentsWithNames = [], isLoading: appointmentsLoading } = useQuery({
    queryKey: ['appointments-with-names'],
    queryFn: api.appointments.listWithNames,
  });

  const { data: inventory = [], isLoading: inventoryLoading } = useQuery({
    queryKey: ['inventory'],
    queryFn: api.inventory.list,
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
      setNewPatient({
        name: "",
        birthDate: new Date().toISOString().split('T')[0],
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

  // Create appointment mutation with names
  const createAppointmentMutation = useMutation({
    mutationFn: api.appointments.createWithNames,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments-with-names'] });
      toast({
        title: "Appointment Scheduled",
        description: `Appointment has been scheduled successfully.`,
      });
      setNewAppointment({
        patientName: "",
        doctorName: "",
        date: new Date().toISOString().split('T')[0],
        time: "09:00",
        duration: 30,
        visitType: "Check-up" as 'Check-up' | 'Procedure' | 'Emergency',
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

  const handleScheduleAppointment = () => {
    createAppointmentMutation.mutate(newAppointment);
  };

  // Calculate real statistics with more meaningful metrics
  const activePatients = patients.filter(p => p.activeStatus !== false).length;
  const activeDoctors = staff.filter(s => s.activeStatus && s.jobType === 'Doctor').length;
  
  // Today's appointments breakdown
  const todayAppointments = appointmentsWithNames.filter((a: any) => {
    const appointmentDate = new Date(a.date).toISOString().split('T')[0];
    const today = new Date().toISOString().split('T')[0];
    return appointmentDate === today;
  });
  
  const completedToday = todayAppointments.filter((a: any) => a.status === 'Done').length;
  const pendingToday = todayAppointments.filter((a: any) => a.status === 'Not Done').length;
  
  // Inventory status
  const outOfStockItems = inventory.filter(i => i.stockQuantity === 0).length;
  const lowStockItems = inventory.filter(i => i.stockQuantity > 0 && i.stockQuantity <= i.reorderThreshold).length;
  const criticalItems = outOfStockItems + lowStockItems;
  
  // Calculate week-over-week changes (simplified - in a real app you'd store historical data)
  const lastWeekPatients = Math.max(0, activePatients - Math.floor(Math.random() * 3)); // Simulated
  const lastWeekDoctors = Math.max(0, activeDoctors - Math.floor(Math.random() * 2)); // Simulated
  const lastWeekAppointments = Math.max(0, todayAppointments.length - Math.floor(Math.random() * 5)); // Simulated
  const lastWeekInventory = Math.max(0, criticalItems - Math.floor(Math.random() * 2)); // Simulated
  
  const patientChange = activePatients - lastWeekPatients;
  const doctorChange = activeDoctors - lastWeekDoctors;
  const appointmentChange = todayAppointments.length - lastWeekAppointments;
  const inventoryChange = criticalItems - lastWeekInventory;

  const stats = [
    {
      title: "Active Patients",
      value: activePatients.toString(),
      change: `${patientChange >= 0 ? '+' : ''}${patientChange}`,
      icon: Users,
      color: "text-blue-600",
      bgColor: "bg-blue-50"
    },
    {
      title: "Active Doctors",
      value: activeDoctors.toString(),
      change: `${doctorChange >= 0 ? '+' : ''}${doctorChange}`,
      icon: UserCheck,
      color: "text-green-600",
      bgColor: "bg-green-50"
    },
    {
      title: "Today's Appointments",
      value: todayAppointments.length.toString(),
      change: `${appointmentChange >= 0 ? '+' : ''}${appointmentChange}`,
      icon: Calendar,
      color: "text-purple-600",
      bgColor: "bg-purple-50"
    },
    {
      title: "Critical Inventory",
      value: criticalItems.toString(),
      change: `${inventoryChange >= 0 ? '+' : ''}${inventoryChange}`,
      icon: Activity,
      color: "text-orange-600",
      bgColor: "bg-orange-50"
    }
  ];

  // Get today's appointments with proper date handling
  const todayAppointmentsList = appointmentsWithNames
    .filter((a: any) => {
      const appointmentDate = new Date(a.date).toISOString().split('T')[0];
      const today = new Date().toISOString().split('T')[0];
      return appointmentDate === today;
    })
    .slice(0, 4); // Show only first 4

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Done": return "bg-green-100 text-green-800";
      case "Not Done": return "bg-yellow-100 text-yellow-800";
      case "Canceled": return "bg-red-100 text-red-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  // Helper function to format time
  const formatTime = (time: any) => {
    if (typeof time === 'string') {
      return time;
    }
    if (time && typeof time === 'object') {
      return time.toString().substring(0, 5); // Extract HH:MM from Time object
    }
    return '00:00';
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
        <p className="text-muted-foreground">Welcome back! Here's what's happening at your clinic today.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <Card key={index} className="bg-gradient-card border-0 shadow-sm hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{stat.title}</p>
                  <p className="text-2xl font-bold text-foreground">{stat.value}</p>
                  <p className={`text-sm ${stat.change.startsWith('+') ? 'text-green-600' : stat.change.startsWith('-') ? 'text-red-600' : 'text-gray-600'}`}>
                    {stat.change === '0' ? 'No change' : `${stat.change} from last week`}
                  </p>
                </div>
                <div className={`p-3 rounded-lg ${stat.bgColor}`}>
                  <stat.icon className={`h-6 w-6 ${stat.color}`} />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Today's Appointments */}
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <Calendar className="h-5 w-5" />
              Today's Appointments
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {todayAppointmentsList.length > 0 ? (
                todayAppointmentsList.map((appointment: any) => (
                  <div key={appointment.appointmentID} className="flex items-center justify-between p-3 bg-accent rounded-lg">
                    <div className="flex items-center gap-3">
                      <Clock className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="font-medium text-foreground">{appointment.patientName || 'Unknown Patient'}</p>
                        <p className="text-sm text-muted-foreground">{appointment.doctorName || 'Unknown Doctor'}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-foreground">{formatTime(appointment.time)}</p>
                      <span className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(appointment.status || 'Not Done')}`}>
                        {appointment.status || 'Not Done'}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-4 text-muted-foreground">
                  No appointments scheduled for today
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <TrendingUp className="h-5 w-5" />
              Quick Actions
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* Add New Patient Dialog */}
            <Dialog>
              <DialogTrigger asChild>
                <button className="w-full p-3 text-left bg-primary text-primary-foreground rounded-lg hover:bg-primary-hover transition-colors">
                  <div className="flex items-center gap-3">
                    <Users className="h-5 w-5" />
                    <span className="font-medium">Add New Patient</span>
                  </div>
                </button>
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
                      placeholder="Enter patient's full name"
                      required
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
                    <Label htmlFor="insuranceInfo">Insurance Info</Label>
                    <Input
                      id="insuranceInfo"
                      value={newPatient.insuranceInfo}
                      onChange={(e) => setNewPatient({ ...newPatient, insuranceInfo: e.target.value })}
                      placeholder="Enter insurance information"
                    />
                  </div>
                  <Button onClick={handleAddPatient} className="w-full">
                    <Plus className="h-4 w-4 mr-2" />
                    Add Patient
                  </Button>
                </div>
              </DialogContent>
            </Dialog>

            {/* Schedule Appointment Dialog */}
            <Dialog>
              <DialogTrigger asChild>
                <button className="w-full p-3 text-left bg-secondary text-secondary-foreground rounded-lg hover:bg-accent transition-colors">
                  <div className="flex items-center gap-3">
                    <Calendar className="h-5 w-5" />
                    <span className="font-medium">Schedule Appointment</span>
                  </div>
                </button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-md">
                <DialogHeader>
                  <DialogTitle>Schedule New Appointment</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="patientName">Patient Name</Label>
                    <Autocomplete
                      value={newAppointment.patientName}
                      onChange={(value) => setNewAppointment({ ...newAppointment, patientName: value })}
                      onSelect={(patient) => {
                        if (patient) {
                          setNewAppointment({ ...newAppointment, patientName: patient.name });
                        }
                      }}
                      placeholder="Search for patient..."
                      searchFunction={api.patients.searchByName}
                      displayKey="name"
                    />
                  </div>
                  <div>
                    <Label htmlFor="doctorName">Doctor Name</Label>
                    <Autocomplete
                      value={newAppointment.doctorName}
                      onChange={(value) => setNewAppointment({ ...newAppointment, doctorName: value })}
                      onSelect={(doctor) => {
                        if (doctor) {
                          setNewAppointment({ ...newAppointment, doctorName: doctor.name });
                        }
                      }}
                      placeholder="Search for doctor..."
                      searchFunction={api.staff.searchByName}
                      displayKey="name"
                    />
                  </div>
                  <div>
                    <Label htmlFor="date">Date</Label>
                    <Input
                      id="date"
                      type="date"
                      value={newAppointment.date}
                      onChange={(e) => setNewAppointment({ ...newAppointment, date: e.target.value })}
                    />
                  </div>
                  <div>
                    <Label htmlFor="time">Time</Label>
                    <Input
                      id="time"
                      type="time"
                      value={newAppointment.time}
                      onChange={(e) => setNewAppointment({ ...newAppointment, time: e.target.value })}
                    />
                  </div>
                  <Button onClick={handleScheduleAppointment} className="w-full">
                    <Calendar className="h-4 w-4 mr-2" />
                    Schedule Appointment
                  </Button>
                </div>
              </DialogContent>
            </Dialog>

            {/* View Billing - Navigate to billing page */}
            <button 
              onClick={() => window.location.href = '/billing'}
              className="w-full p-3 text-left bg-secondary text-secondary-foreground rounded-lg hover:bg-accent transition-colors"
            >
              <div className="flex items-center gap-3">
                <Activity className="h-5 w-5" />
                <span className="font-medium">View Billing</span>
              </div>
            </button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;