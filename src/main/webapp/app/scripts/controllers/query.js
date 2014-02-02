'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService,$rootScope) {

    $rootScope.appState = 128;
    $scope.result = undefined;

    $scope.searchStories = function(queryString, terms) {
      $scope.result = queryService.query({
        query: queryString,
        tags: terms ? terms : []
      }, function(successData) {
        $scope.queryString = queryString;
        $scope.terms = terms;
      });      
    }

    $scope.searchWithFilter = function(term) {
      $scope.searchStories($scope.queryString, [term]);
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