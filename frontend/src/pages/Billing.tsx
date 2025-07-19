import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
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
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Search, CreditCard, DollarSign, Calendar, User, CheckCircle, XCircle, Eye, Plus, Trash2, Edit, MoreHorizontal } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Billing } from "@/lib/types";
import { Autocomplete } from "@/components/ui/autocomplete";

const Billing = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedBilling, setSelectedBilling] = useState<any>(null);
  const [isViewDetailsOpen, setIsViewDetailsOpen] = useState(false);
  const [isCreateBillingOpen, setIsCreateBillingOpen] = useState(false);
  const [isEditBillingOpen, setIsEditBillingOpen] = useState(false);
  const [isDeleteBillingOpen, setIsDeleteBillingOpen] = useState(false);
  const [editingBilling, setEditingBilling] = useState<any>(null);
  const [newBilling, setNewBilling] = useState({
    appointmentID: 0,
    amount: 0,
    paid: false,
    paymentDate: ""
  });
  const [selectedPatientName, setSelectedPatientName] = useState("");
  const [selectedDoctorName, setSelectedDoctorName] = useState("");
  const [appointmentSearchTerm, setAppointmentSearchTerm] = useState("");

  // Fetch billing records with appointment details
  const { data: billingRecords = [], isLoading, error } = useQuery({
    queryKey: ['billing-with-details'],
    queryFn: async () => {
      const billing = await api.billing.list();
      // For now, we'll fetch appointments separately and combine them
      const appointments = await api.appointments.listWithNames();
      
      return billing.map((bill: Billing) => {
        const appointment = appointments.find((app: any) => app.appointmentID === bill.appointmentID);
        return {
          ...bill,
          patientName: appointment?.patientName || 'Unknown Patient',
          doctorName: appointment?.doctorName || 'Unknown Doctor',
          appointmentDate: appointment?.date || 'Unknown Date',
          visitType: appointment?.visitType || 'Unknown Type'
        };
      });
    },
  });

  // Fetch appointments for billing creation
  const { data: appointments = [] } = useQuery({
    queryKey: ['appointments-with-names'],
    queryFn: api.appointments.listWithNames,
  });

  // Create billing mutation
  const createBillingMutation = useMutation({
    mutationFn: (data: Omit<Billing, 'billingID'>) => api.billing.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billing-with-details'] });
      toast({
        title: "Billing Created",
        description: "Billing record has been created successfully.",
      });
      setIsCreateBillingOpen(false);
      // Reset form
      setNewBilling({
        appointmentID: 0,
        amount: 0,
        paid: false,
        paymentDate: ""
      });
      setSelectedPatientName("");
      setSelectedDoctorName("");
      setAppointmentSearchTerm("");
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to create billing record",
        variant: "destructive",
      });
    }
  });

  // Update billing mutation
  const updateBillingMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: any }) => api.billing.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billing-with-details'] });
      toast({
        title: "Billing Updated",
        description: "Billing record has been updated successfully.",
      });
      setIsEditBillingOpen(false);
      setEditingBilling(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to update billing record",
        variant: "destructive",
      });
    }
  });

  // Delete billing mutation
  const deleteBillingMutation = useMutation({
    mutationFn: (id: number) => api.billing.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billing-with-details'] });
      toast({
        title: "Billing Deleted",
        description: "Billing record has been deleted successfully.",
      });
      setIsDeleteBillingOpen(false);
      setSelectedBilling(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to delete billing record",
        variant: "destructive",
      });
    }
  });

  const handleViewDetails = (billing: any) => {
    setSelectedBilling(billing);
    setIsViewDetailsOpen(true);
  };

  const handleMarkAsPaid = (billing: any) => {
    updateBillingMutation.mutate({ 
      id: billing.billingID, 
      data: { 
        appointmentID: billing.appointmentID,
        amount: billing.amount,
        paid: true, 
        paymentDate: new Date().toISOString().split('T')[0] 
      }
    });
  };

  const handleEditBilling = (billing: any) => {
    setEditingBilling(billing);
    setIsEditBillingOpen(true);
  };

  const handleDeleteBilling = (billing: any) => {
    setSelectedBilling(billing);
    setIsDeleteBillingOpen(true);
  };

  const handleUpdateBilling = () => {
    if (editingBilling) {
      updateBillingMutation.mutate({ 
        id: editingBilling.billingID, 
        data: editingBilling 
      });
    }
  };

  const handleConfirmDelete = () => {
    if (selectedBilling) {
      deleteBillingMutation.mutate(selectedBilling.billingID);
    }
  };

  const handleCreateBilling = () => {
    if (newBilling.appointmentID === 0) {
      toast({
        title: "Error",
        description: "Please select an appointment",
        variant: "destructive",
      });
      return;
    }
    
    if (newBilling.amount <= 0) {
      toast({
        title: "Error",
        description: "Please enter a valid amount",
        variant: "destructive",
      });
      return;
    }

    createBillingMutation.mutate(newBilling);
  };

  const handleAppointmentSelect = (appointment: any) => {
    if (appointment) {
      setNewBilling(prev => ({ ...prev, appointmentID: appointment.appointmentID }));
      setSelectedPatientName(appointment.patientName);
      setSelectedDoctorName(appointment.doctorName);
      setAppointmentSearchTerm(`${appointment.patientName} - ${appointment.doctorName} (${appointment.visitType})`);
      
      // Auto-calculate amount based on visit type
      const amount = getBillingAmount(appointment.visitType);
      setNewBilling(prev => ({ ...prev, amount }));
    }
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

  const filteredBilling = billingRecords.filter((billing: any) => {
    const searchLower = searchTerm.toLowerCase();
    return (
      billing.patientName?.toLowerCase().includes(searchLower) ||
      billing.doctorName?.toLowerCase().includes(searchLower) ||
      billing.visitType?.toLowerCase().includes(searchLower) ||
      billing.amount?.toString().includes(searchLower)
    );
  });

  const getStatusColor = (paid: boolean) => {
    return paid ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800";
  };

  const getStatusIcon = (paid: boolean) => {
    return paid ? CheckCircle : XCircle;
  };

  const totalRevenue = billingRecords.reduce((sum: number, billing: any) => sum + (billing.paid ? billing.amount : 0), 0);
  const pendingAmount = billingRecords.reduce((sum: number, billing: any) => sum + (billing.paid ? 0 : billing.amount), 0);
  const totalBills = billingRecords.length;
  const paidBills = billingRecords.filter((billing: any) => billing.paid).length;

  if (isLoading) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-muted-foreground">Loading billing records...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-destructive">Error loading billing records: {error.message}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Billing</h1>
          <p className="text-muted-foreground">Manage patient billing and payments</p>
        </div>
        <Dialog open={isCreateBillingOpen} onOpenChange={setIsCreateBillingOpen}>
          <DialogTrigger asChild>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Create Billing
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Create New Billing Record</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="appointment">Select Appointment</Label>
                <Autocomplete
                  value={appointmentSearchTerm}
                  onChange={setAppointmentSearchTerm}
                  onSelect={handleAppointmentSelect}
                  placeholder="Search for appointment..."
                  searchFunction={async (query: string) => {
                    return appointments.filter((app: any) => 
                      app.patientName?.toLowerCase().includes(query.toLowerCase()) ||
                      app.doctorName?.toLowerCase().includes(query.toLowerCase()) ||
                      app.visitType?.toLowerCase().includes(query.toLowerCase())
                    );
                  }}
                  displayKey="patientName"
                />
                {selectedPatientName && (
                  <div className="mt-2 p-2 bg-gray-50 rounded text-sm">
                    <p><strong>Patient:</strong> {selectedPatientName}</p>
                    <p><strong>Doctor:</strong> {selectedDoctorName}</p>
                  </div>
                )}
              </div>
              
              <div>
                <Label htmlFor="amount">Amount ($)</Label>
                <Input
                  id="amount"
                  type="number"
                  step="0.01"
                  value={newBilling.amount}
                  onChange={(e) => setNewBilling({ ...newBilling, amount: parseFloat(e.target.value) || 0 })}
                  placeholder="Enter amount"
                />
              </div>
              
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="paid"
                  checked={newBilling.paid}
                  onChange={(e) => setNewBilling({ ...newBilling, paid: e.target.checked })}
                  className="rounded"
                />
                <Label htmlFor="paid">Mark as paid</Label>
              </div>
              
              {newBilling.paid && (
                <div>
                  <Label htmlFor="paymentDate">Payment Date</Label>
                  <Input
                    id="paymentDate"
                    type="date"
                    value={newBilling.paymentDate}
                    onChange={(e) => setNewBilling({ ...newBilling, paymentDate: e.target.value })}
                  />
                </div>
              )}
              
              <Button 
                onClick={handleCreateBilling} 
                className="w-full"
                disabled={createBillingMutation.isPending}
              >
                <Plus className="h-4 w-4 mr-2" />
                {createBillingMutation.isPending ? "Creating..." : "Create Billing"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total Revenue</p>
                <p className="text-2xl font-bold text-foreground">${totalRevenue.toFixed(2)}</p>
              </div>
              <div className="p-3 rounded-lg bg-green-50">
                <DollarSign className="h-6 w-6 text-green-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Pending Amount</p>
                <p className="text-2xl font-bold text-foreground">${pendingAmount.toFixed(2)}</p>
              </div>
              <div className="p-3 rounded-lg bg-orange-50">
                <CreditCard className="h-6 w-6 text-orange-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total Bills</p>
                <p className="text-2xl font-bold text-foreground">{totalBills}</p>
              </div>
              <div className="p-3 rounded-lg bg-blue-50">
                <Calendar className="h-6 w-6 text-blue-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Paid Bills</p>
                <p className="text-2xl font-bold text-foreground">{paidBills}</p>
              </div>
              <div className="p-3 rounded-lg bg-purple-50">
                <CheckCircle className="h-6 w-6 text-purple-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Search */}
      <Card className="bg-gradient-card border-0 shadow-sm">
        <CardContent className="p-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="Search by patient name, doctor name, visit type, or amount..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      {/* Billing Records List */}
      <div className="space-y-4">
        {filteredBilling.map((billing: any) => {
          const StatusIcon = getStatusIcon(billing.paid);
          return (
            <Card key={billing.billingID} className="bg-gradient-card border-0 shadow-sm hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 flex-1">
                    {/* Patient & Doctor Info */}
                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        <User className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium text-foreground">{billing.patientName}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Calendar className="h-4 w-4 text-muted-foreground" />
                        <span className="text-sm text-muted-foreground">{billing.doctorName}</span>
                      </div>
                    </div>

                    {/* Appointment Details */}
                    <div className="space-y-2">
                      <div className="text-sm text-muted-foreground">
                        <span className="font-medium">Visit Type:</span> {billing.visitType}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        <span className="font-medium">Date:</span> {new Date(billing.appointmentDate).toLocaleDateString()}
                      </div>
                    </div>

                    {/* Amount & Status */}
                    <div className="space-y-2">
                      <div className="text-lg font-bold text-foreground">
                        ${billing.amount.toFixed(2)}
                      </div>
                      <Badge className={getStatusColor(billing.paid)}>
                        <StatusIcon className="h-3 w-3 mr-1" />
                        {billing.paid ? 'Paid' : 'Pending'}
                      </Badge>
                    </div>
                  </div>

                                    {/* Actions */}
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleViewDetails(billing)}
                    >
                      <Eye className="h-4 w-4 mr-2" />
                      View
                    </Button>
                    {!billing.paid && (
                      <Button
                        size="sm"
                        onClick={() => handleMarkAsPaid(billing)}
                        disabled={updateBillingMutation.isPending}
                      >
                        <CheckCircle className="h-4 w-4 mr-2" />
                        Mark as Paid
                      </Button>
                    )}
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="outline" size="sm">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => handleEditBilling(billing)}>
                          <Edit className="h-4 w-4 mr-2" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          onClick={() => handleDeleteBilling(billing)}
                          className="text-red-600 focus:text-red-600"
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {filteredBilling.length === 0 && (
        <Card className="bg-gradient-card border-0 shadow-sm">
          <CardContent className="p-12 text-center">
            <CreditCard className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium text-foreground mb-2">No billing records found</h3>
            <p className="text-muted-foreground">Billing records will appear here when appointments are completed.</p>
          </CardContent>
        </Card>
      )}

      {/* View Details Dialog */}
      <Dialog open={isViewDetailsOpen} onOpenChange={setIsViewDetailsOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Billing Details</DialogTitle>
          </DialogHeader>
          {selectedBilling && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Patient</Label>
                  <p className="text-foreground">{selectedBilling.patientName}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Doctor</Label>
                  <p className="text-foreground">{selectedBilling.doctorName}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Visit Type</Label>
                  <p className="text-foreground">{selectedBilling.visitType}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Amount</Label>
                  <p className="text-foreground font-bold">${selectedBilling.amount.toFixed(2)}</p>
                </div>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Appointment Date</Label>
                <p className="text-foreground">{new Date(selectedBilling.appointmentDate).toLocaleDateString()}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Status</Label>
                <Badge className={getStatusColor(selectedBilling.paid)}>
                  {selectedBilling.paid ? 'Paid' : 'Pending'}
                </Badge>
              </div>
              {selectedBilling.paymentDate && (
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Payment Date</Label>
                  <p className="text-foreground">{new Date(selectedBilling.paymentDate).toLocaleDateString()}</p>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Edit Billing Dialog */}
      <Dialog open={isEditBillingOpen} onOpenChange={setIsEditBillingOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Edit Billing Record</DialogTitle>
          </DialogHeader>
          {editingBilling && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Patient</Label>
                  <p className="text-foreground">{editingBilling.patientName}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium text-muted-foreground">Doctor</Label>
                  <p className="text-foreground">{editingBilling.doctorName}</p>
                </div>
              </div>
              <div>
                <Label htmlFor="editAmount">Amount ($)</Label>
                <Input
                  id="editAmount"
                  type="number"
                  step="0.01"
                  value={editingBilling.amount}
                  onChange={(e) => setEditingBilling({ ...editingBilling, amount: parseFloat(e.target.value) || 0 })}
                  placeholder="Enter amount"
                />
              </div>
              <div>
                <Label htmlFor="editStatus">Payment Status</Label>
                <Select 
                  value={editingBilling.paid ? "paid" : "pending"} 
                  onValueChange={(value) => setEditingBilling({ ...editingBilling, paid: value === "paid" })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="pending">Pending</SelectItem>
                    <SelectItem value="paid">Paid</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              {editingBilling.paid && (
                <div>
                  <Label htmlFor="editPaymentDate">Payment Date</Label>
                  <Input
                    id="editPaymentDate"
                    type="date"
                    value={editingBilling.paymentDate || new Date().toISOString().split('T')[0]}
                    onChange={(e) => setEditingBilling({ ...editingBilling, paymentDate: e.target.value })}
                  />
                </div>
              )}
              <div className="flex justify-end space-x-2 pt-4">
                <Button 
                  variant="outline" 
                  onClick={() => setIsEditBillingOpen(false)}
                >
                  Cancel
                </Button>
                <Button 
                  onClick={handleUpdateBilling}
                  disabled={updateBillingMutation.isPending}
                >
                  {updateBillingMutation.isPending ? "Updating..." : "Update Billing"}
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={isDeleteBillingOpen} onOpenChange={setIsDeleteBillingOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Billing Record</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this billing record? This action cannot be undone.
              {selectedBilling && (
                <div className="mt-2 p-2 bg-gray-50 rounded">
                  <p><strong>Patient:</strong> {selectedBilling.patientName}</p>
                  <p><strong>Amount:</strong> ${selectedBilling.amount.toFixed(2)}</p>
                  <p><strong>Status:</strong> {selectedBilling.paid ? 'Paid' : 'Pending'}</p>
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleConfirmDelete}
              className="bg-red-600 hover:bg-red-700"
              disabled={deleteBillingMutation.isPending}
            >
              {deleteBillingMutation.isPending ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Billing; 