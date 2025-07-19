DROP DATABASE IF EXISTS clinic_db;
CREATE DATABASE clinic_db;
USE clinic_db;

-- This is to reset the tables for use.
DROP TABLE IF EXISTS Feedback, Billing, Appointment_Inventory, Appointment, Inventory, Patient, Staff;

-- Table creation for Staff (Doctors and Non-Doctors)
CREATE TABLE Staff (
    StaffID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    JobType ENUM('Doctor', 'Nurse', 'Admin') NOT NULL,
    Specialization VARCHAR(100),
    LicenseNumber VARCHAR(50),
    Phone VARCHAR(20),
    Email VARCHAR(100),
    HireDate DATE,
    WorkingDays VARCHAR(50),
    ActiveStatus BOOLEAN DEFAULT TRUE
);

-- Table creation for Patients
CREATE TABLE Patient (
    PatientID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    BirthDate DATE,
    Phone VARCHAR(20),
    Email VARCHAR(100),
    InsuranceInfo VARCHAR(100),
    FirstVisitDate DATE,
    PrimaryDoctorID INT,
    ActiveStatus BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (PrimaryDoctorID) REFERENCES Staff(StaffID)
);

-- Table creation for Inventory (Medical Supplies & Equipment)
CREATE TABLE Inventory (
    ItemID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Type ENUM('Medicine', 'Equipment') NOT NULL,
    Purpose VARCHAR(255),
    StockQuantity INT DEFAULT 0,
    ReorderThreshold INT DEFAULT 10,
    UnitPrice DECIMAL(10,2),
    SupplierInfo VARCHAR(100),
    ExpiryDate DATE,
    ActiveStatus BOOLEAN DEFAULT TRUE
);

-- Table creation for Appointments
CREATE TABLE Appointment (
    AppointmentID INT PRIMARY KEY AUTO_INCREMENT,
    PatientID INT NOT NULL,
    DoctorID INT NOT NULL,
    Date DATE NOT NULL,
    Time TIME NOT NULL,
    Duration INT,
    VisitType ENUM('Check-up', 'Procedure', 'Emergency') NOT NULL,
    Status ENUM('Done', 'Not Done', 'Canceled') DEFAULT 'Not Done',
    Notes TEXT,
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID),
    FOREIGN KEY (DoctorID) REFERENCES Staff(StaffID)
);

-- Table creation for Appointment_Inventory (Used Medical Supplies)
CREATE TABLE Appointment_Inventory (
    AppointmentID INT,
    ItemID INT,
    QuantityUsed INT,
    PRIMARY KEY (AppointmentID, ItemID),
    FOREIGN KEY (AppointmentID) REFERENCES Appointment(AppointmentID),
    FOREIGN KEY (ItemID) REFERENCES Inventory(ItemID)
);

-- Table creation for Billing (Per Appointment)
CREATE TABLE Billing (
    BillingID INT PRIMARY KEY AUTO_INCREMENT,
    AppointmentID INT UNIQUE,
    Amount DECIMAL(10,2) NOT NULL,
    Paid BOOLEAN DEFAULT FALSE,
    PaymentDate DATE,
    FOREIGN KEY (AppointmentID) REFERENCES Appointment(AppointmentID)
);

-- Table creation for Feedback
CREATE TABLE Feedback (
    FeedbackID INT PRIMARY KEY AUTO_INCREMENT,
    AppointmentID INT,
    DoctorID INT,
    PatientID INT,
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Comments TEXT,
    FOREIGN KEY (AppointmentID) REFERENCES Appointment(AppointmentID),
    FOREIGN KEY (DoctorID) REFERENCES Staff(StaffID),
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID)
);

-- Sample Data to be Inserted in the Database

-- a. Sample Data for Staff Table
 INSERT INTO Staff (Name, JobType, Specialization, LicenseNumber, Phone, Email, HireDate, WorkingDays)
VALUES 
('Dr. Santos', 'Doctor', 'Pediatrics', 'DOC1234', '09171234567', 'santos@clinic.com', '2020-01-15', 'Mon-Fri'),
('Nurse Dela Cruz', 'Nurse', NULL, NULL, '09179876543', 'delacruz@clinic.com', '2021-06-01', 'Mon-Sat');

-- b. Sample Data for Patient Table
INSERT INTO Patient (Name, BirthDate, Phone, Email, InsuranceInfo, FirstVisitDate, PrimaryDoctorID)
VALUES 
('Juan Dela Cruz', '1990-05-12', '09201234567', 'juan@gmail.com', 'Maxicare', '2024-05-01', 1);

-- c. Sample Data for Inventory Table
INSERT INTO Inventory (Name, Type, Purpose, StockQuantity, ReorderThreshold, UnitPrice, SupplierInfo, ExpiryDate)
VALUES 
('Paracetamol', 'Medicine', 'Pain relief', 100, 20, 5.00, 'MediPharma Inc.', '2025-12-31'),
('Stethoscope', 'Equipment', 'Vital check', 10, 2, 1500.00, 'HealthEquip Co.', NULL);

-- d. Sample Data for Appointment Table
INSERT INTO Appointment (PatientID, DoctorID, Date, Time, Duration, VisitType, Status, Notes)
VALUES 
(1, 1, '2025-06-10', '09:00:00', 30, 'Check-up', 'Not Done', 'Routine check-up');

-- e. Sample Data for Billing Table 
INSERT INTO Billing (AppointmentID, Amount, Paid)
VALUES (1, 500.00, FALSE);

-- f. Sample Data for Usage Of Inventory Table
INSERT INTO Appointment_Inventory (AppointmentID, ItemID, QuantityUsed)
VALUES (1, 1, 2);

-- g. Sample Data for Feedback Table
INSERT INTO Feedback (AppointmentID, DoctorID, PatientID, Rating, Comments)
VALUES (1, 1, 1, 5, 'Very professional and helpful!');
