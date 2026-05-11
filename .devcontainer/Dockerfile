FROM mcr.microsoft.com/devcontainers/java:1-17-bullseye

# Prevent interactive prompts during apt installs
ENV DEBIAN_FRONTEND=noninteractive

# Install required system dependencies
RUN apt-get update && apt-get -y install --no-install-recommends \
    unzip \
    wget \
    git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set up Android SDK paths
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Download and install Android SDK Command Line Tools
# Link: https://developer.android.com/studio#command-line-tools-only
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && cd ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip \
    && unzip -q cmdline-tools.zip \
    && rm cmdline-tools.zip \
    && mv cmdline-tools latest

# Pre-install specific Android packages and accept licenses
# (Doing this in the Dockerfile caches the download so Codespaces launch fast)
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Grant the non-root 'vscode' user access to the Android SDK
RUN chown -R vscode:vscode ${ANDROID_HOME}

# Revert to interactive mode
ENV DEBIAN_FRONTEND=dialog
