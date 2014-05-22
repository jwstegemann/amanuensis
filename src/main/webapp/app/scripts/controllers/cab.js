'use strict';

angular.module('amanuensisApp')
  .controller('CabCtrl', function ($scope,$route,$rootScope,$location,$window) {

    $scope.cabQueryString = undefined;

    $scope.select = function() {
    	$scope.$broadcast('addStoryToSlot');
        $scope.toggleMenu();
    }

    $scope.cancel = function() {
    		//reset stack etc.
    		$rootScope.selectMode = false;
    		$rootScope.stack = undefined;
        $scope.toggleMenu();            
    }
    
    $scope.goToStack = function() {
        $location.url('/story/' + $rootScope.stack.storyId);
        $scope.toggleMenu();
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
        $scope.toggleMenu();
    }

    $scope.deleteStory = function() {
        $scope.$broadcast('deleteStory');
        $scope.toggleMenu();
    }

    $scope.createSlot = function() {
        $scope.$broadcast('createSlot');
        $scope.toggleMenu();
    }

    $scope.addStory = function() {
        $scope.$broadcast('addStory');
        $scope.toggleMenu();
    }

    $scope.createStoryInSlot = function() {
        $scope.$broadcast('createStoryInSlot');   
        $scope.toggleMenu();
    }

    $scope.shareStory = function() {
        $scope.$broadcast('shareStory');   
        $scope.toggleMenu();
    }

    $scope.starStory = function() {
        $scope.$broadcast('starStory'); 
        $scope.toggleMenu();
    }

    $scope.unstarStory = function() {
        $scope.$broadcast('unstarStory');   
        $scope.toggleMenu();
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

    $scope.startEditing = function() {
        $scope.$broadcast('startEditing');        
        $scope.toggleMenu();
    }  

    $scope.doneEditing = function() {
        $scope.$broadcast('doneEditing');        
        $scope.toggleMenu();
    }    

    $scope.logout = function() {
        $scope.$broadcast('logout');        
        $scope.toggleMenu();
    }  

    $scope.findPaths = function() {
        $scope.$broadcast('selectTarget');
        $scope.toggleMenu();
    }

    $scope.selectTarget = function() {
        $scope.$broadcast('findPaths');
        $scope.toggleMenu();
    }

    $scope.cancelTarget = function() {
        //reset stack etc.
        $rootScope.targetMode = false;
        $rootScope.targetStack = undefined;
        $scope.toggleMenu();
    }
    
    $scope.goToTargetStack = function() {
        $location.url('/story/' + $rootScope.targetStack.storyId);
    }    

    $scope.changePassword = function() {
        $scope.$broadcast('changePassword');
        $scope.toggleMenu();
    }

    $scope.reloadStory = function() {
        $scope.$broadcast('reloadStory');
        $scope.toggleMenu();
    }

    $scope.addAttachment = function() {
        $scope.$broadcast('addAttachment');
        $scope.toggleMenu();
    }

    $scope.controlText = function(signal, event) {
        event.stopPropagation();
        event.preventDefault();
        $scope.$broadcast('markdown_' + signal);
        $scope.toggleMenu();
    }

    $scope.goHome = function() {
        $location.url('/query');
        $scope.toggleMenu();
    }

    $scope.menuElement = $('#cab');

    $scope.toggleMenu = function() {
        $scope.menuElement.toggleClass('active-menu');
    }
});
