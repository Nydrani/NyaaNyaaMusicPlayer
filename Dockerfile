FROM openjdk:8

ENV ANDROID_SDK_TOOLS_REV "4333796"
ENV ANDROID_COMPILE_SDK "27"
ENV ANDROID_BUILD_TOOLS "27.0.3"

ENV ANDROID_HOME "/opt/android-sdk-linux"
ENV PATH "$PATH:${ANDROID_HOME}/tools"

RUN mkdir "$ANDROID_HOME" .android \
    && cd "$ANDROID_HOME" \
    && curl -o sdk.zip "https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS_REV}.zip" \
    && unzip sdk.zip \
    && rm sdk.zip
    
# Install Android Build Tool and Libraries
RUN yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses
RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/tools/bin/sdkmanager \
    "tools" \
    "platform-tools" \
    "build-tools;${ANDROID_BUILD_TOOLS}" \
    "platforms;android-${ANDROID_COMPILE_SDK}"

RUN mkdir /application
WORKDIR /application
