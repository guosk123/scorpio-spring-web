package com.machloop.iosp.sdk.subscribe;

import com.machloop.iosp.sdk.Metadata;

public interface MetadataListener {

  void consume(final Metadata[] metadatas, final ConsumeContext context);

}
