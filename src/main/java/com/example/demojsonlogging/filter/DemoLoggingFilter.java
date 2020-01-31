package com.example.demojsonlogging.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static net.logstash.logback.argument.StructuredArguments.entries;
import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
public class DemoLoggingFilter extends OncePerRequestFilter {

    private static final String REQ_ATTRIBUTE_START_AT = "request-start-at";

    private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 50;

    private boolean includeQueryString = false;

    private boolean includeClientInfo = false;

    private boolean includeHeaders = false;

    private boolean includePayload = false;

    private boolean shouldLogBefore = true;

    private boolean shouldLogAfter = true;

    @Nullable
    private Predicate<String> headerPredicate;

    private int maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;

    /**
     * Set whether the query string should be included in the log message.
     * <p>Should be configured using an {@code <init-param>} for parameter name
     * "includeQueryString" in the filter definition in {@code web.xml}.
     */
    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    /**
     * Return whether the query string should be included in the log message.
     */
    protected boolean isIncludeQueryString() {
        return this.includeQueryString;
    }

    /**
     * Set whether the client address and session id should be included in the
     * log message.
     * <p>Should be configured using an {@code <init-param>} for parameter name
     * "includeClientInfo" in the filter definition in {@code web.xml}.
     */
    public void setIncludeClientInfo(boolean includeClientInfo) {
        this.includeClientInfo = includeClientInfo;
    }

    /**
     * Return whether the client address and session id should be included in the
     * log message.
     */
    protected boolean isIncludeClientInfo() {
        return this.includeClientInfo;
    }

    /**
     * Set whether the request headers should be included in the log message.
     * <p>Should be configured using an {@code <init-param>} for parameter name
     * "includeHeaders" in the filter definition in {@code web.xml}.
     *
     * @since 4.3
     */
    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    /**
     * Return whether the request headers should be included in the log message.
     *
     * @since 4.3
     */
    protected boolean isIncludeHeaders() {
        return this.includeHeaders;
    }

    /**
     * Set whether the request payload (body) should be included in the log message.
     * <p>Should be configured using an {@code <init-param>} for parameter name
     * "includePayload" in the filter definition in {@code web.xml}.
     *
     * @since 3.0
     */
    public void setIncludePayload(boolean includePayload) {
        this.includePayload = includePayload;
    }

    /**
     * Return whether the request payload (body) should be included in the log message.
     *
     * @since 3.0
     */
    protected boolean isIncludePayload() {
        return this.includePayload;
    }

    /**
     * Configure a predicate for selecting which headers should be logged if
     * {@link #setIncludeHeaders(boolean)} is set to {@code true}.
     * <p>By default this is not set in which case all headers are logged.
     *
     * @param headerPredicate the predicate to use
     * @since 5.2
     */
    public void setHeaderPredicate(@Nullable Predicate<String> headerPredicate) {
        this.headerPredicate = headerPredicate;
    }

    /**
     * The configured {@link #setHeaderPredicate(Predicate) headerPredicate}.
     *
     * @since 5.2
     */
    @Nullable
    protected Predicate<String> getHeaderPredicate() {
        return this.headerPredicate;
    }

    /**
     * Set the maximum length of the payload body to be included in the log message.
     * Default is 50 characters.
     *
     * @since 3.0
     */
    public void setMaxPayloadLength(int maxPayloadLength) {
        Assert.isTrue(maxPayloadLength >= 0, "'maxPayloadLength' should be larger than or equal to 0");
        this.maxPayloadLength = maxPayloadLength;
    }

    /**
     * Return the maximum length of the payload body to be included in the log message.
     *
     * @since 3.0
     */
    protected int getMaxPayloadLength() {
        return this.maxPayloadLength;
    }

    /**
     * Extracts the message payload portion of the message created by
     * {@link #createRequestMessageMap(HttpServletRequest)} when
     * {@link #isIncludePayload()} returns true.
     *
     * @since 5.0.3
     */
    @Nullable
    protected String getRequestMessagePayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper =
                WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, getMaxPayloadLength());
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Determine whether to call the {@link #beforeRequest}/{@link #afterRequest}
     * methods for the current request, i.e. whether logging is currently active
     * (and the log message is worth building).
     * <p>The default implementation always returns {@code true}. Subclasses may
     * override this with a log level check.
     *
     * @param request current HTTP request
     * @return {@code true} if the before/after method should get called;
     * {@code false} otherwise
     * @since 4.1.5
     */
    protected boolean shouldLogBefore(HttpServletRequest request) {
        return this.shouldLogBefore;
    }

    protected boolean shouldLogAfter(HttpServletRequest request, HttpServletResponse response) {
        return this.shouldLogAfter;
    }

    public void setShouldLogBefore(boolean logBefore) {
        this.shouldLogBefore = logBefore;
    }

    public void setShouldLogAfter(boolean logAfter) {
        this.shouldLogAfter = logAfter;
    }

    /**
     * Concrete subclasses should implement this method to write a log message
     * <i>before</i> the request is processed.
     *
     * @param cachedRequest current wrapped HTTP request
     */
    protected void beforeRequest(HttpServletRequest cachedRequest) {
        log.info("START request: {}", entries(createRequestMessageMap(cachedRequest)));
    }

    /**
     * Concrete subclasses should implement this method to write a log message
     * <i>after</i> the request is processed.
     *
     * @param cachedRequest  current wrapped HTTP request
     * @param cachedResponse request's wrapped HTTP response
     */
    protected void afterRequest(HttpServletRequest cachedRequest, HttpServletResponse cachedResponse) throws IOException {
        log.info("END request: {}", entries(createResponseMessageMap(cachedRequest, cachedResponse)), value("type", "HTTP_REQUEST"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put("request_id", UUID.randomUUID().toString());
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;
        boolean shouldLogBefore = shouldLogBefore(requestToUse);

        if (isFirstRequest) {
            if (isIncludePayload() && !(request instanceof ContentCachingRequestWrapper)) {
                requestToUse = new ContentCachingRequestWrapper(request, getMaxPayloadLength());
            }
        }

        if (shouldLogBefore) {
            beforeRequest(requestToUse);
        }

        HttpServletResponse responseToUse = response;
        boolean shouldLogAfter = shouldLogAfter(requestToUse, responseToUse);
        if (shouldLogAfter && !isAsyncStarted(requestToUse)) {
            if (isIncludePayload() && !(response instanceof ContentCachingResponseWrapper)) {
                responseToUse = new ContentCachingResponseWrapper(response);
            }
        }
        try {
            requestToUse.setAttribute(REQ_ATTRIBUTE_START_AT, System.currentTimeMillis());
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            if (shouldLogAfter && !isAsyncStarted(requestToUse)) {
                afterRequest(requestToUse, responseToUse);
            }
            MDC.clear();
        }
    }

    /**
     * Create a log message for the given request.
     * <p>If {@code includeQueryString} is {@code true}, then the inner part
     * of the log message will take the form {@code request_uri?query_string};
     * otherwise the message will simply be of the form {@code request_uri}.
     */
    protected Map<String, Object> createRequestMessageMap(HttpServletRequest request) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("method", request.getMethod());
        requestMap.put("request_uri", request.getRequestURI());

        if (isIncludeQueryString()) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                requestMap.put("full_uri", requestMap.get("request_uri") + "?" + queryString);
            }
        }

        if (isIncludeClientInfo()) {
            String client = request.getRemoteAddr();
            if (StringUtils.hasLength(client)) {
                requestMap.put("client", client);
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                requestMap.put("session_id", session.getId());
            }
            String user = request.getRemoteUser();
            if (user != null) {
                requestMap.put("user", user);
            }
        }

        if (isIncludeHeaders()) {
            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            if (getHeaderPredicate() != null) {
                Enumeration<String> names = request.getHeaderNames();
                while (names.hasMoreElements()) {
                    String header = names.nextElement();
                    if (!getHeaderPredicate().test(header)) {
                        headers.set(header, "[redacted]");
                    }
                }
            }
            requestMap.put("headers", headers);
        }

        if (isIncludePayload()) {
            String payload = getRequestMessagePayload(request);
            if (payload != null) {
                requestMap.put("request_body", payload);
            }
        } else {
            requestMap.put("request_body", "[redacted]");
        }
        return requestMap;
    }

    protected Map<String, Object> createResponseMessageMap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> responseMap = new HashMap<>(createRequestMessageMap(request));
        String payload = null;
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                wrapper.copyBodyToResponse();
            }
        }
        responseMap.put("response_code", response.getStatus());
        if (isIncludePayload()) {
            if (payload != null) {
                responseMap.put("response_body", payload);
            }
        } else {
            responseMap.put("response_body", "[redacted]");
        }

        // Request execution duration in milliseconds
        long startAt = (long) request.getAttribute(REQ_ATTRIBUTE_START_AT);
        responseMap.put("duration", System.currentTimeMillis() - startAt);
        return responseMap;
    }
}
