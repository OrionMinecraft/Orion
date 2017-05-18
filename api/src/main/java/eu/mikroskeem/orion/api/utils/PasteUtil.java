package eu.mikroskeem.orion.api.utils;

import com.google.gson.JsonParser;
import eu.mikroskeem.orion.api.Orion;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Hastebin wrapper utility
 *
 * @author Mark Vainomaa
 */
@Slf4j
public class PasteUtil {
    private final static AtomicInteger THREAD_COUNTER = new AtomicInteger(0);

    private static OkHttpClient httpClient = null;
    private static ExecutorService executorService = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Text paste thread " + THREAD_COUNTER.getAndIncrement());
        return thread;
    });

    private synchronized static void init() {
        if(httpClient == null){
            httpClient = new OkHttpClient();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                executorService.shutdown();
                try {
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                }
                catch (InterruptedException ignored){}
            }, "Paste utility executor service shutdown thread"));
        }
    }

    /**
     * Paste text to hastebin
     *
     * @param content Paste content
     * @param resultConsumer Lambda to get URL (null if exception)
     */
    public static void pasteText(@NotNull String content, Consumer<URL> resultConsumer) {
        init();
        String hastebinUrl = Orion.getServer().getConfiguration().getDebug().getHastebinUrl();
        executorService.execute(() -> {
            try {
                URL url = URI.create(hastebinUrl + "/documents").toURL();
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), content);
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                try(Response response = httpClient.newCall(request).execute()){
                    String key = new JsonParser().parse(response.body().string()).getAsJsonObject().get("key").getAsString();
                    resultConsumer.accept(URI.create(hastebinUrl + "/" + key).toURL());
                }
                return;
            } catch (MalformedURLException e){
                log.error("Hastebin URL is malformed. See stacktrace below.");
                e.printStackTrace();
            } catch (IOException e){
                log.warn("Failed to paste. See stacktrace below");
                e.printStackTrace();
            }
            resultConsumer.accept(null);
        });
    }
}
