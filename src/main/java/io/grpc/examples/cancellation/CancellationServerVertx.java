package io.grpc.examples.cancellation;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.grpcio.server.GrpcIoServer;
import io.vertx.launcher.application.VertxApplication;

public class CancellationServerVertx extends VerticleBase
{
  private static final int PORT = 50051;

  public static void main(String[] args)
  {
    VertxApplication.main(new String[] { CancellationServerVertx.class.getName() });
    System.out.println("Server started, listening on " + PORT);
  }

  @Override
  public Future<?> start()
  {
    GrpcIoServer grpcServer = GrpcIoServer.server(vertx);

    SlowEcho service = new SlowEcho();

    grpcServer.addService(service);

    return vertx
        .createHttpServer()
        .requestHandler(grpcServer)
        .listen(PORT);
  }
}
