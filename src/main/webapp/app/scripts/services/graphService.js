'use strict';

angular.module('amanuensisApp')
  .factory('graphService', function($resource) {

    // Public API here
    return $resource('/graph/findpaths/:sourceStoryId/:tagName/:targetStoryId', {}, {
      query: {
        method: 'GET',
        isArray: true
      }});

  });