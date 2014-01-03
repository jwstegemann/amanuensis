'use strict';

angular.module('amanuensisApp')
  .controller('StoryCtrl', function ($scope,$routeParams,storyService) {

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
    			$context.story.id = data.id
    		});
    		console.log("story gespeichert!");    		
    	}
    }

    $scope.delete = function() {
    	storyService.delete({storyId: $routeParams.storyId});
    	console.log("story gelöscht!");    	
    }

  });
