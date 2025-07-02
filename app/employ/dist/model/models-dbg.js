sap.ui.define(
  ["sap/ui/model/json/JSONModel", "sap/ui/Device"],
  function (JSONModel, Device) {
    "use strict";

    return {
      /**
       * Provides runtime information for the device the UI5 app is running on as a JSONModel.
       * @returns {sap.ui.model.json.JSONModel} The device model.
       */
      createDeviceModel: function () {
        var oModel = new JSONModel(Device);
        oModel.setDefaultBindingMode("OneWay");
        return oModel;
      },

      /**
       * Fetch current user info from backend and return as JSONModel
       * @returns {sap.ui.model.json.JSONModel} The user model.
       */
      getCurrentUser: async function () {
        // Create OData V4 model instance for action call
        const oModel = new sap.ui.model.odata.v4.ODataModel({
          serviceUrl: "/odata/v4/employee/",
          synchronizationMode: "None",
          operationMode: "Server",
        });

        // Bind context to the userInfo function
        let oAction = oModel.bindContext("/userInfo(...)");

        try {
          // Invoke the action
          await oAction.invoke();
        } catch (err) {
          console.error("Error invoking userInfo:", err);
        }

        // Get the result object
        const oResult = oAction.getBoundContext().getObject();
        console.log("Fetched user info:", oResult);

        // Prepare user data
        const roles = oResult?.roles || {};
        const isAdmin = roles.hasOwnProperty("Admin");
        const id = oResult?.id || "";

        // Build JSON model and set properties explicitly
        const oUserModel = new JSONModel();
        oUserModel.setProperty("/isAdmin", isAdmin);
        oUserModel.setProperty("/roles", roles);
        oUserModel.setProperty("/id", id);

        return oUserModel;
      },
    };
  }
);
