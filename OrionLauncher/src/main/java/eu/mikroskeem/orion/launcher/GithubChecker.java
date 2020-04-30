/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2020 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.orion.launcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * @author Mark Vainomaa
 */
final class GithubChecker {
    private final Logger log;
    private final Properties ver;
    private final OkHttpClient client;

    GithubChecker(@NotNull Logger log, @NotNull Properties ver, @NotNull OkHttpClient client) {
        this.log = log;
        this.ver = ver;
        this.client = client;
    }

    void check() {
        HttpUrl url = HttpUrl.parse("https://api.github.com/repos/" +
                ver.getProperty("gitRepository") +
                "/compare/" +
                ver.getProperty("gitBranch") +
                "..." +
                ver.getProperty("gitCommitId"));
        Request request = new Request.Builder()
                .get()
                .url(requireNonNull(url))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Failed to obtain latest version info from GitHub!", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    //noinspection ConstantConditions
                    try(Reader content = response.body().charStream()) {
                        JsonObject root = new JsonParser().parse(content).getAsJsonObject();
                        boolean isBehind = root.get("status").getAsString().equals("behind");
                        if(isBehind) {
                            log.info("This Orion version is behind by {} commits!", root.get("behind_by").getAsInt());
                        } else {
                            log.info("This Orion version is up to date!");
                        }
                    }
                } else {
                    //noinspection ConstantConditions
                    if(response.body().contentType().toString().contains("application/json")) {
                        //noinspection ConstantConditions
                        try(Reader content = response.body().charStream()) {
                            JsonObject root = new JsonParser().parse(content).getAsJsonObject();
                            String message = root.get("message").getAsString();
                            if(message.equals("Not Found")) {
                                log.warn("Commit id {} not found, failed to obtain version information",
                                        ver.getProperty("gitCommitId"));
                                return;
                            } else {
                                log.warn("Github response while trying to compare commits: {}", message);
                                return;
                            }
                        }
                    }
                    throw new IOException("HTTP code: " + response.code());
                }
            }
        });
    }
}