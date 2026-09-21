#!/usr/bin/env bash

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

if [[ "$(uname -s)" == "Darwin" ]]; then
    JAVA_21_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null)" || {
        echo "未找到 JDK 21，请先安装后再启动后端。" >&2
        exit 1
    }
    export JAVA_HOME="${JAVA_21_HOME}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
fi

cd "${SCRIPT_DIR}/api-26-9-13"
exec mvn spring-boot:run "$@"
