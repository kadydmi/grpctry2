package service.server;

import com.service.codegen.HandshakeRequest;
import com.service.codegen.HandshakeResponse;
import com.service.codegen.HandshakeServiceGrpc;
import io.grpc.stub.StreamObserver;

public class HandshakeServiceImpl extends HandshakeServiceGrpc.HandshakeServiceImplBase {

    @Override
    public void hello(
            HandshakeRequest request, StreamObserver<HandshakeResponse> responseObserver) {

        System.out.println(request.getFirstName() + " " + request.getLastName() + " just pinged you");

        String greeting = new StringBuilder()
                .append("Hello, ")
                .append(request.getFirstName())
                .append(" ")
                .append(request.getLastName())
                .toString();

        HandshakeResponse response = HandshakeResponse.newBuilder()
                .setGreeting(greeting)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
