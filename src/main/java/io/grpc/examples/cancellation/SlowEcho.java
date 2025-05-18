package io.grpc.examples.cancellation;

import java.util.concurrent.FutureTask;

import io.grpc.Status;
import io.grpc.examples.echo.EchoGrpc;
import io.grpc.examples.echo.EchoRequest;
import io.grpc.examples.echo.EchoResponse;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

class SlowEcho extends EchoGrpc.EchoImplBase
{
  @Override
  public void unaryEcho(EchoRequest request, StreamObserver<EchoResponse> responseObserver)
  {
    System.out.println("\nUnary RPC started: " + request.getMessage());

    ServerCallStreamObserver<EchoResponse> serverCallStreamObserver = null;
    if(responseObserver instanceof ServerCallStreamObserver<?>)
    {
      serverCallStreamObserver = (ServerCallStreamObserver<EchoResponse>)responseObserver;
    }

    for(int i = 0; i < 10; i++)
    {
      FutureTask<Void> task = new FutureTask<>(() ->
      {
        Thread.sleep(100); // Do some work
        return null;
      });

      if(serverCallStreamObserver != null && serverCallStreamObserver.isCancelled())
      {
        task.cancel(true);
        System.out.println("Unary RPC cancelled");
        responseObserver.onError(Status.CANCELLED.withDescription("RPC cancelled").asRuntimeException());
        return;
      }

      task.run();
    }
    responseObserver.onNext(EchoResponse.newBuilder().setMessage(request.getMessage()).build());
    responseObserver.onCompleted();
    System.out.println("Unary RPC completed");
  }
}
