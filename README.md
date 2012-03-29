TinyEasyServer是一个非常简单的Java嵌入Web Server，最早用于我开发的搜索服务，作为前端。这个Server的目的是实现一个简单好用的多语言间的进程间通讯工具。

简单的例子：


````java
public class MyServer {

	public static void main(String[] args) throws IOException {


		TinyEasyServer server = new TinyEasyServer();
		SmartServerConfig config = new SmartServerConfig();
		server.start(config);
	}
}

class SmartServerConfig extends ServerConfig {

	@Override
	public HttpHandleCls generateHttpHandleCls(String tagFile) {

		if (tagFile.equals("/")) {

			return new BaseHHC();
		}
		return new BaseHHC();
	}
}

class BaseHHC implements HttpHandleCls {

	HttpProtocolCls m_hpc;

	@Override
	public void setHPC(HttpProtocolCls hpc) {

		m_hpc = hpc;
	}

	@Override
	public void response() {

		m_hpc.httpResponse(200, "text/html", "server ok");
	}
}
````
