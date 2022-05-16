package service.client;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import com.service.codegen.HandshakeRequest;
import com.service.codegen.HandshakeResponse;
import com.service.codegen.HandshakeServiceGrpc;
import com.service.codegen.StreamingHandshakeResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();
        System.out.println("***STARTING CLIENT***");
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        HandshakeServiceGrpc.HandshakeServiceStub stub = HandshakeServiceGrpc.newStub(channel);

        StreamObserver<StreamingHandshakeResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(StreamingHandshakeResponse streamingHandshakeResponse) {
                System.out.println("*Processing response:");
                switch (streamingHandshakeResponse.getMessageCase()) {
                    case HANDSHAKERESPONSE:
                        HandshakeResponse greeting = streamingHandshakeResponse.getHandshakeResponse();
                        System.out.println("*SERVER RESPONSE:[" + greeting.getGreeting() + "]");
                        break;
                    case STATUS:
                        com.google.rpc.Status status = streamingHandshakeResponse.getStatus();
                        System.out.println("*SERVER RESPONSE: Status error:");
                        System.out.println("-Status code:" + Code.forNumber(status.getCode()));
                        System.out.println("-Status message:" + status.getMessage());
                        for (Any any : status.getDetailsList()) {
                            if (any.is(ErrorInfo.class)) {
                                ErrorInfo errorInfo;
                                try {
                                    errorInfo = any.unpack(ErrorInfo.class);
                                    System.out.println("-Reason:" + errorInfo.getReason());
                                    System.out.println("-Domain:" + errorInfo.getDomain());
                                    System.out.println("-Server advise to insert token:" + errorInfo.getMetadataMap()
                                            .get("insertToken"));
                                } catch (InvalidProtocolBufferException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        break;
                    default:
                        System.out.println("*Unknown message case");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("*REQUEST FAILED:" + Status.fromThrowable(throwable));
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("*REQUEST COMPLETED");
                countDownLatch.countDown();
            }
        };

        StreamObserver<HandshakeRequest> requestObserver = stub.hello(responseObserver);
        try {
            System.out.println("*SENDING REQUEST");
            HandshakeRequest request = HandshakeRequest.newBuilder()
                    .setFirstName("Dmitry")
                    .setLastName("GRPC")
                    .setSecurityToken("validTokenVal")
                    .build();
            requestObserver.onNext(request);
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();
        if (!countDownLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("*Could not finish within 1 minute");
        }
        channel.shutdown();
    }
}