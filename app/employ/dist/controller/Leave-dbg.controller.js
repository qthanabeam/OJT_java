sap.ui.define(
  [
    "ojt/employ/controller/BaseController",
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/ui/model/json/JSONModel",
    "sap/ui/model/Filter",
    "sap/ui/model/FilterOperator",
  ],
  function (
    BaseController,
    Controller,
    MessageToast,
    JSONModel,
    Filter,
    FilterOperator
  ) {
    "use strict";

    return BaseController.extend("ojt.employ.controller.Leave", {
      onInit: function () {
        var oRouter = this.getOwnerComponent().getRouter();
        oRouter
          .getRoute("leave")
          .attachPatternMatched(this._onRouteMatched, this);

        // Initialize new leave request model
        var oNewLeaveModel = new JSONModel({
          startDate: null,
          endDate: null,
          reason: "",
        });
        this.getView().setModel(oNewLeaveModel, "newLeave");

        // Store employeeId
        this._employeeId = null;
      },

      _onRouteMatched: function (oEvent) {
        var sEmployeeId = oEvent.getParameter("arguments").employeeId;
        console.log("Employee ID from URL:", sEmployeeId);
        this._employeeId = sEmployeeId;

        var oView = this.getView();
        oView.bindElement({
          path: "/Employees(" + sEmployeeId + ")",
          events: {
            dataReceived: function (oEvent) {
              var oContext = oEvent.getParameter("context");
              if (oContext) {
                console.log("Binding successful for employee:", sEmployeeId);
                console.log("Binding context:", oContext.getObject());
              } else {
                console.error(
                  "Binding failed: No context returned for employee",
                  sEmployeeId
                );
                MessageToast.show("Không thể tải thông tin nhân viên");
              }
            }.bind(this),
            dataRequested: function () {
              console.log("Data requested for employee:", sEmployeeId);
            },
          },
        });

        // Filter leave requests for this employee
        var oTable = this.byId("leaveTable");
        var oBinding = oTable.getBinding("items");

        if (oBinding) {
          var oFilter = new Filter(
            "employee/ID",
            FilterOperator.EQ,
            sEmployeeId
          );
          oBinding.filter([oFilter]);
          oBinding.refresh();
          console.log(
            "Applied filter for leave requests: employee/ID =",
            sEmployeeId
          );
        } else {
          console.error("Table binding not found");
        }
      },

      onNavBack: function () {
        this.getOwnerComponent().getRouter().navTo("list");
      },

      onAddLeaveRequest: function () {
        var oModel = this.getView().getModel();
        var oNewLeaveModel = this.getView().getModel("newLeave");

        // Use stored employeeId
        var sEmployeeId = this._employeeId;
        if (!sEmployeeId) {
          console.error("Employee ID is missing or invalid");
          MessageToast.show("Lỗi: Không thể xác định ID nhân viên");
          return;
        }

        var oNewLeave = oNewLeaveModel.getData();

        if (!oNewLeave.startDate || !oNewLeave.endDate) {
          MessageToast.show("Vui lòng điền đầy đủ ngày bắt đầu và kết thúc");
          return;
        }

        console.log("Creating leave request for employee ID: ", sEmployeeId);
        console.log("Leave request payload:", {
          employee: { ID: sEmployeeId },
          startDate: oNewLeave.startDate,
          endDate: oNewLeave.endDate,
          status: "Pending",
          reason: oNewLeave.reason || "",
        });

        // Create new leave request using OData V4
        var oListBinding = oModel.bindList("/LeaveRequests");
        var oNewContext = oListBinding.create({
          employee: { ID: sEmployeeId },
          startDate: oNewLeave.startDate,
          endDate: oNewLeave.endDate,
          status: "Pending",
          reason: oNewLeave.reason || "",
        });

        oNewContext
          .created()
          .then(
            function () {
              console.log(
                "New Leave Request created: ",
                oNewContext.getObject()
              );
              MessageToast.show("Yêu cầu nghỉ phép đã được tạo");
              oNewLeaveModel.setData({
                startDate: null,
                endDate: null,
                reason: "",
              });
              // Refresh table binding
              var oTable = this.byId("leaveTable");
              var oBinding = oTable.getBinding("items");
              if (oBinding) {
                oBinding.filter([
                  new Filter("employee/ID", FilterOperator.EQ, sEmployeeId),
                ]);
                oBinding.refresh();
                console.log(
                  "Table binding refreshed for employee: " + sEmployeeId
                );
              }
            }.bind(this)
          )
          .catch(function (oError) {
            console.error("Error creating leave request: ", oError);
            MessageToast.show(
              "Lỗi khi tạo yêu cầu nghỉ phép: " +
                (oError.message || "Unknown error")
            );
          });
      },

      onApproveRequest: function (oEvent) {
        var oItem = oEvent.getSource().getParent().getParent();
        var oContext = oItem.getBindingContext();
        var oModel = this.getView().getModel();

        if (!oContext) {
          MessageToast.show("Không thể xác định yêu cầu nghỉ phép");
          return;
        }

        var sLeaveID = oContext.getProperty("ID");
        var oOperation = oModel.bindContext("/updateLeaveStatus(...)");
        oOperation.setParameter("leaveID", sLeaveID);
        oOperation.setParameter("status", "Approved");

        oOperation
          .execute()
          .then(function () {
            MessageToast.show("Yêu cầu nghỉ phép đã được phê duyệt");
            oModel.refresh();
          })
          .catch(function (oError) {
            MessageToast.show(
              "Lỗi khi phê duyệt yêu cầu nghỉ phép: " +
                (oError.message || "Unknown error")
            );
          });
      },

      onRejectRequest: function (oEvent) {
        var oItem = oEvent.getSource().getParent().getParent();
        var oContext = oItem.getBindingContext();
        var oModel = this.getView().getModel();

        if (!oContext) {
          MessageToast.show("Không thể xác định yêu cầu nghỉ phép");
          return;
        }

        var sLeaveID = oContext.getProperty("ID");
        var oOperation = oModel.bindContext("/updateLeaveStatus(...)");
        oOperation.setParameter("leaveID", sLeaveID);
        oOperation.setParameter("status", "Rejected");

        oOperation
          .execute()
          .then(function () {
            MessageToast.show("Yêu cầu nghỉ phép đã bị từ chối");
            oModel.refresh();
          })
          .catch(function (oError) {
            MessageToast.show(
              "Lỗi khi từ chối yêu cầu nghỉ phép: " +
                (oError.message || "Unknown error")
            );
          });
      },
    });
  }
);
