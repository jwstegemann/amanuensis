'use strict';

angular.module('amanuensisApp')
  .controller('LoginDialogCtrl', function ($scope, $rootScope, $http, $location, authService, utilService, gettextCatalog, $window) {

    $scope.reset = function() {
      $scope.welcome = welcomeMessage;

      $scope.pwd = undefined;
      $scope.login = undefined;
    }

    $scope.doLogin = function() {
      /*if(!(($location.host() === 'localhost' || $location.host() === '0.0.0.0') && $location.port() === 9000)) {
        if ($location.protocol() !== 'https') {
            $rootScope.$broadcast('error',{errorMessage: 'Please use https in your URL to make sure, that nobody gets to know your credentials.'});
        }
      } */

      //do not buffer failed login-tries
      var config = {method: 'POST', url: '/user/login', ignoreAuthModule: true, data: {
        username: $scope.login,
        password: $scope.pwd
      }};

      $http(config).
        success(function(data, status, headers, config) {
          $scope.setUserContext(data);

          $('#login-modal-content').removeClass('error');
          $scope.welcome = welcomeMessage;
          
          $scope.reset();
          authService.loginConfirmed();
        }).
        error(function(data, status, headers, config) {
          $scope.welcome = errorMessage;
          $('#login-modal-content').addClass('error');
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

    $scope.$on('logout', function() {
      var config = {method: 'GET', url: '/user/logout'};
      $location.url('/query').replace();                                
      $http(config).
        success(function(data, status, headers, config) {
          $scope.welcome = gettextCatalog.getString("You logged out successfully.");
          $rootScope.$broadcast('event:auth-loginRequired');          $
        })
    });


    $scope.setUserContext = function(data) {
      $rootScope.userContext = data;
      
      //set language
      if (angular.isDefined(data.lang)) {
        gettextCatalog.currentLanguage = data.lang;            
      }
      else {
        gettextCatalog.currentLanguage = 'en';            
      }
    }

    var defaultLanguage = $window.navigator.language;

    if (angular.isDefined(defaultLanguage) && defaultLanguage.length > 1) {
      console.log("detected browser language: " + defaultLanguage);
      gettextCatalog.currentLanguage = defaultLanguage.substr(0,2);
    }

    var welcomeMessage = gettextCatalog.getString('Welcome to Colibri. Please login...');
    var errorMessage = gettextCatalog.getString('Something is wrong. Please try again...');
    
    $scope.reset();

    // force login when loaded and not developing locally
      var config = {method: 'GET', url: '/user/info', ignoreAuthModule: true};   
      $http(config).
        success(function(data, status, headers, config) {
          $scope.setUserContext(data);
        }).
        error(function(data, status, headers, config) {
          $rootScope.$broadcast('event:auth-loginRequired');
        });
  });


angular.module('amanuensisApp')
  .controller('LoginCtrl', function ($scope, $rootScope) {

    $rootScope.appState = 10000;

  });
