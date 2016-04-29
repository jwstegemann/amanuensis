'use strict';

angular.module('amanuensisApp')
  .factory('slotService', function ($resource) {

    // Public API here
    return $resource('', {}, {
      listOut: {
        method: 'GET',
        url: '/story/:storyId/out/:slotName',
        isArray: true
      },
      listIn: {
        method: 'GET',
        url: '/story/:storyId/in/:slotName',
        isArray: true
      },
      ćreate: {
        url: '/story/:storyId/out/:slotName',
        method: 'POST'
      },
      add: {
        method: 'PUT',
        url: '/story/:toStoryId/out/:slotName/:storyId'
      },
      remove: {
        method: 'DELETE',
        url: '/story/:fromStoryId/out/:slotName/:storyId'
      }      
    });

  });
