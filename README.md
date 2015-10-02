# TTSBrowser

SHORT DESCRIPTION

This applications shows an html file in a WebView. You can also enter any other URL into the EditText field 
above the webview, but the main functionality only works with the html file (the html file can be found in
the project under app/src/main/assets and is called "ThirdTest.html").

You can click on any of the paragraphs to select the whole paragraph. If you click on "READ" the text will 
read by a TTS engine. You can also tell the engine to read by nodding you phone. The engines stops when you
click on the "STOP READING" Button or when you shake your phone.
You can move on to the next or previous paragraph by tilting your phone or by saying "next" or "previous".
Moreover, the first paragraph can be selected by saying "select".

NOTE: The app sometimes crashes when shaking to phone to stop the TTS engine.

INSTALL THE APP

The apk file to install the application can be found under app/build/outputs/aps and is called apk-debug.apk.
It can be copied to a device via USB. Clicking on the file in any file manager will install the file (allow
installation of apps from unknown sources in the settings)

DOWNLOAD THE PROJECT TO ANDROID STUDIO

In Android Studio you can click on "VCS" and then select "Checkout from VCS>>GitHub". You can then enter 
the URL and log in to GitHub with your account to download the files. If you are requested by Adnroid Studio 
to enter a master password just click "`cancel"' and you can continue entering your data oder downloading the 
project files.

