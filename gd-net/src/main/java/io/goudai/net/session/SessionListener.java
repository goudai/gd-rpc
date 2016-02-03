package io.goudai.net.session;


public interface SessionListener{
	
	void onCreated(AbstractSession session);
	
	void onRead(AbstractSession session, Object obj);

	void onWrite(AbstractSession session, Object obj);
	
	void onDestory(AbstractSession session);
	
	void onException(AbstractSession session, Exception e);

	void onConnected(AbstractSession session);
}
