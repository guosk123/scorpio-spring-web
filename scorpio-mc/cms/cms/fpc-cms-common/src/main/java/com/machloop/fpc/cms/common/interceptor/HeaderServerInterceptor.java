package com.machloop.fpc.cms.common.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class HeaderServerInterceptor implements ServerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderServerInterceptor.class);

  @VisibleForTesting
  static final Metadata.Key<String> CUSTOM_SERVER_HEADER_KEY = Metadata.Key
      .of("custom_server_header_key", Metadata.ASCII_STRING_MARSHALLER);

  @VisibleForTesting
  static final Metadata.Key<String> CUSTOM_CLIENT_HEADER_KEY = Metadata.Key
      .of("custom_client_header_key", Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
      ServerCallHandler<ReqT, RespT> next) {

    LOGGER.trace("this is a serverInterceptor, headers is {}.", headers);
    // if the authentication fails by closing the connection
    // call.close(Status.UNAUTHENTICATED, headers);
    return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {

      @Override
      public void close(Status status, Metadata trailers) {
        super.close(status, trailers);
      }

    }, headers);
  }

}
