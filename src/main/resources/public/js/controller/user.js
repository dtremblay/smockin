
app.controller('userController', function($scope, $uibModalInstance, $http, $timeout, utils, globalVars, restClient, data) {


    //
    // Labels
    $scope.userHeading = 'New User';
    $scope.usernameLabel = 'Username';
    $scope.fullNameLabel = 'Full Name';
    $scope.roleLabel = 'Role';
    $scope.passwordLabel = 'Password';
    $scope.confirmPasswordLabel = 'Confirm Password';
    $scope.dateCreatedLabel = 'Date Created';

    $scope.usernamePlaceholderTxt = "Enter a username";
    $scope.fullNamePlaceholderTxt = "Enter the full name";
    $scope.passwordPlaceholderTxt = "Enter a password";
    $scope.confirmPasswordPlaceholderTxt = "Confirm the above password ";


    //
    // Buttons
    $scope.cancelButtonLabel = 'Cancel';
    $scope.saveButtonLabel = 'Save';
    $scope.deleteButtonLabel = "Delete";


    //
    // Alerts
    $scope.alerts = [];

    var closeAlertFunc = function() {
        $scope.alerts = [];
    };

   function showAlert(msg, type) {

        if (type == null) {
            type = 'danger';
        }

        $scope.alerts = [];
        $scope.alerts.push({ "type" : type, "msg" : msg });

        $timeout(closeAlertFunc, globalVars.AlertTimeoutMillis);
    }

    $scope.closeAlert = closeAlertFunc;


    //
    // Data Objects
    $scope.userData = {
        "extId" : null,
        "username" : null,
        "fullName" : null,
        "role" : "REGULAR",
        "password" : null,
        "confirmPassword" : null
    };

    if (data != null) {

        $scope.userHeading = 'View User';

        $scope.userData.extId = data.extId;
        $scope.userData.username = data.username;
        $scope.userData.fullName = data.fullName;
        $scope.userData.role = data.role;
        $scope.userData.dateCreated = data.dateCreated;
        $scope.userData.password = "********";
        $scope.userData.confirmPassword = "********";
    }


    //
    // Scoped Functions
    $scope.doSaveUser = function() {

        if (data != null) {
            doUpdateUser();
        } else {
            doCreateUser();
        }

    }

    $scope.doDeleteUser = function() {

        if (data == null) {
            return;
        }

        if (data.role == "SYS_ADMIN") {
            showAlert("The System Admin user cannot be deleted");
            return;
        }

        utils.openDeleteConfirmation("Are you sure you wish to delete this user?", function(result) {

            if (!result) {
                return;
            }

            restClient.doDelete($http, '/user/' + data.extId, function(status, data) {

                if (status == 204) {
                    $uibModalInstance.close("ok");
                    return;
                }

                showAlert(globalVars.GeneralErrorMessage);
            });

        });

    };

    $scope.doCancel = function() {
        $uibModalInstance.dismiss('cancel');
    };


    //
    // Internal Functions
    function doCreateUser() {

        // Validation
        if (utils.isBlank($scope.userData.username)) {
            showAlert("'Username' is required");
            return;
        }

        if (utils.isBlank($scope.userData.fullName)) {
            showAlert("'Full Name' is required");
            return;
        }

        if (utils.isBlank($scope.userData.role)) {
            showAlert("'Role' is required");
            return;
        }

        // TODO validate password format
        // expect 1 digit, 1 upper & 1 lower case character)
        if (utils.isBlank($scope.userData.password)) {
            showAlert("'Password' is required");
            return;
        }

        if ($scope.userData.password != $scope.userData.confirmPassword) {
            showAlert("'Password' and 'Confirm Password' do not match");
            return;
        }

        // Send
        restClient.doPost($http, '/user', $scope.userData, function(status, data) {

            if (status == 201) {
                $uibModalInstance.close("ok");
                return;
            }

            showAlert(globalVars.GeneralErrorMessage);
        });

    };

    function doUpdateUser() {

        // TODO

    }

});
