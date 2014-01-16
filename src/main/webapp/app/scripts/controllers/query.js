'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService,$rootScope) {

    $rootScope.appState = 128;
    $scope.result = undefined;

    $scope.searchStories = function(queryString) {
      $scope.result = queryService.query({queryString: queryString}, function(successData) {
        $scope.queryString = queryString;
      });      
    }

    if (angular.isDefined($routeParams.queryString)) {
      $scope.searchStories($routeParams.queryString);
    }

    setTimeout(function() {
      $('#query-input').focus();
    }, 100);

    $scope.$on('search', function(event, params) {
      $scope.searchStories(params.cabQueryString);
    });



  });