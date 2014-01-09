'use strict';

angular.module('amanuensisApp', [
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngAnimate'
])
  .config(function ($routeProvider) {
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

  }).run(function ($rootScope) {
    //init mode and stack
    $rootScope.selectMode = false;
    $rootScope.stack = undefined;
    $rootScope.appState = undefined;
  });
