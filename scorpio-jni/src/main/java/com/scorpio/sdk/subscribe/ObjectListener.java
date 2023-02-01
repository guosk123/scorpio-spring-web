package com.machloop.iosp.sdk.subscribe;

import com.machloop.iosp.sdk.FullObject;

public interface ObjectListener {

  void consume(final FullObject[] objects, final ConsumeContext context);

}
