
package com.example.productservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfiguration {

    @Value("${grpc.server.port:9091}")
    private int grpcPort;

    @Bean
    public Server grpcServer(ProductGrpcService productGrpcService)
            throws IOException {
        Server server = ServerBuilder.forPort(grpcPort)
                .addService(productGrpcService)
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        return server;
    }
}
