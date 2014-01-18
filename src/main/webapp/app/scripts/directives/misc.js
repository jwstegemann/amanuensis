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
  .directive('scrollbar', function() {
    return {
      restrict: 'A',
      scope: false,
      link: function(scope, elem, attr) {
        elem.mCustomScrollbar({
          autoHideScrollbar: true,
          horizontalScroll: false,
          mouseWheel: true,
          scrollButtons:{
            enable: false
          },
          advanced:{
            updateOnBrowserResize: true,
            updateOnContentResize: false,
            autoExpandHorizontalScroll: false,
            autoScrollOnFocus: false,
            normalizeMouseWheelDelta: false,
          }
          //theme: 'light-thin'
        });

          
        //console.log("attr: " + attr.refreshOnChange);

        if (attr.refreshOnChange) {
          var updateTimeout

//          scope.watchCollection(attr.refreshOnChange, function(newNames, oldNames) {
          scope.$watch(attr.refreshOnChange, function() {
//            console.log("should I?");
            if (!updateTimeout) {
              updateTimeout = setTimeout(function() {
//                console.log("update scrollbar... on " + elem.context.className);
                elem.mCustomScrollbar('update');
                updateTimeout = undefined;
              }, 50);
            }
          });
        }
      }
    } 
  })
  .directive("markdown", function () {
    return {
      restrict: 'A',
      scope: true,
      link: function (scope, element, attrs) {

        scope.$on('updateView', function(event, data) {
          if (data.markdown) {
            element.html(marked(data.markdown));
          }

          //FixMe: Do this only it it is scrollable
          element.mCustomScrollbar('update');
        });

      }
    };
  });
