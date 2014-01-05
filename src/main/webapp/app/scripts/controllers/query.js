'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService) {

  	$scope.reload = function() {
    	// init StoryContext
   		$scope.stories = queryService.findAll();
    }

    // init controller
    $scope.reload();
  });