package com.gyregroup.log4j.sns.example;
import org.apache.log4j.Logger;


public class SendSMS
{
	private static Logger log = Logger.getLogger(SendSMS.class);
	
	public static void main(String[] args)
	{
		log.info("should NOT receive this as SMS");
		log.error("test SMS delivery");
	}
}
