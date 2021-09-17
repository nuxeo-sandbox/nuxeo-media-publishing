# Description

This repository contains a plugin to publish assets to external services such as Facebook, YouTube and Wistia

# How to build

* Build with unit tests and  docker image:

```
git clone https://github.com/nuxeo-sandbox/nuxeo-media-publishing
cd nuxeo-media-publishing
mvn clean install
```
* To skip unit tests (you need to set access tokens, etc. and there may be limitations in the number of posts you can do, when you run the tests several times a day)

```
mvn -DskipTests=true clean install
```

* To build the plugin without building the Docker image, use:

```
mvn -DskipDocker=true clean install
```


# YouTube
## Google Cloud Console Configuration
- First, you need a Google account (and make sure this account has a YouTube channel)
- Go to the [google cloud console](https://console.cloud.google.com/)
- Create or use an existing project
- In `API Library`, activate the `YouTube Data API v3`
- in `Credentials`, create a new `OAuth 2.0 Client IDs` and set the callback url to `https://MY_SERVER/nuxeo/site/oauth2/youtube/callback`
- Configure the `OAuth consent screen` and make sure that your account is in the test user list

## Nuxeo Configuration
- log into webui as an administrator,
- go to `Administration > Cloud Services` and edit the `youtube` service
- Set the client id and client secret that you got when configuration the OAuth 2 client in the google cloud console
- Don't forget to check enabled

## How to use
- As a user, log into webui and go to `User Settings > Cloud Services`
- Click on Connect to `youtube`
- Grant the permission to the app and if everything works fine you'll see `youtube` in the `Connected Accounts` list 
- Open a video file asset in the application, click on `Publish to Youtube`

Pay attention to the following when doing tests: 
- there is a daily quota which limits uploads to 5 per day
- publish videos as private or google may flag your account and even deactivate it

# Facebook
## Facebook Configuration
- First, you need a facebook developer account.
- Go to https://developers.facebook.com/
- Go to My Apps and create a new `Business` application
  - In `Settings > Basic`, take a copy the app id and app secret
- Add the `Facebook Login` product to your app
  - Set the redirect url to `https://MY_SERVER/nuxeo/site/oauth2/facebook/callback`
- Go to `Role > Test Users`
  - get the username and password for your test user
- Log into Facebook with the test user
  - create a page 

## Nuxeo Configuration
- log into webui as an administrator,
- go to `Administration > Cloud Services` and edit the `facebook` service
- Set the client id and client secret with the app id and app secret
- Don't forget to check enabled

## How to use
- As a user, log into webui and go to `User Settings > Cloud Services`
- Click on Connect to facebook
- Grant the permission to the app and if everything works fine you'll see `facebook` in the `Connected Accounts` list
- Open a picture or video file asset in the application, click on `Publish to Facebook`

Pay attention to the following when doing tests:
- don't use your facebook account, use a facebook test users

# Known limitations
- cannot publish an asset to several YouTube channels
- Publishing to YouTube is limited to the user own channel

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# Nuxeo Marketplace
This plugin is published on the [marketplace](https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-media-publishing)

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com)