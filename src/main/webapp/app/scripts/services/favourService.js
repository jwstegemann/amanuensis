'use strict';

angular.module('amanuensisApp')
  .factory('favourService', function ($resource) {

    // Public API here
    return $resource('/like/:storyId', {}, {
      like: {
        method: 'POST'
      },
      unlike: {
        method: 'DELETE'
      },
      due: {
        url: '/due/:storyId?date=:date',
        method: 'POST'
      },
      undue: {
        url: '/due/:storyId',
        method: 'DELETE'
      }      
    });

  });