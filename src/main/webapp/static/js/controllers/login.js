'use strict';

/* Controllers */

function LoginCtrl($scope, $http, authService) {
	$scope.username="";
	$scope.password="";

	$scope.handleKeyLoginForm = function(key) {
		console.log("key: " + key);
		if (key == 13) $scope.login();
	}

    $scope.login = function() {
   
		var loginRequest = {username: $scope.username, password: $scope.password}

		// empty password for next try
		$scope.password=""
	
      	$http.post('/user/login',loginRequest)
			.success(function() {
				// evtl. alte Login-Meldungen ausblenden
				$("#loginError").hide();
				console.log("login successfull!");
				authService.loginConfirmed();
			})
			.error(function(response) {
				// Login-Fehler anzeigen
				$("#loginError").show();
				console.log("login-error: " + response);
			});
    	}
  }

//SchuelerListCtrl.$inject = ['$scope','$http','authService'];
