package com.project.two;
import java.io.StringReader;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.util.List;

import javax.print.attribute.standard.Media;

import com.project.two.business.BusinessLayer;

import companydata.*;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


// import com.project.two.model.Department;
/**
 * Root resource (exposed at "myresource" path)
 */
@Path("CompanyServices")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * 
     */
//mvn clean
//mvn compile
//mvn package
//and to start wildfly, go to the bin folder and open standalone.bat
    
    @DELETE
    @Path("/company")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCompanyRecords(@QueryParam("company") String companyName) {
        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");

            
            int rowsDeleted = dl.deleteCompany(companyName);

            // Check if rows were deleted successfully
            if (rowsDeleted > 0) {
                // Success JSON response
                String successMessage = String.format("{\"success\":\"%s's information deleted.\"}", companyName);
                return Response.ok(successMessage, MediaType.APPLICATION_JSON).build();
            } else {
                // No rows deleted, could be because the company doesn't exist
                String errorMessage = String.format("{\"error\":\"No records found for company '%s'.\"}", companyName);
                return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
            }
        } catch (Exception e) {
            // Handle errors and return appropriate error message
            String errorMessage = String.format("{\"error\":\"An error occurred: %s\"}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } finally {
            // Ensure the DataLayer is properly closed
            if (dl != null) {
                dl.close();
            }
        }
    }

    
    
    @GET
    @Path("/department")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDepartment(@QueryParam("company") String companyName, @QueryParam("dept_id") int deptId) {
        // Validate inputs
        if (companyName == null || companyName.trim().isEmpty()) {
            String errorMessage = "{\"error\":\"Missing or invalid company name.\"}";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        if (deptId <= 0) {
            String errorMessage = "{\"error\":\"Invalid department ID.\"}";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }

        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");  
            
            companydata.Department dep = dl.getDepartment(companyName, deptId);
            
            if (dep == null) {
               
                String errorMessage = String.format(
                    "{\"error\":\"Department not found for company '%s' and ID %d.\"}",
                    companyName, deptId
                );
                return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
            }

          
            if (dep.getDeptName() == null || dep.getDeptNo() == null || dep.getLocation() == null) {
                String errorMessage = "{\"error\":\"Invalid department data: some required fields are null.\"}";
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
            }

            // If all checks pass, construct and return the JSON response
            String departmentJson = String.format(
                "{\"sucess\":\"dept_id\":%d, \"company\":\"%s\", \"dept_name\":\"%s\", \"dept_no\":\"%s\", \"location\":\"%s\"}",
                dep.getId(), dep.getCompany(), dep.getDeptName(), dep.getDeptNo(), dep.getLocation()
            );
            return Response.ok(departmentJson, MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
           
            e.printStackTrace();
            
            String errorMessage = String.format(
                "{\"error\":\"Error retrieving department for company '%s' and ID %d: %s\"}",
                companyName, deptId, e.getMessage()
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    @GET
    @Path("/departments")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDepartments(@QueryParam("company") String companyName) {
       // Check for null or empty query parameter
        if (companyName == null || companyName.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid company name.\"}")
                    .build();
        }

        DataLayer dl = null;
        
            
            dl = new DataLayer("ceb1810");
            List<Department> departments = dl.getAllDepartment(companyName);

            try{
        
           
            // If  list is empty, return an error message
            if (departments == null || departments.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"No departments found for the specified company.\"}")
                        .build();
            }

            StringBuilder jsonResponse = new StringBuilder("{\"success\":[");

            for (int i = 0; i < departments.size(); i++) {
                Department dept = departments.get(i);
                jsonResponse.append("{")
                        .append("\"dept_id\":").append(dept.getId()).append(",")
                        .append("\"company\":\"").append(dept.getCompany()).append("\",")
                        .append("\"dept_name\":\"").append(dept.getDeptName()).append("\",")
                        .append("\"dept_no\":\"").append(dept.getDeptNo()).append("\",")
                        .append("\"location\":\"").append(dept.getLocation()).append("\"")
                        .append("}");
                
             
                if (i < departments.size() - 1) {
                    jsonResponse.append(",");
                }
            }


           jsonResponse.append("]}");


          
            return Response.status(Response.Status.OK)
                    .entity(jsonResponse.toString())
                    .build();

        }catch (Exception e) {
            // Return an error message if something goes wrong
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
          
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            
            }
        }    
    }



    @PUT
    @Path("/department")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDepartment(String jsonData) {
        DataLayer dl = null;
    
        try {
            // Parse the JSON to get just the id and company
            JsonObject json = Json.createReader(new StringReader(jsonData)).readObject();
            int deptId = json.getInt("dept_id");
            String company = json.getString("company");
    
          
            dl = new DataLayer("ceb1810");
            
            // Get existing department
            Department dept = dl.getDepartment(company, deptId);
            if (dept == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Department not found\"}")
                        .build();
            }
    
            // Update department fields from JSON
            dept.setDeptName(json.getString("dept_name", dept.getDeptName()));
            dept.setDeptNo(json.getString("dept_no", dept.getDeptNo()));
            dept.setLocation(json.getString("location", dept.getLocation()));
    
            // Update the department
            Department result = dl.updateDepartment(dept);
            
            return Response.status(Response.Status.OK)
                    .entity(String.format(
                        "{\"dept_id\":%d,\"company\":\"%s\",\"dept_name\":\"%s\",\"dept_no\":\"%s\",\"location\":\"%s\"}",
                        result.getId(),
                        result.getCompany(),
                        result.getDeptName(),
                        result.getDeptNo(),
                        result.getLocation()
                    ))
                    .build();
    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    
    @POST
    @Path("/department")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response createDepartment(
            @FormParam("company") String company,
            @FormParam("dept_name") String deptName,
            @FormParam("dept_no") String deptNo,
            @FormParam("location") String location) {
        DataLayer dl = null;

        try {
           
            dl = new DataLayer("ceb1810");

            // Create a new department object
            Department newDepartment = new Department();
            newDepartment.setCompany(company);
            newDepartment.setDeptName(deptName);
            newDepartment.setDeptNo(deptNo);
            newDepartment.setLocation(location);

            // validation and insertion to the Business Layer
            Department insertedDept = BusinessLayer.createDepartment(dl, newDepartment);

            // Return success JSON
            String jsonResponse = "{"
                    + "\"success\":{"
                    + "\"dept_id\":" + insertedDept.getId() + ","
                    + "\"company\":\"" + insertedDept.getCompany() + "\","
                    + "\"dept_name\":\"" + insertedDept.getDeptName() + "\","
                    + "\"dept_no\":\"" + insertedDept.getDeptNo() + "\","
                    + "\"location\":\"" + insertedDept.getLocation() + "\""
                    + "}}";

            return Response.status(Response.Status.OK)
                    .entity(jsonResponse)
                    .build();

        } catch (Exception e) {
            // Return error JSON
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            // Close the DataLayer
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @DELETE
    @Path("/department")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDepartment(@QueryParam("company") String companyName, @QueryParam("dept_id") int deptId) {
        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");

            // Delete the company's information
            int rowsDeleted = dl.deleteDepartment(companyName,deptId);

            // Check if rows were deleted successfully
            if (rowsDeleted > 0) {
                // Success JSON response
                String successMessage = String.format("{\"success\":\"%s's departments deleted.\"}", companyName);
                return Response.ok(successMessage, MediaType.APPLICATION_JSON).build();
            } else {
                // No rows deleted, could be because the company doesn't exist
                String errorMessage = String.format("{\"error\":\"No records found for company '%s'.\"}", companyName);
                return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
            }
        } catch (Exception e) {
            // Handle errors and return appropriate error message
            String errorMessage = String.format("{\"error\":\"An error occurred: %s\"}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } finally {
            // Ensure the DataLayer is properly closed
            if (dl != null) {
                dl.close();
            }
        }
    }

    @GET
    @Path("/employee")
    @Produces("application/json")
    public Response getEmployee(
            @QueryParam("company") String company,
            @QueryParam("emp_id") int empId) {
        DataLayer dl = null;

        try {
       
            dl = new DataLayer("ceb1810");

            // Validate inputs
            if (company == null || company.trim().isEmpty()) {
                throw new Exception("Missing or invalid company name.");
            }
            if (empId <= 0) {
                throw new Exception("Invalid employee ID.");
            }

            // Retrieve the employee using the DataLayer
            Employee emp = dl.getEmployee(empId);

            // Check if the employee belongs to the requested company
            // if (!emp.get().equals(company)) {
            //     throw new Exception("Employee does not belong to the specified company.");
            // }

            // Build the success JSON response
            String jsonResponse = "{" 
                    + "\"success\":" + "{"
                    + "\"emp_id\":" + emp.getId() + ","
                    + "\"emp_name\":\"" + emp.getEmpName() + "\","
                    + "\"emp_no\":\"" + emp.getEmpNo() + "\","
                    + "\"hire_date\":\"" + emp.getHireDate() + "\","
                    + "\"job\":\"" + emp.getJob() + "\","
                    + "\"salary\":" + emp.getSalary() + ","
                    + "\"dept_id\":" + emp.getDeptId() + ","
                    + "\"mng_id\":" + emp.getMngId()
                    + "}";

            return Response.status(Response.Status.OK)
                    .entity(jsonResponse)
                    .build();

        } catch (Exception e) {
            // Return error JSON
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            // Close the DataLayer
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @GET
    @Path("/employees")
    @Produces("application/json")
    public Response getEmployees(@QueryParam("company") String company) {
        DataLayer dl = null;

        try {
          
            dl = new DataLayer("ceb1810");

            // Validate input
            if (company == null || company.trim().isEmpty()) {
                throw new Exception("Missing or invalid company name.");
            }

            // Retrieve the list of employees
            List<Employee> employees = dl.getAllEmployee(company);
            

            // Check if the list is empty
            if (employees == null || employees.isEmpty()) {
                throw new Exception("No employees found for the specified company.");
            }

            // Build the success JSON response
            StringBuilder jsonResponse = new StringBuilder("{\"success\":[");
            for (Employee emp : employees) {
                jsonResponse.append("{")
                        .append("\"emp_id\":").append(emp.getId()).append(",")
                        .append("\"emp_name\":\"").append(emp.getEmpName()).append("\",")
                        .append("\"emp_no\":\"").append(emp.getEmpNo()).append("\",")
                        .append("\"hire_date\":\"").append(emp.getHireDate()).append("\",")
                        .append("\"job\":\"").append(emp.getJob()).append("\",")
                        .append("\"salary\":").append(emp.getSalary()).append(",")
                        .append("\"dept_id\":").append(emp.getDeptId()).append(",")
                        .append("\"mng_id\":").append(emp.getMngId())
                        .append("},");
            }
            // Remove the trailing comma and close the JSON array
            jsonResponse.setLength(jsonResponse.length() - 1);
            jsonResponse.append("]");

            return Response.status(Response.Status.OK)
                    .entity(jsonResponse.toString())
                    .build();

        } catch (Exception e) {
            // Return error JSON
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            // Close the DataLayer
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @POST
    @Path("/employee")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response addEmployee(
            @FormParam("company") String company,
            @FormParam("emp_name") String empName,
            @FormParam("emp_no") String empNo,
            @FormParam("hire_date") Date hireDate,
            @FormParam("job") String job,
            @FormParam("salary") double salary,
            @FormParam("dept_id") int deptId,
            @FormParam("mng_id") int mngId) {

        DataLayer dl = null;

        try {
            dl = new DataLayer("ceb1810");
            BusinessLayer bl = new BusinessLayer();

            // Validation
            if (!company.equals("ceb1810")) {
                throw new Exception("Invalid company name.");
            }
            if (!bl.isDeptIdValid(deptId, company, dl)) {
                throw new Exception("Invalid department ID.");
            }
            if (!bl.isMngIdValid(mngId, company, dl)) {
                throw new Exception("Invalid manager ID.");
            }
            if (!bl.isHireDateValid(hireDate)) {
                throw new Exception("Invalid hire date. Must be a past or current weekday.");
            }

            // Fetch all employees and check if the empNo is unique
            List<Employee> allEmployees = dl.getAllEmployee(company);
            for (Employee existingEmployee : allEmployees) {
                if (existingEmployee.getEmpNo().equals(empNo)) {
                    throw new Exception("Employee number is not unique.");
                }
            }

            
            Employee employee = new Employee();
            employee.setEmpName(empName);
            employee.setEmpNo(empNo);
            employee.setHireDate(hireDate);
            employee.setJob(job);
            employee.setSalary(salary);
            employee.setDeptId(deptId);
            employee.setMngId(mngId);
        

            dl.insertEmployee(employee);
            

            String jsonResponse = "{" +
            "\"emp_id\":" + employee.getId() + "," +
            "\"emp_name\":\"" + employee.getEmpName() + "\"," +
            "\"emp_no\":\"" + employee.getEmpNo() + "\"," +
            "\"hire_date\":\"" + employee.getHireDate() + "\"," +
            "\"job\":\"" + employee.getJob() + "\"," +
            "\"salary\":" + employee.getSalary() + "," +
            "\"dept_id\":" + employee.getDeptId() + "," +
            "\"mng_id\":" + employee.getMngId() +
            "}";

            return Response.status(Response.Status.OK)
            .entity("{\"success\":" + jsonResponse + "}")
            .build();
    

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @PUT
    @Path("/employee")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEmployee(String jsonData){
        DataLayer dl = null;
        

        try {
            // Parse the JSON to extract input data
            JsonObject json = Json.createReader(new StringReader(jsonData)).readObject();
            int emp_id = json.getInt("emp_id");
            String company = json.getString("company");
    
           
            dl = new DataLayer("ceb1810");
    
         
            Employee emp = dl.getEmployee(emp_id);
            if (emp == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Employee not found\"}")
                        .build();
            }
    
            // Update fields based on input, retaining existing values for non-updated fields
            emp.setEmpName(json.getString("emp_name", emp.getEmpName()));
            emp.setEmpNo(json.getString("emp_no", emp.getEmpNo()));
            String hireDateString = json.getString("hire_date", emp.getHireDate().toString()); //casting to string then converting back to date
            Date hireDate = Date.valueOf(hireDateString); 
            emp.setHireDate(hireDate);
            emp.setJob(json.getString("job", emp.getJob()));
            emp.setSalary(json.getJsonNumber("salary").doubleValue());
            emp.setDeptId(json.getInt("dept_id", emp.getDeptId()));
            emp.setMngId(json.getInt("mng_id", emp.getMngId()));
    
            // Business layer validations
            BusinessLayer bl = new BusinessLayer();
            if (!bl.isDeptIdValid(emp.getDeptId(), company, dl)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid Department ID.\"}")
                        .build();
            }
            if (!bl.isMngIdValid(emp.getMngId(), company, dl)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid Manager ID.\"}")
                        .build();
            }
            if (!bl.isHireDateValid(emp.getHireDate())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid Hire Date. Must be a weekday and not in the future.\"}")
                        .build();
            }
    
            // Update employee in the database
            dl.updateEmployee(emp);
    
            // Return success JSON
            String jsonResponse = String.format(
                    "{\"success\":{"
                            + "\"emp_id\":%d,"
                            + "\"emp_name\":\"%s\","
                            + "\"emp_no\":\"%s\","
                            + "\"hire_date\":\"%s\","
                            + "\"job\":\"%s\","
                            + "\"salary\":%.2f,"
                            + "\"dept_id\":%d,"
                            + "\"mng_id\":%d"
                            + "}}",
                    emp.getId(),
                    emp.getEmpName(),
                    emp.getEmpNo(),
                    emp.getHireDate(),
                    emp.getJob(),
                    emp.getSalary(),
                    emp.getDeptId(),
                    emp.getMngId()
            );
    
            return Response.status(Response.Status.OK)
                    .entity(jsonResponse)
                    .build();
    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @DELETE
    @Path("/employee")
    @Produces("application/json")
    public Response deleteEmployee(@QueryParam("company") String companyName, @QueryParam("emp_id") int empId) {
        // Check for null or empty query parameters
        if (companyName == null || companyName.isEmpty() || empId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid company name or employee ID.\"}")
                    .build();
        }

        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");  

           
        int rowsDeleted =  dl.deleteEmployee(empId);  

            // If the employee was not deleted, return error
            if (rowsDeleted <= 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Employee not found or could not be deleted.\"}")
                        .build();
            }

            // Return success message
            return Response.status(Response.Status.OK)
                    .entity("{\"success\":\"Employee " + empId + " deleted.\"}")
                    .build();

        } catch (Exception e) {
           
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
           
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    @GET
    @Path("/timecard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimecard(@QueryParam("company") String companyName, @QueryParam("timecard_id") int timecardId) {
        // Check for null or empty query parameters
        if (companyName == null || companyName.isEmpty() || timecardId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid company name or employee ID.\"}")
                    .build();
        }

        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");  

            
             Timecard timecard  = dl.getTimecard( timecardId);  

            // If no timecards are found, return an error message
            if (timecard == null ) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"No timecards found for the specified employee.\"}")
                        .build();
            }

            // Build the JSON response for the list of timecards
            StringBuilder jsonResponse = new StringBuilder("");
            
                jsonResponse.append("{")
                        .append("\"timecard_id\":").append(timecard.getId()).append(",")
                        .append("\"start_time\":\"").append(timecard.getStartTime()).append("\",")
                        .append("\"end_time\":\"").append(timecard.getEndTime()).append("\",")
                        .append("\"emp_id\":").append(timecard.getEmpId())
                        .append("},");
            

            // Remove the trailing comma and close the JSON array
            if (jsonResponse.charAt(jsonResponse.length() - 1) == ',') {
                jsonResponse.setLength(jsonResponse.length() - 1);
            }
            jsonResponse.append("");

            // Return the response
            return Response.status(Response.Status.OK)
                    .entity(jsonResponse.toString())
                    .build();

        } catch (Exception e) {
            // Return an error message if something goes wrong
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            // Close the DataLayer connection
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @GET
    @Path("/timecards")
    @Produces("application/json")
    public Response getTimecards(@QueryParam("company") String companyName, @QueryParam("emp_id") int empId) {
        // Check for null or empty query parameters
        if (companyName == null || companyName.isEmpty() || empId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid company name or employee ID.\"}")
                    .build();
        }

        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");  

           
            List<Timecard> timecards = dl.getAllTimecard( empId);  

            // If no timecards are found, return an error message
            if (timecards == null || timecards.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"No timecards found for the specified employee.\"}")
                        .build();
            }

            // Build the JSON response for the list of timecards
            StringBuilder jsonResponse = new StringBuilder("[");
            for (Timecard timecard : timecards) {
                jsonResponse.append("{")
                        .append("\"timecard_id\":").append(timecard.getId()).append(",")
                        .append("\"start_time\":\"").append(timecard.getStartTime()).append("\",")
                        .append("\"end_time\":\"").append(timecard.getEndTime()).append("\",")
                        .append("\"emp_id\":").append(timecard.getEmpId())
                        .append("},");
            }

            // Remove the trailing comma and close the JSON array
            if (jsonResponse.charAt(jsonResponse.length() - 1) == ',') {
                jsonResponse.setLength(jsonResponse.length() - 1);
            }
            jsonResponse.append("]");

            // Return the response
            return Response.status(Response.Status.OK)
                    .entity(jsonResponse.toString())
                    .build();

        } catch (Exception e) {
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
          
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @POST
    @Path("/timecard")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTimecard(
            @FormParam("company") String company,
            @FormParam("emp_id") int empId,
            @FormParam("start_time") String startTime,
            @FormParam("end_time") String endTime) {

        DataLayer dl = null;

        try {
         
            dl = new DataLayer("ceb1810");

            // Business Layer Validations
            if (!BusinessLayer.isCompanyValid(company)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid company.\"}")
                        .build();
            }

            if (!BusinessLayer.isEmpIdValid(dl, empId, company)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid employee ID.\"}")
                        .build();
            }

            if (!BusinessLayer.areStartAndEndTimesValid(startTime, endTime)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid start or end time.\"}")
                        .build();
            }

            if (!BusinessLayer.isStartTimeUnique(dl, empId, startTime)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Start time is not unique for this employee.\"}")
                        .build();
            }

            // Create and Insert Timecard
            Timecard timecard = new Timecard();
            timecard.setEmpId(empId);
            timecard.setStartTime(Timestamp.valueOf(startTime));
            timecard.setEndTime(Timestamp.valueOf(endTime));

            Timecard insertedTimecard = dl.insertTimecard(timecard);

            // Success Response
            String jsonResponse = String.format(
                    "{\"success\":{"
                            + "\"timecard_id\":%d,"
                            + "\"start_time\":\"%s\","
                            + "\"end_time\":\"%s\","
                            + "\"emp_id\":%d"
                            + "}}",
                    insertedTimecard.getId(),
                    insertedTimecard.getStartTime(),
                    insertedTimecard.getEndTime(),
                    insertedTimecard.getEmpId()
            );

            return Response.status(Response.Status.OK)
                    .entity(jsonResponse)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @PUT
    @Path("/timecard")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTimecard(String jsonData) {
        DataLayer dl = null;

        try {
            // Parse the input JSON
            JsonObject json = Json.createReader(new StringReader(jsonData)).readObject();
            int timecardId = json.getInt("timecard_id");
            String company = json.getString("company");
            String startTime = json.getString("start_time");
            String endTime = json.getString("end_time");
            int empId = json.getInt("emp_id");

            // Initialize DataLayer
            dl = new DataLayer("ceb1810");

            // Fetch the existing timecard
            Timecard timecard = dl.getTimecard(timecardId);
            if (timecard == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Timecard not found.\"}")
                        .build();
            }

            // Business Layer Validations
            if (!BusinessLayer.isCompanyValid(company)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid company.\"}")
                        .build();
            }

            if (!BusinessLayer.isEmpIdValid(dl, empId, company)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid employee ID.\"}")
                        .build();
            }

            if (!BusinessLayer.areStartAndEndTimesValid(startTime, endTime)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid start or end time.\"}")
                        .build();
            }

            if (!BusinessLayer.isStartTimeUnique(dl, empId, startTime)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Start time is not unique for this employee.\"}")
                        .build();
            }

            // Update fields
            timecard.setEmpId(empId);
            timecard.setStartTime(Timestamp.valueOf(startTime));
            timecard.setEndTime(Timestamp.valueOf(endTime));

            // Update the timecard in the database
            dl.updateTimecard(timecard);

            // Success Response
            String jsonResponse = String.format(
                    "{\"success\":{"
                            + "\"timecard_id\":%d,"
                            + "\"start_time\":\"%s\","
                            + "\"end_time\":\"%s\","
                            + "\"emp_id\":%d"
                            + "}}",
                    timecard.getId(),
                    timecard.getStartTime(),
                    timecard.getEndTime(),
                    timecard.getEmpId()
            );

            return Response.status(Response.Status.OK)
                    .entity(jsonResponse)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    


    @DELETE
    @Path("/timecard")
    @Produces("application/json")
    public Response deleteTimcard(@QueryParam("company") String companyName, @QueryParam("timecard_id") int timecardId) {
        // Check for null or empty query parameters
        if (companyName == null || companyName.isEmpty() ) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid company name or employee ID.\"}")
                    .build();
        }

        DataLayer dl = null;
        try {
            dl = new DataLayer("ceb1810");  

           
        int rowsDeleted =  dl.deleteTimecard(timecardId);  

            // If the employee was not deleted, return error
            if (rowsDeleted <= 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Timecard not found or could not be deleted.\"}")
                        .build();
            }

            // Return success message
            return Response.status(Response.Status.OK)
                    .entity("{\"success\":\"Timecard " + timecardId + " deleted.\"}")
                    .build();

        } catch (Exception e) {
           
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        } finally {
           
            if (dl != null) {
                try {
                    dl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    

    //for testing if the project is accessible
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Ur trash lol";
    }

    public static void main(String[] args) throws Exception {
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        dl = new DataLayer("ceb1810");
        // Department dept = new Department("ceb1810","IT","d50","rochester");
        //  dept = dl.insertDepartment(dept);
        Timecard t1 = new Timecard();
        // t1.setEmpId(1);
        // t1.setEndTime();
        // t1.setStartTime();
        // t1.setId(0);
        // dl.insertTimecard(t1);
        // List<Employee> cardList = dl.getAllEmployee("ceb1810");

        
        
        // System.out.println(cardList);

        
    }



    
}
