'use strict';

angular.module('amanuensisApp')
  .controller('QueryCtrl', function ($scope,$routeParams,queryService) {

    // init StoryContext
   	$scope.stories = queryService.findAll();

  });