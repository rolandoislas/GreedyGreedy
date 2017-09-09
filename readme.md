# GreedyGreedy

Release: [![Build Status](https://travis-ci.org/rolandoislas/GreedyGreedy.svg?branch=master)](https://travis-ci.org/rolandoislas/GreedyGreedy)
Dev: [![Build Status](https://travis-ci.org/rolandoislas/GreedyGreedy.svg?branch=develop)](https://travis-ci.org/rolandoislas/GreedyGreedy)

Farkle, Zilch, 10000 Dice: A Game of Dice

# Download

- [Android]
- [Desktop]
- [Web]

# Building

GreedyGreedy uses a gradle build system.

## Desktop

`./gradlew desktop:dist`

Output: `./desktop/build/libs/desktop-<version>.jar`

## Android

`./gradlew android:assembleRelease`

Output: `./android/build/outputs/apk/android-release-unsigned.apk`

## Server

`./gradlew server:dist`

Output: `./server/build/libs/server-<version>.jar`

### Environment Variables

#### JAWSDB_URL

mysql://username:password@somehost.tld:1337/some_database

#### REDIS_URL

redis://username:password@somehost.tld:1337

#### Google API

The Android in app purchases via Google Play require access to the Google
 Play developer console account associated with the app.

- GOOGLE_API_REFRESH_TOKEN
- GOOGLE_API_CLIENT_SECRET
- GOOGLE_API_CLIENT_ID

A valid Google oauth refresh (not access) token is required. 
 See [Google Play Authorization].
 
# Contributing

Pull requests are welcomed, but do keep the license in mind.

The working branch is `develop`. Branch `master` is reserved for stable builds.
 
# License and Credits

Greedy Greedy is licensed under the GPLv2.

See [license.txt](license.txt) and [third-party.txt](third-party.txt)



[Android]: https://play.google.com/store/apps/details?id=com.rolandoislas.greedygreedy
[Desktop]: https://github.com/rolandoislas/GreedyGreedy/releases
[Web]: https://greedygreedy.rolandoislas.com/game
[Google Play Authorization]: https://developers.google.com/android-publisher/authorization