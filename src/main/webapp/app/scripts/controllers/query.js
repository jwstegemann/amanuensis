'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService,$rootScope) {

    $rootScope.appState = 128;
    $scope.result = undefined;

    $scope.page = 0;
    $scope.pages = 0;
    $scope.lastQuery = undefined;

    $scope.searchStories = function(queryString, terms, fromDate) {
      $scope.lastQuery = {
        query: queryString,
        tags: terms ? terms : [],
        page: $scope.page
      };

      if (angular.isDefined(fromDate)) {
        $scope.lastQuery.fromDate = fromDate;
      }

      $scope.result = queryService.query($scope.lastQuery, function(successData) {
        $scope.queryString = queryString;
        $scope.terms = terms;

        $scope.pages = $scope.calcPages();
      });   
   
    }

    $scope.searchWithFilter = function(term) {
      $scope.searchStories($scope.queryString, [term]);
    }

    $scope.searchWithDate = function(fromDate) {
      $scope.searchStories($scope.queryString, [], fromDate);
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

    $scope.calcPages = function() {
      if (angular.isUndefined($scope.result)) return 0;
      else {
        return Math.floor($scope.result.hits.total / 25) + 1
      }
    }

    $scope.nextPage = function() {
      $scope.page++;
      $scope.searchStories($scope.lastQuery.query, $scope.lastQuery.terms);
    }

    $scope.previousPage = function() {
      $scope.page--;
      $scope.searchStories($scope.lastQuery.query, $scope.lastQuery.terms);
    }

  });