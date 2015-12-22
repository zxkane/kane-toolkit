# Introduction #

They're example projects to demonstrate how to use Eclipse p2 API, they demonstrate below p2 features:
  * Install an application into empty folder
  * Install additional features into the existing application
  * Publish the repository with special requirements
  * Install the application from customized repository

# Details #
Project 'com.example.mail' is a standalone RCP based on Eclipse mail template.

Project 'com.example.mail.feature' is a feature of Eclipse that contains the plugin 'com.example.mail'.

There is a product configuration definition in project 'com.example.mail'. Using export the product can generate the repository of Mail RCP application. It requires many features of Eclipse SDK for enabling p2/p2 UI capabilities.

Project 'com.example.mail.desktop' and 'com.example.mail.desktop.feature' are placeholder, they don't contain any code.

Project 'com.example.p2.installer' also is a standalone RCP that has capability to install repository into empty folder or existing application.

Project 'com.example.p2.generator' is headless application to generate the special repository for feature 'com.example.mail.desktop.feature'. The repository uses the customized touchpoint type and actions.

Project 'com.example.p2.touchpoint' implements itself touchpoint and actions.

# More #
[P2 example tutorial(in Chinese)](http://docs.google.com/View?id=ddqccrw2_576xpnxdfhq)