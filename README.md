tinderbox
=========

Another [@crockpotveggies](https://twitter.com/crockpotveggies) experiment.

**Heads up!** As soon as you turn on this bot, it WILL start messaging all of your contacts. Please also check out the **Support** section below (the original author is no longer supporting this software) and the [Wiki](https://github.com/crockpotveggies/tinderbox/wiki) for tips on getting started. You will probably want to customize the messages. To do so, edit this [file](https://github.com/crockpotveggies/tinderbox/blob/master/app/models/bot/tasks/message/FunMessages.scala) then re-build the app using [these instructions](https://github.com/crockpotveggies/tinderbox/wiki/Editing-the-Code).

## What is Tinderbox?

Tinderbox is an experiment built on the Tinder app API. Tinderbox is a full Tinder solution that learns who you're attracted to (using machine learning) and also has a built-in bot that can start conversations. It is a full desktop interface for Tinder.

![Screenshot](https://raw.githubusercontent.com/crockpotveggies/tinderbox/master/public/img/screenshot.jpg)

Tinderbox is built on top of [Play!](http://www.playframework.com/) 2.2 and uses Akka, Spark, and other libraries for face detection in the background.

### Getting Started

Read the "Running" section below to learn how to start the app.

Tinderbox starts up a server that is accessible in your browser at `http://localhost:9000/`. Once you're inside, note that there is a bot running in the background. If you watch the logs in terminal, it will show you debug statements about the moves the bot is taking. There is an action log available in the main interface.

Most of Tinder's primary functionality has been re-created in Tinderbox. Note that if Tinder updates or changes its API it could potentially break this app.

### Running

There are a few ways to run the code:

####Use the OSX app launcher
(For OSX only) Use the .app launcher for OSX (you can drag and drop this into the Dock, too!)
![App Launcher](https://raw.githubusercontent.com/crockpotveggies/tinderbox/master/public/img/screenshots/app-launcher.png)

####Use the startup scripts
Before you run the startup scripts, fetch the Stanford NLP models. You can do this automatically by running:
```
bash lib/fetch_nlp_models
```

Then for OSX/Linux run the following command from the root of the project directory:
```
bash dist/tinderbox-1.1-SNAPSHOT/bin/tinderbox
```
Or Windows, run the following command from the root of the project directory:
```
dist/tinderbox-1.1-SNAPSHOT/bin/tinderbox.bat
```

**Please note:** Tinderbox was not tested on a Windows platform. There's no guarantee the software will behave the same as a UNIX machine.

### Authenticating with Tinder

You will need to authorize using a Facebook `access_token` and also with your Facebook ID. The login page has a link for you to create an access token. Copy the token from Facebook's URL and paste it in the Tinderbox login. (Note that FB security measures obscure the access token after a second or two, so you have to be fast with the copy to clipboard.)

Tinderbox will automatically attempt to find your Facebook ID once you copy and paste your `access_token`. If it doesn't appear within one second you'll need to manually enter it. For convenience, you can also paste the entire URL from the Facebook authentication URL and the app will auto-extract your `access_token`.

Note: in some cases the Facebook ID cannot be obtained, and you can get your FB ID from [findmyfacebookid.com](http://findmyfacebookid.com/). It will look like a long ~10 digit number.

## Support

The original author is not offering any further support. The code is released "as is".

## Facial Analysis and Predictions

Tinderbox attempts to make a prediction based on profiles you've previously liked. This is done using elementary Machine Learning methods. Tinderbox will examine previous likes/dislikes and develop a model based on colors found within faces of profiles. The more likes/dislikes made, the more accurate the model will become.

![Screenshot](https://raw.githubusercontent.com/crockpotveggies/tinderbox/master/public/img/tinderbox_eigenfaces_models.jpg)

> Currently, the prediction being made is very elementary and uses EigenFaces for facial analysis. Eigenvector values are developed from
> pixel models of each yes/no model of faces and compared against a new recommendation undergoing analysis. Whichever image is closest to
> either a yes/no model determines whether a "like" or "dislike" will be made. Each face is normalized and grayscaled before being added to
> the EigenFaces model.

The recommendation system could use some work since it is useful to combine EigenFaces methods with other analysis systems. If you're finding that auto likes/dislikes are not meeting your standards, you can erase the facial modeling data using the "Clear Models" button on the dashboard.

## Auto-Messaging

Tinderbox has built-in automated messaging. The built in "Tinderbot" looks for conversations that fit patterns within pre-set message trees. The tree directions are based on positive or negative sentiment, and when the conversation no longer fits the tree the bot will notify the user to take over the conversation.

## Word from the Author

Tinderbox is the first bot I've built to interact on my behalf. Since it is a tool that automates a lot of the Tinder experience, be forewarned not everyone appreciates being met through a robot. And they certainly don't appreciate if you decide to use this code to spam the Tinder world.

## Editing the Code

Check out [this wiki page](https://github.com/crockpotveggies/tinderbox/wiki/Editing-the-Code) for instructions on editing the code and building the app.

## License

Tinderbox is distributed under the Creative Commons Attribution-NonCommercial 3.0 license, a human readable version is available at [https://creativecommons.org/licenses/by-nc/3.0/](https://creativecommons.org/licenses/by-nc/3.0/).

Because the Tinder API hasn't been officially released as a public API, Tinderbox is licensed for non-commercial reasons only. I have no affiliation with Tinder, and you use this repository at your own risk. Please think before mixing this code with something that could be considered abusive of the Tinder API.

Please note this software comes with NO Warranty.
