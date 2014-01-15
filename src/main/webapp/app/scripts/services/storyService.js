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
      createInSlot: {
        method: 'POST',
        url: '/story/:fromStoryId/:slotName',
        params: {}
      },      
      update: {
        method: 'PUT',
        params: {storyId: '@id'}
      }
    });

  });
