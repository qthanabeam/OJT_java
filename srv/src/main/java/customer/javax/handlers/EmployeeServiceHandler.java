package customer.javax.handlers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cds.services.request.UserInfo;
import com.sap.cds.Result;
import com.sap.cds.Row;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnUpdate;

import cds.gen.employeeservice.*;;

@Component
@ServiceName(EmployeeService_.CDS_NAME)
public class EmployeeServiceHandler implements EventHandler {
    @Autowired
    UserInfo userInfo;
    @Autowired
    PersistenceService persistenceService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceHandler.class);

    // Calculate years of service
    private long getYearsOfService(LocalDate hireDate) {
        return ChronoUnit.YEARS.between(hireDate, LocalDate.now());
    }

    // Handle updateLeaveStatus action/function
    @On(event = UpdateLeaveStatusContext.CDS_NAME)
    public void updateLeaveStatus(UpdateLeaveStatusContext context) {
        String leaveID = context.getLeaveID();
        String status = context.getStatus();
        String[] validStatuses = { "Pending", "Approved", "Rejected" };
        if (!Arrays.asList(validStatuses).contains(status)) {
            context.getMessages().error("Invalid status value");
            context.setCompleted();
            return;
        }
        CqnUpdate updateQuery = Update.entity(LeaveRequests_.CDS_NAME)
                .data(Map.of("status", status))
                .where(leaveRequest -> leaveRequest.get(LeaveRequests.ID).eq(leaveID));
        long updated = persistenceService.run(updateQuery).rowCount();
        if (updated == 0) {
            context.getMessages().error("Leave request not found");
            context.setCompleted();
            return;
        }
        // For action context, use setResult
        context.setResult("Status updated successfully");
    }

    @On(event = UserInfoContext.CDS_NAME)
    public void userInfo(UserInfoContext context) {
        User result = User.create();
        if (!userInfo.isAuthenticated()) {
            result.put("id", "anonymous");
            result.put("roles", new ArrayList<String>());
        } else {
            String userId = userInfo.getName();
            Collection<String> roles = userInfo.getRoles();

            List<String> filteredRoles = new ArrayList<String>();
            if (roles != null) {
                for (String role : roles) {
                    if (!"openid".equals(role)) {
                        filteredRoles.add(role);
                    }
                }
            }

            System.out.println("Filtered roles: " + filteredRoles);

            result.put("id", userId != null ? userId : "unknown");
            result.put("roles", filteredRoles);
        }
        context.setResult(result);
    }

    // Handle calculateSalary as a custom function
    @On(event = CalculateSalaryContext.CDS_NAME)
    public void calculateSalary(CalculateSalaryContext context) {

        final String employeeID = context.getEmployeeID();

        if (employeeID == null || employeeID.isEmpty()) {
            context.getMessages().error("EmployeeID parameter is required");
            context.setCompleted();
            return;
        }

        CqnSelect query = Select.from(Employees_.CDS_NAME)
                .columns(emp -> emp._all())
                .where(emp -> emp.get(Employees.ID).eq(employeeID));

        Result result = persistenceService.run(query);
        if (result.first().isEmpty()) {
            context.getMessages().error("Employee not found");
            context.setCompleted();
            return;
        }

        Row employeeRow = result.first().get();
        Map<String, Object> employeeData = new HashMap<>();
        employeeRow.forEach((key, value) -> employeeData.put(key, value));

        LocalDate hireDate = LocalDate.parse(employeeData.get(Employees.HIRE_DATE).toString());
        long yearsOfService = getYearsOfService(hireDate);

        BigDecimal baseSalary = new BigDecimal(employeeData.getOrDefault("role_baseSalary", "0").toString());
        BigDecimal allowance = new BigDecimal(employeeData.getOrDefault("role_allowance", "0").toString());
        BigDecimal serviceBonus = BigDecimal.valueOf(yearsOfService * 1000);
        BigDecimal performanceBonus = BigDecimal.valueOf(
                Double.parseDouble(employeeData.getOrDefault(Employees.PERFORMANCE_RATING, "1").toString()) * 500);

        BigDecimal totalSalary = baseSalary
                .add(allowance)
                .add(serviceBonus)
                .add(performanceBonus)
                .setScale(2, RoundingMode.HALF_UP);

        CqnUpdate updateQuery = Update.entity(Employees_.CDS_NAME)
                .data(Map.of(Employees.SALARY, totalSalary))
                .where(empUpdate -> empUpdate.get(Employees.ID).eq(employeeID));

        persistenceService.run(updateQuery);

        // Return the calculated salary
        context.setResult(totalSalary);
    }

    // Restrict access for CREATE, UPDATE, DELETE operations
    @Before(event = { CqnService.EVENT_CREATE, CqnService.EVENT_UPDATE, CqnService.EVENT_DELETE }, entity = {
            Employees_.CDS_NAME, LeaveRequests_.CDS_NAME })
    public void restrictAccess(EventContext context) {
        if (!userInfo.isAuthenticated() || !userInfo.getRoles().contains("Admin")) {
            logger.info("Unauthorized access attempt by user: {}", userInfo.getName());
            context.getMessages().error("Admin role required for this operation");
            context.setCompleted();
        }
    }

    // Restrict LeaveRequests updates to Admins
    @Before(event = CqnService.EVENT_UPDATE, entity = LeaveRequests_.CDS_NAME)
    public void restrictLeaveRequestUpdate(EventContext context) {
        if (!userInfo.isAuthenticated() || !userInfo.getRoles().contains("Admin")) {
            context.getMessages().error("Only Admins can approve/reject leave requests");
            context.setCompleted();
        }
    }
}