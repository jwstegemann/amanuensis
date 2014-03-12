'use strict';

var KEYS = {
    backspace: 8,
    tab: 9,
    enter: 13,
    escape: 27,
    space: 32,
    up: 38,
    down: 40,
    comma: 188
};

var typeahead = angular.module('selectbox', []);

typeahead.directive('selectbox', ["$timeout", function($timeout) {
    return {
        restrict: 'E',
        require: 'ngModel',
        scope: {
            value: '=ngModel',
            optionsList: '=options',
            placeholder: '@',
            valueSelected: '&'
        },
        template: ' \
            <div class="selectbox"> \
                <div class="value"><span class="value"><i ng-show=\"committed.icon\" class=\"fa fa-fw on-selectbox {{committed.icon}}\"></i>{{committed.label}}</span><i class="fa fa-caret-down handler" ng-click="toggleOptions()"></i></div> \
                <div class=\"options\" ng-show=\"optionsVisible\"> \
                    <ul class=\"option-list\"> \
                        <li class=\"option-item {{item.css}}\" ng-repeat=\"item in optionsList\" \
                            ng-class=\"{selected: $index === selected}\" ng-click=\"commit()\"\
                            ng-mouseenter=\"select($index)\" > \
                            <i ng-show=\"item.icon\" class=\"fa fa-fw on-selectbox {{item.icon}}\"></i>{{item.label}} \
                        </li> \
                    </ul> \
                </div> \
            </div>',
        replace: true,
        controller: ["$scope","$attrs","$element", function($scope, $attrs, $element) {

//            console.log("input-class:" + $scope.inputClass)

            $scope.reset = function() {
                $scope.optionsVisible= false;
                $scope.selected = undefined;
                $scope.committed = {label: $scope.placeholder || 'please choose...'};
            }

            $scope.select = function(index) {
                if (index < 0) {
                    index = $scope.optionsList.length - 1;
                }
                else if (index >= $scope.optionsList.length) {
                    index = 0;
                }

//                console.log("selected: " + index);
                $scope.selected = index
            }

            $scope.selectNext = function() {
                if (angular.isDefined($scope.selected)) {
                    $scope.select(++$scope.selected);
                }
                else {
                    $scope.select(0);   
                }
            }

            $scope.selectPrev = function() {
                $scope.select(--$scope.selected);
            }

            $scope.commit = function() {
                if (angular.isDefined($scope.selected) && $scope.selected < $scope.optionsList.length) {
                    $scope.committed = $scope.optionsList[$scope.selected];
                    $scope.value = $scope.committed.value;

                    if (angular.isDefined($scope.valueSelected)) $scope.valueSelected({value: $scope.optionsList[$scope.selected].value});
                }
                $scope.optionsVisible = false;
            }

            $scope.$watch('value', function(newValue) {
                if (angular.isUndefined($scope.committed) || (angular.isDefined($scope.committed) && newValue !== $scope.committed.value)) {
                    for (var i=0; i < $scope.optionsList.length; i++) {
                        if ($scope.optionsList[i].value === newValue) {
                            $scope.committed = $scope.optionsList[i];
                        }
                    }
                }
            });

            $scope.toggleOptions = function() {
                $scope.optionsVisible = !$scope.optionsVisible;
            }

            $scope.handleKey = function(evt) {
                var handled = false;
                // handle keys only when options are visible
                if ($scope.optionsVisible) {
                    if (evt.which === KEYS.down) {
//                        console.log("select next")
                        $scope.selectNext();
                        handled = true;
                    }
                    else if (evt.which === KEYS.up) {
                        $scope.selectPrev();
//                        console.log("select prev")                        
                        handled = true;
                    }
                    else if (evt.which === KEYS.enter) {
//                        console.log("enter")
                        if (angular.isDefined($scope.selected)) {
                            $scope.commit();
                        }   
                        handled = true;
                    }
                    else if (evt.which === KEYS.escape) {
//                        console.log("escape")
                        $scope.optionsVisible = false;
                        $scope.selected = undefined;
                        handled = true;
                    }
                }

                if (handled) {
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    $scope.$apply();                    
                }
            }

            $scope.reset();

        }],
        link: function($scope, $element, $attrs, ngModelCtrl) {
            $element.bind('keyup', $scope.handleKey);
        }
    };
}]);
