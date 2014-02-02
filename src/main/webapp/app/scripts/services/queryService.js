'use strict';

angular.module('amanuensisApp')
  .factory('queryService', function($resource) {

    // Public API here
    return $resource('/query', {}, {
      query: {
        method: 'POST',
        isArray: false
      }});

  });