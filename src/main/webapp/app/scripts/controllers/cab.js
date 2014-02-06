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

    $scope.getUserContext = function() {
        $http({method: 'GET', url: '/user/login'}).
            success(function(data, status, headers, config) {
        })        
    }

    $scope.search = function() {
        if ($rootScope.appState === 128) {
            $scope.$broadcast('search', {
                cabQueryString: $scope.cabQueryString    
            });
        }
        else {
            if (angular.isDefined($scope.cabQueryString) && $scope.cabQueryString.length > 0) {
                $location.url('/query/' + $scope.cabQueryString);
            }
            else {
                $location.url('/query');
            }
        }
    }

    $scope.search = function() {
        if ($rootScope.appState === 128) {
            $scope.$broadcast('search', {
                cabQueryString: $scope.cabQueryString    
            });
        }
        else {
            if (angular.isDefined($scope.cabQueryString) && $scope.cabQueryString.length > 0) {
                $location.url('/query/' + $scope.cabQueryString);
            }
            else {
                $location.url('/query');
            }
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

});
