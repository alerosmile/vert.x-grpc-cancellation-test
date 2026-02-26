package io.grpc.examples.cancellation;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServiceBridge;

public class CancellationServerVertx extends AbstractVerticle
{
  private static final int PORT = 50051;

  public static void main(String[] args)
  {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new CancellationServerVertx());
    System.out.println("Server started, listening on " + PORT);
  }

  @Override
  public void start()
  {
    GrpcServer grpcServer = GrpcServer.server(vertx);

    SlowEcho service = new SlowEcho();

    GrpcServiceBridge.bridge(service).bind(grpcServer);

    vertx
        .createHttpServer()
        .requestHandler(grpcServer)
        .listen(PORT);
  }
}
