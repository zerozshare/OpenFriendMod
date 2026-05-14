Core binaries are not included in the source distribution.

To build a working OpenFriend jar from this source you need pre-built Core
binaries from https://github.com/zerozshare/OpenFriendCore/releases :

    openfriend-darwin-amd64
    openfriend-darwin-arm64
    openfriend-linux-amd64
    openfriend-linux-arm64
    openfriend-windows-amd64.exe

Place them in this folder (helper/src/main/resources/openfriend/), then run:

    ./gradlew :fabric:remapJar

The compiled jar lands in fabric/build/libs/.

If you only need to read or contribute to the mod source, you don't need
the Core binaries — the gradle build will still compile, it just won't
produce a runnable mod jar.
