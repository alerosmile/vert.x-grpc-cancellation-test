package io.grpc.examples.cancellation;

import java.util.concurrent.CompletableFuture;

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

    CompletableFuture.runAsync(() ->
    {
      for(int i = 0; i < 20; i++)
      {
        try
        {
          Thread.sleep(100);
        }
        catch(InterruptedException e)
        {
        }

        if(responseObserver instanceof ServerCallStreamObserver<?> serverCallStreamObserver)
        {
          if(serverCallStreamObserver.isCancelled())
          {
            System.out.println("Unary RPC cancelled");
            responseObserver.onError(Status.CANCELLED.withDescription("RPC cancelled").asRuntimeException());
            return;
          }
        }
      }

      responseObserver.onNext(EchoResponse.newBuilder().setMessage(request.getMessage()).build());
      responseObserver.onCompleted();
      System.out.println("Unary RPC completed");
    });
  }
}
