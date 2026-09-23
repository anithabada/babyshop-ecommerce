package com.babyshop.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Shared Gson instance for the whole app (JSON <-> Java object conversion). */
public final class JsonUtil {

    // Java's module system blocks reflective access to java.time internals on
    // recent JDKs, so Gson needs an explicit adapter to (de)serialize LocalDateTime.
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                            src == null ? null : new JsonPrimitive(src.format(ISO)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                            json == null || json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString(), ISO))
            .create();

    private JsonUtil() {}
}
