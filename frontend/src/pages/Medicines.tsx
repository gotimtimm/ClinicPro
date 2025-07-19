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
import { Plus, Search, Pill, Wrench, Package, AlertTriangle, Calendar, MoreHorizontal, Edit, Trash2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { Inventory } from "@/lib/types";

const Medicines = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState("");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<Inventory | null>(null);
  const [deletingItem, setDeletingItem] = useState<Inventory | null>(null);
  const [newItem, setNewItem] = useState<Omit<Inventory, 'itemID'>>({
    name: "",
    type: "Medicine",
    purpose: "",
    stockQuantity: 0,
    reorderThreshold: 10,
    unitPrice: 0,
    supplierInfo: "",
    expiryDate: new Date().toISOString().split('T')[0],
    activeStatus: true
  });

  // Fetch inventory from API
  const { data: inventory = [], isLoading, error } = useQuery({
    queryKey: ['inventory'],
    queryFn: api.inventory.list,
  });

  // Create inventory mutation
  const createInventoryMutation = useMutation({
    mutationFn: api.inventory.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      toast({
        title: "Item Added",
        description: `${newItem.name} has been added to inventory.`,
      });
      setIsDialogOpen(false);
      setNewItem({
        name: "",
        type: "Medicine",
        purpose: "",
        stockQuantity: 0,
        reorderThreshold: 10,
        unitPrice: 0,
        supplierInfo: "",
        expiryDate: new Date().toISOString().split('T')[0],
        activeStatus: true
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to add item",
        variant: "destructive",
      });
    }
  });

  // Update inventory mutation
  const updateInventoryMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: Inventory }) => api.inventory.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      toast({
        title: "Item Updated",
        description: "Inventory item has been updated successfully.",
      });
      setIsEditDialogOpen(false);
      setEditingItem(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to update item",
        variant: "destructive",
      });
    }
  });

  // Delete inventory mutation
  const deleteInventoryMutation = useMutation({
    mutationFn: (id: number) => api.inventory.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      toast({
        title: "Item Deleted",
        description: "Inventory item has been deleted successfully.",
      });
      setIsDeleteDialogOpen(false);
      setDeletingItem(null);
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message || "Failed to delete item",
        variant: "destructive",
      });
    }
  });

  const handleAddItem = () => {
    createInventoryMutation.mutate(newItem);
  };

  const handleEditItem = (item: Inventory) => {
    setEditingItem(item);
    setIsEditDialogOpen(true);
  };

  const handleDeleteItem = (item: Inventory) => {
    setDeletingItem(item);
    setIsDeleteDialogOpen(true);
  };

  const handleUpdateItem = () => {
    if (editingItem) {
      updateInventoryMutation.mutate({ id: editingItem.itemID, data: editingItem });
    }
  };

  const handleConfirmDelete = () => {
    if (deletingItem) {
      deleteInventoryMutation.mutate(deletingItem.itemID);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "In Stock": return "bg-green-100 text-green-800";
      case "Low Stock": return "bg-yellow-100 text-yellow-800";
      case "Out of Stock": return "bg-red-100 text-red-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-muted-foreground">Loading medicines...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6 animate-fade-in">
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-destructive">Error loading medicines: {error.message}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Inventory</h1>
          <p className="text-muted-foreground">Manage your medicine and equipment stock</p>
        </div>
        <Dialog>
          <DialogTrigger asChild>
            <Button className="bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="h-4 w-4 mr-2" />
              Add Item
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Add New Item</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="itemName">Item Name *</Label>
                <Input
                  id="itemName"
                  value={newItem.name}
                  onChange={(e) => setNewItem({ ...newItem, name: e.target.value })}
                  placeholder="Enter item name"
                  required
                />
              </div>
              <div>
                <Label htmlFor="itemType">Item Type</Label>
                <Select 
                  value={newItem.type} 
                  onValueChange={(value) => setNewItem({ ...newItem, type: value as "Medicine" | "Equipment" })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Medicine">Medicine</SelectItem>
                    <SelectItem value="Equipment">Equipment</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="purpose">Purpose</Label>
                <Input
                  id="purpose"
                  value={newItem.purpose}
                  onChange={(e) => setNewItem({ ...newItem, purpose: e.target.value })}
                  placeholder={newItem.type === "Medicine" ? "e.g., Pain relief, Antibiotic" : "e.g., Diagnostic, Surgical, Monitoring"}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="stockQuantity">Stock Quantity</Label>
                  <Input
                    id="stockQuantity"
                    type="number"
                    value={newItem.stockQuantity}
                    onChange={(e) => setNewItem({ ...newItem, stockQuantity: parseInt(e.target.value) })}
                    placeholder="0"
                  />
                </div>
                <div>
                  <Label htmlFor="reorderThreshold">Reorder Threshold</Label>
                  <Input
                    id="reorderThreshold"
                    type="number"
                    value={newItem.reorderThreshold}
                    onChange={(e) => setNewItem({ ...newItem, reorderThreshold: parseInt(e.target.value) })}
                    placeholder={newItem.type === "Medicine" ? "10" : "5"}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="unitPrice">Unit Price</Label>
                  <Input
                    id="unitPrice"
                    type="number"
                    step="0.01"
                    value={newItem.unitPrice}
                    onChange={(e) => setNewItem({ ...newItem, unitPrice: parseFloat(e.target.value) })}
                    placeholder="0.00"
                  />
                </div>
                <div>
                  <Label htmlFor="expiryDate">Expiry Date</Label>
                  <Input
                    id="expiryDate"
                    type="date"
                    value={newItem.expiryDate}
                    onChange={(e) => setNewItem({ ...newItem, expiryDate: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <Label htmlFor="supplierInfo">Supplier Info</Label>
                <Input
                  id="supplierInfo"
                  value={newItem.supplierInfo}
                  onChange={(e) => setNewItem({ ...newItem, supplierInfo: e.target.value })}
                  placeholder="Enter supplier information"
                />
              </div>
              <Button onClick={handleAddItem} className="w-full">
                <Plus className="h-4 w-4 mr-2" />
                Add Item
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <Card className="bg-gradient-card border-0 shadow-sm">
        <CardContent className="p-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="Search inventory..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4">
        {inventory
          .filter(item => 
            item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.purpose.toLowerCase().includes(searchTerm.toLowerCase())
          )
          .map((item) => (
                      <Card key={item.itemID} className="bg-gradient-card border-0 shadow-sm">
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    {item.type === "Medicine" ? (
                      <Pill className="h-8 w-8 text-primary" />
                    ) : (
                      <Wrench className="h-8 w-8 text-primary" />
                    )}
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-foreground">{item.name}</h3>
                        <Badge variant="outline" className="text-xs">
                          {item.type}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">{item.purpose || 'No purpose specified'}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-center">
                      <p className="text-2xl font-bold text-foreground">{item.stockQuantity}</p>
                      <p className="text-xs text-muted-foreground">In Stock</p>
                    </div>
                    <Badge className={getStatusColor(item.stockQuantity > item.reorderThreshold ? 'In Stock' : item.stockQuantity > 0 ? 'Low Stock' : 'Out of Stock')}>
                      {item.stockQuantity > item.reorderThreshold ? 'In Stock' : item.stockQuantity > 0 ? 'Low Stock' : 'Out of Stock'}
                    </Badge>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="outline" size="sm">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => handleEditItem(item)}>
                          <Edit className="h-4 w-4 mr-2" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          onClick={() => handleDeleteItem(item)}
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
        ))}
      </div>

      {/* Edit Item Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Edit Item</DialogTitle>
          </DialogHeader>
          {editingItem && (
            <div className="space-y-4">
              <div>
                <Label htmlFor="editItemName">Item Name *</Label>
                <Input
                  id="editItemName"
                  value={editingItem.name}
                  onChange={(e) => setEditingItem({ ...editingItem, name: e.target.value })}
                  placeholder="Enter item name"
                  required
                />
              </div>
              <div>
                <Label htmlFor="editItemType">Item Type</Label>
                <Select 
                  value={editingItem.type} 
                  onValueChange={(value) => setEditingItem({ ...editingItem, type: value as "Medicine" | "Equipment" })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Medicine">Medicine</SelectItem>
                    <SelectItem value="Equipment">Equipment</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="editPurpose">Purpose</Label>
                <Input
                  id="editPurpose"
                  value={editingItem.purpose}
                  onChange={(e) => setEditingItem({ ...editingItem, purpose: e.target.value })}
                  placeholder={editingItem.type === "Medicine" ? "e.g., Pain relief, Antibiotic" : "e.g., Diagnostic, Surgical, Monitoring"}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="editStockQuantity">Stock Quantity</Label>
                  <Input
                    id="editStockQuantity"
                    type="number"
                    value={editingItem.stockQuantity}
                    onChange={(e) => setEditingItem({ ...editingItem, stockQuantity: parseInt(e.target.value) })}
                    placeholder="0"
                  />
                </div>
                <div>
                  <Label htmlFor="editReorderThreshold">Reorder Threshold</Label>
                  <Input
                    id="editReorderThreshold"
                    type="number"
                    value={editingItem.reorderThreshold}
                    onChange={(e) => setEditingItem({ ...editingItem, reorderThreshold: parseInt(e.target.value) })}
                    placeholder={editingItem.type === "Medicine" ? "10" : "5"}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="editUnitPrice">Unit Price</Label>
                  <Input
                    id="editUnitPrice"
                    type="number"
                    step="0.01"
                    value={editingItem.unitPrice}
                    onChange={(e) => setEditingItem({ ...editingItem, unitPrice: parseFloat(e.target.value) })}
                    placeholder="0.00"
                  />
                </div>
                <div>
                  <Label htmlFor="editExpiryDate">Expiry Date</Label>
                  <Input
                    id="editExpiryDate"
                    type="date"
                    value={editingItem.expiryDate}
                    onChange={(e) => setEditingItem({ ...editingItem, expiryDate: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <Label htmlFor="editSupplierInfo">Supplier Info</Label>
                <Input
                  id="editSupplierInfo"
                  value={editingItem.supplierInfo}
                  onChange={(e) => setEditingItem({ ...editingItem, supplierInfo: e.target.value })}
                  placeholder="Enter supplier information"
                />
              </div>
              <div className="flex justify-end space-x-2 pt-4">
                <Button 
                  variant="outline" 
                  onClick={() => setIsEditDialogOpen(false)}
                >
                  Cancel
                </Button>
                <Button 
                  onClick={handleUpdateItem}
                  disabled={updateInventoryMutation.isPending}
                >
                  {updateInventoryMutation.isPending ? "Updating..." : "Update Item"}
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Item</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this item? This action cannot be undone.
              {deletingItem && (
                <div className="mt-2 p-2 bg-gray-50 rounded">
                  <p><strong>Name:</strong> {deletingItem.name}</p>
                  <p><strong>Type:</strong> {deletingItem.type}</p>
                  <p><strong>Stock:</strong> {deletingItem.stockQuantity}</p>
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleConfirmDelete}
              className="bg-red-600 hover:bg-red-700"
              disabled={deleteInventoryMutation.isPending}
            >
              {deleteInventoryMutation.isPending ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Medicines;