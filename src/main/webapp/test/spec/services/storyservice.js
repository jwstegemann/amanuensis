'use strict';

describe('Service: storyService', function () {

  // load the service's module
  beforeEach(module('amanuensisApp'));

  // instantiate service
  var storyService;
  beforeEach(inject(function (_storyService_) {
    storyService = _storyService_;
  }));

  it('should do something', function () {
    expect(!!storyService).toBe(true);
  });

});
