import React, { useState } from "react";
import { Plus } from "lucide-react";
import { Button } from "./ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "./ui/dialog";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Textarea } from "./ui/textarea";
import { toast } from "./ui/sonner";
import { api } from "../lib/api";
import { Inventory } from "../lib/types";

export function CreateInventoryButton() {
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState<Omit<Inventory, 'itemID'>>({
    name: '',
    type: 'Medicine',
    purpose: '',
    stockQuantity: 0,
    reorderThreshold: 10,
    unitPrice: 0,
    supplierInfo: '',
    expiryDate: '',
    activeStatus: true
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      await api.inventory.create(formData);
      toast.success("Inventory item created successfully!");
      setOpen(false);
      // Reset form
      setFormData({
        name: '',
        type: 'Medicine',
        purpose: '',
        stockQuantity: 0,
        reorderThreshold: 10,
        unitPrice: 0,
        supplierInfo: '',
        expiryDate: '',
        activeStatus: true
      });
    } catch (err: any) {
      toast.error(err.message || "Failed to create inventory item");
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
          Add Medicine
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Add New Medicine/Equipment</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Name *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              placeholder="Enter item name"
              required
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="type">Type *</Label>
            <Select value={formData.type} onValueChange={(value) => handleInputChange('type', value as Inventory['type'])}>
              <SelectTrigger>
                <SelectValue placeholder="Select type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="Medicine">Medicine</SelectItem>
                <SelectItem value="Equipment">Equipment</SelectItem>
              </SelectContent>
            </Select>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="purpose">Purpose</Label>
            <Input
              id="purpose"
              value={formData.purpose}
              onChange={(e) => handleInputChange('purpose', e.target.value)}
              placeholder="e.g., Pain relief, Vital check"
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="stockQuantity">Stock Quantity</Label>
              <Input
                id="stockQuantity"
                type="number"
                value={formData.stockQuantity}
                onChange={(e) => handleInputChange('stockQuantity', parseInt(e.target.value) || 0)}
                min="0"
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="reorderThreshold">Reorder Threshold</Label>
              <Input
                id="reorderThreshold"
                type="number"
                value={formData.reorderThreshold}
                onChange={(e) => handleInputChange('reorderThreshold', parseInt(e.target.value) || 0)}
                min="0"
              />
            </div>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="unitPrice">Unit Price</Label>
            <Input
              id="unitPrice"
              type="number"
              step="0.01"
              value={formData.unitPrice}
              onChange={(e) => handleInputChange('unitPrice', parseFloat(e.target.value) || 0)}
              min="0"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="supplierInfo">Supplier Information</Label>
            <Input
              id="supplierInfo"
              value={formData.supplierInfo}
              onChange={(e) => handleInputChange('supplierInfo', e.target.value)}
              placeholder="Supplier name or contact"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="expiryDate">Expiry Date</Label>
            <Input
              id="expiryDate"
              type="date"
              value={formData.expiryDate}
              onChange={(e) => handleInputChange('expiryDate', e.target.value)}
            />
          </div>
          
          <div className="flex justify-end space-x-2 pt-4">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "Creating..." : "Create Item"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}