'use strict';

/* global constants */

var MODE_BROWSE = 0;
var MODE_ADD_TO_SLOT = 1;
var MODE_ADD_TO_NEW_SLOT = 2;


angular.module('amanuensisApp', [
  'ngResource',
  'ngSanitize',
  'ngRoute'
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
    $rootScope.mode = MODE_BROWSE;
    $rootScope.stack = undefined;
  });
