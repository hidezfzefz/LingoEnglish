# Building Lingo on a shared/low-resource desktop

The project is tuned for an 8-thread laptop that also runs a browser, chat, and
other desktop apps. Do not raise these limits unless there is a specific reason.

## Configure live calls

Live calls need a Supabase URL and anon key at build time. Put them in
`local.properties` with the helper (this preserves `sdk.dir`):

```bash
./scripts/configure_live.sh https://YOUR-PROJECT.supabase.co YOUR_ANON_KEY
```

Never put the Supabase service-role key or LiveKit API secret in the app. See
`supabase/README.md` for the backend deployment.

## Release build

JDK 21 is required. Set `JAVA_HOME` or point Gradle at your JDK; do not commit a
machine-specific `org.gradle.java.home` path.

```bash
cd /path/to/LingoEnglish
./gradlew :app:assembleRelease --no-daemon --max-workers=2 --console=plain
./gradlew --stop
```

The APK is written to:

```
app/build/outputs/apk/release/app-release.apk
```

Install it without erasing progress (the debug signing key is used for both
debug and release, so `-r` keeps app data):

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

## What keeps the machine responsive

`gradle.properties` intentionally sets:

- `org.gradle.workers.max=2` and `org.gradle.parallel=false`
- 2 GB Gradle heap / 1.5 GB Kotlin daemon
- `org.gradle.priority=low`
- no Gradle VFS file watching
- daemon idle shutdown after 60 seconds
- `-XX:+PerfDisableSharedMem` on both JVMs. The crash log
  (`SIGBUS` in `PerfMemory::alloc`) is a JVM shared-performance-memory failure;
  disabling the shared perf file avoids that class of crash.

Always run `./gradlew --stop` after a build. The daemon is not needed while
editing UI or documentation.
