'use strict';

describe('Directive: errorDialog', function () {

  // load the directive's module
  beforeEach(module('amanuensisApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<error-dialog></error-dialog>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the errorDialog directive');
  }));
});
