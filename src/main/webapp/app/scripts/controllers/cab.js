'use strict';

angular.module('amanuensisApp')
  .controller('CabCtrl', function ($scope,$route,$rootScope,$location,$window) {

    $scope.select = function() {
    	$scope.$broadcast('addStoryToSlot');
    }

    $scope.cancel = function() {
    		//reset stack etc.
    		$rootScope.selectMode = false;
    		$rootScope.stack = undefined;
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

    $scope.goToStack = function() {
        $location.url('/story/' + $rootScope.stack.storyId);
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

});
