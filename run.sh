#!/bin/bash

# Convert MP3 to WAV for javax.sound.sampled (if ffmpeg available)
if [ ! -f music.wav ] && command -v ffmpeg &>/dev/null; then
    echo "Converting music.mp3 to music.wav ..."
    ffmpeg -i music.mp3 -acodec pcm_s16le -ar 44100 music.wav -y 2>/dev/null
fi

mkdir -p bin

# Compile with JavaFX if available, otherwise standard
if javac --module-path "$JAVA_FX_HOME/lib" \
    --add-modules javafx.media \
    -d bin -encoding UTF-8 src/*.java 2>/dev/null; then
    java --module-path "$JAVA_FX_HOME/lib" \
        --add-modules javafx.media \
        -cp bin SalaryCatApp
else
    javac -d bin -encoding UTF-8 src/*.java && java -cp bin SalaryCatApp
fi
