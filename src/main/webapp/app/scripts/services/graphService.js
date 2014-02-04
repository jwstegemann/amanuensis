'use strict';

angular.module('amanuensisApp')
  .factory('graphService', function($resource) {

    // Public API here
    return $resource('/paths', {}, {
      query: {
        method: 'POST',
        isArray: true
      }});

  });