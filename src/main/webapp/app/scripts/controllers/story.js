'use strict';

angular.module('amanuensisApp')
  .controller('StoryCtrl', function ($scope,$routeParams,storyService,slotService,$rootScope,$location,$window,utilService,growl) {

    // init StoryContext
    $scope.reload = function() {
        $scope.inSlots = false;
        $scope.inStories = false;
        $scope.outSlots = false;
        $scope.outStories = false;

        if (angular.isDefined($routeParams.storyId)) {
        	$scope.context = storyService.get({storyId: $routeParams.storyId}, function(successData) {
                //Todo: Adjust when opening slots and stories from routeParams
                $rootScope.appState = 1;
            });
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

            $rootScope.appState = 1;
        }
    }

    $scope.$on('saveStory', function() {
    	if (angular.isDefined($scope.context.story.id)) {
    		storyService.update($scope.context.story, function(successData) {
                growl.addSuccessMessage($scope.context.story.title + ' has been saved.');
            });
    	}
    	else {
    		storyService.create($scope.context.story, function(successData) {
    			$scope.context.story.id = successData.id;
                growl.addSuccessMessage($scope.context.story.title + ' has been created.');
    		});
    	}
    });

    $scope.$on('deleteStory', function() {
        //keep title for message
        var oldTitle = $scope.context.story.title;
    	storyService.delete({storyId: $routeParams.storyId}, function(successData) {
            growl.addSuccessMessage(oldTitle + ' has been deleted.');    
            $window.history.back();
        });
    });

    $scope.$on('addStoryToSlot', function() {
        if (angular.isUndefined($scope.context.story.id)) {
            alert("cannot add unsaved story to slot");
        }
        else {
            slotService.add({
                toStoryId: $rootScope.stack.storyId,
                slotName: $rootScope.stack.slotName,
                storyId: $scope.context.story.id
            }, null, function (successData) {
                $rootScope.selectMode = false;
                growl.addSuccessMessage($scope.context.story.title + ' has been linked to ' + $rootScope.stack.storyTitle + ' as ' + $rootScope.stack.slotName); 
                $rootScope.stack = undefined;
                $scope.reload();
            });
        }
    });    

    $scope.selectStory = function() {
        if ($scope.newSlotName.length > 2) {
            //ToDo: better check and show error-message
            $rootScope.selectMode = true;
            $rootScope.stack = {
                storyId: $scope.context.story.id,
                storyTitle: $scope.context.story.title.substr(0,25),
                slotName: $scope.newSlotName
            };
            $location.url('/query');

            utilService.hideModal('#slot-name-modal');
        }  
    }

    $scope.$on('createSlot', function() {
        if (angular.isDefined($scope.context.story.id)) {
            utilService.showModal('#slot-name-modal');
        }
        //ToDo: show error-message
    });

    $scope.cancelNewSlot = function() {
        utilService.hideModal('#slot-name-modal');    
    }

    $scope.selectSlot = function(slotName, inbound) {
        if ($scope.activeSlot === slotName) {
            if (inbound) $scope.hideInStories();
            else $scope.hideOutStories();
            
            $scope.activeSlot = undefined;
            $rootScope.appState = 2;
        }
        else {
            $scope.$broadcast('selectSlot',{
                storyId: $scope.context.story.id,
                storyTitle: $scope.context.story.title,
                slotName: slotName
            });
            
            //ToDo: wait for some Confirmation
            if (inbound) $scope.showInStories();
            else $scope.showOutStories();
            
            $scope.activeSlot = slotName;
            $rootScope.appState = 4;
        }
    }

    $scope.openStory = function(storyId) {
        $location.url('/story/' + storyId);
    }

    /*
     * listening to toggleLeft and -Right events
     */

    $scope.$on('toggleLeft', function() {
        if ($scope.inSlots) {
            $scope.hideInSlots();
            $rootScope.appState = 1;
        }
        else if (angular.isDefined($scope.context.story.id)) {
            $scope.showInSlots();
            $rootScope.appState = 2;
        }
    });

    $scope.$on('toggleRight', function() {
        if ($scope.outSlots) {
            $scope.hideOutSlots();
            $rootScope.appState = 1;
        }
        else if (angular.isDefined($scope.context.story.id)) {
            $scope.showOutSlots();
            $rootScope.appState = 2;
        }
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
