'use strict';

angular.module('amanuensisApp')
  .controller('FindPathsCtrl', function ($scope, graphService, $rootScope, utilService, gettextCatalog) {

    $scope.searchStories = function() {
      $scope.result = graphService.query({
        sourceStoryId: $scope.sourceStory.id,
        tagName: $scope.tagName,
        targetStoryId: $scope.targetStory.id,
        page: $scope.page
      }, function(successData) {
      });      
    }

    $scope.tagNameEntered = function() {
      if ($scope.newTagName.length > 3) {

            $scope.tagName = $scope.newTagName;
            $scope.searchStories();

            utilService.hideModal('#tag-name-modal');

            $scope.newTagName = undefined;
        }
        else {
            $scope.title = gettextCatalog.getString('Your input was too short...');
        }      

    };


    $scope.askForTagName = function() {
        $scope.title = gettextCatalog.getString('Please enter a tag-name...');
        utilService.showModal('#tag-name-modal');
    };

    $scope.cancelTagName = function() {
        utilService.hideModal('#tag-name-modal');
    }

    $scope.nextPage = function() {
      $scope.page++;
      $scope.searchStories();
    }

    $scope.previousPage = function() {
      $scope.page--;
      $scope.searchStories();
    }

    /*
     * init controller
     */

    $scope.page = 0;

    $rootScope.appState = 256;

    if (angular.isUndefined($rootScope.targetStack) ||
      angular.isUndefined($rootScope.targetStack.source) ||
      angular.isUndefined($rootScope.targetStack.target)) {

      $rootScope.$broadcast('error',{errorMessage: gettextCatalog.getString('Please select the stories you want to search between first.')});      

    }
    else {

      $scope.sourceStory = $rootScope.targetStack.source;
      $scope.targetStory = $rootScope.targetStack.target;

      $rootScope.targetMode = false;

      $scope.askForTagName();
    }

  });
