#!/bin/bash

### kgit 사용 방법 및 실행 예시 ###

# 저장소 초기화:
#   ./kgit.sh init

# 파일 추가:
#   ./kgit.sh add example.txt

# 트리 오브젝트 생성:
#   ./kgit.sh write

# 트리 구조 출력:
#   ./kgit.sh ls <트리해시>

# 커밋 오브젝트 생성:
#   ./kgit.sh commit <트리해시> "커밋 메시지"

# 오브젝트 내용 확인:
#   ./kgit.sh cat pretty-print <오브젝트해시>

# 태그 생성:
#   ./kgit.sh tag v1.0 <커밋해시> "릴리즈 메시지"

# jar 파일이 없으면 자동으로 빌드합니다.


JAR_PATH="build/libs/kgit.jar"
GRADLEW="./gradlew"

if [ ! -f "$JAR_PATH" ]; then
  echo "실행 jar 파일이 없습니다. 빌드를 시도합니다..."
  if [ -f "$GRADLEW" ]; then
    $GRADLEW shadowJar
  else
    echo "gradle 빌드에 실패했습니다. 수동으로 빌드해 주세요."
    exit 1
  fi
fi

java -jar "$JAR_PATH" "$@"
