import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as XML
import zipfile


def run(command):
    subprocess.run([str(part) for part in command], check=True)


def main():
    parser = argparse.ArgumentParser(description="Checa recursos/Kotlin/testes JVM com cache local; não gera nem instala APK.")
    parser.add_argument("--gradle-home", type=Path, default=Path.home() / ".gradle")
    parser.add_argument("--sdk", type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    sdk = args.sdk
    if sdk is None:
        for line in (root / "local.properties").read_text().splitlines():
            if line.startswith("sdk.dir="):
                sdk = Path(line.split("=", 1)[1].replace("\\:", ":").replace("\\ ", " "))
    if sdk is None:
        raise SystemExit("Informe --sdk ou sdk.dir em local.properties.")
    android = sdk / "platforms/android-37.0/android.jar"
    merger = root / "app/build/intermediates/incremental/debug/mergeDebugResources/merger.xml"
    config = root / "app/build/generated/source/buildConfig/debug/com/senac/viaja/BuildConfig.java"
    if not android.is_file() or not merger.is_file() or not config.is_file():
        raise SystemExit("Faltam SDK/metadados de uma sincronização Gradle anterior. Use o build Gradle completo quando disponível.")
    aapt = next(args.gradle_home.glob("caches/*/transforms/*/transformed/aapt2-*/aapt2"), None)
    if aapt is None:
        raise SystemExit("AAPT2 não encontrado no cache local.")
    modules = args.gradle_home / "caches/modules-2/files-2.1"
    jars = sorted(p for p in modules.rglob("*.jar") if not p.name.endswith(("-sources.jar", "-javadoc.jar")))
    tests = sorted(p for p in (root / "app/src/test/java").rglob("*Test.kt") if p.name != "ExampleUnitTest.kt")
    with tempfile.TemporaryDirectory(prefix="viaja-android-check-") as temporary:
        work = Path(temporary)
        generated = work / "generated"
        classes = work / "classes"
        classes.mkdir()
        sources = list(dict.fromkeys(node.attrib["path"] for node in XML.parse(merger).getroot().iter("source") if "path" in node.attrib))
        resource_zips = []
        for index, path in enumerate(sources):
            directory = Path(path)
            if directory.is_dir() and any(directory.rglob("*.*")):
                archive = work / f"resource-{index}.zip"
                run([aapt, "compile", "--dir", directory, "-o", archive])
                resource_zips.append(archive)
        manifest = XML.parse(root / "app/src/main/AndroidManifest.xml")
        manifest.getroot().set("package", "com.senac.viaja")
        for node in manifest.getroot().iter():
            for key, value in list(node.attrib.items()):
                node.set(key, value.replace("${applicationId}", "com.senac.viaja"))
        manifest_file = work / "AndroidManifest.xml"
        manifest.write(manifest_file, encoding="utf-8", xml_declaration=True)
        command = [aapt, "link", "--auto-add-overlay", "--min-sdk-version", "24", "--target-sdk-version", "37", "-I", android,
                   "--manifest", manifest_file, "--java", generated, "-o", work / "resources.ap_"]
        for archive in resource_zips:
            command.extend(["-R", archive])
        run(command)
        print("Recursos: compile/link AAPT2 aprovado.", flush=True)
        for index, aar in enumerate(sorted(modules.rglob("*.aar"))):
            with zipfile.ZipFile(aar) as archive:
                if "classes.jar" in archive.namelist():
                    target = work / f"library-{index}.jar"
                    target.write_bytes(archive.read("classes.jar"))
                    jars.append(target)
        run(["javac", "-d", classes, generated / "com/senac/viaja/R.java", config])
        classpath = os.pathsep.join(str(p) for p in [classes, android] + jars)
        production = sorted((root / "app/src/main/java").rglob("*.kt"))
        run(["java", "-cp", classpath, "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler", "-no-stdlib", "-no-reflect", "-jvm-target", "11",
             "-classpath", classpath, "-d", classes] + production + tests)
        print("Fontes Kotlin checados com R real. Esta checagem não substitui o build Gradle.", flush=True)
        names = [".".join(path.relative_to(root / "app/src/test/java").with_suffix("").parts) for path in tests]
        run(["java", "-cp", classpath, "org.junit.runner.JUnitCore"] + names)


if __name__ == "__main__":
    main()
