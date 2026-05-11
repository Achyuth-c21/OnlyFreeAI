#!/bin/bash
set -e

echo "=== OnlyFreeAI Codespace Setup ==="

# Java is already installed in the universal image
echo "Java version:"
java --version

# Set up Android SDK
export ANDROID_HOME="$HOME/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"

echo "Downloading Android SDK Command Line Tools..."
cd "$ANDROID_HOME/cmdline-tools"
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip
rm cmdline-tools.zip
mv cmdline-tools latest

# Add to PATH for this script
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# Persist environment variables for future terminal sessions
echo '' >> ~/.bashrc
echo '# Android SDK' >> ~/.bashrc
echo "export ANDROID_HOME=$ANDROID_HOME" >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc

echo "Accepting SDK licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1 || true

echo "Installing Android 34 platform and build tools..."
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Make gradlew executable
chmod +x gradlew 2>/dev/null || true

echo ""
echo "=== Setup Complete! ==="
echo "Run: ./gradlew assembleDebug"
