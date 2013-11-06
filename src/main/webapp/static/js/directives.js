'use strict';

/* Directives */


angular.module('klaraDirectives', []).directive('onKeyUp', function() {
    return function(scope, elm, attrs) {
        //Evaluate the variable that was passed
        //In this case we're just passing a variable that points
        //to a function we'll call each keyup
        var keyupFn = scope.$eval(attrs.onKeyUp);
        elm.bind('keyup', function(evt) {
            //$apply makes sure that angular knows 
            //we're changing something
            scope.$apply(function() {
                keyupFn.call(scope, evt.which);
            });
        });
    };
}).directive('onEnter', function() {
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
}).directive('entityTable', function($timeout) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: { 
            options: '=options',
            createLink: '@create',
            deleteFunction: '&delete',
            reloadFunction: '&reload',
            entities: '=entities',
            columns: '=columns',
            editLinkPrefix: '@editPrefix'
//            selectedElement: '=selectedElement',
        },
        controller: function($scope, $element, $attrs, $filter, $location) {
            $scope.selectAllValue = false;
            $scope.sortReverse = false;

            $scope.selection = {};

//            $scope.sortPredicate = "name"; //TODO: init-Wert
            $scope.colspan = $scope.columns.length + 1;

            $scope.selectAll = function() {
                if ($scope.options.selection == 'many') {
                    angular.forEach($scope.entities, function(item, index) {
                        item.selected = $scope.selectAllValue;
                    });
                }
            } 

            $scope.sort = function(attribute) {
                if (attribute == $scope.sortPredicate) {
                  $scope.sortReverse = !$scope.sortReverse;
                }
                else {
                  $scope.sortPredicate = attribute;
                  $scope.sortReverse = false;
                }
            }

            $scope.selectedItems = function() {
              if ($scope.options.selection == 'many') {
                var visibleItems = $filter('filter')($scope.entities, $scope.filterValue);
                return $filter('filter')(visibleItems, {selected: true});
              }
              else if ($scope.options.selection == 'single') {
                return [$scope.selection.element];
              }
              else {
                return [];
              }
            }

            $scope.deleteSelected = function() {
              if (!$scope.options.noDelete) {
                  var numberText = "den ausgewählten Eintrag";
                  var selectedItems = $scope.selectedItems();
                  if (selectedItems.length > 1) numberText = "die " + selectedItems.length + " ausgewählten Einträge";
                  bootbox.confirm("Sind Sie sicher, dass Sie " + numberText + " löschen möchten?", function(result) {
                    if (result) {
                        $scope.$apply(function($scope) {
                            angular.forEach(selectedItems, function(item, index) {
                                $scope.deleteFunction({id: item._id});
                            });
                        });
                    }
                  }); 
              }
            }            

            $scope.editEntity = function(id, $event) {
                if (!$scope.options.noEdit) {
                    $location.path($scope.editLinkPrefix + id);
                }
            }

            $scope.createEntity = function() {
                if (!$scope.options.noCreate) {
                    $location.path($scope.createLink);
                }
            }

            /*
             * handle resizable columns
            */
            $scope.pressed = false;
            $scope.start = undefined;
            $scope.startX = 0;
            $scope.startWidth = 0;

            // prevent sorting when resizing cols test
            $scope.resizeClick = function($event) {
                  $event.stopImmediatePropagation()
            }
                
            $scope.startResize = function($event) {
                $scope.start = angular.element($event.target).parent();

                $scope.pressed = true;
                $scope.startX = $event.pageX;
                $scope.startWidth = $scope.start.width();

                $event.preventDefault();

                $scope.start.parent().mousemove(function(e) {
                  if($scope.pressed) {
                      $($scope.start).width($scope.startWidth+(e.pageX-$scope.startX));
                      e.preventDefault();
                  }
                });
            }

            $scope.stopResize =  function($event) {
                $event.preventDefault();

                if($scope.pressed) {
                    $scope.pressed = false;
                    $scope.start.parent().off('mousemove');
                }
            }
        },
        templateUrl: "templates/entityTable.html"
    }
}).directive('onFinishRender', function () {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            console.log("done linking: " + scope.$last);
            if (scope.$last === true) {
                scope.$eval(attr.onFinishRender);
            }
        }
    }
})

/*.directive('selectAll', function() {
    return {
        restrict: 'A',
        replace: false,
        transclude: true,
        scope: { model: '=model'},
        controller: function($scope, $element, $attrs) {
            $scope.selectValue = false;

            $scope.selectAll = function() {
                angular.forEach($scope.model, function(item, index) {
                    item.selected = $scope.selectValue;
                });
            }
        },
        template: '<input type="checkbox" id="select_all" ng-model="selectValue" ng-change="selectAll()" />',
//        compile: function(element, attr, linker) {
//        },
        link: function($scope, $element, $attrs) {


        }
    }
}).directive('resizableColumns', function() {
    return {
        restrict: 'A',
        replace: false,
        transclude: false,
        compile: function($element, $attr, $linker) {
            var pressed = false;
            var start = undefined;
            var startX, startWidth;

            // prevent sorting when resizing cols
            $element.find("thead tr th .sizehandler").click(function(e) {
              e.stopImmediatePropagation()
            });
            
            $element.find("thead tr th .sizehandler").mousedown(function(e) {
                start = $(this).parent();
                pressed = true;
                startX = e.pageX;
                startWidth = start.width();

                e.preventDefault();

                $(start.parent().mousemove(function(e) {
                  if(pressed) {
                      $(start).width(startWidth+(e.pageX-startX));
                      e.preventDefault();
                  }
                }));
            });

            $(document).mouseup(function(e) {
                e.preventDefault();

                if(pressed) {
                    pressed = false;
                    start.parent().off('mousemove');
                }
            });            
        }
    }
})
*/



