using {JAVAX} from '../db/data-model';

@path: 'employee'
service EmployeeService {
  entity Roles         as projection on JAVAX.Roles;
  entity Departments   as projection on JAVAX.Departments;
  entity Employees     as projection on JAVAX.Employees;
  entity LeaveRequests as projection on JAVAX.LeaveRequests;
  action   calculateSalary(employeeID : UUID)                 returns Decimal(10, 2);
  action   updateLeaveStatus(leaveID : UUID, status : String) returns String;
  function userInfo()                                         returns user;

  type user {
    id    : String;
    roles : array of String;
  }

}
