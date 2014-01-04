'use strict';

describe('Service: slotService', function () {

  // load the service's module
  beforeEach(module('amanuensisApp'));

  // instantiate service
  var slotService;
  beforeEach(inject(function (_slotService_) {
    slotService = _slotService_;
  }));

  it('should do something', function () {
    expect(!!slotService).toBe(true);
  });

});
