'use strict';

angular.module('amanuensisApp')
  .controller('CabCtrl', function ($scope,$route,$rootScope,$location,$window) {

    $scope.cabQueryString = undefined;

    $scope.select = function() {
    	$scope.$broadcast('addStoryToSlot');
    }

    $scope.cancel = function() {
    		//reset stack etc.
    		$rootScope.selectMode = false;
    		$rootScope.stack = undefined;
    }
    
    $scope.goToStack = function() {
        $location.url('/story/' + $rootScope.stack.storyId);
    }

    $scope.toggleLeft = function() {
    	$scope.$broadcast('toggleLeft');
    }

    $scope.toggleRight = function() {
    	$scope.$broadcast('toggleRight');
    }

    $scope.goBack = function() {
        $window.history.back();
    }

    $scope.saveStory = function() {
        $scope.$broadcast('saveStory');
    }

    $scope.deleteStory = function() {
        $scope.$broadcast('deleteStory');
    }

    $scope.createSlot = function() {
        $scope.$broadcast('createSlot');
    }

    $scope.addStory = function() {
        $scope.$broadcast('addStory');
    }

    $scope.createStoryInSlot = function() {
        $scope.$broadcast('createStoryInSlot');   
    }

    $scope.shareStory = function() {
        $scope.$broadcast('shareStory');   
    }

    $scope.likeStory = function() {
        $scope.$broadcast('likeStory'); 
    }

    $scope.unlikeStory = function() {
        $scope.$broadcast('unlikeStory');   
    }

    $scope.getUserContext = function() {
        $http({method: 'GET', url: '/user/login'}).
            success(function(data, status, headers, config) {
        })        
    }

    $scope.resetSearch = function() {
        $scope.cabQueryString = undefined;
    }

    $scope.search = function(jumpIfEmpty) {
        if (angular.isDefined($scope.cabQueryString) && $scope.cabQueryString.length > 0) {
            if ($rootScope.appState === 128) {
                $scope.$broadcast('search', {
                    cabQueryString: $scope.cabQueryString    
                });
            }
            else {
                $location.url('/query/' + $scope.cabQueryString);   
            }       
        }
        else if (jumpIfEmpty){
            $location.url('/query');
        }
    }

    $scope.doneEditing = function() {
        $scope.$broadcast('doneEditing');        
    }    

    $scope.logout = function() {
        $scope.$broadcast('logout');        
    }  

    $scope.findPaths = function() {
        $scope.$broadcast('selectTarget');
    }

    $scope.selectTarget = function() {
        $scope.$broadcast('findPaths');
    }

    $scope.cancelTarget = function() {
            //reset stack etc.
            $rootScope.targetMode = false;
            $rootScope.targetStack = undefined;
    }
    
    $scope.goToTargetStack = function() {
        $location.url('/story/' + $rootScope.targetStack.storyId);
    }    

    $scope.changePassword = function() {
        $scope.$broadcast('changePassword');
    }

    $scope.reloadStory = function() {
        $scope.$broadcast('reloadStory');
    }

    $scope.addAttachment = function() {
        $scope.$broadcast('addAttachment');
    }

    $scope.goHome = function() {
        $location.url('/query');
    }
});
