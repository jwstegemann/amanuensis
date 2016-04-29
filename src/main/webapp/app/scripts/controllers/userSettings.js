'use strict';

angular.module('amanuensisApp')
  .controller('UserSettingsCtrl', function ($scope, $rootScope, $http, utilService, gettextCatalog) {

    var errorMessage = gettextCatalog.getString('Something is wrong. Please try again...');

    $scope.reset = function() {
      $scope.oldPwd = undefined;
      $scope.newPwd = undefined;
      $scope.repeatPwd = undefined;
    }

    $scope.change = function() {
      if (!(angular.isDefined($scope.oldPwd) && $scope.oldPwd.length > 0)) {
        $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Please enter your actual password.')});        
      }
      else if (!(angular.isDefined($scope.newPwd) && $scope.newPwd.length > 6)) {
        $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Please enter a new password of at least 6 characters.')});        
      }
      else if (!(angular.isDefined($scope.repeatPwd) && $scope.newPwd === $scope.repeatPwd)) {
        $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('I am inconsolable, but the two entries of your new password do not match.')});        
      }
      else {
        $http.post('/user/changePwd', {
          oldPwd: $scope.oldPwd,
          newPwd: $scope.newPwd
        }).success(function(successData) {
          utilService.hideModal('#usersettings-modal');        
        });
      }
    }

    $rootScope.goToNewPassword = function() {
      $('#password-new1-input').focus();
    }

    $rootScope.goToRepeatPassword = function() {
      $('#password-new2-input').focus();
    }

    /* 
     * handle login
     */
    $scope.$on('changePassword', function() {
        utilService.showModal('#usersettings-modal');  
    });

    $scope.cancel = function() {
        utilService.hideModal('#usersettings-modal');      
    }

    $scope.reset();

  });

