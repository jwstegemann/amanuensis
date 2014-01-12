'use strict';

angular.module('amanuensisApp')
  .controller('LoginDialogCtrl', function ($scope, $rootScope, $http, authService, utilService) {

    var welcomeMessage = 'Welcome to Amanuensis. Please login...';
    var errorMessage = 'Something is wrong. Please try again...';


    $scope.reset = function() {
      $scope.welcome = welcomeMessage;

      $scope.pwd = undefined;
      $scope.login = undefined;
    }


    var keyStr = 'ABCDEFGHIJKLMNOP' +
          'QRSTUVWXYZabcdef' +
          'ghijklmnopqrstuv' +
          'wxyz0123456789+/' +
          '=';

    function base64(input) {

          var output = "";
          var chr1, chr2, chr3 = "";
          var enc1, enc2, enc3, enc4 = "";
          var i = 0;

          do {
              chr1 = input.charCodeAt(i++);
              chr2 = input.charCodeAt(i++);
              chr3 = input.charCodeAt(i++);

              enc1 = chr1 >> 2;
              enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
              enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
              enc4 = chr3 & 63;

              if (isNaN(chr2)) {
                  enc3 = enc4 = 64;
              } else if (isNaN(chr3)) {
                  enc4 = 64;
              }

              output = output +
                      keyStr.charAt(enc1) +
                      keyStr.charAt(enc2) +
                      keyStr.charAt(enc3) +
                      keyStr.charAt(enc4);
              chr1 = chr2 = chr3 = "";
              enc1 = enc2 = enc3 = enc4 = "";
          } while (i < input.length);

          return output;
      }


    $scope.getUserInfo = function(login) {

      //do not buffer failed login-tries
      var config = {method: 'GET', url: '/user/login', ignoreAuthModule: true};

      if (login) {
        $http.defaults.headers.common.Authorization = 'Basic ' + base64($scope.login + ':' + $scope.pwd);
        
      }

      $http(config).
        success(function(data, status, headers, config) {
          $rootScope.userContext = data;
          
          if (login) {
            $scope.reset();
            authService.loginConfirmed();
          }

        }).
        error(function(data, status, headers, config) {
          if (login) {
            $scope.welcome = errorMessage;
            $('#loginInput').focus();

            $http.defaults.headers.common.Authorization = null
          }
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
    $scope.getUserInfo(false);

  });


angular.module('amanuensisApp')
  .controller('LoginCtrl', function ($scope, $rootScope) {

    $rootScope.appState = 10000;

  });
