Core binaries are not included in the source distribution.

Pre-built Core binaries are at:
    https://github.com/zerozshare/OpenFriendCore/releases

Place them in this folder (helper/src/main/resources/openfriend/), then run:
    ./gradlew :fabric:remapJar

If you only need to read or contribute to the mod source, you don't need
the Core binaries — gradle compiles fine without them, it just won't
produce a runnable mod jar.
