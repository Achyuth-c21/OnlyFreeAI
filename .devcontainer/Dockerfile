# ──────────────────────────────────────────────────────────────
# OnlyFreeAI — Codespace Dockerfile
# Everything baked in. Zero setup on open.
# ──────────────────────────────────────────────────────────────
FROM mcr.microsoft.com/devcontainers/java:17-bullseye

# ── Environment Variables ────────────────────────────────────
ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_USER_HOME=/home/vscode/.gradle
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/34.0.0"

# ── System Dependencies ─────────────────────────────────────
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    git \
    curl \
    lib32z1 \
    lib32stdc++6 \
    && rm -rf /var/lib/apt/lists/*

# ── Android SDK Command-Line Tools ──────────────────────────
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && cd ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip \
    && unzip -q cmdline-tools.zip \
    && rm cmdline-tools.zip \
    && mv cmdline-tools latest

# ── Accept All SDK Licenses ─────────────────────────────────
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true

# ── Install Android SDK Components ──────────────────────────
# Matches: compileSdk=34, buildToolsVersion from AGP 8.4.0
RUN sdkmanager --install \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;google;m2repository" \
    "extras;android;m2repository"

# ── Fix Permissions for vscode user ─────────────────────────
RUN chown -R vscode:vscode ${ANDROID_HOME} \
    && mkdir -p ${GRADLE_USER_HOME} \
    && chown -R vscode:vscode ${GRADLE_USER_HOME}

# ── Pre-download Gradle Distribution ────────────────────────
# Gradle 8.6 as specified in gradle-wrapper.properties
RUN mkdir -p ${GRADLE_USER_HOME}/wrapper/dists \
    && chown -R vscode:vscode ${GRADLE_USER_HOME}

# ── Switch to non-root user ─────────────────────────────────
USER vscode

# ── Pre-warm Gradle Wrapper ─────────────────────────────────
# Downloads Gradle 8.6 binary so ./gradlew doesn't wait on first run
COPY --chown=vscode:vscode gradle/ /tmp/gradle-warmup/gradle/
COPY --chown=vscode:vscode gradlew /tmp/gradle-warmup/gradlew
COPY --chown=vscode:vscode build.gradle.kts /tmp/gradle-warmup/build.gradle.kts
COPY --chown=vscode:vscode settings.gradle.kts /tmp/gradle-warmup/settings.gradle.kts
RUN cd /tmp/gradle-warmup \
    && chmod +x gradlew \
    && ./gradlew --version \
    && rm -rf /tmp/gradle-warmup
