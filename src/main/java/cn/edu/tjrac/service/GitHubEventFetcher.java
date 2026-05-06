package cn.edu.tjrac.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@EnableScheduling
public class GitHubEventFetcher {

    @Value("${bigdata.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${bigdata.kafka.topic}")
    private String kafkaTopic;

    private final String githubApiUrl = "https://api.github.com/events";
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong eventCounter = new AtomicLong(0);

    private ExecutorService executor;
    private KafkaProducer<String, String> kafkaProducer;
    private volatile boolean kafkaAvailable = false;

    @PostConstruct
    public void init() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GitHub-Event-Fetcher");
            t.setDaemon(true);
            return t;
        });

        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            kafkaProducer = new KafkaProducer<>(props);
            kafkaAvailable = true;
            log.info("Kafka producer initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Kafka producer: {}", e.getMessage());
            kafkaAvailable = false;
        }

        running.set(true);
        startFetching();
    }

    private void startFetching() {
        executor.submit(() -> {
            log.info("GitHub Event Fetcher started - fetching real data from: {}", githubApiUrl);
            while (running.get()) {
                try {
                    fetchAndSendEvents();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in fetch loop: {}", e.getMessage());
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    private void fetchAndSendEvents() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(githubApiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "E-Log-CA-System");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                String body = response.toString();
                JSONArray events = JSON.parseArray(body);

                log.info("Fetched {} real GitHub events", events.size());

                for (Object obj : events) {
                    JSONObject event = (JSONObject) obj;
                    JSONObject logEntry = transformToLogEntry(event);

                    String jsonLog = JSON.toJSONString(logEntry);

                    if (kafkaAvailable) {
                        ProducerRecord<String, String> record = new ProducerRecord<>(kafkaTopic, jsonLog);
                        kafkaProducer.send(record);
                    }

                    long count = eventCounter.incrementAndGet();
                    if (count % 10 == 0) {
                        log.info("Sent {} events to Kafka - Type: {}, Actor: {}, Endpoint: {}",
                                count,
                                logEntry.getString("method"),
                                logEntry.getString("ip"),
                                logEntry.getString("endpoint"));
                    }
                }

                if (kafkaAvailable) {
                    kafkaProducer.flush();
                }
            } else if (responseCode == 403) {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                log.warn("GitHub API rate limited (403). Real data unavailable. Waiting 60 seconds before retry...");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                log.warn("GitHub API returned status {}. Waiting 30 seconds before retry...", responseCode);
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            log.error("Network error fetching GitHub events: {}. Waiting 30 seconds before retry...", e.getMessage());
            try {
                Thread.sleep(30000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONObject transformToLogEntry(JSONObject event) {
        JSONObject logEntry = new JSONObject();

        logEntry.put("timestamp", System.currentTimeMillis());
        logEntry.put("ip", extractActorId(event));
        logEntry.put("method", event.getString("type"));
        logEntry.put("endpoint", extractEndpoint(event));
        logEntry.put("statusCode", 200);
        logEntry.put("responseTime", calculateResponseTime(event));

        return logEntry;
    }

    private String extractActorId(JSONObject event) {
        JSONObject actor = event.getJSONObject("actor");
        if (actor != null) {
            return "user:" + actor.getString("login");
        }
        return "unknown";
    }

    private String extractEndpoint(JSONObject event) {
        JSONObject repo = event.getJSONObject("repo");
        if (repo != null) {
            return "/github/" + repo.getString("name");
        }
        return "/github/unknown";
    }

    private int calculateResponseTime(JSONObject event) {
        String eventType = event.getString("type");
        if (eventType == null) {
            return 50;
        }

        switch (eventType) {
            case "PushEvent":
                return 100 + (int)(Math.random() * 200);
            case "PullRequestEvent":
                return 150 + (int)(Math.random() * 300);
            case "IssuesEvent":
                return 80 + (int)(Math.random() * 150);
            case "WatchEvent":
                return 30 + (int)(Math.random() * 50);
            case "CreateEvent":
                return 50 + (int)(Math.random() * 100);
            case "DeleteEvent":
                return 40 + (int)(Math.random() * 80);
            case "ForkEvent":
                return 120 + (int)(Math.random() * 250);
            case "IssueCommentEvent":
                return 90 + (int)(Math.random() * 180);
            case "PullRequestReviewEvent":
                return 130 + (int)(Math.random() * 270);
            case "PushHookEvent":
                return 100 + (int)(Math.random() * 200);
            default:
                return 50 + (int)(Math.random() * 100);
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (executor != null) {
            executor.shutdownNow();
        }
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
        log.info("GitHub Event Fetcher stopped. Total events sent: {}", eventCounter.get());
    }
}
