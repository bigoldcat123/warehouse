#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${SCRIPT_DIR}/api-26-9-13"

if [[ ! -f "${BACKEND_DIR}/pom.xml" ]]; then
    echo "错误：未找到后端项目：${BACKEND_DIR}/pom.xml" >&2
    exit 1
fi

if [[ "$(uname -s)" == "Darwin" ]] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
    export JAVA_HOME
    JAVA_HOME="$(/usr/libexec/java_home -v 21)"
fi

if ! command -v java >/dev/null 2>&1; then
    echo "错误：未找到 Java，请安装 JDK 21。" >&2
    exit 1
fi

if ! java -version 2>&1 | head -n 1 | grep -q 'version "21'; then
    echo "错误：后端需要 JDK 21，当前 Java 为：" >&2
    java -version >&2
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "错误：未找到 Maven，请先安装 Maven 3.9 或更高版本。" >&2
    exit 1
fi

echo "正在启动后端：http://127.0.0.1:8080"
cd "${BACKEND_DIR}"
exec mvn spring-boot:run "$@"
