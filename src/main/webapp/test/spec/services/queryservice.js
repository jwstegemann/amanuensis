'use strict';

describe('Service: queryService', function () {

  // load the service's module
  beforeEach(module('amanuensisApp'));

  // instantiate service
  var queryService;
  beforeEach(inject(function (_queryService_) {
    queryService = _queryService_;
  }));

  it('should do something', function () {
    expect(!!queryService).toBe(true);
  });

});
