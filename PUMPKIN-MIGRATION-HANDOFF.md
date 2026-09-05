# Pumpkin Migration Handoff

Updated: 2026-09-01

## Current State

- Work continues on `Aevienne/PatchBukkit`, branch `diag/playasia-jvm-core-dump`.
- Base Pumpkin runs on isolated Pumpkin DEV allocation `0663eb80...`, port `25553`, without PatchBukkit.
- PatchBukkit crashes during embedded JVM startup after loading `patchbukkit.jar`, before Java plugins load. The failure reproduces with an empty plugin directory and the test plugin.
- The Pumpkin DEV world is copied and must remain untouched during bridge tests.
- `DEVPlayAsia` (`71b0701f...`) and all production servers are off limits. Do not send panel power commands to them.

## Code Changes

- `9ef6cc6`: added JVM diagnostics, Java module opens, and no-unsafe settings for JOML and related libraries.
- `f89057a`: added the GitHub Actions Linux bridge build.
- `672673c`: added automated deployment and a 30-second Pumpkin DEV test.

## Automation

Workflow: `https://github.com/Aevienne/PatchBukkit/actions/workflows/build-linux.yml`

The workflow builds the Java JAR and Linux native library on GitHub Actions. The deployment job runs only on `diag/playasia-jvm-core-dump`, uses the `pumpkin-dev` environment, tests only Pumpkin DEV, and restores the bridge and test plugin to `.disabled` after the run. It does not start the panel server or touch production.

Required environment secrets are configured outside the repository. Never store credentials, tokens, passwords, or private keys in this repository.

## Resource Safety

- Do not build Rust on the Calagopus VM. A previous VM build caused memory pressure and production container restarts.
- Do not build Rust on the laptop. A previous WSL build caused system instability.
- Use GitHub Actions for Linux bridge builds.
- Check workflow run `33498881654` before starting another run. It was still running when this handoff was written.

## Next Session

1. Check the latest `Build Linux bridge` run and inspect the native build or deploy log.
2. If the build succeeds, inspect the Pumpkin DEV test output for JVM startup and plugin loading.
3. Keep the bridge disabled after testing unless an explicit test is underway.
4. Record the result in `PLAYASIA_JVM_CORE_DUMP.md` and push the branch.

All reports must use plain English. Production remains untouched.
