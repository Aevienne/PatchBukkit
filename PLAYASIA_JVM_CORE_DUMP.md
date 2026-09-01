# PlayAsia JVM Core Dump

## Environment

- Pumpkin fork: `Aevienne/Pumpkin`
- PatchBukkit fork: `Aevienne/PatchBukkit`
- Pumpkin target: Java 25 container, Pumpkin DEV allocation `25553`
- PatchBukkit nightly: commit `9ec7a3d`
- Test data: copied DEV world and an empty `patchbukkit/patchbukkit-plugins` directory

## Reproduction

Run from the target volume with PatchBukkit enabled:

```bash
RUST_BACKTRACE=1 docker run --rm --entrypoint "" \
  -e RUST_BACKTRACE=1 \
  -v /var/lib/calagopus-wings/volumes/0663eb80-d3b8-45bd-86b1-579098d1f653:/home/container \
  -w /home/container \
  ghcr.io/ptero-eggs/yolks:java_25 timeout 15 ./pumpkin
```

## Observed Output

Pumpkin loads all copied worlds and reaches its running state. PatchBukkit then logs:

```text
Starting PatchBukkit
PatchBukkit loaded successfully
Initializing JVM in background...
Initializing JVM with assets path: "/home/container/plugins/data/patchbukkit/jassets"
Found 1 JAR entries in jassets: ["/home/container/plugins/data/patchbukkit/jassets/patchbukkit.jar"]
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
timeout: the monitored command dumped core
```

The process exits during JVM initialization after the `Unsafe` warnings, before any Java plugin loads. The same failure reproduced with the official PatchBukkit test plugin. An empty Java plugin directory does not prevent the failure.

## Control

With `libpatchbukkit.so` disabled, the same Pumpkin binary reaches:

```text
Server is now running. Connect using port: Java Edition: 0.0.0.0:25553
```

The target remains offline after testing. The rollback step restores `libpatchbukkit.so.disabled` after each run.

## Scope

This report records the first reproducible compatibility failure. It does not identify the native crash frame. The next diagnostic should capture a host core dump or run the JVM under `gdb`, then inspect the Rust JNI/FFM worker and Java 25 initialization path.
