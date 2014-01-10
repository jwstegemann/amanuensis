'use strict';

angular.module('amanuensisApp', [
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngAnimate',
  'angular-growl'
]).config(function ($routeProvider) {
    $routeProvider
      .when('/story/:storyId?', {
        templateUrl: 'views/story.html',
        controller: 'StoryCtrl'
      })
      .when('/slot/:storyId/:slotName', {
        templateUrl: 'views/slot.html',
        controller: 'SlotCtrl'
      })
      .when('/query', {
        templateUrl: 'views/query.html',
        controller: 'QueryCtrl'
      })
      .otherwise({
        redirectTo: '/query'
      });

  }).config(function(growlProvider) {
    growlProvider.onlyUniqueMessages(false);
    growlProvider.globalTimeToLive(3000);

  }).config(function($httpProvider) {
    // interecpt error when communication with the backend
    $httpProvider.interceptors.push(function($q,$rootScope) {
      return {
        'responseError': function(rejection) {
          console.log("Fatal-Error communicating with the backend: " + angular.toJson(rejection));
          $rootScope.$broadcast('error',{errorMessage: 'Error communicating with Amanuensis-backend. Please try again or contact your system-administrator.'});
          return $q.reject(rejection);
        }
      };
    });

  }).run(function ($rootScope, $http) {
    //init mode and stack
    $rootScope.selectMode = false;
    $rootScope.stack = undefined;
    $rootScope.appState = undefined;
  });


