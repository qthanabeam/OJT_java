sap.ui.define(["sap/ui/core/mvc/Controller"], function (Controller) {
  "use strict";

  return Controller.extend("ojt.employ.controller.App", {
    onInit: function () {
      var oRouter = this.getOwnerComponent().getRouter();
      oRouter.initialize();
    },

    onNavToList: function () {
      this.getOwnerComponent().getRouter().navTo("list");
    },

    onNavToAddEmployee: function () {
      this.getOwnerComponent().getRouter().navTo("addEmployee");
    },
  });
});
