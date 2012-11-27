package de.unikl.bcverifier.web;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.time.Duration;

import de.unikl.bcverifier.boogie.BoogieRunner;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see de.unikl.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{    	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<HomePage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
        getRequestCycleSettings().setTimeout(Duration.minutes(5)); 
		String boogiePath = (String) getServletContext().getInitParameter("boogieCommand");
		if (boogiePath != null) {
			BoogieRunner.setBoogieCommand(boogiePath);
		}
	}
	
	@Override
	public final Session newSession(Request request, Response response) {
		return new ConfigSession(request);
	}
}
