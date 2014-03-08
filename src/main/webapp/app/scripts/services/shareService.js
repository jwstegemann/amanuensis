'use strict';

angular.module('amanuensisApp')
  .factory('shareService', function ($resource) {

    // Public API here
    return $resource('/share/:storyId', {}, {
      share: {
        method: 'POST',
        url: '/share/:storyId/:rights/:user',
        params: {}
      },
      unshare: {
        method: 'DELETE',
        url: '/share/:storyId/:user',
        params: {}
      },
      listShares: {
        method: 'GET',
        isArray: true
      }
    });

  });
