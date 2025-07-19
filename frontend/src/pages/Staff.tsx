import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Plus, Search, UserCheck, Phone, Mail, MapPin, MoreHorizontal, Eye, Edit, Calendar, Trash2, Clock } from "lucide-react";
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
import { Staff as StaffType } from "@/lib/types";

const Staff = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState("");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newStaff, setNewStaff] = useState<Omit<StaffType, 'staffID'>>({
    name: "",
    jobType: "Doctor",
    specialization: "",
    licenseNumber: "",
    phone: "",
    email: "",
    hireDate: new Date().toISOString().split('T')[0],
    workingDays: "Mon-Fri",
    activeStatus: true
  });

  // Action button states
  const [selectedStaff, setSelectedStaff] = useState<StaffType | null>(null);
  const [isViewProfileOpen, setIsViewProfileOpen] = useState(false);
  const [isEditDetailsOpen, setIsEditDetailsOpen] = useState(false);
  const [isViewScheduleOpen, setIsViewScheduleOpen] = useState(false);
  const [isRemoveStaffOpen, setIsRemoveStaffOpen] = useState(false);
  const [editingStaff, setEditingStaff] = useState<StaffType | null>(null);

  // Fetch staff from API
  const { data: staff = [], isLoading, error } = useQuery({
    queryKey: ['staff'],
    queryFn: api.staff.list,
  });

  // Create staff mutation
  const createStaffMutation = useMutation({
    mutationFn: api.staff.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staff'] });
      toast({
        title: "Staff Added",
        description: `${newStaff.name} has been added to the team.`,
      });
      setIsDialogOpen(false);
      setNewStaff({
        name: "",
        jobType: "Doctor",
        specialization: "",
        licenseNumber: "",
        phone: "",
        email: "",
        hireDate: new Date().toISOString().split('T')[0],
        workingDays: "Mon-Fri",
        activeStatus: true
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to add staff member",
        variant: "destructive",
      });
    }
  });

  // Update staff mutation
  const updateStaffMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: StaffType }) => api.staff.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staff'] });
      toast({
        title: "Staff Updated",
        description: "Staff information has been updated successfully.",
      });
      setIsEditDetailsOpen(false);
      setEditingStaff(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to update staff member",
        variant: "destructive",
      });
    }
  });

  // Delete staff mutation
  const deleteStaffMutation = useMutation({
    mutationFn: (id: number) => api.staff.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staff'] });
      toast({
        title: "Staff Removed",
        description: "Staff member has been removed successfully.",
      });
      setIsRemoveStaffOpen(false);
      setSelectedStaff(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to remove staff member",
        variant: "destructive",
      });
    }
  });

  const handleAddStaff = () => {
    createStaffMutation.mutate(newStaff);
  };

  const handleViewProfile = (member: StaffType) => {
    setSelectedStaff(member);
    setIsViewProfileOpen(true);
  };

  const handleEditDetails = (member: StaffType) => {
    setEditingStaff(member);
    setIsEditDetailsOpen(true);
  };

  const handleViewSchedule = (member: StaffType) => {
    setSelectedStaff(member);
    setIsViewScheduleOpen(true);
  };

  const handleRemoveStaff = (member: StaffType) => {
    setSelectedStaff(member);
    setIsRemoveStaffOpen(true);
  };

  const handleUpdateStaff = () => {
    if (editingStaff) {
      updateStaffMutation.mutate({ id: editingStaff.staffID!, data: editingStaff });
    }
  };

  const handleDeleteStaff = () => {
    if (selectedStaff) {
      deleteStaffMutation.mutate(selectedStaff.staffID!);
    }
  };

  const filteredStaff = staff.filter(member =>
    member.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    member.jobType?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    member.specialization?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Available": return "bg-green-100 text-green-800";
      case "Busy": return "bg-red-100 text-red-800";
      case "Off Duty": return "bg-gray-100 text-gray-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  const getShiftColor = (shift: string) => {
    switch (shift) {
      case "Morning": return "bg-blue-100 text-blue-800";
      case "Full Day": return "bg-purple-100 text-purple-800";
      case "Night": return "bg-orange-100 text-orange-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  const calculateYearsOfService = (hireDate: string) => {
    if (!hireDate) return 'N/A';
    const today = new Date();
    const hire = new Date(hireDate);
    let years = today.getFullYear() - hire.getFullYear();
    const monthDiff = today.getMonth() - hire.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < hire.getDate())) {
      years--;
    }
    return `${years} years`;
  };

  if (isLoading) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-muted-foreground">Loading staff...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-destructive">Error loading staff: {error.message}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Staff Management</h1>
          <p className="text-muted-foreground">Manage your healthcare team</p>
        </div>
        <Dialog>
          <DialogTrigger asChild>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Add New Staff
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Add New Staff Member</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="staffName">Full Name *</Label>
                <Input
                  id="staffName"
                  value={newStaff.name}
                  onChange={(e) => setNewStaff({ ...newStaff, name: e.target.value })}
                  placeholder="Enter full name"
                  required
                />
              </div>
              <div>
                <Label htmlFor="jobType">Job Type</Label>
                <Select value={newStaff.jobType} onValueChange={(value) => setNewStaff({ ...newStaff, jobType: value as 'Doctor' | 'Nurse' | 'Admin' })}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select job type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Doctor">Doctor</SelectItem>
                    <SelectItem value="Nurse">Nurse</SelectItem>
                    <SelectItem value="Admin">Admin</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="specialization">Specialization</Label>
                <Input
                  id="specialization"
                  value={newStaff.specialization}
                  onChange={(e) => setNewStaff({ ...newStaff, specialization: e.target.value })}
                  placeholder="e.g., Cardiology, Pediatrics"
                />
              </div>
              <div>
                <Label htmlFor="licenseNumber">License Number</Label>
                <Input
                  id="licenseNumber"
                  value={newStaff.licenseNumber}
                  onChange={(e) => setNewStaff({ ...newStaff, licenseNumber: e.target.value })}
                  placeholder="License number (if applicable)"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="staffPhone">Phone</Label>
                  <Input
                    id="staffPhone"
                    value={newStaff.phone}
                    onChange={(e) => setNewStaff({ ...newStaff, phone: e.target.value })}
                    placeholder="Phone number"
                  />
                </div>
                <div>
                  <Label htmlFor="workingDays">Working Days</Label>
                  <Input
                    id="workingDays"
                    value={newStaff.workingDays}
                    onChange={(e) => setNewStaff({ ...newStaff, workingDays: e.target.value })}
                    placeholder="e.g., Mon-Fri"
                  />
                </div>
              </div>
              <div>
                <Label htmlFor="staffEmail">Email</Label>
                <Input
                  id="staffEmail"
                  type="email"
                  value={newStaff.email}
                  onChange={(e) => setNewStaff({ ...newStaff, email: e.target.value })}
                  placeholder="Email address"
                />
              </div>
              <div>
                <Label htmlFor="hireDate">Hire Date</Label>
                <Input
                  id="hireDate"
                  type="date"
                  value={newStaff.hireDate}
                  onChange={(e) => setNewStaff({ ...newStaff, hireDate: e.target.value })}
                />
              </div>
              <Button onClick={handleAddStaff} className="w-full">
                <Plus className="h-4 w-4 mr-2" />
                Add Staff Member
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
              placeholder="Search staff by name, role, or department..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      {/* Staff Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredStaff.map((member) => (
          <Card key={member.staffID} className="bg-gradient-card border-0 shadow-sm hover:shadow-md transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-gradient-primary rounded-full">
                    <UserCheck className="h-5 w-5 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-lg text-foreground">{member.name}</CardTitle>
                    <p className="text-sm text-muted-foreground">{member.jobType}</p>
                  </div>
                </div>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon">
                      <MoreHorizontal className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent className="bg-popover border shadow-md">
                    <DropdownMenuItem onClick={() => handleViewProfile(member)}>
                      <Eye className="h-4 w-4 mr-2" />
                      View Profile
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleEditDetails(member)}>
                      <Edit className="h-4 w-4 mr-2" />
                      Edit Details
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleViewSchedule(member)}>
                      <Calendar className="h-4 w-4 mr-2" />
                      View Schedule
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => handleRemoveStaff(member)} className="text-destructive">
                      <Trash2 className="h-4 w-4 mr-2" />
                      Remove Staff
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm">
                  <MapPin className="h-4 w-4 text-muted-foreground" />
                  <span className="text-foreground">{member.specialization || 'No specialization'}</span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <Phone className="h-4 w-4 text-muted-foreground" />
                  <span className="text-foreground">{member.phone || 'N/A'}</span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <Mail className="h-4 w-4 text-muted-foreground" />
                  <span className="text-foreground">{member.email || 'N/A'}</span>
                </div>
              </div>
              
              <div className="flex items-center justify-between pt-3 border-t border-border">
                <div className="flex gap-2">
                  <Badge className={getStatusColor(member.activeStatus ? 'Available' : 'Inactive')}>
                    {member.activeStatus ? 'Active' : 'Inactive'}
                  </Badge>
                  <Badge className={getShiftColor(member.workingDays || 'N/A')}>
                    {member.workingDays || 'N/A'}
                  </Badge>
                </div>
              </div>
              
              <div className="text-xs text-muted-foreground pt-1">
                Hired: {member.hireDate || 'N/A'}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredStaff.length === 0 && (
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-12 text-center">
            <UserCheck className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium text-foreground mb-2">No staff found</h3>
            <p className="text-muted-foreground mb-4">Try adjusting your search criteria or add a new staff member.</p>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Add First Staff Member
            </Button>
          </CardContent>
        </Card>
      )}

             {/* View Profile Dialog */}
       <Dialog open={isViewProfileOpen} onOpenChange={setIsViewProfileOpen}>
         <DialogContent className="sm:max-w-md">
           <DialogHeader>
             <DialogTitle>Staff Profile: {selectedStaff?.name}</DialogTitle>
           </DialogHeader>
           {selectedStaff && (
             <div className="space-y-4">
               <div>
                 <Label>Full Name</Label>
                 <p className="text-foreground">{selectedStaff.name}</p>
               </div>
               <div>
                 <Label>Job Type</Label>
                 <p className="text-foreground">{selectedStaff.jobType}</p>
               </div>
               <div>
                 <Label>Specialization</Label>
                 <p className="text-foreground">{selectedStaff.specialization || 'N/A'}</p>
               </div>
               <div>
                 <Label>License Number</Label>
                 <p className="text-foreground">{selectedStaff.licenseNumber || 'N/A'}</p>
               </div>
               <div>
                 <Label>Phone</Label>
                 <p className="text-foreground">{selectedStaff.phone || 'N/A'}</p>
               </div>
               <div>
                 <Label>Email</Label>
                 <p className="text-foreground">{selectedStaff.email || 'N/A'}</p>
               </div>
               <div>
                 <Label>Hire Date</Label>
                 <p className="text-foreground">{selectedStaff.hireDate ? new Date(selectedStaff.hireDate).toLocaleDateString() : 'N/A'}</p>
               </div>
               <div>
                 <Label>Working Days</Label>
                 <p className="text-foreground">{selectedStaff.workingDays || 'N/A'}</p>
               </div>
               <div>
                 <Label>Years of Service</Label>
                 <p className="text-foreground">{calculateYearsOfService(selectedStaff.hireDate || '')}</p>
               </div>
               <div>
                 <Label>Active Status</Label>
                 <Badge className={getStatusColor(selectedStaff.activeStatus ? 'Available' : 'Inactive')}>
                   {selectedStaff.activeStatus ? 'Active' : 'Inactive'}
                 </Badge>
               </div>
             </div>
           )}
         </DialogContent>
       </Dialog>

             {/* Edit Details Dialog */}
       <Dialog open={isEditDetailsOpen} onOpenChange={setIsEditDetailsOpen}>
         <DialogContent className="sm:max-w-md">
           <DialogHeader>
             <DialogTitle>Edit Staff Details: {editingStaff?.name}</DialogTitle>
           </DialogHeader>
           {editingStaff && (
             <div className="space-y-4">
               <div>
                 <Label htmlFor="editStaffName">Full Name *</Label>
                 <Input
                   id="editStaffName"
                   value={editingStaff.name}
                   onChange={(e) => setEditingStaff({ ...editingStaff, name: e.target.value })}
                   placeholder="Enter full name"
                   required
                 />
               </div>
               <div>
                 <Label htmlFor="editJobType">Job Type</Label>
                 <Select value={editingStaff.jobType} onValueChange={(value) => setEditingStaff({ ...editingStaff, jobType: value as 'Doctor' | 'Nurse' | 'Admin' })}>
                   <SelectTrigger>
                     <SelectValue placeholder="Select job type" />
                   </SelectTrigger>
                   <SelectContent>
                     <SelectItem value="Doctor">Doctor</SelectItem>
                     <SelectItem value="Nurse">Nurse</SelectItem>
                     <SelectItem value="Admin">Admin</SelectItem>
                   </SelectContent>
                 </Select>
               </div>
               <div>
                 <Label htmlFor="editSpecialization">Specialization</Label>
                 <Input
                   id="editSpecialization"
                   value={editingStaff.specialization}
                   onChange={(e) => setEditingStaff({ ...editingStaff, specialization: e.target.value })}
                   placeholder="e.g., Cardiology, Pediatrics"
                 />
               </div>
               <div>
                 <Label htmlFor="editLicenseNumber">License Number</Label>
                 <Input
                   id="editLicenseNumber"
                   value={editingStaff.licenseNumber}
                   onChange={(e) => setEditingStaff({ ...editingStaff, licenseNumber: e.target.value })}
                   placeholder="License number (if applicable)"
                 />
               </div>
               <div className="grid grid-cols-2 gap-4">
                 <div>
                   <Label htmlFor="editStaffPhone">Phone</Label>
                   <Input
                     id="editStaffPhone"
                     value={editingStaff.phone}
                     onChange={(e) => setEditingStaff({ ...editingStaff, phone: e.target.value })}
                     placeholder="Phone number"
                   />
                 </div>
                 <div>
                   <Label htmlFor="editWorkingDays">Working Days</Label>
                   <Input
                     id="editWorkingDays"
                     value={editingStaff.workingDays}
                     onChange={(e) => setEditingStaff({ ...editingStaff, workingDays: e.target.value })}
                     placeholder="e.g., Mon-Fri"
                   />
                 </div>
               </div>
               <div>
                 <Label htmlFor="editStaffEmail">Email</Label>
                 <Input
                   id="editStaffEmail"
                   type="email"
                   value={editingStaff.email}
                   onChange={(e) => setEditingStaff({ ...editingStaff, email: e.target.value })}
                   placeholder="Email address"
                 />
               </div>
               <div>
                 <Label htmlFor="editHireDate">Hire Date</Label>
                 <Input
                   id="editHireDate"
                   type="date"
                   value={editingStaff.hireDate}
                   onChange={(e) => setEditingStaff({ ...editingStaff, hireDate: e.target.value })}
                 />
               </div>
               <Button onClick={handleUpdateStaff} className="w-full">
                 <Edit className="h-4 w-4 mr-2" />
                 Update Staff Member
               </Button>
             </div>
           )}
         </DialogContent>
       </Dialog>

             {/* View Schedule Dialog */}
       <Dialog open={isViewScheduleOpen} onOpenChange={setIsViewScheduleOpen}>
         <DialogContent className="sm:max-w-md">
           <DialogHeader>
             <DialogTitle>Schedule for {selectedStaff?.name}</DialogTitle>
           </DialogHeader>
           {selectedStaff && (
             <div className="space-y-4">
               <div>
                 <Label>Current Status</Label>
                 <Badge className={getStatusColor(selectedStaff.activeStatus ? 'Available' : 'Inactive')}>
                   {selectedStaff.activeStatus ? 'Available' : 'Inactive'}
                 </Badge>
               </div>
               <div>
                 <Label>Working Days</Label>
                 <p className="text-foreground">{selectedStaff.workingDays || 'N/A'}</p>
               </div>
               <div>
                 <Label>Upcoming Shifts</Label>
                 <div className="space-y-2 text-sm">
                   <div className="flex justify-between">
                     <span>Monday</span>
                     <span className="text-muted-foreground">Morning Shift</span>
                   </div>
                   <div className="flex justify-between">
                     <span>Tuesday</span>
                     <span className="text-muted-foreground">Full Day</span>
                   </div>
                   <div className="flex justify-between">
                     <span>Wednesday</span>
                     <span className="text-muted-foreground">Night Shift</span>
                   </div>
                   <div className="flex justify-between">
                     <span>Thursday</span>
                     <span className="text-muted-foreground">Morning Shift</span>
                   </div>
                   <div className="flex justify-between">
                     <span>Friday</span>
                     <span className="text-muted-foreground">Full Day</span>
                   </div>
                 </div>
               </div>
               <div>
                 <Label>Notes</Label>
                 <p className="text-sm text-muted-foreground">
                   Schedule is based on current working days configuration. 
                   Contact the staff member for any schedule changes.
                 </p>
               </div>
             </div>
           )}
         </DialogContent>
       </Dialog>

      {/* Remove Staff Dialog */}
      <AlertDialog open={isRemoveStaffOpen} onOpenChange={setIsRemoveStaffOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently remove {selectedStaff?.name} from the
              system.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setIsRemoveStaffOpen(false)}>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteStaff} className="bg-destructive text-destructive-foreground hover:bg-destructive-hover">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Staff;