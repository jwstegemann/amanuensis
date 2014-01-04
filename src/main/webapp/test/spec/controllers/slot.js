'use strict';

describe('Controller: SlotCtrl', function () {

  // load the controller's module
  beforeEach(module('amanuensisApp'));

  var SlotCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    SlotCtrl = $controller('SlotCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
