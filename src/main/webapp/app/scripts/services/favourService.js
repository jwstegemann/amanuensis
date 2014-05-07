'use strict';

angular.module('amanuensisApp')
  .factory('favourService', function ($resource) {

    // Public API here
    return $resource('/star/:storyId', {}, {
      star: {
        method: 'POST'
      },
      unstar: {
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