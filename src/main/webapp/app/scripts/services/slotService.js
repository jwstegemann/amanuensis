'use strict';

angular.module('amanuensisApp')
  .factory('slotService', function ($resource) {

    // Public API here
    return $resource('/story/:storyId/:slotName', {}, {
      list: {
        method: 'GET',
        isArray: true
      },
      ćreate: {
        method: 'POST'
      },
      add: {
        method: 'PUT',
        url: '/story/:toStoryId/:slotName/:storyId'
      },
      remove: {
        method: 'DELETE',
        url: '/story/:fromStoryId/:slotName/:storyId'
      }      
    });

  });
