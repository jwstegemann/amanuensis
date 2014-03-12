'use strict';

angular.module('amanuensisApp', [
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngAnimate',
  'angular-growl',
  'http-auth-interceptor',
  'chieffancypants.loadingBar',
  'ngTagsInput',
  'typeahead',
  'selectbox'
]).config(function ($routeProvider) {
    $routeProvider
      .when('/story/:storyId?', {
        templateUrl: 'views/story.html',
        controller: 'StoryCtrl',
        reloadOnSearch: false
      })
      .when('/story/:slotName/:fromStoryId/:fromStoryTitle', {
        templateUrl: 'views/story.html',
        controller: 'StoryCtrl',
        reloadOnSearch: false
      })
      .when('/query/:queryString?', {
        templateUrl: 'views/query.html',
        controller: 'QueryCtrl'
      })
      .when('/login', {
        templateUrl: 'views/login.html',
        controller: 'LoginCtrl'
      })
      .when('/graph/findpaths', {
        templateUrl: 'views/findpaths.html',
        controller: 'FindPathsCtrl'
      })
      .otherwise({
        redirectTo: '/query'
      });

  }).config(function(growlProvider) {
    growlProvider.onlyUniqueMessages(false);
    growlProvider.globalTimeToLive(3000);

  }).config(function($httpProvider) {
    // interecpt error when communication with the backend
    $httpProvider.interceptors.push(['$q','$rootScope', function($q,$rootScope) {
      return {
        'responseError': function(rejection) {
          if (rejection.status !== 401) {
            if (rejection.status === 404) {
              console.log("NotFound-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: 'The object you wanted to work with could not be found.'});
            }
            else if (rejection.status === 412) {
              console.log("Validation-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: rejection.data[0].text});
            }
            else if (rejection.status === 400) {
              console.log("Bad-Request-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: rejection.data});
            }            
            else {
              console.log("Fatal-Error communicating with the backend: " + angular.toJson(rejection));
              $rootScope.$broadcast('error',{errorMessage: 'Error communicating with Amanuensis-backend. Please try again or contact your system-administrator.'});
            }
          }
          return $q.reject(rejection);
        }
      };
    }]);

  }).run(function ($rootScope, $location) {
    //init mode and stack
    $rootScope.selectMode = false;
    $rootScope.stack = undefined;
    $rootScope.appState = undefined;
    $rootScope.editMode = false;
    $rootScope.targetMode = false;    
    $rootScope.targetStack = undefined;    

    if(!(($location.host() === 'localhost' || $location.host() === '0.0.0.0') && $location.port() === 9000)) {
      if ($location.protocol() !== 'https') {
        //FixMe: Is there a way to switch the protocol?
        $rootScope.$broadcast('error',{errorMessage: 'Please use https in your URL to make sure, that nobody gets to know your credentials.'});
      }
    }

  });


