/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;

/**
 * On Tomcat 7 the ServletRequest we get is 
 * org.apache.catalina.connector.RequestFacade which
 * does not implement markSupported.
 * 
 * On Glassfish 4 the ServletRequest we get is also 
 * org.apache.catalina.connector.RequestFacade which
 * does not implement markSupported.
 * 
 * @author jbuhacoff
 */
public class RepeatableRequestFilter implements Filter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RepeatableRequestFilter.class);

    @Override
    public void init(FilterConfig fc) throws ServletException {
        log.debug("init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("ServletRequest class {}", request.getClass().getName());
        if (request instanceof HttpServletRequest) {
            log.debug("Wrapping ServletRequest with RepeatableServletRequest");
            chain.doFilter(new RepeatableServletRequest((HttpServletRequest) request), response);
        } else {
            log.debug("Not wrapping ServletRequest; continuing filter chain");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        log.debug("destroy");
    }

    public static class RepeatableServletRequest extends HttpServletRequestWrapper {

        private ByteArrayServletInputStream in = null;

        public RepeatableServletRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (in == null) {
                try (ServletInputStream stream = super.getInputStream()) {
                    log.debug("ServletInputStream markSupported? {}", stream.markSupported());
                    byte[] entity = IOUtils.toByteArray(stream);
                    in = new ByteArrayServletInputStream(entity);
                }
            }
            if( in.isFinished() ) {
                in.reset();
            }
            return in;
        }
    }

    // tentative, not being used right now
    public static class CopyableByteArrayInputStream extends ByteArrayInputStream {

        /**
         * The original offset as specified by the constructors; in contrast the
         * "pos" variable changes so after constructing the instance and reading
         * just one byte it's not possible to know what the original offset was.
         */
        protected int offset;

        public CopyableByteArrayInputStream(byte[] buf) {
            super(buf);
            this.offset = 0;
        }

        public CopyableByteArrayInputStream(byte[] buf, int offset, int length) {
            super(buf, offset, length);
            this.offset = offset;
        }

        /**
         * Unlike reset() which resets the current read position to the last
         * marked position or to zero, this reset(pos) method resets the current
         * read position to the specified index. The mark is not affected so if
         * you mark() at pos=5, then reset(0), then reset(), the final reset()
         * will move the current position back to index 5. Also note that if you
         * use the (buf,offset,length) constructor then using this reset(pos)
         * method it's possible to reset the current index to an index lower
         * than the originally provided offset.
         *
         * @param pos
         */
        public void reset(int pos) {
            this.pos = pos;
        }

        public byte[] copy() {
            byte[] copy = new byte[count];
            System.arraycopy(buf, offset, copy, 0, count - offset);
            return copy;
        }
    }

    /**
     * Wraps ByteArrayInputStream and extends ServletInputStream
     */
    public static class ByteArrayServletInputStream extends ServletInputStream {

        private ByteArrayInputStream in;

        public ByteArrayServletInputStream(byte[] entity) {
            in = new ByteArrayInputStream(entity);
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

        @Override
        public boolean isFinished() {
            return in.available() == 0;
        }

        @Override
        public boolean isReady() {
            return in.available() > 0;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return in.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }
    }
}
