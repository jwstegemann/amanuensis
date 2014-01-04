'use strict';

angular.module('amanuensisApp')
  .controller('SlotCtrl', function ($scope,$routeParams,slotService,$rootScope) {

    // init StoryContext
   	$scope.stories = slotService.query({
    	storyId: $routeParams.storyId,
    	slotName: $routeParams.slotName
    });

    $scope.add = function() {
    	$rootScope.mode = MODE_ADD_TO_SLOT;
    	$rootScope.stack = {
    		storyId: $routeParams.storyId,
    		slotName: $routeParams.slotName
    	};
    }

    $scope.remove = function(storyId, index) {
    	slotService.remove({
    		fromStoryId: $routeParams.storyId,
    		slotName: $routeParams.slotName,
    		storyId: storyId
    	}, function (successData) {
    		$scope.stories.splice(index,1);
	    	console.log("story aus slot entfernt!");    	
    	});
    }

  });
