'use strict';

angular.module('amanuensisApp')
  .factory('queryService', function($resource, $q) {

    // Public API here
    return $resource('/query', {}, {
      query: {
        method: 'POST',
        isArray: false
      },
      suggestTags: {
        url: '/query/suggest/tags/:text',
        method: 'GET',
        isArray: true,
        transformResponse: function(data) {
          var list = angular.fromJson(data).suggest[0].options.map(function(value) {
            return value.text
          });  
          return $q.when(list);
        }
      }
    });

  });