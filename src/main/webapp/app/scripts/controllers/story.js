'use strict';

angular.module('amanuensisApp')
  .controller('StoryCtrl', function ($scope,$routeParams,storyService,slotService,$rootScope,$location) {

    // init StoryContext
    if (angular.isDefined($routeParams.storyId)) {
    	$scope.context = storyService.get({storyId: $routeParams.storyId});
    } 
    else {
    	// init empty StoryContext
    	$scope.context = { 
    		story: {
    			id: undefined,
    			title: '',
    			content: ''
    		},
    		inSlots: [],
    		outSlots: []
    	};
    }

    $scope.save = function() {
    	if (angular.isDefined($scope.context.story.id)) {
    		storyService.update($scope.context.story);
    		console.log("story gespeichert!");
    	}
    	else {
    		storyService.create($scope.context.story, function(data) {
    			$scope.context.story.id = data.id;
    		});
    		console.log("story gespeichert!");    		
    	}
    }

    $scope.delete = function() {
    	storyService.delete({storyId: $routeParams.storyId});
    	console.log("story gelöscht!");    	
    }

    $scope.addMeToSlot = function(toStoryId, slotName) {
        if (angular.isUndefined($scope.context.story.id)) {
            alert("cannot add unsaved story to slot");
        }
        else {
            slotService.add({
                toStoryId: toStoryId,
                slotName: slotName,
                storyId: $scope.context.story.id
            }, null);
        }
    }

    $scope.addStoryToSlot = function() {
        $rootScope.mode = MODE_ADD_TO_NEW_SLOT;
        $rootScope.stack = {
            storyId: $routeParams.storyId,
        };
        $location.url('/query');
    }

  });
