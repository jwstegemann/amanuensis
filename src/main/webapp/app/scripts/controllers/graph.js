'use strict';

angular.module('amanuensisApp')
  .controller('FindPathsCtrl', function ($scope, graphService, $rootScope, utilService) {

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
            $scope.title = 'Your input was too short...';
        }      

    };


    $scope.askForTagName = function() {
        $scope.title = 'Please enter a tag-name...';
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

      $rootScope.$broadcast('error',{errorMessage: 'Please select the stories you want to search between first.'});      

    }
    else {

      $scope.sourceStory = $rootScope.targetStack.source;
      $scope.targetStory = $rootScope.targetStack.target;

      $rootScope.targetMode = false;

      $scope.askForTagName();
    }

  });


/*
 * Controller for Tag-Name-Dialog
 */
angular.module('amanuensisApp')
  .controller('MyModalCtrl', function ($scope,utilService) {
    $scope.$on('testMe',function() {
    console.log("init modal");
  });

});



/*

    


*/