// package customer.javax.handlers;

// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.time.LocalDate;
// import java.time.temporal.ChronoUnit;
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.Map;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// import com.sap.cds.services.EventContext;
// import com.sap.cds.services.cds.CqnService;
// import com.sap.cds.services.handler.EventHandler;
// import com.sap.cds.services.handler.annotations.Before;
// import com.sap.cds.services.handler.annotations.On;
// import com.sap.cds.services.handler.annotations.ServiceName;
// import com.sap.cds.services.request.UserInfo;
// import com.sap.cds.services.cds.CdsCreateEventContext;
// import com.sap.cds.services.cds.CdsUpdateEventContext;
// import com.sap.cds.services.cds.CdsUpsertEventContext;
// import com.sap.cds.services.persistence.PersistenceService;
// import com.sap.cds.services.ErrorStatuses;
// import com.sap.cds.services.ServiceException;
// import com.sap.cds.ql.Select;
// import com.sap.cds.ql.Update;
// import com.sap.cds.ql.cqn.CqnValue;

// import cds.gen.employeeservice.EmployeeService_;
// import cds.gen.employeeservice.Employees_;
// import cds.gen.employeeservice.Roles_;
// import cds.gen.employeeservice.LeaveRequests_;
// import cds.gen.employeeservice.CalculateSalaryContext;
// import cds.gen.employeeservice.UpdateLeaveStatusContext;
// import cds.gen.employeeservice.UserInfoContext;

// @Component
// @ServiceName(EmployeeService_.CDS_NAME)
// public class EmployeeServiceHandler implements EventHandler {

// @Autowired
// private UserInfo userInfo;

// @Autowired
// private PersistenceService persistenceService;

// private static final int SERVICE_BONUS_PER_YEAR = 1000;
// private static final int PERFORMANCE_BONUS_MULTIPLIER = 500;
// private static final Logger logger =
// LoggerFactory.getLogger(EmployeeServiceHandler.class);

// @Before(event = { CqnService.EVENT_CREATE, CqnService.EVENT_UPSERT,
// CqnService.EVENT_UPDATE }, entity = Employees_.CDS_NAME)
// public void autoCalculateSalary(EventContext ctx) {
// logger.info("Auto calculating salary for employee operations");
// if (ctx instanceof CdsCreateEventContext) {
// ((CdsCreateEventContext)
// ctx).getCqn().entries().forEach(this::calculateEmployeeSalary);
// } else if (ctx instanceof CdsUpdateEventContext) {
// ((CdsUpdateEventContext)
// ctx).getCqn().entries().forEach(this::calculateEmployeeSalary);
// } else if (ctx instanceof CdsUpsertEventContext) {
// ((CdsUpsertEventContext)
// ctx).getCqn().entries().forEach(this::calculateEmployeeSalary);
// }
// }

// @On(event = CalculateSalaryContext.CDS_NAME)
// public void calculateSalary(CalculateSalaryContext context) {
// String employeeID = context.getEmployeeID();
// if (employeeID == null || employeeID.isEmpty()) {
// throw new ServiceException(ErrorStatuses.BAD_REQUEST, "Employee ID is
// required");
// }

// try {
// var employeeResult = persistenceService.run(
// Select.from(Employees_.CDS_NAME)
// .columns(e -> e._all(), e -> e.role()._all())
// .where(e -> e.get("ID").eq(employeeID)));

// if (!employeeResult.first().isPresent()) {
// throw new ServiceException(ErrorStatuses.NOT_FOUND, "Employee not found");
// }

// var employee = employeeResult.first().get();
// BigDecimal totalSalary = calculateTotalSalary(employee);

// persistenceService.run(
// Update.entity(Employees_.CDS_NAME)
// .set("salary", CqnValue.of(totalSalary))
// .where(e -> e.get("ID").eq(employeeID)));

// context.setResult(totalSalary);
// logger.info("Calculated salary for employee {}: {}", employeeID,
// totalSalary);

// } catch (ServiceException e) {
// throw e;
// } catch (Exception e) {
// logger.error("Error calculating salary for employee: {}", employeeID, e);
// throw new ServiceException(ErrorStatuses.SERVER_ERROR, "Error calculating
// salary: " + e.getMessage());
// }
// }

// @On(event = UpdateLeaveStatusContext.CDS_NAME)
// public void updateLeaveStatus(UpdateLeaveStatusContext context) {
// String leaveID = context.getLeaveID();
// String status = context.getStatus();

// if (leaveID == null || leaveID.isEmpty()) {
// throw new ServiceException(ErrorStatuses.BAD_REQUEST, "Leave ID is
// required");
// }

// String[] validStatuses = { "Pending", "Approved", "Rejected" };
// boolean isValidStatus = false;
// for (String validStatus : validStatuses) {
// if (validStatus.equals(status)) {
// isValidStatus = true;
// break;
// }
// }

// if (!isValidStatus) {
// throw new ServiceException(ErrorStatuses.BAD_REQUEST,
// "Invalid status value. Must be: Pending, Approved, or Rejected");
// }

// try {
// var updateResult = persistenceService.run(
// Update.entity(LeaveRequests_.CDS_NAME)
// .set("status", CqnValue.of(status))
// .where(lr -> lr.get("ID").eq(leaveID)));

// // Kiểm tra có update được không
// var checkResult = persistenceService.run(
// Select.from(LeaveRequests_.CDS_NAME)
// .where(lr -> lr.get("ID").eq(leaveID)));

// if (!checkResult.first().isPresent()) {
// throw new ServiceException(ErrorStatuses.NOT_FOUND, "Leave request not
// found");
// }

// context.setResult("Status updated successfully");
// logger.info("Updated leave request {} status to: {}", leaveID, status);

// } catch (ServiceException e) {
// throw e;
// } catch (Exception e) {
// logger.error("Error updating leave status for ID: {}", leaveID, e);
// throw new ServiceException(ErrorStatuses.SERVER_ERROR, "Error updating leave
// status: " + e.getMessage());
// }
// }

// @On(event = UserInfoContext.CDS_NAME)
// public void getUserInfo(UserInfoContext context) {
// try {
// String userId = userInfo.getName();
// Boolean authenticated = userInfo.isAuthenticated();
// Collection<String> roles = userInfo.getRoles();

// Map<String, Object> userInfoMap = new HashMap<>();
// userInfoMap.put("id", userId != null ? userId : "unknown");
// userInfoMap.put("authenticated", authenticated != null ? authenticated :
// false);
// userInfoMap.put("roles", roles);

// context.setResult(userInfoMap);
// logger.info("Retrieved user info for: {}", userId);

// } catch (Exception e) {
// logger.error("Error getting user info", e);
// throw new ServiceException(ErrorStatuses.SERVER_ERROR, "Error retrieving user
// information");
// }
// }

// @Before(event = { CqnService.EVENT_CREATE, CqnService.EVENT_UPDATE,
// CqnService.EVENT_DELETE }, entity = {
// Employees_.CDS_NAME, LeaveRequests_.CDS_NAME })
// public void checkAdminRole(EventContext ctx) {
// if (!userInfo.hasRole("Admin")) {
// logger.warn("Non-admin user {} attempted restricted operation",
// userInfo.getName());
// throw new ServiceException(ErrorStatuses.FORBIDDEN, "Admin role required for
// this operation");
// }
// }

// @Before(event = CqnService.EVENT_UPDATE, entity = LeaveRequests_.CDS_NAME)
// public void checkLeaveUpdatePermission(EventContext ctx) {
// if (!userInfo.hasRole("Admin")) {
// logger.warn("Non-admin user {} attempted to update leave request",
// userInfo.getName());
// throw new ServiceException(ErrorStatuses.FORBIDDEN, "Only Admins can
// approve/reject leave requests");
// }
// }

// private void calculateEmployeeSalary(Map<String, Object> entry) {
// Object hireDateObj = entry.get("hireDate");
// Object roleIdObj = entry.get("role_ID");
// Object performanceRatingObj = entry.get("performanceRating");

// if (hireDateObj != null && roleIdObj != null) {
// try {
// LocalDate hireDate = LocalDate.parse(hireDateObj.toString());
// String roleId = roleIdObj.toString();
// BigDecimal totalSalary = calculateSalaryForEmployee(hireDate, roleId,
// performanceRatingObj);
// if (totalSalary != null) {
// entry.put("salary", totalSalary);
// logger.info("Auto-calculated salary for employee entry: {}", totalSalary);
// }
// } catch (Exception e) {
// logger.error("Error calculating salary for employee entry", e);
// }
// }
// }

// private BigDecimal calculateTotalSalary(Map<String, Object> employee) {
// Object hireDateObj = employee.get("hireDate");
// Object performanceRatingObj = employee.get("performanceRating");
// @SuppressWarnings("unchecked")
// Map<String, Object> role = (Map<String, Object>) employee.get("role");

// if (hireDateObj == null || role == null) {
// logger.warn("Missing required data for salary calculation");
// return BigDecimal.ZERO;
// }

// try {
// LocalDate hireDate = LocalDate.parse(hireDateObj.toString());
// BigDecimal baseSalary = getBigDecimalValue(role.get("baseSalary"),
// BigDecimal.ZERO);
// BigDecimal allowance = getBigDecimalValue(role.get("allowance"),
// BigDecimal.ZERO);
// long yearsOfService = ChronoUnit.YEARS.between(hireDate, LocalDate.now());
// BigDecimal serviceBonus = BigDecimal.valueOf(yearsOfService *
// SERVICE_BONUS_PER_YEAR);
// BigDecimal performanceRating = getBigDecimalValue(performanceRatingObj,
// BigDecimal.ONE);
// BigDecimal performanceBonus =
// performanceRating.multiply(BigDecimal.valueOf(PERFORMANCE_BONUS_MULTIPLIER));

// BigDecimal totalSalary =
// baseSalary.add(allowance).add(serviceBonus).add(performanceBonus);
// return totalSalary.setScale(2, RoundingMode.HALF_UP);

// } catch (Exception e) {
// logger.error("Error in salary calculation", e);
// return BigDecimal.ZERO;
// }
// }

// private BigDecimal calculateSalaryForEmployee(LocalDate hireDate, String
// roleId, Object performanceRatingObj) {
// BigDecimal baseSalary = getBaseSalaryFromRole(roleId);
// BigDecimal allowance = getAllowanceFromRole(roleId);

// if (baseSalary == null) {
// logger.warn("Could not find base salary for role ID: {}", roleId);
// return null;
// }

// long yearsOfService = ChronoUnit.YEARS.between(hireDate, LocalDate.now());
// BigDecimal serviceBonus = BigDecimal.valueOf(yearsOfService *
// SERVICE_BONUS_PER_YEAR);
// BigDecimal performanceRating = getBigDecimalValue(performanceRatingObj,
// BigDecimal.ONE);
// BigDecimal performanceBonus =
// performanceRating.multiply(BigDecimal.valueOf(PERFORMANCE_BONUS_MULTIPLIER));

// BigDecimal totalSalary = baseSalary
// .add(allowance != null ? allowance : BigDecimal.ZERO)
// .add(serviceBonus)
// .add(performanceBonus);

// return totalSalary.setScale(2, RoundingMode.HALF_UP);
// }

// private BigDecimal getBaseSalaryFromRole(String roleId) {
// try {
// var result = persistenceService.run(
// Select.from(Roles_.CDS_NAME)
// .columns("baseSalary")
// .where(r -> r.get("ID").eq(roleId)));

// if (result.first().isPresent()) {
// var row = result.first().get();
// return getBigDecimalValue(row.get("baseSalary"), null);
// }
// } catch (Exception e) {
// logger.error("Error fetching base salary for role ID: {}", roleId, e);
// }
// return null;
// }

// private BigDecimal getAllowanceFromRole(String roleId) {
// try {
// var result = persistenceService.run(
// Select.from(Roles_.CDS_NAME)
// .columns("allowance")
// .where(r -> r.get("ID").eq(roleId)));

// if (result.first().isPresent()) {
// var row = result.first().get();
// return getBigDecimalValue(row.get("allowance"), BigDecimal.ZERO);
// }
// } catch (Exception e) {
// logger.error("Error fetching allowance for role ID: {}", roleId, e);
// }
// return BigDecimal.ZERO;
// }

// private BigDecimal getBigDecimalValue(Object value, BigDecimal defaultValue)
// {
// if (value == null)
// return defaultValue;
// try {
// if (value instanceof BigDecimal)
// return (BigDecimal) value;
// else if (value instanceof Number)
// return BigDecimal.valueOf(((Number) value).doubleValue());
// else
// return new BigDecimal(value.toString());
// } catch (Exception e) {
// logger.warn("Could not convert value to BigDecimal: {}", value, e);
// return defaultValue;
// }
// }
// }