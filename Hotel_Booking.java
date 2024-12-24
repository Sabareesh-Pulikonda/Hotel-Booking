package com.Hotel_reservation.jdbc;
import java.sql.*;
import java.util.Scanner;

public class Booking {
    // Database connection details
    private static final String url = "jdbc:mysql://localhost:3306/Hotel_booking";
    private static final String uname = "root";
    private static final String password = "Tiger";
    private static Connection con;

    
    public static void main(String[] args) throws Exception {
        // Load the JDBC driver and establish the connection
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(url, uname, password);
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = true;
        
        while (loggedIn) {
            // Display main menu options
            System.out.println("\nWelcome to Saravana Restaurant");
            System.out.println("1. Book a Room");
            System.out.println("2. View My Bookings");
            System.out.println("3. Get Room Number");
            System.out.println("4. Update User Details");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Logout\n");
            System.out.print("Select an option: ");
            int select = scanner.nextInt();

            // Handle menu selection with switch-case
            switch(select) {
                case 1:
                    bookRoom(con,scanner); 
                    break;
                case 2:
                    viewBookings(con,scanner); 
                    break;
                case 3:
                    getRoomNumber(con,scanner); 
                    break;
                case 4:
                    updateDetails(con,scanner); 
                    break;
                case 5:
                    cancellation(con,scanner); 
                    break;
                case 6:
                    logout(loggedIn); 
                    scanner.close();
                    con.close();
                    return;
                default:
                    System.out.println("Hey,Select only valid Options. Please,Try again....");
            }
        }
    }

    // Method to book a room for a user
    static void bookRoom(Connection con, Scanner scanner) throws Exception {
        
        displayAvailableRooms(con);// Display available rooms before proceeding with booking
        scanner.nextLine();
        System.out.print("Enter User Name: ");
        String user = scanner.nextLine();

        // Check if user has reached booking limit
        if (getBookingCountForUser(con, user) >= 3) { 
            System.out.println("You have reached the maximum booking limit of 3 rooms.");
            return;
        }

        System.out.print("Enter Room Number: ");
        int room_no = scanner.nextInt();
        scanner.nextLine();

        // Check if selected room is available
        if (!isRoomAvailable(con, room_no)) {
            System.out.println("The selected room is not available. Please choose another room.");
            return;
        }

        System.out.print("Enter your Contact Number: ");
        String contact = scanner.next();

        // SQL query to insert new booking
        final String QUERY = "INSERT INTO Bookings (Guest_name, room_no, contact_no) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(QUERY)) {
            pstmt.setString(1, user);
            pstmt.setInt(2, room_no);
            pstmt.setString(3, contact);
            int rowsAffected = pstmt.executeUpdate();

            // Confirm successful booking and update room status
            if (rowsAffected > 0) {
                System.out.println("Booking successful.");
                final String UPDATE_ROOMS = "UPDATE Rooms SET status = 'booked' WHERE room_no = ?";
                try (PreparedStatement roomStmt = con.prepareStatement(UPDATE_ROOMS)) {
                    roomStmt.setInt(1, room_no);
                    roomStmt.executeUpdate();
                }
            } else {
                System.out.println("Booking failed. Please try again.");
            }
        }
    }

    // Method to display bookings of a user
    static void viewBookings(Connection con, Scanner scanner) throws Exception {
        System.out.print("Enter User Name to view bookings: ");
        scanner.nextLine(); 
        String user = scanner.nextLine();

        // SQL query to retrieve bookings for a specific user
        String selectQuery = "SELECT * FROM Bookings WHERE Guest_name = ?";
        try (PreparedStatement pstmt = con.prepareStatement(selectQuery)) {
            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();

            // Display bookings in a formatted table
            System.out.println("+--------------+----------------------+-------------+--------------+------------------------+");
            System.out.println("|  Booking ID  |      User Name       | Room Number |   Contact No |        Booking Date    |");
            System.out.println("+--------------+----------------------+-------------+--------------+------------------------+");

            while (rs.next()) {
                System.out.printf("| %-12d | %-20s | %-11d | %-12s | %-22s |%n",
                        rs.getInt("Booking_Id"),
                        rs.getString("Guest_name"),
                        rs.getInt("room_no"),
                        rs.getString("contact_no"),
                        rs.getTimestamp("Booking_date").toString());
            }
            System.out.println("+--------------+----------------------+-------------+--------------+------------------------+");
        }
    }

    // Method to get room number for a specific booking by booking ID and user name
    static void getRoomNumber(Connection con, Scanner scanner) throws Exception {
        System.out.print("Enter Booking ID: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); 
        System.out.print("Enter User Name: ");
        String name = scanner.nextLine();

        // SQL query to retrieve room number for a booking
        final String SQL = "SELECT Room_No FROM Bookings WHERE Booking_Id = ? AND Guest_name = ?";
        try (PreparedStatement pstmt = con.prepareStatement(SQL)) {
            pstmt.setInt(1, bookId);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Room Number for Booking ID " + bookId + " and User Name " + name + " is: " + rs.getInt("Room_No"));
            } else {
                System.out.println("No bookings found for the given Booking ID and User Name.");
            }
        }
    }

    // Method to update booking details
    static void updateDetails(Connection con, Scanner scanner) throws Exception {
        System.out.print("Enter Booking ID: ");
        int bookId = scanner.nextInt();

        // Check if booking exists before updating
        if (!bookingExists(con, bookId)) {
            System.out.println("No bookings found for the given Booking ID.");
            return;
        }

        System.out.print("Enter New Room Number: ");
        int newRoomNo = scanner.nextInt();
        scanner.nextLine(); 
        System.out.print("Enter New User Name: ");
        String newUserName = scanner.nextLine();
        System.out.print("Enter New Contact Number: ");
        String newContact = scanner.next();

        // SQL query to update booking details
        final String SQL = "UPDATE Bookings SET guest_name = ?, room_no = ?, contact_no = ? WHERE Booking_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(SQL)) {
            pstmt.setString(1, newUserName);
            pstmt.setInt(2, newRoomNo);
            pstmt.setString(3, newContact);
            pstmt.setInt(4, bookId);
            int rowsUpdated = pstmt.executeUpdate();

            System.out.println(rowsUpdated > 0 ? "Details updated successfully." : "Update failed.");
        }
    }

    // Method to cancel a booking by booking ID
    static void cancellation(Connection con, Scanner scanner) throws Exception {
        System.out.print("Enter your Booking ID to cancel: ");
        int bookId = scanner.nextInt();

        // Check if booking exists before cancellation
        if (!bookingExists(con, bookId)) {
            System.out.println("No bookings found for the given Booking ID.");
            return;
        }

        // Retrieve room number of booking before deletion
        final String ROOM_QUERY = "SELECT room_no FROM Bookings WHERE Booking_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(ROOM_QUERY)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int roomNo = rs.getInt("room_no");

                // Delete booking from Bookings table
                final String DELETE_QUERY = "DELETE FROM Bookings WHERE Booking_id = ?";
                try (PreparedStatement deleteStmt = con.prepareStatement(DELETE_QUERY)) {
                    deleteStmt.setInt(1, bookId);
                    deleteStmt.executeUpdate();

                    // Update room status to available after cancellation
                    final String UPDATE_ROOM_STATUS = "UPDATE Rooms SET status = 'available' WHERE room_no = ?";
                    try (PreparedStatement updateStmt = con.prepareStatement(UPDATE_ROOM_STATUS)) {
                        updateStmt.setInt(1, roomNo);
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Cancellation completed successfully.");
                }
            }
        }
    }

    // Helper method to check if a booking exists
    static boolean bookingExists(Connection con, int bookId) throws Exception {
        final String SQL = "SELECT Booking_Id FROM Bookings WHERE Booking_Id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(SQL)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    // Helper method to get booking count for a specific user
    static int getBookingCountForUser(Connection con, String user) throws Exception {
        final String COUNT_QUERY = "SELECT COUNT(*) FROM Bookings WHERE guest_name = ?";
        try (PreparedStatement pstmt = con.prepareStatement(COUNT_QUERY)) {
            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Helper method to check if a room is available
    static boolean isRoomAvailable(Connection con, int room_no) throws Exception {
        final String ROOM_CHECK_QUERY = "SELECT * FROM Rooms WHERE room_no = ? AND status = 'available'";
        try (PreparedStatement pstmt = con.prepareStatement(ROOM_CHECK_QUERY)) {
            pstmt.setInt(1, room_no);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    // Method to display all available rooms
    static void displayAvailableRooms(Connection con) throws Exception {
        final String QUERY = "SELECT room_no FROM Rooms WHERE status = 'available'";
        try (PreparedStatement pstmt = con.prepareStatement(QUERY)) {
            ResultSet rs = pstmt.executeQuery();

            System.out.println("+----------------+");
            System.out.println("| Available Room |");
            System.out.println("+----------------+");

            while (rs.next()) {
                System.out.printf("| %-14d |\n", rs.getInt("room_no"));
            }

            System.out.println("+----------------+");
        }
    }

    // Method to simulate a logout process
    static void logout(boolean loggedIn) throws Exception {
    	loggedIn = false;
        System.out.println("Logging out...");
        for (int i = 0; i < 4; i++) {
            System.out.print(".");
            Thread.sleep(300);
        }
        System.out.println("\nThank you for using the Hotel Booking System.");
    }
}
