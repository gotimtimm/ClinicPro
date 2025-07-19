import React, { useState } from "react";
import { Plus } from "lucide-react";
import { Button } from "./ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "./ui/dialog";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { toast } from "./ui/sonner";
import { api } from "../lib/api";
import { Patient } from "../lib/types";

export function CreatePatientButton() {
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState<Omit<Patient, 'patientID'>>({
    name: '',
    birthDate: '',
    phone: '',
    email: '',
    insuranceInfo: '',
    firstVisitDate: new Date().toISOString().split('T')[0],
    primaryDoctorID: 1,
    activeStatus: true
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      await api.patients.create(formData);
      toast.success("Patient created successfully!");
      setOpen(false);
      // Reset form
      setFormData({
        name: '',
        birthDate: '',
        phone: '',
        email: '',
        insuranceInfo: '',
        firstVisitDate: new Date().toISOString().split('T')[0],
        primaryDoctorID: 1,
        activeStatus: true
      });
    } catch (err: any) {
      toast.error(err.message || "Failed to create patient");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof typeof formData, value: string | number | boolean) => {
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
          Add New Patient
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Create New Patient</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Full Name *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              placeholder="Enter patient's full name"
              required
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="birthDate">Birth Date</Label>
            <Input
              id="birthDate"
              type="date"
              value={formData.birthDate}
              onChange={(e) => handleInputChange('birthDate', e.target.value)}
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
              placeholder="patient@email.com"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="insurance">Insurance Information</Label>
            <Input
              id="insurance"
              value={formData.insuranceInfo}
              onChange={(e) => handleInputChange('insuranceInfo', e.target.value)}
              placeholder="Insurance provider"
            />
          </div>
          
          <div className="flex justify-end space-x-2 pt-4">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "Creating..." : "Create Patient"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}