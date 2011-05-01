package org.tiny4.TinyEasyServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.GZIPOutputStream;

public class HttpProtocolCls {
	
	public static final int HTTP_METHOD_ERROR = 0;
	public static final int HTTP_METHOD_GET = 1;
	public static final int HTTP_METHOD_POST = 2;

	public int method = 0;
	public String target;
	public String targetFile;
	public String queryStrings;

	public Map<String, String> queryString = new WeakHashMap<String, String>();
	public Map<String, String> headers = new WeakHashMap<String, String>();

	private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

	public String m_ip;

	public PrintStream m_out;

	private static byte[] GZipBytes(byte[] content) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
					byteArrayOutputStream);
			gzipOutputStream.write(content);
			gzipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	public HttpProtocolCls(PrintStream out, Socket cSocket) {
		m_out = out;
	}

	public void gzippedHttpResponse(int retCode, String contentType,
			String content) {
		StringBuilder sb = new StringBuilder();

		sb.append("HTTP/1.0 ").append(retCode).append(" Server OK\r\n");
		sb.append("Server: TinyEasyServer 0.0001\r\n");
		sb.append("Content-Type: ").append(contentType).append("\r\n");

		sb.append("Content-Encoding: gzip\r\n");
		sb.append("Content-Length: ");
		byte[] zipped = GZipBytes(content.getBytes(DEFAULT_CHARSET));
		sb.append(zipped.length);
		sb.append("\r\n").append("\r\n");
		m_out.print(sb.toString());
		try {
			m_out.write(zipped);
			m_out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void httpResponse(int retCode, String contentType, String content) {
		StringBuilder sb = new StringBuilder();

		sb.append("HTTP/1.0 ").append(retCode).append(" Server OK\r\n");
		sb.append("Server: TinyEasyServer 0.0001\r\n");
		sb.append("Content-Type: ").append(contentType).append("\r\n");
		sb.append("Content-Length: ");
		sb.append(content.getBytes(DEFAULT_CHARSET).length);
		sb.append("\r\n").append("\r\n");
		sb.append(content);
		m_out.print(sb.toString());
		m_out.flush();

	}

	public void httpRedirect(String Url) {
		StringBuilder sb = new StringBuilder();

		sb.append("HTTP/1.0 ").append(302).append(" Server OK\r\n");
		sb.append("Server: TinyEasyServer 0.0001\r\n");
		sb.append("Content-Type: text/html\r\n");
		sb.append("Location: ").append(Url).append("\r\n");
		sb.append("Content-Length: ").append(Url.length()).append("\r\n");
		sb.append(Url);

		m_out.print(sb.toString());
	}

	public void getMethod(String HttpLine) {

		String[] hl = HttpLine.split("\\s");

		if (hl.length < 2)
			return;

		if (hl[0].toUpperCase().equals("GET")) {
			method = HttpProtocolCls.HTTP_METHOD_GET;
		} else if (hl[0].toUpperCase().equals("POST")) {
			method = HttpProtocolCls.HTTP_METHOD_POST;
		}

		target = hl[1];

	}

	public void getQueryStrings() {

		String[] hl = target.split("\\?");
		if (hl.length < 2) {
			targetFile = target;
			return;
		}
		targetFile = hl[0];
		queryStrings = hl[1];

		String[] qs = queryStrings.split("&");
		for (int i = 0; i < qs.length; i++) {
			String[] qsp = qs[i].split("=");
			if (qsp.length > 1) {

				queryString.put(qsp[0].trim(), qsp[1].trim());
			}
		}
	}

	public void getQueryStringfromPost(String queryStrings) {
		String[] qs = queryStrings.split("&");
		for (int i = 0; i < qs.length; i++) {
			String[] qsp = qs[i].split("=");
			if (qsp.length > 1) {

				String key = qsp[0].trim();
				String value = qsp[1].trim();
				if (!key.isEmpty() && !value.isEmpty()) {
					queryString.put(key, value);
				}
			} else {
				queryString.put(qsp[0].trim(), "");
			}
		}

	}

	public void addHeader(String HttpLine) {

		String[] hl = HttpLine.split(":");

		if (hl.length < 2)
			return;

		headers.put(hl[0], hl[1]);
	}
}
