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

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		final String ASG = System.getenv("ASG_NAME");

		context.getLogger().log("Input: " + input);
		String inputJson = String.valueOf(input);

		AmazonAutoScaling client = AmazonAutoScalingClientBuilder.standard().build();
		DescribeAutoScalingGroupsRequest describeRequest = new DescribeAutoScalingGroupsRequest()
				.withAutoScalingGroupNames(ASG);
		DescribeAutoScalingGroupsResult describeResponse = client.describeAutoScalingGroups(describeRequest);
		List<AutoScalingGroup> list = describeResponse.getAutoScalingGroups();
		AutoScalingGroup group = list.get(0);

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
