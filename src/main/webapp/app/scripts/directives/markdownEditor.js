'use strict';

angular.module('amanuensisApp')
.directive('markdownEditor', ["$timeout", function($timeout) {
    return {
        restrict: 'E',
        require: 'ngModel',
        scope: {
            value: '=ngModel',
            placeholder: '@',
            textareaClass: '@',
            textareaId: '@'
        },
        template: ' \
            <div> \
                <textarea id="{{textareaId}}" placeholder="{{placeholder}}" class="{{textareaClass}}" ng-model="value" \
                    uploadable fill-vertical margin="20"> \
                </textarea> \
            </div> \
            ',
        replace: true,
        controller: ["$scope","$attrs","$element", function($scope, $attrs, $element) {

            function addMarkdown(format, endFormat) {
                //TODO: get this cached
                $scope.textarea = $element.find('textarea')[0];
                $scope.textarea.focus();

                var selectionStart = $scope.textarea.selectionStart;
                var selectionEnd = $scope.textarea.selectionEnd;
                var selectionLength = selectionEnd - selectionStart;

                $scope.value = $scope.value.substr(0,selectionStart) + format + 
                    $scope.value.substr(selectionStart, selectionLength) + endFormat + 
                    $scope.value.substr(selectionEnd);

                $timeout(function() {
                    $scope.textarea.setSelectionRange(selectionStart+format.length,selectionEnd+format.length);
                }, 100);

            }

            function prependMarkdownLine(format) {
                $scope.textarea = $element.find('textarea')[0];

                var selectionStart = $scope.textarea.selectionStart;
                
                var lines = $scope.value.split("\n");

                var lineNumber = $scope.value.substr(0,$scope.textarea.selectionStart).split("\n").length - 1;
                var currentLine = lines[lineNumber];  

                var pre = lines.slice(0,lineNumber).join('\n');
                var post = lines.slice(lineNumber).join('\n');

                $scope.value = pre + '\n' + format + post;

                $timeout(function() {
                    $scope.textarea.setSelectionRange(selectionStart, selectionStart);
                }, 100);
            }

            $scope.$on('markdown_bold', function() {
                addMarkdown('__','__');
            });

            $scope.$on('markdown_italic', function() {
                addMarkdown('_','_');
            });
 
            $scope.$on('markdown_link', function() {
                addMarkdown('[',']()');
            });

            $scope.$on('markdown_header', function() {
                prependMarkdownLine('#');
            });

            $scope.$on('markdown_list-ul', function() {
                prependMarkdownLine('* ');
            });            

        }],
        link: function($scope, $element, $attrs, ngModelCtrl) {

        }
    };
}]);
