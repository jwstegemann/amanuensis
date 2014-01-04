'use strict';

angular.module('amanuensisApp')
  .factory('queryService', function ($resource) {

    // Public API here
    return $resource('/query', {}, {
      findAll: {
        method: 'GET',
        isArray: true
      }});

  });