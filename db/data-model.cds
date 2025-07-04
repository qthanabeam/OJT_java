namespace JAVAX;

using {
      managed,
      cuid
} from '@sap/cds/common';
@readonly
entity Roles : managed {
      key ID         : UUID;
          name       : String(50);
          baseSalary : Decimal(10, 2);
          allowance  : Decimal(10, 2) default 0;
}

@readonly
entity Departments : managed {
      key ID   : UUID;
          name : String(50);
}

entity Employees : managed, cuid {
      firstName         : String(50);
      lastName          : String(50);
      dateOfBirth       : Date;
      gender            : String(10);
      email             : String(100);
      hireDate          : Date;
      salary            : Decimal(10, 2);
      role              : Association to one Roles;
      department        : Association to one Departments;
      performanceRating : Integer default 1; // 1-5
}

entity LeaveRequests : managed, cuid {
      employee  : Association to one Employees;
      startDate : Date;
      endDate   : Date;
      status    : String(20) default 'Pending';
      reason    : String(255);
}
