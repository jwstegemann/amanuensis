'use strict';

angular.module('amanuensisApp')
  .controller('FindPathsCtrl', function ($scope, graphService, $rootScope) {

    $scope.searchStories = function(queryString, terms) {
      $scope.result = graphService.query({
        sourceStoryId: $scope.sourceStory.id,
        tagName: $scope.tagName,
        targetStoryId: $scope.targetStory.id
      }, function(successData) {
      });      
    }

    /*
     * init controller
     */

    $rootScope.appState = 256;

    if (angular.isUndefined($rootScope.targetStack) ||
      angular.isUndefined($rootScope.targetStack.source) ||
      angular.isUndefined($rootScope.targetStack.target)) {

      $rootScope.$broadcast('error',{errorMessage: 'Please select the stories you want to search between first.'});      

    }
    else {
      $scope.sourceStory = $rootScope.targetStack.source;
      $scope.targetStory = $rootScope.targetStack.target;

      $scope.tagName ='offen';
      $rootScope.targetMode = false;
      $scope.searchStories();
    }

  });
