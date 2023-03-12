
#!/bin/bash -x
mkdir -p fortGnox.iconset

for x in 16 32 64 128 256 512; do 
    sips -z $x $x fortGnox.png --out fortGnox.iconset/icon_${x}x${x}.png
    xx=$((x * 2))
    sips -z $xx $xx fortGnox.png --out fortGnox.iconset/icon_${x}x${x}@2.png
done
iconutil -c icns fortGnox.iconset
