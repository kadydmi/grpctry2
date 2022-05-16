package service.server;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import com.service.codegen.HandshakeRequest;
import com.service.codegen.HandshakeResponse;
import com.service.codegen.HandshakeServiceGrpc;
import com.service.codegen.StreamingHandshakeResponse;
import io.grpc.stub.StreamObserver;

public class HandshakeServiceImpl extends HandshakeServiceGrpc.HandshakeServiceImplBase {

    @Override
    public StreamObserver<HandshakeRequest> hello(StreamObserver<StreamingHandshakeResponse> responseObserver) {

        return new StreamObserver<>() {
            @Override
            public void onNext(HandshakeRequest request) {
                System.out.println(request.getFirstName() + " " + request.getLastName() + " just pinged");

                if (!request.getSecurityToken().equals("validTokenVal")) {
                    System.out.println("Unforch, Security token [" + request.getSecurityToken() + "] is invalid");
                    com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                            .setCode(Code.NOT_FOUND.getNumber())
                            .setMessage("Security token not found")
                            .addDetails(Any.pack(ErrorInfo.newBuilder()
                                    .setReason("Invalid Token")
                                    .setDomain("com.service.server")
                                    .putMetadata("insertToken", "validTokenVal")
                                    .build()))
                            .build();
                    StreamingHandshakeResponse streamingHandshakeResponse = StreamingHandshakeResponse.newBuilder()
                            .setStatus(status)
                            .build();
                    responseObserver.onNext(streamingHandshakeResponse);
                } else {
                    System.out.println("Security token was valid. Sending greetings back to " + request.getFirstName() + " " + request.getLastName());
                    String greeting = new StringBuilder()
                            .append("Hello, ")
                            .append(request.getFirstName())
                            .append(" ")
                            .append(request.getLastName())
                            .toString();

                    StreamingHandshakeResponse streamingHandshakeResponse = StreamingHandshakeResponse.newBuilder()
                            .setHandshakeResponse(HandshakeResponse.newBuilder()
                                    .setGreeting(greeting)
                                    .build())
                            .build();

                    responseObserver.onNext(streamingHandshakeResponse);
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                System.out.println("*COMPLETED");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("*ERROR:" + t.getMessage());
            }
        };


    }
}
