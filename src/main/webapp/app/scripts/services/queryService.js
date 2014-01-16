'use strict';

angular.module('amanuensisApp')
  .factory('queryService', function($resource) {

    // Public API here
    return $resource('/query/:queryString', {}, {
      query: {
        method: 'GET',
        isArray: false
      }});

  });