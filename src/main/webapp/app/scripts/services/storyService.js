'use strict';

angular.module('amanuensisApp')
  .factory('storyService', function ($resource) {

    // Public API here
    return $resource('/story/:storyId', {}, {
      create: {
        method: 'POST',
        url: '/story',
        params: {}
      },
      update: {
        method: 'PUT',
        params: {storyId: '@id'}
      }
    });

  });
