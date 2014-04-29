'use strict';

angular.module('amanuensisApp')
  .factory('graphService', function($resource) {

    // Public API here
    return $resource('/graph/findpaths/:sourceStoryId/:tagName/:targetStoryId?page=:page', {}, {
      query: {
        method: 'GET',
        isArray: true
      },
      favourites: {
        method: 'GET',
        url: '/graph/favourites?page=:page',
        isArray: true,
      }
    });

  });