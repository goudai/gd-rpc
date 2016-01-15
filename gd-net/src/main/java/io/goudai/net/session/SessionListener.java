package io.goudai.net.session;


public interface SessionListener{
	
	void onCreated(AbstractSession session);
	
	void onMessage(AbstractSession session, Object obj);
	
	void onDestory(AbstractSession session);
	
	void onException(AbstractSession session, Exception e);

}
