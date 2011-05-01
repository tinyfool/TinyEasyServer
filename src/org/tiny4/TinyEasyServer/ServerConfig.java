package org.tiny4.TinyEasyServer;

public abstract  class ServerConfig {
	
	public abstract HttpHandleCls generateHttpHandleCls(final String tagFile);
	
	public int port(){
		
		return 3721;
	}
}
