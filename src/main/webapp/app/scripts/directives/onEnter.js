'use strict';

angular.module('amanuensisApp')
  .directive('onEnter', function() {
    return function(scope, elm, attrs) {
        function applyKeyup() {
          scope.$apply(attrs.onEnter);
        };           
        
        var allowedKeys = scope.$eval(attrs.keys);
        elm.bind('keyup', function(evt) {
            //if no key restriction specified, always fire
            if (evt.which == 13) {
                applyKeyup();
            } 
        });
    };
  })
  .directive('onEscape', function() {
    return function(scope, elm, attrs) {
        function applyKeyup() {
          scope.$apply(attrs.onEnter);
        };           
        
        var allowedKeys = scope.$eval(attrs.keys);
        elm.bind('keyup', function(evt) {
            //if no key restriction specified, always fire
            if (evt.which == 27) {
                applyKeyup();
            } 
        });
    };
  })
  .directive('scrollbar', function($parse) {
    return {
      restrict: 'E',
      transclude: true,
      template:  '<div diesisteintest=17><div ng-transclude></div></div>',
      replace: true,
      link: function($scope, $elem, $attr) {
        $elem.perfectScrollbar({
          wheelSpeed: $parse($attr.wheelSpeed)() || 50,
          wheelPropagation: $parse($attr.wheelPropagation)() || false,
          minScrollbarLength: $parse($attr.minScrollbarLength)() || false,
        });

        if ($attr.refreshOnChange) {
          $scope.$watchCollection($attr.refreshOnChange, function(newNames, oldNames) {
            // I'm not crazy about setting timeouts but it sounds like thie is unavoidable per
            // http://stackoverflow.com/questions/11125078/is-there-a-post-render-callback-for-angular-js-directive
            setTimeout(function() { $elem.perfectScrollbar('update'); }, 10);
          });
        }
      }
    } 
  }); 