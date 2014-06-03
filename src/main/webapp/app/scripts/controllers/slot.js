'use strict';

function BaseSlotCtrl($scope,slotService,$rootScope,$location,$window) {

    $scope.storyListViewMode = 'all';
    $scope.sortOrder = 'created';  
    $scope.reverseOrder = false;

    // init StoryContext
    $scope.reload = function(storyId, storyTitle, slotName) {
        $scope.storyId = storyId;
        $scope.storyTitle = storyTitle;
        $scope.slotName = slotName;

        $scope.stories = $scope.retrieveMethod({
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
    //          console.log("story aus slot entfernt!");        
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

    function closeSortList(element) {
        element.hide();
    }

    $scope.changeStorySort = function(element, $event) {
        var e = $(element);
        e.show();
        $event.stopPropagation();
        $(document).one('click', function() {
            closeSortList(e);
        });
    }    

    $scope.setStorySort = function(order, element) {
        $scope.sortOrder = order;
        closeSortList($(element));
    }      

    $scope.reverseSort = function() {
        $scope.reverseOrder = !$scope.reverseOrder;
    }

}


angular.module('amanuensisApp')
  .controller('OutboundSlotCtrl', function($scope,slotService,$rootScope,$location,$window) {
    BaseSlotCtrl($scope,slotService,$rootScope,$location,$window);

    $scope.ctype = 'outbound';
    $scope.retrieveMethod = slotService.listOut;

  });


angular.module('amanuensisApp')
  .controller('InboundSlotCtrl', function($scope,slotService,$rootScope,$location,$window) {
    BaseSlotCtrl($scope,slotService,$rootScope,$location,$window);

    $scope.ctype = 'inbound';
    $scope.retrieveMethod = slotService.listIn;

  });

