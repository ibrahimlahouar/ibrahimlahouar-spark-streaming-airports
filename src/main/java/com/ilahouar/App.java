package com.ilahouar;

import com.ilahouar.receiver.AirportReceiver;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String API_URL = "https://opensky-network.org/api/flights/all";
    private static final String MINIO_BUCKET = "airports-bucket";

    public static void main(String[] args) throws Exception {

        // Spark Configuration
        SparkConf conf = new SparkConf()
                .setAppName("Airport Streaming App")
                .setMaster("local[*]")
                .set("spark.driver.bindAddress", "127.0.0.1")
                .set("spark.executor.extraJavaOptions", "--add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED")
                .set("spark.driver.extraJavaOptions", "--add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED");

        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext ssc = new JavaStreamingContext(sc, Durations.minutes(120));

        // Initialize MinIO client
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();

        // Ensure the bucket exists in MinIO
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(MINIO_BUCKET).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(MINIO_BUCKET).build());
            logger.info("Bucket '{}' created successfully.", MINIO_BUCKET);
        } else {
            logger.info("Bucket '{}' already exists.", MINIO_BUCKET);
        }

        // Schedule API calls every 2 hours
        Instant startOfJune = Instant.parse("2024-06-01T00:00:00Z");
        Instant now = Instant.now();
        Instant currentStart = startOfJune;

        while (currentStart.isBefore(now)) {
            Instant currentEnd = currentStart.plus(2, ChronoUnit.HOURS);
            if (currentEnd.isAfter(now)) {
                currentEnd = now;
            }

            String apiUrlWithParams = generateApiUrlWithParams(currentStart, currentEnd);
            String data = callApi(apiUrlWithParams);

            // Save to MinIO
            String objectName = "airports_" + currentStart.toString() + ".json";
            InputStream stream = new java.io.ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(MINIO_BUCKET)
                            .object(objectName)
                            .stream(stream, data.length(), -1)
                            .contentType("application/json")
                            .build()
            );
            logger.info("Data successfully retrieved and stored in MinIO as '{}'.", objectName);

            currentStart = currentEnd;
        }

        // Start Streaming
        ssc.start();
        ssc.awaitTermination();
    }

    private static String generateApiUrlWithParams(Instant start, Instant end) {
        long begin = start.getEpochSecond();
        long endEpoch = end.getEpochSecond();
        return API_URL + "?begin=" + begin + "&end=" + endEpoch;
    }

    // Helper method to call the API
    public static String callApi(String apiUrl) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            HttpResponse response = client.execute(request);
            InputStream inputStream = response.getEntity().getContent();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(inputStream);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        }
    }
}