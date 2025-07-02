sap.ui.define(
  [
    "ojt/employ/controller/BaseController", 
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/ui/model/json/JSONModel",
    "sap/m/MessageBox",
  ],
  function (BaseController, Controller, MessageToast, JSONModel, MessageBox) {
    "use strict";
    return BaseController.extend("ojt.employ.controller.AddEmployee", {
      onInit: function () {
        this._resetNewEmployeeModel();
      },

      _resetNewEmployeeModel: function () {
        var oNewEmployeeModel = new JSONModel({
          firstName: "",
          lastName: "",
          dateOfBirth: null,
          gender: "",
          email: "",
          hireDate: null,
          role_ID: "",
          department_ID: "",
          performanceRating: 1,
          salary: 0,
        });
        this.getView().setModel(oNewEmployeeModel, "newEmployee");
      },

      onNavBack: function () {
        this._resetNewEmployeeModel();
        this.getOwnerComponent().getRouter().navTo("list");
      },

      onSubmitNewEmployee: function () {
        var oModel = this.getView().getModel();
        var oNewEmployeeModel = this.getView().getModel("newEmployee");
        var oNewEmployee = oNewEmployeeModel.getData();

        if (
          !oNewEmployee.firstName ||
          !oNewEmployee.lastName ||
          !oNewEmployee.email ||
          !oNewEmployee.role_ID ||
          !oNewEmployee.department_ID
        ) {
          MessageToast.show("Vui lòng điền đầy đủ các trường bắt buộc");
          return;
        }

        var rexMail = /^\w+[\w-+\.]*\@\w+([-\.]\w+)*\.[a-zA-Z]{2,}$/;
        if (!rexMail.test(oNewEmployee.email)) {
          MessageToast.show("Email không hợp lệ");
          return;
        }

        MessageBox.confirm("Bạn có chắc muốn tạo nhân viên mới?", {
          title: "Xác nhận",
          onClose: function (oAction) {
            if (oAction === MessageBox.Action.OK) {
              var oListBinding = oModel.bindList("/Employees");
              var oNewContext = oListBinding.create({
                firstName: oNewEmployee.firstName,
                lastName: oNewEmployee.lastName,
                dateOfBirth: oNewEmployee.dateOfBirth,
                gender: oNewEmployee.gender,
                email: oNewEmployee.email,
                hireDate: oNewEmployee.hireDate,
                role_ID: oNewEmployee.role_ID,
                department_ID: oNewEmployee.department_ID,
                performanceRating: parseInt(oNewEmployee.performanceRating, 10),
                salary: 0,
              });

              oNewContext
                .created()
                .then(
                  function () {
                    MessageToast.show("Nhân viên đã được tạo");
                    this._resetNewEmployeeModel();
                    oModel.refresh();
                    this.getOwnerComponent().getRouter().navTo("list");
                  }.bind(this)
                )
                .catch(function (oError) {
                  MessageToast.show(
                    "Lỗi khi tạo nhân viên: " +
                      (oError.message || "Unknown error")
                  );
                });
            }
          }.bind(this),
        });
      },

      onCancel: function () {
        this._resetNewEmployeeModel();
        this.getOwnerComponent().getRouter().navTo("list");
      },
    });
  }
);
