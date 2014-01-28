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
          autoHideScrollbar: false,
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
            setTimeout(function() {
              $("#story-content").mCustomScrollbar('update');
            }, 100);
          }

        });

      }
    };
  })
  .directive("uploadable", function ($rootScope,$http) {
    return {
      restrict: 'A',
      scope: true,
      link: function(scope, elem, attrs) {

        var lastValue;
        var editor = elem.context;

        scope.uploadFile = function(file, isImage) {
          var formData = new FormData(),
            filename = "file-" + Date.now();

          if (file.name) {
            filename = file.name
          }

          formData.append('file', file, filename);

          $http.post('/attachment/' + scope.context.story.id, formData, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
          }).success(function(data) {
            scope.onUploadedFile(data, isImage, filename);
          }).error(function() {
            scope.onErrorUploading();
          });

        }

        scope.onUploadedFile = function(data, isImage, linkText) {
          var filename = data['filename'];
          if (filename) {
            var text = editor.value.replace(lastValue, "[" + linkText + "](" + filename + ")");
            editor.value = text;

            //ToDo: Update scope with new editor-value
          }
        };

        scope.onErrorUploading = function() {
          var text = editor.value.replace(lastValue, "");
          editor.value = text;
          $rootScope.$broadcast('error',{errorMessage: 'An error occured uploading your file.'});
        };

        //helper functions
        function appendInItsOwnLine(previous, appended) {
          return (previous + "\n\n[[D]]" + appended)
            .replace(/(\n{2,})\[\[D\]\]/, "\n\n")
            .replace(/^(\n*)/, "");
        }

        function isAnImage(contentType) {
          if (contentType === 'image/jpeg'
            || contentType === 'image/png'
            || contentType === 'image/gif'
            ) return true;
          else return false;
        }

        function imagePrefix(isImage) {
          if (isImage) return "!";
          else return "";
        }

        scope.insertProgressText = function(isImage) {
              lastValue = '[Uploading file...]()'
              editor.value = appendInItsOwnLine(editor.value, imagePrefix(isImage) + lastValue);
        }

        scope.isUploadPossible = function() {
          if (angular.isDefined(scope.context.story.id) && scope.context.story.id !== "") {
            return true;
          }
          else {
            $rootScope.$broadcast('error',{errorMessage: 'Sorry, but you cannot upload attachments to an unsaved story.'});
            return false;
          }
        }

        //define event listeners
        scope.onPaste = function(e) {
          var clipboardData = e.clipboardData;

          if (typeof clipboardData === "object" && clipboardData.items !== null) {
            for (var i = 0; i < clipboardData.items.length; i++) {
              var item = clipboardData.items[i];
              //ToDo: check if file is allowes
              var file = item.getAsFile();
              if (file !== null && scope.isUploadPossible()) {
                var isImage = isAnImage(file.type)
                scope.insertProgressText(isImage);
                scope.uploadFile(file, isImage);
              }
            }
          }

          return true;
        }

        scope.onDrop = function(e) {
          var result = false;
          for (var i = 0; i < e.dataTransfer.files.length; i++) {
            var file = e.dataTransfer.files[i];
            //ToDo: check if file is allowes
            if (scope.isUploadPossible()) {
              var isImage = isAnImage(file.type)
              scope.insertProgressText(isImage);
              scope.uploadFile(file, isImage);
            }
          }

          return true;
        }

        //add event listeners
        editor.addEventListener('paste', function(e) {
            scope.onPaste(e);
        }, false);
        editor.addEventListener('drop', function(e) {
            e.stopPropagation();
            e.preventDefault();
            scope.onDrop(e);
        }, false);
        editor.addEventListener('dragenter', function(e) {
            e.stopPropagation();
            e.preventDefault();
        }, false);
        editor.addEventListener('dragover', function(e) {
            e.stopPropagation();
            e.preventDefault();
        }, false);        


      }
    }
  });