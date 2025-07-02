// package customer.javax;

// import cds.gen.employeeservice.*;
// import com.sap.cds.services.handler.annotations.ServiceName;
// import com.sap.cds.services.handler.annotations.On;
// import com.sap.cds.services.handler.annotations.Before;
// import com.sap.cds.services.handler.EventHandler;
// import com.sap.cds.services.persistence.PersistenceService;
// import com.sap.cds.services.cds.CdsUpdateEventContext;
// import com.sap.cds.services.cds.CdsCreateEventContext;
// import com.sap.cds.services.cds.CdsDeleteEventContext;
// // import com.sap.cds.services.cds.CdsService;
// import com.sap.cds.services.request.UserInfo;
// import com.sap.cds.Result;
// import com.sap.cds.Row;
// import org.springframework.stereotype.Component;

// import java.time.LocalDate;
// import java.time.temporal.ChronoUnit;
// import java.util.Map;
// import java.util.List;
// import java.util.Optional;

// @Component
// @ServiceName("EmployeeService")
// public class EmployeeServiceHandler implements EventHandler {

// private final PersistenceService db;
// private final UserInfo user;

// public EmployeeServiceHandler(PersistenceService db, UserInfo user) {
// this.db = db;
// this.user = user;
// }

// @On(event = "userInfo")
// public void userInfo(UserInfoContext ctx){
// ctx.setResult("213");
// }

// // // Action: updateLeaveStatus
// // @On(event = "updateLeaveStatus")
// // public String updateLeaveStatus(Map<String, Object> data) {
// // String leaveID = (String) data.get("leaveID");
// // String status = (String) data.get("status");

// // if (!List.of("Pending", "Approved", "Rejected").contains(status)) {
// // throw new RuntimeException("Invalid status value");
// // }

// // int updated = db.run(UpdateBuilder.update("EmployeeService.LeaveRequests")
// // .set("status", status)
// // .where("ID", leaveID));

// // if (updated == 0) {
// // throw new RuntimeException("Leave request not found");
// // }

// // return "Status updated successfully";
// // }

// // // Action: calculateSalary
// // @On(event = "calculateSalary")
// // public Double calculateSalary(Map<String, Object> data) {
// // String employeeID = (String) data.get("employeeID");

// // Result result = db.run(Select.from("EmployeeService.Employees")
// // .columns("*", "role.*")
// // .where("ID", employeeID));

// // Optional<Row> employeeOpt = result.first();
// // if (employeeOpt.isEmpty()) {
// // throw new RuntimeException("Employee not found");
// // }

// // Row employee = employeeOpt.get();
// // LocalDate hireDate = employee.get("hireDate", LocalDate.class);
// // int years = (int) ChronoUnit.YEARS.between(hireDate, LocalDate.now());

// // double base = employee.get("role.baseSalary", Double.class);
// // double allowance = employee.get("role.allowance", Double.class);
// // double perfBonus = (employee.get("performanceRating", Double.class) !=
// null)
// // ? employee.get("performanceRating", Double.class) * 500
// // : 500;
// // double serviceBonus = years * 1000;

// // double total = base + allowance + perfBonus + serviceBonus;
// // double rounded = Math.round(total * 100.0) / 100.0;

// // db.run(UpdateBuilder.update("EmployeeService.Employees")
// // .set("salary", rounded)
// // .where("ID", employeeID));

// // return rounded;
// // }

// // // Function: userInfo
// // @On(event = "userInfo")
// // public Map<String, Object> userInfoHandler() {
// // return Map.of(
// // "id", user.getName(),
// // "roles", user.getAttributes().getOrDefault("roles", List.of()));
// // }

// // // Role restriction
// // @Before(event = { CdsService.EVENT_CREATE, CdsService.EVENT_UPDATE,
// // CdsService.EVENT_DELETE }, entity = {
// // "EmployeeService.Employees", "EmployeeService.LeaveRequests" })
// // public void restrictToAdmin(CdsService event) {
// // if (!user.isInRole("Admin")) {
// // throw new RuntimeException("Admin role required for this operation");
// // }
// // }

// // @Before(event = CdsService.EVENT_UPDATE, entity =
// // "EmployeeService.LeaveRequests")
// // public void restrictLeaveUpdate(CdsUpdateEventContext ctx) {
// // if (!user.isInRole("Admin")) {
// // throw new RuntimeException("Only Admins can approve/reject leave
// requests");
// // }
// // }
// }
