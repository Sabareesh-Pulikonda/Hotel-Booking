# Hotel Reservation System

## Overview
The **Hotel Reservation System** is a simple Java-based application that allows users to book rooms, view bookings, update details, and cancel bookings. It connects to a MySQL database to manage the reservations and rooms.

## Features
1. **Book a Room**: Allows users to book available rooms.
2. **View My Bookings**: Allows users to view their past bookings.
3. **Get Room Number**: Displays the room number based on the booking ID and username.
4. **Update User Details**: Allows users to update their booking information.
5. **Cancel Booking**: Allows users to cancel their booking and free up the room.
6. **Logout**: User can log out of the system.

## Database Structure
The system uses two main tables in the MySQL database:
1. **Bookings**: Stores booking details including `Booking_Id`, `Guest_name`, `room_no`, `contact_no`, and `Booking_date`.
2. **Rooms**: Stores information about the rooms, including `room_no` and `status` (either "available" or "booked").

### Tables:
- **Bookings** Table:
  | Booking_Id (Primary Key) | Guest_name | room_no (Foreign Key) | contact_no | Booking_date |
  |--------------------------|------------|-----------------------|------------|--------------|
  
- **Rooms** Table:
  | room_no (Primary Key) | status (available/booked) |
  
### Sample Query to Create Tables:

```sql
CREATE TABLE Rooms (
    room_no INT PRIMARY KEY,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE Bookings (
    Booking_Id INT AUTO_INCREMENT PRIMARY KEY,
    Guest_name VARCHAR(100) NOT NULL,
    room_no INT NOT NULL,
    contact_no VARCHAR(15) NOT NULL,
    Booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_no) REFERENCES Rooms(room_no)
);


## Requirements
Java 8 or higher: The application is built with Java.
MySQL: Used for the backend database.
JDBC: For database interaction between Java and MySQL.
IDE: Eclipse or any Java IDE for development.