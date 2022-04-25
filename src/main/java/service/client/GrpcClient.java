package service.client;

import com.service.codegen.HandshakeRequest;
import com.service.codegen.HandshakeResponse;
import com.service.codegen.HandshakeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        HandshakeServiceGrpc.HandshakeServiceBlockingStub stub
                = HandshakeServiceGrpc.newBlockingStub(channel);

        HandshakeResponse response = stub.hello(HandshakeRequest.newBuilder()
                .setFirstName("Dmitry")
                .setLastName("Grpc")
                .build());

        System.out.println(response.getGreeting());

        channel.shutdown();
    }
}