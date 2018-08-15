# LetsChat
An application for chat using Firebase. Developed in Kotlin.

## Features

•	Chat using Firebase<br/> 
•	Send Emoticon<br/>
•	Send gallery and camera image<br/>
•	Send Location<br/>
•	Pinch zoom for image<br/>
•	Persistent data of chat<br/>

## Screens

<img src="https://github.com/snj07/LetsChat/blob/master/screenshots/login.png" width="300" style="display: inline; margin: 0 5px;" />
<img src="https://github.com/snj07/LetsChat/blob/master/screenshots/chat.png" width="300" style="display: inline; margin: 0 5px;"/>
<img src="https://github.com/snj07/LetsChat/blob/master/screenshots/feature.png" width="300" />


### Geting Started

Update following values in MainActitivty

```kotlin
       const val CHAT_REFERENCE = "message" //database name
       const val STORAGE_PATH = "gs://friendlychat.appspot.com" //your firebase storage path
       const val STORAGE_FOLDER = "images" // folder in storage area
```

### Places Api

```xml

<string name="api_key_google_places">XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX</string>

```

Step 1: Create Firebase project<br/>
Step 2: Create application in project<br/>
Step 3: Generate google-services.json and place it in app folder of project.<br/>
Step 4: Enable Authentication in Firebase<br/>
Step 5: Add database with "message" name in Firebase real time database<br/>
Step 6: Create folder in Firebase storage<br/>


## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
