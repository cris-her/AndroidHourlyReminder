# How To Translate

To translate 'Hourly Reminder' to your language you need to translate following files:

  * [strings.xml](/app/src/main/res/values/strings.xml)
  * [about.html](/app/src/main/res/raw/about.html)

Additional file from 'android-library'
  * [https://gitlab.com/axet/android-library/.../strings.xml](https://gitlab.com/axet/android-library/blob/master/src/main/res/values/strings.xml)

In additional, you need to figure it out, which is the best way to pronounce current time. For example you may want to pronounce "hours" after hours and "minutes" after minutes. Or just pronounce time as it is. To figure it out which way is the best, try to pronounce time in all combinations and learn how you do it. It may be necessary to pronounce 'O' when here is no minutes (10:01), have different ending or skipping the words. English rule set examples:

  * "Time is 12 O'clock"
  * "Time is 23 hundred hours"
  * "Current time is 10 oh 5 AM"
  * "Current time is 14 35" (14:35)
  * "Current time is 0 9" (00:09)
  * "Current time is 1 5" (01:09)

Also please read about "Quantity strings" here:

  * https://developer.android.com/guide/topics/resources/string-resource#Plurals

 You can find how speech time string is built here:

  * [TTS.java](/app/src/main/java/com/github/axet/hourlyreminder/app/TTS.java)

```java
            if (speakAMPMFlag) { // do we need to prounounce AM/PM?
                speakAMPM = HourlyApplication.getHourString(context, en, hour);
            }

            // we can use "10 hours" or just "10"
            speakHour = HourlyApplication.getQuantityString(context, en, R.plurals.hours, h);
            
            if (min < 10) // in case we need to say 10 hours, 08 minutes : 10 "o" 8
                speakMinute = String.format("o %d", min);
            else
                speakMinute = HourlyApplication.getQuantityString(context,
                  en,
                  R.plurals.minutes, min);

            if (min != 0) {
                speak = HourlyApplication.getString(context,
                  en,
                  R.string.speak_time,
                  speakHour + " " + speakMinute + " " + speakAMPM);
            } else {
                if (speakAMPMFlag)
                    speak = HourlyApplication.getString(context,
                      en,
                      R.string.speak_time,
                      speakHour + " " + speakAMPM);
                else
                    speak = HourlyApplication.getString(context,
                      en,
                      R.string.speak_time_24,
                      speakHour);
            }
            tts.setLanguage(en);
```

Then add those files and speech engine to the repository using "New Issue" or create pull request against 'dev' branch.
