'use strict';

angular.module('amanuensisApp')
  .directive("errorDialog", function ($rootScope) {
        "use strict";

        return {
                restrict: 'A',
                template:
                  '<div class="md-modal md-effect-3" id="slot-name-modal">'+
                    '<div class="md-content error">'+
                      '<h3>A fatal error occured: <i style="float: right" class="fa fa-times action" ng-click="cancelErrorMessage()"></i></h3>'+
                      '<div class="error-icon">'+
                      ' <i class="fa fa-exclamation fa-3x fa-fw"></i>'+
                      '</div>'+
                      '<div class="error-message">{{errorMessage}}</div>'+
                    '</div>'+
                  '</div>',
                replace: true,
                scope: true,
                controller: ['$scope', '$element', 'utilService', '$location', '$timeout', function ($scope, $element, utilService, $location, $timeout) {

                        $scope.$on("error", function (event, param) {
                          if (angular.isDefined(param.errorMessage)) {
                            $scope.errorMessage = param.errorMessage;
                          }
                          else {
                            $scope.errorMessage = 'This should not have happened. Please inform your system-administrator.';
                          }

                          utilService.showModalElement($element);
                        });

                        $scope.cancelErrorMessage = function (message) {
                          $location.url("/query");
                          setTimeout(function(){
                            utilService.hideModalElement($element);  
                          }, 500);
                          
                        };

                }]
        };
});
