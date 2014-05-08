'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService,$rootScope,graphService) {

    $rootScope.appState = 128;
    $scope.result = undefined;

    $scope.page = 0;
    $scope.pages = 0;
    $scope.lastQuery = undefined;
    $scope.lastMethod = undefined;

    $scope.mainSearch = undefined;

    $scope.title = undefined;

    $scope.searchStories = function(method, queryString, terms, fromDate) {
      if (method === queryService.query) {
        $scope.title = "Result of Search for " + queryString;
      }
      else if (method === queryService.myLatest) {
        $scope.title = "My latest Activities" + queryString;
      }
      else if (method === queryService.othersLatest) {
        $scope.title = "Latest Activites of other users" + queryString;
      }
      else if (method === queryService.toDos) {
        $scope.title = "My ToDos";
      }
      else if (method === queryService.favourites) {
        $scope.title = "My Starred Stories";
      }      

      $scope.lastMethod = method;
      $scope.lastQuery = {
        query: queryString,
        tags: terms ? terms : [],
        page: $scope.page
      };

      if (angular.isDefined(fromDate)) {
        $scope.lastQuery.fromDate = fromDate;
      }

      $scope.result = method($scope.lastQuery, function(successData) {
        $scope.queryString = queryString;
        $scope.terms = terms;

        $scope.pages = $scope.calcPages();
      });   
   
    }

    $scope.searchWithFilter = function(term) {
      $scope.searchStories($scope.lastMethod, $scope.queryString, [term]);
    }

    $scope.searchWithDate = function(fromDate) {
      $scope.searchStories($scope.lastMethod, $scope.queryString, [], fromDate);
    }

    if (angular.isDefined($routeParams.queryString)) {
      $scope.searchStories(queryService.query, $routeParams.queryString);
    }

    setTimeout(function() {
      $('#query-input').focus();
    }, 50);

    $scope.$on('search', function(event, params) {
      $scope.searchStories(queryService.query, params.cabQueryString);
    });

    $scope.calcPages = function() {
      if (angular.isUndefined($scope.result)) return 0;
      else {
        if ($scope.result.hits.total === -1 ) {
          return -1
        } 
        else {
          return Math.floor($scope.result.hits.total / 25) + 1
        }
      }
    }

    $scope.nextPage = function() {
      $scope.page++;
      $scope.searchStories($scope.lastMethod, $scope.lastQuery.query, $scope.lastQuery.terms);
    }

    $scope.previousPage = function() {
      $scope.page--;
      $scope.searchStories($scope.lastMethod, $scope.lastQuery.query, $scope.lastQuery.terms);
    }

    $scope.doSearch = function() {
      if (angular.isDefined($scope.mainSearch) && $scope.mainSearch.length > 0) {
        $scope.searchStories(queryService.query, $scope.mainSearch);
      }
    }

    $scope.searchToDos = function() {
      $scope.searchStories(queryService.toDos, null);
    }

    $scope.searchMyLatest = function() {
      $scope.searchStories(queryService.myLatest, '');
    }

    $scope.searchOthersLatest = function() {
      $scope.searchStories(queryService.othersLatest, '');
    }

    $scope.searchNotifications = function() {
      $scope.searchStories(queryService.query, '@' + $scope.userContext.name);
    }

    $scope.searchFavourites = function() {
      $scope.searchStories(queryService.favourites, null);
    }

  });