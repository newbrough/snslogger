package com.gyregroup.log4j.sns;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

/**
 * simple log4j Appender that forwards messages to an SNS topic
 * 
 * example log4j.properties:
 * log4j.appender.pager=com.gyregroup.log4j.sns.SNSAppender
 * log4j.appender.threshold=ERROR							# don't send message for every little log message
 * log4j.appender.pager.topic=notify-admin					# each message goes to an SNS topic created in AWS
 * log4j.appender.pager.accessKey=${AWS_ACCESS_KEY}			# keep your AWS keys private or someone can run up your bill
 * log4j.appender.pager.secretKey=${AWS_SECRET_KEY}			# this syntax will read them from environment vars at runtime
 * log4j.rootLogger=DEBUG, pager, logfile					# use logfile for most messages, SNS topic for ERROR or more severe
 * 
 * example java usage:
 * Logger log = Logger.getLogger(SomeClass.class);
 * log.warn("this message will NOT go to the SNS topic");
 * log.error("this message will be sent to SNS!");
 */
public class SNSAppender
extends AppenderSkeleton
{
	private AmazonSNS sns = null;
	private String accessKey, secretKey, topic;

	// properties configured by log4j.properties or log4j.xml
	public String getAccessKey() { return accessKey; }
	public void setAccessKey(String accessKey) { 
		this.accessKey = accessKey; 
		}
	public String getSecretKey() { return secretKey; }
	public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
	public String getTopic() { return topic; }
	public void setTopic(String topic) { this.topic = topic; }

	@Override public boolean requiresLayout() { return false; }
	
	/** initialize the AWS service client */
	private AmazonSNS getSNS()
	throws Exception
	{
		if(sns==null) 
		{
			synchronized(this)
			{
				final AWSCredentials credentials = new BasicAWSCredentials(getAccessKey(), getSecretKey());
				sns = new AmazonSNSClient(credentials);
			}
		}
		return sns;
	}

	@Override 
	public void close() { sns.shutdown(); }

	/** call the service client to deliver the message
	 * NOTE: messages>140chars will be truncated for SMS destinations
	 */
	@Override
	protected void append(LoggingEvent event) 
	{
    	try 
    	{
    		String msg = event.getMessage().toString();
    		final PublishRequest publishRequest = new PublishRequest(topic, msg);
    		getSNS().publish(publishRequest);
    	}
    	catch(Exception e) 
    	{
    		errorHandler.error("exception sending SNS: " + e);
    	}
	}
}
