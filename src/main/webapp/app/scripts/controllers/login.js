'use strict';

angular.module('amanuensisApp')
  .controller('LoginDialogCtrl', function ($scope, $rootScope, $http, $location, authService, utilService) {

    var welcomeMessage = 'Welcome to Amanuensis. Please login...';
    var errorMessage = 'Something is wrong. Please try again...';


    $scope.reset = function() {
      $scope.welcome = welcomeMessage;

      $scope.pwd = undefined;
      $scope.login = undefined;
    }

    $scope.doLogin = function() {

      //do not buffer failed login-tries
      var config = {method: 'POST', url: '/user/login', ignoreAuthModule: true, data: {
        username: $scope.login,
        password: $scope.pwd
      }};

      $http(config).
        success(function(data, status, headers, config) {
          $rootScope.userContext = data;
          
          $scope.reset();
          authService.loginConfirmed();
        }).
        error(function(data, status, headers, config) {
          $scope.welcome = errorMessage;
          $('#loginInput').focus();
        });
    }

    $rootScope.goToPassword = function() {
      $('#passwordInput').focus();
    }

    /* 
     * handle login
     */
    $scope.$on('event:auth-loginRequired', function() {
        utilService.showModal('#login-modal');  
    });

    $scope.$on('event:auth-loginConfirmed', function() {
        utilService.hideModal('#login-modal');  
    });


    $scope.reset();

    // force login when loaded and not developing locally
    if(!(($location.host() === 'localhost' || $location.host() === '0.0.0.0') && $location.port() === 9000)) {
      $rootScope.$broadcast('event:auth-loginRequired'); 
    } 
    else {
      $scope.login = 'dummy';
      $scope.pwd = 'dummy';

      $scope.doLogin();

      console.log("switched of login at app-start because you are developing locally...");
    }

  });


angular.module('amanuensisApp')
  .controller('LoginCtrl', function ($scope, $rootScope) {

    $rootScope.appState = 10000;

  });
