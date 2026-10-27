
package com.example.productservice.grpc;

import com.assignment.protos.product.v1.CreateProductRequest;
import com.assignment.protos.product.v1.GetProductRequest;
import com.assignment.protos.product.v1.ListProductsRequest;
import com.assignment.protos.product.v1.ListProductsResponse;
import com.assignment.protos.product.v1.ProductResponse;
import com.assignment.protos.product.v1.ProductServiceGrpc;
import com.assignment.protos.product.v1.ReleaseStockRequest;
import com.assignment.protos.product.v1.ReserveStockRequest;
import com.assignment.protos.product.v1.StockOperationResponse;
import com.assignment.protos.product.v1.UpdateStockRequest;
import com.example.productservice.entity.ProductEntity;
import com.example.productservice.exception.InsufficientStockException;
import com.example.productservice.exception.InvalidStockQuantityException;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.service.ProductService;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    public ProductGrpcService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void createProduct(CreateProductRequest request,
                              StreamObserver<ProductResponse> responseObserver) {
        execute(responseObserver, () -> {
            com.example.productservice.model.CreateProductRequest restRequest =
                    new com.example.productservice.model.CreateProductRequest();

            restRequest.setName(request.getName());
            restRequest.setDescription(request.getDescription());
            restRequest.setPrice(new BigDecimal(request.getPrice()));
            restRequest.setCurrency(request.getCurrency());
            restRequest.setStockQuantity(request.getStockQuantity());

            return toProductResponse(productService.createProduct(restRequest));
        });
    }

    @Override
    public void getProduct(GetProductRequest request,
                           StreamObserver<ProductResponse> responseObserver) {
        execute(responseObserver, () ->
                toProductResponse(
                        productService.getProductById(request.getProductId())));
    }

    @Override
    public void listProducts(ListProductsRequest request,
                             StreamObserver<ListProductsResponse> responseObserver) {
        execute(responseObserver, () -> {
            List<ProductResponse> products = productService.getAllProducts()
                    .stream()
                    .map(this::toProductResponse)
                    .toList();

            return ListProductsResponse.newBuilder()
                    .addAllProducts(products)
                    .build();
        });
    }

    @Override
    public void updateStock(UpdateStockRequest request,
                            StreamObserver<ProductResponse> responseObserver) {
        execute(responseObserver, () ->
                toProductResponse(
                        productService.updateStock(
                                request.getProductId(),
                                request.getStockQuantity())));
    }

    @Override
    public void reserveStock(ReserveStockRequest request,
                             StreamObserver<StockOperationResponse> responseObserver) {
        execute(responseObserver, () -> {
            ProductEntity product = productService.reserveStock(
                    request.getProductId(), request.getQuantity());

            return StockOperationResponse.newBuilder()
                    .setProductId(product.getId())
                    .setStockQuantity(product.getStockQuantity())
                    .build();
        });
    }

    @Override
    public void releaseStock(ReleaseStockRequest request,
                             StreamObserver<StockOperationResponse> responseObserver) {
        execute(responseObserver, () -> {
            ProductEntity product = productService.releaseStock(
                    request.getProductId(), request.getQuantity());

            return StockOperationResponse.newBuilder()
                    .setProductId(product.getId())
                    .setStockQuantity(product.getStockQuantity())
                    .build();
        });
    }

    private ProductResponse toProductResponse(ProductEntity product) {
        return ProductResponse.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(
                        product.getDescription() == null
                                ? ""
                                : product.getDescription())
                .setPrice(product.getPrice().toPlainString())
                .setCurrency(product.getCurrency())
                .setStockQuantity(product.getStockQuantity())
                .setCreatedAt(toTimestamp(product.getCreatedAt()))
                .setUpdatedAt(toTimestamp(product.getUpdatedAt()))
                .build();
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(dateTime.getNano())
                .build();
    }

    private <T> void execute(StreamObserver<T> observer,
                             GrpcOperation<T> operation) {
        try {
            observer.onNext(operation.run());
            observer.onCompleted();
        } catch (Exception exception) {
            observer.onError(
                    toGrpcStatus(exception).asRuntimeException());
        }
    }

    private Status toGrpcStatus(Exception exception) {
        if (exception instanceof ProductNotFoundException) {
            return Status.NOT_FOUND
                    .withDescription(exception.getMessage());
        }

        if (exception instanceof InsufficientStockException) {
            return Status.FAILED_PRECONDITION
                    .withDescription(exception.getMessage());
        }

        if (exception instanceof InvalidStockQuantityException
                || exception instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(exception.getMessage());
        }

        if (exception instanceof DataIntegrityViolationException) {
            return Status.ALREADY_EXISTS
                    .withDescription(exception.getMessage());
        }

        return Status.INTERNAL
                .withDescription("An unexpected error occurred");
    }

    @FunctionalInterface
    private interface GrpcOperation<T> {
        T run() throws Exception;
    }
}
