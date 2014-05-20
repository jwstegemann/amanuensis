'use strict';

angular.module('amanuensisApp')
  .directive('onFinishRender', function($timeout) {
    return {
      restrict: 'A',
      link: function($scope, $element, $attr) {
        if ($scope.$last === true) {
          $timeout(function() {
//            console.log("updating scrollbar@");
          });
        }
      }
    }
  });