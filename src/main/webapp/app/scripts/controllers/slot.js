'use strict';

angular.module('amanuensisApp')
  .controller('SlotCtrl', function ($scope,$routeParams,slotService) {

    // init StoryContext
   	$scope.stories = slotService.query({
    	storyId: $routeParams.storyId,
    	slotName: $routeParams.slotName
    });

    $scope.add = function() {
    	console.log("not yet implemented!");
    }

    $scope.remove = function(storyId) {
    	slotService.remove({
    		fromStoryId: $routeParams.storyId,
    		slotName: $routeParams.slotName,
    		storyId: storyId
    	});
    	console.log("story aus slot entfernt!");    	
    }

  });
