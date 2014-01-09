'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService,$rootScope) {

  	$scope.reload = function() {
    	// init StoryContext
   		$scope.stories = queryService.findAll({}, function(successData) {
   			$rootScope.appState = 128;
   		});
    }

    // init controller
    $scope.reload();

  });