use std::{fs, path::PathBuf};

use j4rs::{JvmBuilder, MavenArtifactRepo, MavenSettings};

pub fn setup_java(base: PathBuf) {
    let resources = base.join("resources");
    let deps = resources.join("deps");

    let mut java_path = base.clone();
    java_path.pop();
    let java_path = java_path.join("java");

    let patchbukkit_jar = java_path
        .join("patchbukkit")
        .join("build")
        .join("libs")
        .join("patchbukkit.jar");

    let jassets = resources.join("jassets");
    let mut classpath_entries = Vec::new();

    if let Ok(entries) = fs::read_dir(&jassets) {
        for entry in entries.flatten() {
            let path = entry.path();
            if let Some(filename) = path.file_name().and_then(|n| n.to_str()) {
                if filename.starts_with("j4rs") && filename.ends_with(".jar") {
                    classpath_entries.push(j4rs::ClasspathEntry::new(&path));
                }
            }
        }
    }

    let mut jvm_builder = JvmBuilder::new();
    let mut jvm_builder = jvm_builder
        .with_maven_settings(MavenSettings::new(vec![MavenArtifactRepo::from(
            "papermc::https://repo.papermc.io/repository/maven-public/",
        )]))
        .skip_setting_native_lib()
        .with_base_path(&resources)
        .java_opts(vec![
            j4rs::JavaOpt::new("--enable-native-access=ALL-UNNAMED"),
            j4rs::JavaOpt::new("-Dcom.google.protobuf.useUnsafe=false"),
        ]);

    for entry in classpath_entries {
        jvm_builder = jvm_builder.classpath_entry(entry);
    }

    let _jvm = jvm_builder
        .build()
        .map_err(|err| format!("jvm failed to init: {err:?}"))
        .unwrap();

    if !&patchbukkit_jar.exists() {
        panic!(
            "Failed to find patchbukkit.jar, build the java library first by running `gradle build` in the java directory!"
        );
    }

    fs::create_dir_all(&jassets).unwrap();
    let dest_jar = jassets.join("patchbukkit.jar");
    fs::copy(&patchbukkit_jar, &dest_jar)
        .map_err(|err| format!("Failed to copy patchbukkit.jar to jassets: {err:?}"))
        .unwrap();

    let cdylib = std::env::var("CARGO_CDYLIB_FILE_J4RS").unwrap();
    let cdylib = PathBuf::from(cdylib);

    let mut cdylib_to = deps;
    fs::create_dir_all(&cdylib_to).unwrap();

    let original_name = cdylib.file_name().unwrap().to_string_lossy();
    let stem = original_name.split('-').next().unwrap(); // before the first '-'
    let ext = cdylib.extension().unwrap().to_string_lossy();

    cdylib_to.push(format!("{stem}.{ext}"));

    fs::copy(&cdylib, &cdylib_to)
        .map_err(|err| format!("Failed to copy j4rs native lib: {err:?}"))
        .unwrap();
}
