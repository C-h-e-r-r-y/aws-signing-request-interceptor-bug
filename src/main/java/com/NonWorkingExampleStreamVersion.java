package com;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.http.HttpHost;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.RestClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.sync.ResponseInputStream;
import software.amazon.awssdk.sync.StreamingResponseHandler;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

public class NonWorkingExampleStreamVersion {
    public static void main(String[] args) throws Exception {
        final String index = System.getenv("myIndex");
        final String type = System.getenv("myType");
        final String id = System.getenv("myId");
        final String bucket = System.getenv("bucket");
        final String s3Key = System.getenv("s3Key");


        RestClient restClient = RestClient.builder(
                new HttpHost(System.getenv("ElasticSearchHost"),
                        Integer.parseInt(System.getenv("ElasticSearchPort")),
                        System.getenv("ElasticSearchProtocol")))
                .setHttpClientConfigCallback(c ->
                        c.addInterceptorLast(new AWSSigningRequestInterceptor(
                                new AWSSigner(new DefaultAWSCredentialsProviderChain(),
                                        System.getenv("ApplicationRegion"),
                                        "es", () -> LocalDateTime.now(ZoneOffset.UTC))))
                )
                .build();

        final S3Client s3Client = S3Client.create();
        ObjectMapper mapper = new ObjectMapper();

        final ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key).build(), StreamingResponseHandler.toInputStream());
        restClient.performRequest(
                "POST",
                Joiner.on("/").join(Arrays.asList("", index, type, id)),
                Collections.singletonMap("pipeline", "attachment"),
                new EntityTemplate(os -> {
                    final JsonGenerator generator = new JsonFactory().setCodec(mapper).createGenerator(os);
                    generator.writeStartObject();
                    generator.writeFieldName("data");
                    generator.writeBinary(responseInputStream, -1);
                    generator.writeEndObject();
                    generator.close();
                }));
        restClient.close();
        System.out.println("finished");
    }
}
