'use strict';

/* Controllers */

function UserCtrl($scope, $routeParams, $http) {
  $scope.user = {firstName: "nicht", lastName: "angemeldet"};
 

  $scope.init = function() {
  	$scope.getUserInfo();
  }
  
    $scope.getUserInfo = function() {
      $http.get('/user/info').success(function(response) {
        // this piece of code will not be executed until user is authenticated
        $scope.user = response;
      });
    }
}

//UserCtrl.$inject = ['$scope', '$routeParams', '$http'];