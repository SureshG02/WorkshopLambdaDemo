# WorkshopLambdaDemo

# Prerequisite:

# Part 1

1)	Set up free tier AWS account. Note: You will need to provide card details. Till you are within free tier limit there won’t be any charge.
Ref https://aws.amazon.com/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc

2) Install putty.
Ref https://www.putty.org/

# Part 2

3)	Install AWS CLI for windows.
Ref https://docs.aws.amazon.com/cli/latest/userguide/install-windows.html

4)	Install Java 8 and set environment variables.
Ref https://www.oracle.com/java/technologies/javase-jdk8-downloads.html

5)	Download and install STS IDE.
Ref https://spring.io/tools

6) Install AWS SDK toolkit for eclipse.
Ref https://aws.amazon.com/eclipse/


## Use this for your user data (script without newlines)
## install httpd (Linux 2 version)

#!/bin/bash <br />
yum update -y <br />
yum install -y httpd.x86_64 <br />
systemctl start httpd.service <br />
systemctl enable httpd.service <br />
echo "Hello World from $(hostname -f)" > /var/www/html/index.html
