# Family Weekly Calendar (주간 가족 일정표)

가족 구성원들이 실시간으로 일정을 공유하고 관리할 수 있는 안드로이드 기반의 주간 일정표 애플리케이션입니다.

## 📌 주요 기능

- **실시간 일정 공유**: Firebase Firestore를 연동하여 여러 사용자가 동시에 일정을 수정하고 확인할 수 있습니다.
- **주간 뷰 (Weekly View)**: 월요일부터 일요일까지 한눈에 들어오는 직관적인 시간표 레이아웃을 제공합니다.
- **유연한 일정 관리**: 
    - 9:00 ~ 24:00 사이의 일정을 30분 단위로 설정 가능
    - 동일 시간대 여러 일정 추가 시 자동 가로 나열 (Side-by-side)
    - 긴 일정의 경우 블록 통합 표시 (Spanning)
- **커스텀 디자인**:
    - 20가지 이상의 다양한 색상으로 일정 구분
    - 라이트 모드 및 다크 모드(화이트/블랙 테마) 지원
- **반응형 UI**: 좌우/상하 스크롤을 통해 많은 일정도 쾌적하게 확인 가능합니다.

## 🛠 기술 스택

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Modern Android UI)
- **Architecture**: MVVM (ViewModel, StateFlow)
- **Backend**: Firebase Firestore (NoSQL Database)
- **Dependency Management**: Version Catalogs (libs.versions.toml)

## 🚀 시작하기

1. **Firebase 설정**: 
   - `google-services.json` 파일을 `app/` 폴더에 추가해야 합니다.
   - Firestore Database를 생성하고 `schedules` 컬렉션을 준비합니다.
2. **빌드 및 실행**:
   - Android Studio에서 프로젝트를 열고 실행합니다.
   - APK 빌드가 필요한 경우 `Build > Build APK(s)`를 사용하세요.

## 📱 스크린샷 가이드

- **메인 화면**: 주간 시간표 그리드가 표시됩니다.
- **추가/수정**: 하단의 FAB(+) 버튼이나 빈 칸을 클릭하여 다이얼로그를 통해 제목, 시간, 색상을 설정합니다.
- **테마 전환**: 상단 설정 아이콘을 클릭하여 화이트/블랙 테마를 전환할 수 있습니다.

---
© 2024 Family Weekly Calendar Project
