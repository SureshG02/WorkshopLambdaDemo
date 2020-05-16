package com.amazonaws.lambda.demo;

import java.util.List;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		final String FROM = System.getenv("FROM");
		final String TO = System.getenv("TO");
		final String ASG = System.getenv("ASG_NAME");
		final String REGION = System.getenv("REGION");
		final String SUBJECT = "AWS running servers status.";

		context.getLogger().log("Input: " + input);
		String inputJson = String.valueOf(input);

		AmazonAutoScaling client = AmazonAutoScalingClientBuilder.standard().build();
		DescribeAutoScalingGroupsRequest describeRequest = new DescribeAutoScalingGroupsRequest()
				.withAutoScalingGroupNames(ASG);
		DescribeAutoScalingGroupsResult describeResponse = client.describeAutoScalingGroups(describeRequest);
		List<AutoScalingGroup> list = describeResponse.getAutoScalingGroups();
		AutoScalingGroup group = list.get(0);

		// Send notification if instances are running after office hours.
		if (inputJson.contains("Scheduled Event")) {
			// Construct an object to contain the recipient address.
			Destination destination = new Destination().withToAddresses(new String[] { TO });

			// Create the subject and body of the message.
			Content subject = new Content().withData(SUBJECT);
			try {
				System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");
				AmazonSimpleEmailService emailClient = AmazonSimpleEmailServiceClientBuilder.standard()
						.withCredentials(DefaultAWSCredentialsProviderChain.getInstance()).withRegion(REGION)
						.build();
				if (group.getDesiredCapacity() > 0) {
					// Assemble the email.
					String BODY = "There are running servers. If they are not in use please shutdown them.";
					Content textBody = new Content().withData(BODY);
					Body body = new Body().withText(textBody);
					// Create a message with the specified subject and body.
					Message message = new Message().withSubject(subject).withBody(body);
					SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination)
							.withMessage(message);
					emailClient.sendEmail(request);
				} else {
					String BODY = "There are no servers running. You can send email and start all servers.";
					Content textBody = new Content().withData(BODY);
					Body body = new Body().withText(textBody);
					// Create a message with the specified subject and body.
					Message message = new Message().withSubject(subject).withBody(body);
					// Assemble the email.
					SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination)
							.withMessage(message);
					emailClient.sendEmail(request);
				}
				context.getLogger().log("Email sent!");

			} catch (Exception ex) {
				context.getLogger().log("The email was not sent." );
		        context.getLogger().log("Error message: " + ex.getMessage());
			}
		}

		// Shutdown all instances after receiving mail from user.
		if (inputJson.contains("<stop>")) {
			if (group.getDesiredCapacity() > 0) {
				UpdateAutoScalingGroupRequest shutDownRequest = new UpdateAutoScalingGroupRequest()
						.withAutoScalingGroupName(ASG).withMinSize(0).withDesiredCapacity(0);
				client.updateAutoScalingGroup(shutDownRequest);
			}
		}

		// Start all instances after receiving mail from user.
		if (inputJson.contains("<start>")) {
			if (group.getDesiredCapacity() == 0) {
				UpdateAutoScalingGroupRequest startRequest = new UpdateAutoScalingGroupRequest()
						.withAutoScalingGroupName(ASG).withMinSize(3).withDesiredCapacity(3);
				client.updateAutoScalingGroup(startRequest);
			}
		}
		return "AutoScalingGroup instances updated successfully.";
	}

}
