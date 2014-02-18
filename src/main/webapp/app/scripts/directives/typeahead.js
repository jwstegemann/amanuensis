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

var typeahead = angular.module('typeahead', []);

/**
 * @ngdoc directive
 * @name tagsInput.directive:tagsInput
 *
 * @description
 * ngTagsInput is an Angular directive that renders an input box with tag editing support.
 *
 * @param {string} ngModel Assignable angular expression to data-bind to.
 * @param {string=} customClass CSS class to style the control.
 * @param {number=} tabindex Tab order of the control.
 * @param {string=} [placeholder=Add a tag] Placeholder text for the control.
 * @param {number=} [minLength=3] Minimum length for a new tag.
 * @param {number=} maxLength Maximum length allowed for a new tag.
 * @param {number=} minTags Sets minTags validation error key if the number of tags added is less than minTags.
 * @param {number=} maxTags Sets maxTags validation error key if the number of tags added is greater than maxTags.
 * @param {string=} [removeTagSymbol=×] Symbol character for the remove tag button.
 * @param {boolean=} [addOnEnter=true] Flag indicating that a new tag will be added on pressing the ENTER key.
 * @param {boolean=} [addOnSpace=false] Flag indicating that a new tag will be added on pressing the SPACE key.
 * @param {boolean=} [addOnComma=true] Flag indicating that a new tag will be added on pressing the COMMA key.
 * @param {boolean=} [addOnBlur=true] Flag indicating that a new tag will be added when the input field loses focus.
 * @param {boolean=} [replaceSpacesWithDashes=true] Flag indicating that spaces will be replaced with dashes.
 * @param {string=} [allowedTagsPattern=^[a-zA-Z0-9\s]+$*] Regular expression that determines whether a new tag is valid.
 * @param {boolean=} [enableEditingLastTag=false] Flag indicating that the last tag will be moved back into
 *                                                the new tag input box instead of being removed when the backspace key
 *                                                is pressed and the input box is empty.
 * @param {expression} onTagAdded Expression to evaluate upon adding a new tag. The new tag is available as $tag.
 * @param {expression} onTagRemoved Expression to evaluate upon removing an existing tag. The removed tag is available as $tag.
 */
typeahead.directive('typeahead', ["$timeout", function($timeout) {
    return {
        restrict: 'E',
        require: 'ngModel',
        scope: {
            value: '=ngModel',
            source: '&',
            inputClass: '@'
        },
        template: ' \
            <div class="typeahead"> \
                <input class=\"typeahead-input {{inputClass}}\" type=\"text\" ng-model=\"value\" ng-change=\"onChange()\"></input> \
                <div class=\"suggestions\" ng-show=\"suggestionsVisible\"> \
                    <ul class=\"suggestion-list\"> \
                        <li class=\"suggestion-item\" ng-repeat=\"item in suggestionList | limitTo:10\" \
                            ng-class=\"{selected: $index === selected}\" ng-click=\"commit()\"\
                            ng-mouseenter=\"select($index)\" ng-bind=\"item\"> \
                        </li> \
                    </ul> \
                </div> \
            </div>',
        replace: true,
        controller: ["$scope","$attrs","$element", function($scope, $attrs, $element) {

//            console.log("input-class:" + $scope.inputClass)

            var debouncedLoadId;
            var lastPromise;

            $scope.suggestionList = [];
            $scope.suggestionsVisible = false;
            $scope.selected = undefined;

            $scope.select = function(index) {
                if (index < 0) {
                    index = $scope.suggestionList.length - 1;
                }
                else if (index >= $scope.suggestionList.length) {
                    index = 0;
                }

                console.log("selected: " + index);
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
                if (angular.isDefined($scope.selected) && $scope.selected < $scope.suggestionList.length) {
                    $scope.value = $scope.suggestionList[$scope.selected]
                }
                $scope.suggestionsVisible = false;
            }

            $scope.reset = function() {
                $scope.suggestionList = [];
                $scope.suggestionsVisible = false;
                $scope.selected = undefined;

                $timeout.cancel(debouncedLoadId);
            }

            $scope.load = function(query) {
                if (query.length < 3) {
                    $scope.reset();
                    return;
                }

                $timeout.cancel(debouncedLoadId);
                debouncedLoadId = $timeout(function() {
                    self.query = query;

                    var promise = $scope.source({ $query: query });
                    lastPromise = promise;

                    promise.then(function(items) {
                        if (promise !== lastPromise) {
                            return;
                        }

                        $scope.suggestionList = items.data;
                        if ($scope.suggestionList.length > 0) {
                            $scope.suggestionsVisible = true;
                        }
                        else {
                            $scope.reset();
                        }
                    });
                }, 250, false);
            };

            $scope.onChange = function() {
                $scope.load($scope.value);
            }

            $scope.handleKey = function(evt) {
                var handled = false;
                // handle keys only when suggestions are visible
                if ($scope.suggestionsVisible) {
                    if (evt.which === KEYS.down) {
                        console.log("select next")
                        $scope.selectNext();
                        handled = true;
                    }
                    else if (evt.which === KEYS.up) {
                        $scope.selectPrev();
                        console.log("select prev")                        
                        handled = true;
                    }
                    else if (evt.which === KEYS.enter) {
                        console.log("enter")
                        if (angular.isDefined($scope.selected)) {
                            $scope.commit();
                        }   
                        handled = true;
                    }
                    else if (evt.which === KEYS.escape) {
                        console.log("escape")
                        $scope.suggestionsVisible = false;
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

        }],
        link: function($scope, $element, $attrs, ngModelCtrl) {
            $element.find('input').bind('keyup', $scope.handleKey);
        }
    };
}]);
