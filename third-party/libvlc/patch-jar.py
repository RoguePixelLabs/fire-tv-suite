"""Replace only AWindow classes with the patched upstream 3.7.7 sources."""
import pathlib
import sys
import zipfile

original, classes, output = map(pathlib.Path, sys.argv[1:])
prefix = 'org/videolan/libvlc/AWindow'
with zipfile.ZipFile(original) as source, zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as target:
    for entry in source.infolist():
        if entry.filename == prefix + '.class' or entry.filename.startswith(prefix + '$'):
            continue
        target.writestr(entry, source.read(entry.filename))
    for file in classes.rglob('*.class'):
        target.write(file, file.relative_to(classes).as_posix())
