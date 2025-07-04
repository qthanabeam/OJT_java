sap.ui.define(["sap/ui/core/mvc/Controller"], function (Controller) {
  "use strict";

  return Controller.extend("ojt.employ.controller.BaseController", {
    onNavToList: function () {
      this.getOwnerComponent().getRouter().navTo("list");
    },

    onNavToAddEmployee: function () {
      this.getOwnerComponent().getRouter().navTo("addEmployee");
    },

    getRouter: function () {
      return this.getOwnerComponent().getRouter();
    },

    getModel: function (sName) {
      return this.getView().getModel(sName);
    },

    setModel: function (oModel, sName) {
      return this.getView().setModel(oModel, sName);
    },
  });
});
