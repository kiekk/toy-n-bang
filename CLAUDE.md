# PR Rules

PR은 반드시 아래 순서를 따라야 합니다.

1. `feature/*` -> `develop-(back|front)` : 기능 개발 완료 후 develop 브랜치로 머지
2. `develop-(back|front)` -> `deploy-(back|front)` : 배포 준비를 위해 deploy 브랜치로 머지
3. `deploy-(back|front)` -> `master` : 최종 배포를 위해 master 브랜치로 머지

직접 feature에서 master로 PR을 올리거나, 단계를 건너뛰는 것은 금지합니다.
