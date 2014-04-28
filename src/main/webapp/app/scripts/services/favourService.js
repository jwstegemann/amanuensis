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
      }
    });

  });