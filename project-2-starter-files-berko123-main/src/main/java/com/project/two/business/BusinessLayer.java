package com.project.two.business;



import companydata.DataLayer;
import companydata.Department;
import companydata.Employee;
import companydata.Timecard;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BusinessLayer {

    // Validate if dept_no is unique across all companies
    public static boolean isDeptNoUnique(DataLayer dl, String deptNo, String company) throws Exception {
        List<Department> allDepartments = dl.getAllDepartment(company);

        for (Department dept : allDepartments) {
            if (dept.getDeptNo().equals(deptNo)) {
                return false;
            }
        }
        return true;
    }

    // Validate if dept_id exists in the database
    public static boolean doesDeptIdExist(DataLayer dl, int deptId, String company) throws Exception {
        try {
            Department dept = dl.getDepartment(company, deptId);
            return dept != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Update the department with additional validation
    public static Department updateDepartment(DataLayer dl, Department updatedDept) throws Exception {
        // Check if dept_no is unique

       
        if (!isDeptNoUnique(dl, updatedDept.getDeptNo(), updatedDept.getCompany())) {
            throw new Exception("Department number (dept_no) must be unique.");
        }

        // Check if dept_id exists
        if (!doesDeptIdExist(dl, updatedDept.getId(), updatedDept.getCompany())) {
            throw new Exception("Invalid Department ID. Department ID must exist.");
        }

        // If validation passes, update the department
        return dl.updateDepartment(updatedDept);
    }

    public static Department createDepartment(DataLayer dl, Department department) throws Exception {
        // Validation: Check if dept_no is unique across all companies
        List<Department> allDepartments = dl.getAllDepartment(department.getCompany());
        for (Department dep : allDepartments) {
            if (dep.getDeptNo().equals(department.getDeptNo())) {
                throw new Exception("Department number (dept_no) must be unique across all companies.");
            }
        }

        // Insert the new department
        return dl.insertDepartment(department);
    }

     

    public boolean isDeptIdValid(int deptId, String company, DataLayer dl) throws Exception {
        return dl.getDepartment(company, deptId) != null;
    }

    public boolean isMngIdValid(int mngId, String company, DataLayer dl) throws Exception {
        return mngId == 0 || dl.getEmployee( mngId) != null;
    }

    public boolean isHireDateValid(Date hireDate) throws Exception {
        if (hireDate == null) {
            throw new Exception("Hire date cannot be null.");
        }
    
        // Convert java.sql.Date to LocalDate
        LocalDate date = hireDate.toLocalDate();
    
        // Check if date is today or earlier and is a weekday
        DayOfWeek day = date.getDayOfWeek();
        return !date.isAfter(LocalDate.now()) &&
                day != DayOfWeek.SATURDAY &&
                day != DayOfWeek.SUNDAY;
    }

    public static boolean isCompanyValid(String company) throws Exception {
        return "ceb1810".equalsIgnoreCase(company); 
    }

    public static boolean isEmpIdValid(DataLayer dl, int empId, String company) throws Exception {
        Employee emp = dl.getEmployee(empId);
        return emp != null ;
    }

    public static boolean areStartAndEndTimesValid(String startTime, String endTime) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input strings
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        // Ensure start_time <= current date
        if (start.isAfter(LocalDateTime.now())) {
            throw new Exception("Start time cannot be in the future.");
        }

        // Ensure start_time is Monday or later in the current week
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        if (start.toLocalDate().isBefore(monday)) {
            throw new Exception("Start time must be from the Monday of the current week.");
        }

        // Ensure start_time and end_time are on the same day
        if (!start.toLocalDate().isEqual(end.toLocalDate())) {
            throw new Exception("Start and end times must be on the same day.");
        }

        // Ensure end_time is at least 1 hour after start_time
        if (Duration.between(start, end).toHours() < 1) {
            throw new Exception("End time must be at least 1 hour greater than start time.");
        }

        // Ensure times are within valid work hours (08:00 to 18:00)
        LocalTime startOfWork = LocalTime.of(8, 0);
        LocalTime endOfWork = LocalTime.of(18, 0);
        if (start.toLocalTime().isBefore(startOfWork) || end.toLocalTime().isAfter(endOfWork)) {
            throw new Exception("Start and end times must be between 08:00 and 18:00.");
        }

        // Ensure times are on a weekday
        DayOfWeek startDay = start.getDayOfWeek();
        DayOfWeek endDay = end.getDayOfWeek();
        if (startDay == DayOfWeek.SATURDAY || startDay == DayOfWeek.SUNDAY ||
            endDay == DayOfWeek.SATURDAY || endDay == DayOfWeek.SUNDAY) {
            throw new Exception("Start and end times cannot be on Saturday or Sunday.");
        }

        return true;
    }

    public static boolean isStartTimeUnique(DataLayer dl, int empId, String startTime) throws Exception {
        List<Timecard> timecards = dl.getAllTimecard(empId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate startDate = LocalDateTime.parse(startTime, formatter).toLocalDate();
    
        for (Timecard tc : timecards) {
            // Convert Timestamp to LocalDateTime and then to LocalDate
            LocalDate existingStartDate = tc.getStartTime().toLocalDateTime().toLocalDate();
            if (startDate.isEqual(existingStartDate)) {
                throw new Exception("Start time must not be on the same day as any other timecard for this employee.");
            }
        }
        return true;
    }
    
    

        
}

    


    
    

  



   


