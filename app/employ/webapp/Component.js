sap.ui.define(
  [
    "sap/ui/core/UIComponent",
    "sap/ui/Device",
    "sap/ui/model/json/JSONModel",
    "sap/m/MessageToast",
    "sap/ui/core/BusyIndicator",
    "ojt/employ/model/models",
  ],
  function (
    UIComponent,
    Device,
    JSONModel,
    MessageToast,
    BusyIndicator,
    models
  ) {
    "use strict";

    return UIComponent.extend("ojt.employ.Component", {
      metadata: {
        manifest: "json",
        interfaces: ["sap.ui.core.IAsyncContentCreation"],
      },

      init: async function () {
        // Call the base component's init function
        UIComponent.prototype.init.apply(this, arguments);

        // Initialize the router
        this.getRouter().initialize();

        // Set device model
        var oDeviceModel = new JSONModel(Device);
        oDeviceModel.setDefaultBindingMode("OneWay");
        this.setModel(oDeviceModel, "device");

        const userModel = await models.getCurrentUser();
        this.setModel(userModel, "user");
      },
    });
  }
);
