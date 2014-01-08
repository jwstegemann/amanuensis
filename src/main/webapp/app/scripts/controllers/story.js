'use strict';

angular.module('amanuensisApp')
  .controller('StoryCtrl', function ($scope,$routeParams,storyService,slotService,$rootScope,$location,$window) {

    // init StoryContext
    $scope.reload = function() {
        $scope.inSlots = false;
        $scope.inStories = false;
        $scope.outSlots = false;
        $scope.outStories = false;

        if (angular.isDefined($routeParams.storyId)) {
        	$scope.context = storyService.get({storyId: $routeParams.storyId});
        } 
        else {
        	// init empty StoryContext
        	$scope.context = { 
        		story: {
        			id: undefined,
        			title: 'A Story...',
        			content: '... that still has to be written.'
        		},
        		inSlots: [],
        		outSlots: []
        	};
        }
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
    	storyService.delete({storyId: $routeParams.storyId}, function(successData) {
            console.log("story gelöscht!");     
            $window.history.back();
        });
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
            }, null, function () {
                $scope.reload();
            });
        }
    }

    $scope.addStoryToSlot = function() {
        $rootScope.mode = MODE_ADD_TO_NEW_SLOT;
        $rootScope.stack = {
            storyId: $routeParams.storyId,
        };
        $location.url('/query');
    }

    //FixMe: Handle it just in the right controller!
    $scope.selectSlot = function(slotName, inbound) {
        if ($scope.activeSlot === slotName) {
            if (inbound) $scope.hideInStories();
            else $scope.hideOutStories();
            
            $scope.activeSlot = undefined;
        }
        else {
            $scope.$broadcast('selectSlot',{
                storyId: $scope.context.story.id,
                slotName: slotName
            });
            
            //ToDo: wait for some Confirmation
            if (inbound) $scope.showInStories();
            else $scope.showOutStories();
            
            $scope.activeSlot = slotName;
        }
    }

    $scope.openStory = function(storyId) {
        $location.url('/story/' + storyId);
    }

    /*
     * listening to toggleLeft and -Right events
     */

    $scope.$on('toggleLeft', function() {
        if ($scope.inSlots) $scope.hideInSlots();
        else if (angular.isDefined($scope.context.story.id)) $scope.showInSlots();
    });

    $scope.$on('toggleRight', function() {
        if ($scope.outSlots) $scope.hideOutSlots();
        else if (angular.isDefined($scope.context.story.id)) $scope.showOutSlots();
    });


    /* 
     * show and hide slots and strories
     */
    $scope.showInSlots = function() {
        $scope.inSlots = true;
        $scope.outStories = false;
    }

    $scope.hideInSlots = function() {
        $scope.inSlots = false;
        $scope.inStories = false;
    }

    $scope.showInStories = function() {
        $scope.inStories = true;
        $scope.inSlots = true;
        $scope.outStories = false;
        $scope.outSlots = false;
    }

    $scope.hideInStories = function() {
        $scope.inStories = false;
    }


    $scope.showOutSlots = function() {
        $scope.outSlots = true;
        $scope.inStories = false;
    }

    $scope.hideOutSlots = function() {
        $scope.outSlots = false;
        $scope.outStories = false;
    }

    $scope.showOutStories = function() {
        $scope.outStories = true;
        $scope.outSlots = true;
        $scope.inStories = false;
        $scope.inSlots = false;
    }

    $scope.hideOutStories = function() {
        $scope.outStories = false;
    }

    // init controller
    $scope.reload();
  });
