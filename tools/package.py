"""Package unsigned APKs from source, SDK resources, and a caller-supplied LibVLC AAR."""
from pathlib import Path
import sys
from zipfile import ZIP_DEFLATED, ZipFile

operation, *args = sys.argv[1:]
if operation == "extract":
    aar, directory = map(Path, args)
    with ZipFile(aar) as upstream:
        (directory / "vlc-original.jar").write_bytes(upstream.read("classes.jar"))
elif operation == "assemble":
    resources, dex, aar, module, target = args
    with ZipFile(resources) as base, ZipFile(target, "w", ZIP_DEFLATED) as output:
        for entry in base.infolist():
            output.writestr(entry, base.read(entry))
        for file in sorted(Path(dex).glob("classes*.dex")):
            output.write(file, file.name)
        if module != "fire-volume":
            with ZipFile(aar) as upstream:
                libraries = [
                    name for name in upstream.namelist()
                    if name.startswith(("jni/armeabi-v7a/", "jni/arm64-v8a/")) and name.endswith(".so")
                ]
                if not any(name.startswith("jni/armeabi-v7a/") for name in libraries):
                    raise ValueError("LibVLC AAR must contain 32-bit ARM libraries for Fire TV Stick Lite")
                for name in libraries:
                    output.writestr("lib/" + name.removeprefix("jni/"), upstream.read(name))
else:
    raise ValueError("Unknown package operation")
