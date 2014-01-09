'use strict';

angular.module('amanuensisApp')
  .controller('SlotCtrl', function ($scope,slotService,$rootScope,$location,$window) {

    // init StoryContext
    $scope.reload = function(storyId, slotName) {
        $scope.storyId = storyId;
        $scope.slotName = slotName;

       	$scope.stories = slotService.query({
        	storyId: $scope.storyId,
        	slotName: $scope.slotName
        });
    }

    $scope.add = function() {
    	$rootScope.mode = MODE_ADD_TO_SLOT;
    	$rootScope.stack = {
    		storyId: $scope.storyId,
    		slotName: $scope.slotName
    	};
    	$location.url('/query');
    }

    $scope.remove = function(storyId, index, $event) {
        //stop the click-event to go further down...
        if(typeof($event) !== 'undefined') $event.stopPropagation();
    	
        slotService.remove({
    		fromStoryId: $scope.storyId,
    		slotName: $scope.slotName,
    		storyId: storyId
    	}, function (successData) {
    		$scope.stories.splice(index,1);
	    	console.log("story aus slot entfernt!");    	
    	});
    }

    $scope.goBack = function() {
        $window.history.back();
    }

    $scope.$on("selectSlot", function(event, params) {
        $scope.reload(params.storyId, params.slotName);
    });

  });
