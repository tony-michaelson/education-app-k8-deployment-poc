const assert = require('assert');
function pow(x, n) {
  return x**n;
}

describe("pow", function() 

  it("raises to n-th power", function() {
    assert.equal(pow(2, 3), 8);
  });

});
