<p align="center">
  <img src="docs/logo.png" width="96" alt="Pieno">
</p>
<h1 align="center">Pieno</h1>
<p align="center">Unofficial Android app for the Friuli Venezia Giulia subsidised fuel card.</p>
<p align="center">[EN|<a href="README.IT.md">IT</a>]</p>

## What it is

Shows your fuel-card QR at the pump (even offline), the participating stations with prices and a map, the cheapest-nearby search, your refuelling history, and Add to Google Wallet.

## Download

**[Download the APK](https://github.com/macedonga/pieno/releases/download/nightly/pieno.apk)**: automatic build of the latest commit (`nightly` release).

## Build

You need **JDK 17+** and the **Android SDK** (compileSdk 36).

Clone the repo, then run:

```sh
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
```

The APK is in `app/build/outputs/apk/debug/`.

> [!WARNING]
> Pieno is an independent project, **not affiliated with, sponsored by, or endorsed by Insiel S.p.A. or the Autonomous Region of Friuli Venezia Giulia**. Any trademarks belong to their respective owners.
> The app circumvents no protection: it accesses **only your own account's data**, through the official APIs and **your** SPID/CIE login, and sends nothing to third parties. The QR shown is the token signed by the regional backend, redrawn as-is.
> Building an independent interoperable client is lawful under software-interoperability rules (Article 64-quater of Italian Law no. 633/1941, transposing Directive 2009/24/EC) and the user's right to access their own data. **Should I receive a cease-and-desist I consider unfounded, I intend to contest it.**
