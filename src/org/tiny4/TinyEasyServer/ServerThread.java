package org.tiny4.TinyEasyServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ServerThread implements Runnable {

	Socket m_cSocket;
	ServerConfig m_config;
	InputStreamReader m_dataReader;
	PrintStream m_dataWriter;
	public ServerThread(Socket cSocket,ServerConfig config) throws IOException {
		
		m_cSocket = cSocket;
		m_config = config;
		m_dataReader = new InputStreamReader(cSocket.getInputStream());
		m_dataWriter = new PrintStream(cSocket.getOutputStream(), true,ServerConstants.DEFULAT_ENCODE_STR);
	}

	@Override
	public void run() {
		
		boolean isFirstLine = true;
		final HttpProtocolCls hpc = new HttpProtocolCls(m_dataWriter,m_cSocket);
		try {
			int contentLength = 0;
			int bufferLength = 8192;
			int currentLength = 0;
			char[] cbuf = new char[bufferLength];
			int length = 0;
			StringBuilder contentSB = new StringBuilder("");
			do {
				try {
					length = m_dataReader.read(cbuf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (length < 0) {
					break;
				}

				String sbuf = new String(cbuf, 0, length);

				boolean isHeader = true;

				if (sbuf.contains(ServerConstants.CRLF)) {
					String[] lines = sbuf.split(ServerConstants.CRLF);
					for (String line : lines) {
						if (line == null) {
							isHeader = false;
							continue;
						}
						if (line.equals(ServerConstants.CRLF) || line.equals("")) {
							isHeader = false;
							continue;
						}

						if (isHeader == true) {
							if (isFirstLine) {
								hpc.getMethod(line);
								hpc.getQueryStrings();
								isFirstLine = false;
							} else {
								if (line.startsWith("Content-Length:")) {
									contentLength = Integer.parseInt(line
											.substring("Content-Length: "
													.length()));
								}
								hpc.addHeader(line);
							}
						} else {
							contentSB.append(line);
							currentLength += line.length();
						}
					}
				} else {
					contentSB.append(sbuf);
					currentLength += sbuf.length();
				}
			} while (currentLength < contentLength);

			if (hpc.method == HttpProtocolCls.HTTP_METHOD_GET) {
				try {
					hpc.getQueryStringfromPost(contentSB.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			if (hpc.method == HttpProtocolCls.HTTP_METHOD_POST) {
				hpc.getQueryStringfromPost(contentSB.toString());
			}
			if (hpc.method != HttpProtocolCls.HTTP_METHOD_ERROR) {
				
				final HttpHandleCls hhc = m_config.generateHttpHandleCls(hpc.targetFile);

				if (hhc != null) {
					hhc.setHPC(hpc);
					hhc.response();
				}
			}

		} finally {

			try {
				m_dataReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				m_dataWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				m_cSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
