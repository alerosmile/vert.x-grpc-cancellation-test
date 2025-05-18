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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

public class CancellationServer
{
  public static void main(String[] args) throws IOException, InterruptedException
  {
    int port = 50051;
    Server server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
        .addService(new SlowEcho())
        .build()
        .start();
    System.out.println("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
        catch(InterruptedException e)
        {
          e.printStackTrace(System.err);
        }
      }
    });
    server.awaitTermination();
  }
}
