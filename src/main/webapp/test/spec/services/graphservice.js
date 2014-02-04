'use strict';

describe('Service: Graphservice', function () {

  // load the service's module
  beforeEach(module('amanuensisApp'));

  // instantiate service
  var Graphservice;
  beforeEach(inject(function (_Graphservice_) {
    Graphservice = _Graphservice_;
  }));

  it('should do something', function () {
    expect(!!Graphservice).toBe(true);
  });

});
