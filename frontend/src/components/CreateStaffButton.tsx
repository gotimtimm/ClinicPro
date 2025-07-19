import React, { useState } from "react";
import { Plus } from "lucide-react";
import { Button } from "./ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "./ui/dialog";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { toast } from "./ui/sonner";
import { api } from "../lib/api";
import { Staff } from "../lib/types";

export function CreateStaffButton() {
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState<Omit<Staff, 'staffID'>>({
    name: '',
    jobType: 'Doctor',
    specialization: '',
    licenseNumber: '',
    phone: '',
    email: '',
    hireDate: new Date().toISOString().split('T')[0],
    workingDays: 'Mon-Fri',
    activeStatus: true
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      await api.staff.create(formData);
      toast.success("Staff member created successfully!");
      setOpen(false);
      // Reset form
      setFormData({
        name: '',
        jobType: 'Doctor',
        specialization: '',
        licenseNumber: '',
        phone: '',
        email: '',
        hireDate: new Date().toISOString().split('T')[0],
        workingDays: 'Mon-Fri',
        activeStatus: true
      });
    } catch (err: any) {
      toast.error(err.message || "Failed to create staff member");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof typeof formData, value: string | boolean) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
          <Plus className="h-4 w-4 mr-2" />
          Add New Staff
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Create New Staff Member</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Full Name *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              placeholder="Enter staff member's full name"
              required
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="jobType">Job Type *</Label>
            <Select value={formData.jobType} onValueChange={(value) => handleInputChange('jobType', value as Staff['jobType'])}>
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
          
          <div className="space-y-2">
            <Label htmlFor="specialization">Specialization</Label>
            <Input
              id="specialization"
              value={formData.specialization}
              onChange={(e) => handleInputChange('specialization', e.target.value)}
              placeholder="e.g., Cardiology, Pediatrics"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="licenseNumber">License Number</Label>
            <Input
              id="licenseNumber"
              value={formData.licenseNumber}
              onChange={(e) => handleInputChange('licenseNumber', e.target.value)}
              placeholder="Professional license number"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="phone">Phone Number</Label>
            <Input
              id="phone"
              value={formData.phone}
              onChange={(e) => handleInputChange('phone', e.target.value)}
              placeholder="+1 (555) 123-4567"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="email">Email Address</Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              placeholder="staff@clinic.com"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="workingDays">Working Days</Label>
            <Input
              id="workingDays"
              value={formData.workingDays}
              onChange={(e) => handleInputChange('workingDays', e.target.value)}
              placeholder="e.g., Mon-Fri, Mon-Sat"
            />
          </div>
          
          <div className="flex justify-end space-x-2 pt-4">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "Creating..." : "Create Staff Member"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}