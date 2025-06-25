# kgit

Kotlin 기반 Git 프로젝트입니다. 실제 git처럼 CLI에서 커맨드로 사용할 수 있습니다.

## 주요 명령어

- 저장소 초기화: `./kgit.sh init`
- 파일 추가: `./kgit.sh add <파일명>`
- 트리 오브젝트 생성: `./kgit.sh write-tree`
- 트리 구조 출력: `./kgit.sh ls-tree <트리해시>`
- 커밋 오브젝트 생성: `./kgit.sh commit-tree <트리해시> "커밋 메시지"`
- 오브젝트 내용 확인: `./kgit.sh cat-file pretty-print <오브젝트해시>`
- 태그 생성: `./kgit.sh tag <태그명> <커밋해시> "메시지"`

## 실행 방법

1. **jar 빌드**
    - jar 파일이 없으면 `./kgit` 실행 시 자동으로 빌드됩니다.
    - 수동 빌드: `./gradlew shadowJar` 또는 `./gradlew jar`

2. **명령어 실행**
    - 예시:
      ```sh
      ./kgit init
      ./kgit add example.txt
      ./kgit write-tree
      ./kgit ls-tree <트리해시>
      ./kgit commit-tree <트리해시> "커밋 메시지"
      ./kgit cat-file pretty-print <오브젝트해시>
      ./kgit tag v1.0 <커밋해시> "릴리즈 메시지"
      ```

## 프로젝트 구조

- `src/main/kotlin` : 주요 소스 코드
- `object/`, `porcelain/`, `reference/`, `config/` : git 내부 구조와 유사한 기능별 패키지
- `kgit` : 실행 스크립트
- `build.gradle.kts` : 빌드 설정

## 참고

- Kotlin (Java 17) 이상 필요
- Gradle 빌드 사용
- `.kgit` 폴더에 저장소 데이터가 저장됨
