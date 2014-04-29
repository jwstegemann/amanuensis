'use strict';

angular.module('amanuensisApp')
  .factory('queryService', function($resource, $q) {

    // Public API here
    return $resource('/query/fulltext', {}, {
      query: {
        method: 'POST',
        isArray: false
      },
      myLatest: {
        method: 'POST',
        url: '/query/mylatest',
        isArray: false
      },
      othersLatest: {
        method: 'POST',
        url: '/query/otherslatest',
        isArray: false
      },
      toDos: {
        method: 'POST',
        url: '/query/todos',
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