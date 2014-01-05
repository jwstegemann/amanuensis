'use strict';

angular.module('amanuensisApp')
  .controller('CabCtrl', function ($scope,$route,$rootScope) {
    $scope.isNotSelectable = function() {
    	return false;
			//return angular.isUndefined($route.current.scope.addMeToSlot);
    }

    $scope.select = function() {
    	if ($rootScope.mode === MODE_ADD_TO_SLOT) {
				$route.current.scope.addMeToSlot($rootScope.stack.storyId, $rootScope.stack.slotName);
			}
			// MODE_ADD_TO_NEW_SLOT
    	else {
				$route.current.scope.addMeToSlot($rootScope.stack.storyId, $scope.slotName);
			}

			$scope.cancel();
    }

    $scope.cancel = function() {
			//reset stack etc.
			$scope.slotName = undefined;
			$rootScope.mode = MODE_BROWSE
			$rootScope.stack = undefined;
    }

  });
