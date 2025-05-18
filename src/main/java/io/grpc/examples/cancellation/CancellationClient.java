/* Copyright 2023 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. */
package io.grpc.examples.cancellation;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.examples.echo.EchoGrpc;
import io.grpc.examples.echo.EchoRequest;
import io.grpc.examples.echo.EchoResponse;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

/**
 * A client that cancels RPCs to an Echo server.
 */
public class CancellationClient
{
  private final Channel channel;

  public CancellationClient(Channel channel)
  {
    this.channel = channel;
  }

  private void demonstrateCancellation() throws Exception
  {
    ClientCallStreamObserver<EchoRequest> reqCallObserver = echoAsync("Async client or bust!");
    try
    {
      Thread.sleep(500); // Do some work
    }
    catch(InterruptedException ex)
    {
      Thread.currentThread().interrupt();
    }
    reqCallObserver.cancel("That's enough. I'm bored", null);
  }

  public ClientCallStreamObserver<EchoRequest> echoAsync(String text)
  {
    System.out.println("\nYelling: " + text);
    EchoRequest request = EchoRequest.newBuilder().setMessage(text).build();

    AtomicReference<ClientCallStreamObserver<EchoRequest>> reqObserver = new AtomicReference<>();
    EchoGrpc.newStub(channel).unaryEcho(request, new ClientResponseObserver<EchoRequest, EchoResponse>()
    {
      @Override
      public void onNext(EchoResponse response)
      {
        System.out.println("Echo: " + response.getMessage());
      }

      @Override
      public void onCompleted()
      {
        System.out.println("RPC completed");
      }

      @Override
      public void onError(Throwable t)
      {
        System.out.println("RPC failed: " + Status.fromThrowable(t));
      }

      @Override
      public void beforeStart(ClientCallStreamObserver<EchoRequest> requestStream)
      {
        reqObserver.set(requestStream);
      }
    });

    return reqObserver.get();
  }

  public static void main(String[] args) throws Exception
  {
    String target = "localhost:50051";
    if(args.length > 0)
    {
      if("--help".equals(args[0]))
      {
        System.err.println("Usage: [target]");
        System.err.println("");
        System.err.println("  target  The server to connect to. Defaults to " + target);
        System.exit(1);
      }
      target = args[0];
    }

    ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
    try
    {
      CancellationClient client = new CancellationClient(channel);
      client.demonstrateCancellation();
    }
    finally
    {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
