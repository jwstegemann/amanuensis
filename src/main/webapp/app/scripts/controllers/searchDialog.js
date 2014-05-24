'use strict';

angular.module('amanuensisApp')
  .controller('SearchDialogCtrl', function ($scope,$location,utilService) {
    
    $scope.doSearch = function() {
        if (angular.isDefined($scope.query) && $scope.query.length > 0) {
            $location.url('/query/' + $scope.query);
            utilService.hideModal('#search-modal');
        }
    }

    $scope.cancel = function() {
        utilService.hideModal('#search-modal');
    }

});
