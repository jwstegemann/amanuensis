'use strict';

angular.module('amanuensisApp')
  .controller('SlotCtrl', function ($scope,slotService,$rootScope,$location,$window) {

    $scope.storyListViewMode = "all"        

    // init StoryContext
    $scope.reload = function(storyId, storyTitle, slotName) {
        $scope.storyId = storyId;
        $scope.storyTitle = storyTitle;
        $scope.slotName = slotName;

       	$scope.stories = slotService.query({
        	storyId: $scope.storyId,
        	slotName: $scope.slotName
        }, function(successData) {
            if (successData.length > 4) $scope.storyListViewMode = "all"
            else if (successData.length == 1) $scope.storyListViewMode = "single"
            else $scope.storyListViewMode = "few"                
        });

    }

    $scope.$on('addStory', function() {
    	$rootScope.selectMode = true;
    	$rootScope.stack = {
    		storyId: $scope.storyId,
            storyTitle: $scope.storyTitle,
    		slotName: $scope.slotName
    	};
    	$location.url('/query');
    });

    $scope.remove = function(storyInfo, $event) {
        //stop the click-event to go further down...
        if(typeof($event) !== 'undefined') $event.stopPropagation();
    	
        slotService.remove({
    		fromStoryId: $scope.storyId,
    		slotName: $scope.slotName,
    		storyId: storyInfo.id
    	}, function (successData) {
            var index = $scope.stories.indexOf(storyInfo);
            if (index >= 0) {
                $scope.stories.splice(index,1);
            }
//	    	console.log("story aus slot entfernt!");    	
    	});
    }

    $scope.goBack = function() {
        $window.history.back();
    }

    $scope.$on("selectSlot", function(event, params) {
        if (params.inbound === $scope.inbound) {
            $scope.reload(params.storyId, params.storyTitle, params.slotName);
        }
    });

    $scope.hideInStories = function() {
        $scope.$emit("hideInStories");
    }

    $scope.hideInSlots = function() {
        $scope.$emit("hideInSlots");
    }

    $scope.hideOutStories = function() {
        $scope.$emit("hideOutStories");
    }

    $scope.hideOutSlots = function() {
        $scope.$emit("hideOutSlots");
    }

  });
