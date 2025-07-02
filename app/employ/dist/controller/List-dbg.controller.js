sap.ui.define(
  [
    "ojt/employ/controller/BaseController",
    "sap/ui/model/json/JSONModel",
    "sap/m/MessageToast",
    "sap/m/MessageBox",
  ],
  function (BaseController, JSONModel, MessageToast, MessageBox) {
    "use strict";

    return BaseController.extend("ojt.employ.controller.List", {
      onInit: function () {
        var oModel = this.getOwnerComponent().getModel();
        if (!oModel) {
          MessageToast.show("OData model not initialized");
          return;
        }
        this.getView().setModel(oModel);

        var oRouter = this.getOwnerComponent().getRouter();
        oRouter
          .getRoute("list")
          .attachPatternMatched(this._onRouteMatched, this);
      },

      _onRouteMatched: function () {
        var oList = this.byId("employeeTable");
        var oBinding = oList.getBinding("items");
        if (oBinding) {
          oBinding.refresh();
        }
      },

      onItemPress: function (oEvent) {
        var oItem = oEvent.getSource();
        var oRouter = this.getOwnerComponent().getRouter();
        var sEmployeeId = oItem.getBindingContext().getProperty("ID");
        oRouter.navTo("detail", { employeeId: sEmployeeId });
      },

      onDelete: function (oEvent) {
        var oItem = oEvent.getSource().getParent();
        var oContext = oItem.getBindingContext();
        var oModel = this.getView().getModel();

        if (!oContext) {
          MessageToast.show("Không thể xác định nhân viên cần xóa");
          return;
        }

        var sEmployeeName =
          oContext.getProperty("firstName") +
          " " +
          oContext.getProperty("lastName");

        MessageBox.confirm(
          "Bạn có chắc muốn xóa nhân viên " + sEmployeeName + "?",
          {
            title: "Xác nhận xóa",
            onClose: function (oAction) {
              if (oAction === MessageBox.Action.OK) {
                oContext
                  .delete("$auto")
                  .then(function () {
                    MessageToast.show("Nhân viên đã bị xóa");
                    oModel.refresh();
                  })
                  .catch(function (oError) {
                    MessageToast.show(
                      "Lỗi khi xóa nhân viên: " +
                        (oError.message || "Unknown error")
                    );
                  });
              }
            },
          }
        );
      },
    });
  }
);
