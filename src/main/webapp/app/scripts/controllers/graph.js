'use strict';

angular.module('amanuensisApp')
  .controller('GraphCtrl', function ($scope, graphService) {

    $scope.sourceStory = {
      id: "12345",
      title: "Testtarget"
    };   

    $scope.targetStory = {
      id: "12345",
      title: "Testtarget"
    };

  });
