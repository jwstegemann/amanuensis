'use strict';

/* App Module */

var requests = 0;
var loaderTimeout;

function showBusy() {
  if (requests++ == 0) {
    loaderTimeout = setTimeout("$('#busy').show()",300);
  }
}

function hideBusy() {
  if (--requests <= 0) {
    requests = 0;
    clearTimeout(loaderTimeout);
    $("#busy").hide();
  }
}

angular.module('klara', ['klaraFilters','klaraDirectives','http-auth-interceptor','$strap.directives']).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/schueler', {templateUrl: 'partials/schuelerList.html', controller: SchuelerListCtrl}).
      when('/schueler/create', {templateUrl: 'partials/schuelerDetail.html', controller: SchuelerDetailCtrl}).
      when('/schueler/detail/:schuelerId', {templateUrl: 'partials/schuelerDetail.html', controller: SchuelerDetailCtrl}).
      when('/schule', {templateUrl: 'partials/schuleList.html', controller: SchuleListCtrl}).
      when('/schule/create', {templateUrl: 'partials/schuleDetail.html', controller: SchuleDetailCtrl}).
      when('/schule/detail/:schuleId', {templateUrl: 'partials/schuleDetail.html', controller: SchuleDetailCtrl}).
      otherwise({redirectTo: '/schueler'});
  }]).config(function ($httpProvider) {
      $httpProvider.responseInterceptors.push('msgHttpInterceptor');
      var spinnerFunction = function (data, headersGetter) {
          showBusy();
          return data;
      };
      $httpProvider.defaults.transformRequest.push(spinnerFunction);
  }).run(['$rootScope', function($rootScope) {
  
       var login = $('#login-holder');

       login.modal({
          keyboard: false,
          backdrop: "static",
          show: false
       });

       login.on('shown', function () {
          $("#loginUsername").focus();
       })

       // make loginButton submit the form
       $('#loginButton').click(function(e) {
         e.preventDefault();
         $("#loginForm").submit();
       });

       $rootScope.$on('event:auth-loginRequired', function() {
          login.modal('show');
        });
        $rootScope.$on('event:auth-loginConfirmed', function() {
          login.modal('hide');
        });
  }]).factory('dictionary', function ($http, $q) {
    var dictionaryService = {};
    
    dictionaryService.cache = {};

    dictionaryService.reload = function(name) {
      dictionaryService.cache[name] = $http.get("/dict/" + name).then(function(response) {
//        console.log("mapping %o", response.data);
        var dict = {};
        angular.forEach(response.data, function(keyValue, index) {
          dict[keyValue.key] = keyValue.shortText;
        });
        return dict;
      });
    }

    dictionaryService.get = function(name) {
      if (dictionaryService.cache[name] == undefined) {
        dictionaryService.reload(name);
      }
      return (dictionaryService.cache[name]);
    };

    return dictionaryService; 
  }).factory('message', function() {
    var messageService = {};
    var stack_bottomright = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};
    
    messageService.notice = function(title, text) {
      $.pnotify({
        title: title,
        text: text,
        addclass: "stack-bottomright",
        stack: stack_bottomright
      });
    }

    messageService.info = function(title, text) {
      $.pnotify({
        title: title,
        text: text,
        type: 'info',
        addclass: "stack-bottomright",
        stack: stack_bottomright
      });
    }

    messageService.success = function(title, text) {
      $.pnotify({
        title: title,
        text: text,
        type: 'success',
        addclass: "stack-bottomright",
        stack: stack_bottomright
      });
    }

    messageService.error = function(error, status) {
      var details = "";
      if (angular.isDefined(error.details)) details = error.details;
      $.pnotify({
        title: error.text,
        text: details + " (" + status + ")",
        type: 'error',
        addclass: "stack-bottomright",
        stack: stack_bottomright
      });
      console.log("Fehler:" + angular.toJson(error, true));
    }

    messageService.fatal = function(text, status) {
      $.pnotify({
        title: "Technischer Fehler",
        text: text + " (" + status + ").<br>Bitte wenden Sie sich an Ihren Systemadministrator.",
        type: 'error',
        addclass: "stack-bottomright",
        stack: stack_bottomright
      });
      console.log("Technischer Fehler:" + text + " (" + status + ")");
    }

    return messageService; 
  }).factory('msgHttpInterceptor', function ($q, $window, message) {
    return function (promise) {
      return promise.then(function (response) {
        hideBusy();
        return response;
      }, function (response) {
        var contentType = response.headers("Content-Type");
        if (angular.isDefined(contentType) && (angular.lowercase(contentType).indexOf("application/json") != -1)) {
          if (angular.isDefined(response.data) && angular.isArray(response.data)) {
            angular.forEach(response.data, function(msg, index) {
              switch(msg.severity) {
                case "DEBUG": message.notice(msg.text, msg.details); break;
                case "INFO": message.notice(msg.text, msg.details); break;
                case "WARN": message.notice("Achtung:" + msg.text, msg.details); break;
                case "FATAL": message.error(msg, response.status); break;
                default: message.error(msg, response.status); break;
              }
              
            });
          }
        } else {
          message.fatal("in der Kommunikation mit dem Server: " + response.data, response.status);
        }
       
        hideBusy();
        return $q.reject(response);
      });
    };
  });;
