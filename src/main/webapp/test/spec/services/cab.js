'use strict';

describe('Service: cab', function () {

  // load the service's module
  beforeEach(module('amanuensisApp'));

  // instantiate service
  var cab;
  beforeEach(inject(function (_cab_) {
    cab = _cab_;
  }));

  it('should do something', function () {
    expect(!!cab).toBe(true);
  });

});
