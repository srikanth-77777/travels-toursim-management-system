package com.example.spring_boot_app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.text.ParseException;

@RestController
public class GoogleSheetsController {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    public static class SaveTransportRequest {
        private String transportName;
        private String transportType;
        private String from;
        private String to;
        private String startTime;
        private String endTime;
        private String lastDate;
        private boolean repeatTrip;
        private String repeatEnd;
        private String totalPassengers;
        private String amount;
        private String businessID;

        // Getters and setters for the above fields
        public String getTransportName() {
            return transportName;
        }

        public void setTransportName(String transportName) {
            this.transportName = transportName;
        }

        public String getTransportType() {
            return transportType;
        }

        public void setTransportType(String transportType) {
            this.transportType = transportType;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getLastDate() {
            return lastDate;
        }

        public void setLastDate(String lastDate) {
            this.lastDate = lastDate;
        }

        public boolean isRepeatTrip() {
            return repeatTrip;
        }

        public void setRepeatTrip(boolean repeatTrip) {
            this.repeatTrip = repeatTrip;
        }

        public String getRepeatEnd() {
            return repeatEnd;
        }

        public void setRepeatEnd(String repeatEnd) {
            this.repeatEnd = repeatEnd;
        }

        public String getTotalPassengers() {
            return totalPassengers;
        }

        public void setTotalPassengers(String totalPassengers) {
            this.totalPassengers = totalPassengers;
        }

        public String getbusinessID() {
            return businessID;
        }

        public void setTotalbusinessID(String businessID) {
            this.businessID = businessID;
        }

        public String getAmount() {
            return amount;
        }

        public void setTotalamount(String amount) {
            this.amount = amount;
        }
    }

    // Consolidating request classes to reduce redundancy
    static class UserRequest {
        private String email_address;
        private String password;
        private String name;
        private String type;
        private String businessName;
        private String phoneNumber;

        // Getters and Setters (using Lombok could simplify even further if needed)
        public String getEmail_address() {
            return email_address;
        }

        public void setEmail_address(String email_address) {
            this.email_address = email_address;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBusinessName() {
            return businessName;
        }

        public void setBusinessName(String businessName) {
            this.businessName = businessName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class BookingRequest {
        private String transportID;
        private String passengerID;
        private String totalPassengers;

        // Getters and setters
        public String getTransportID() {
            return transportID;
        }

        public void setTransportID(String transportID) {
            this.transportID = transportID;
        }

        public String getPassengerID() {
            return passengerID;
        }

        public void setPassengerID(String passengerID) {
            this.passengerID = passengerID;
        }

        public String getTotalPassengers() {
            return totalPassengers;
        }

        public void setTotalPassengers(String totalPassengers) {
            this.totalPassengers = totalPassengers;
        }
    }

    // Generalized method for creating response
    private ResponseEntity<Map<String, String>> createResponse(String message, HttpStatus status, String key,
            String value) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        if (key != null && value != null) {
            response.put(key, value);
        }
        return new ResponseEntity<>(response, status);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserRequest loginRequest) {
        String emailAddress = loginRequest.getEmail_address();
        String password = loginRequest.getPassword();
        String type = loginRequest.getType();

        String range = "Users!A2:G";
        List<List<Object>> data = googleSheetsService.readSheetData(range);

        for (List<Object> row : data) {
            if (row.size() >= 4 && row.get(1).toString().equals(emailAddress)
                    && row.get(3).toString().equals(password) && row.get(4).toString().equals(type)) {
                return createResponse("Login successful", HttpStatus.OK, "id", row.get(0).toString());
            }
        }
        return createResponse("Invalid email or password", HttpStatus.UNAUTHORIZED, null, null);
    }

    // Signup endpoint
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody UserRequest signupRequest) {
        String emailAddress = signupRequest.getEmail_address();
        String range = "Users!A2:G";
        List<List<Object>> data = googleSheetsService.readSheetData(range);

        for (List<Object> row : data) {
            if (row.size() >= 2 && row.get(1).toString().equals(emailAddress)) {
                return createResponse("Email already exists. Please log in.", HttpStatus.CONFLICT, null, null);
            }
        }

        String id = UUID.randomUUID().toString();
        List<Object> newUser = Arrays.asList(id, signupRequest.getEmail_address(), signupRequest.getName(),
                signupRequest.getPassword(),
                signupRequest.getType(), Optional.ofNullable(signupRequest.getBusinessName()).orElse(""),
                Optional.ofNullable(signupRequest.getPhoneNumber()).orElse(""));

        googleSheetsService.appendRow("Users!A:G", newUser);
        return createResponse("Signup successful", HttpStatus.CREATED, "id", id);
    }

    // Save transport details
    @PostMapping("/saveTransport")
    public ResponseEntity<Map<String, String>> saveTransport(@RequestBody SaveTransportRequest saveTransportRequest) {
        List<Object> transportData = Arrays.asList(
                saveTransportRequest.getbusinessID(), UUID.randomUUID().toString(),
                saveTransportRequest.getTransportType(), saveTransportRequest.getTransportName(),
                saveTransportRequest.getTotalPassengers(), saveTransportRequest.getAmount(),
                saveTransportRequest.getFrom(), saveTransportRequest.getTo(),
                saveTransportRequest.getStartTime(), saveTransportRequest.getEndTime(),
                saveTransportRequest.getLastDate(),
                saveTransportRequest.getRepeatEnd(), saveTransportRequest.isRepeatTrip());

        googleSheetsService.appendRow("Transports!A2:L", transportData);
        return createResponse("Transport details saved successfully.", HttpStatus.CREATED, "transportId",
                transportData.get(0).toString());
    }

    // Booking endpoint to append a new booking to the Bookings sheet
    @PostMapping("/book")
    public ResponseEntity<Map<String, String>> book(@RequestBody BookingRequest bookingRequest) {
        // Prepare the row data to append
        List<Object> bookingData = Arrays.asList(
                bookingRequest.getTransportID(),
                bookingRequest.getPassengerID(),
                bookingRequest.getTotalPassengers());

        // Append the row to the Bookings sheet
        googleSheetsService.appendRow("Bookings!A:C", bookingData);

        // Return success response
        return createResponse("Booking successful", HttpStatus.CREATED, "bookingId", UUID.randomUUID().toString());
    }

    @PostMapping("/search")
    public ResponseEntity<List<List<Object>>> searchTransports(@RequestBody Map<String, Object> searchParams) {
        String from = (String) searchParams.get("fromCity");
        String to = (String) searchParams.get("toCity");
        String date = (String) searchParams.get("date");
        int guests = (int) searchParams.get("guests");
        String type = (String) searchParams.get("type");

        // Assuming the transport sheet data is retrieved from Google Sheets service
        String range = "Transports!A2:N"; // Update the range if necessary
        List<List<Object>> data = googleSheetsService.readSheetData(range);

        List<List<Object>> filteredResults = new ArrayList<>();

        for (List<Object> row : data) {
            String rowFrom = row.get(6).toString().trim(); // "from" column
            String rowTo = row.get(7).toString().trim(); // "to" column
            String rowAvailableSeatsStr = row.get(13).toString().trim(); // "Available Seats" column
            String rowStartTime = row.get(8).toString().trim(); // "startTime" column
            String rowEndTime = row.get(9).toString().trim(); // "endTime" column
            String lastDate = row.get(10).toString().trim(); // "lastDate" column
            String repeatEnd = row.get(11).toString().trim(); // "repeatEnd" column
            String rowtype = row.get(2).toString().trim(); // "repeatEnd" column
            DateTimeFormatter rowFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDate dateFromRowStartTime = LocalDate.parse(rowStartTime, rowFormatter);

            // Parse date with ISO format and extract the date part
            LocalDate dateFromDate = LocalDate.parse(date.substring(0, 10));
            // Check the matching conditions
            boolean isFromMatch = rowFrom.equalsIgnoreCase(from.trim());
            boolean isToMatch = rowTo.equalsIgnoreCase(to.trim());
            boolean istypeMatch = rowtype.equalsIgnoreCase(type.trim());

            // Available seats should be empty or greater than 0
            boolean isAvailableSeatsMatch = rowAvailableSeatsStr.isEmpty()
                    || Integer.parseInt(rowAvailableSeatsStr) >= guests;

            // Check if the date is within the start and end time range
            boolean isDateInRange = isDateInRange(date, rowStartTime, rowEndTime);

            // Check the lastDate condition: If lastDate exists, ensure the date is less
            // than or equal to lastDate
            boolean isLastDateMatch = (lastDate.isEmpty() || date.compareTo(lastDate) <= 0);

            // Check if repeatEnd is "noEnd", meaning it applies to all dates
            boolean isRepeatEndMatch = repeatEnd.equalsIgnoreCase("noEnd");
            System.out.println(dateFromRowStartTime);
            System.out.println(dateFromDate);
            System.out.println(dateFromRowStartTime.equals(dateFromDate));

            // Combine all conditions
            if (istypeMatch && isFromMatch && isToMatch && isAvailableSeatsMatch &&
                    (dateFromRowStartTime.equals(dateFromDate) || isDateInRange || isLastDateMatch || isRepeatEndMatch)) {
                filteredResults.add(row);
            }
        }

        return new ResponseEntity<>(filteredResults, HttpStatus.OK);
    }

    // Helper function to check if a date is within the start and end time range
    private boolean isDateInRange(String searchDate, String startTime, String endTime) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // or your preferred date format
            Date searchDateObj = dateFormat.parse(searchDate);
            Date startDateObj = dateFormat.parse(startTime);
            Date endDateObj = dateFormat.parse(endTime);

            return (searchDateObj.equals(startDateObj) || searchDateObj.after(startDateObj)) &&
                    (searchDateObj.equals(endDateObj) || searchDateObj.before(endDateObj));
        } catch (ParseException e) {
            // Handle the exception appropriately
            e.printStackTrace();
        }
        return false;
    }

}
