# 🏐 Today Volleyball (오늘의 배구)

> **V-리그(KOVO) 프로배구 경기 일정, 실시간 결과, 팀 순위 및 맞춤 알림을 제공하는 안드로이드 애플리케이션**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![MinSDK](https://img.shields.io/badge/MinSDK-28_(Android_9.0)-blue?style=flat-square)](https://developer.android.com/)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-37-orange?style=flat-square)](https://developer.android.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean__Architecture_%2B_MVVM-brightgreen?style=flat-square)](https://developer.android.com/topic/architecture)

---

## 📌 목차 (Table of Contents)
1. [프로젝트 소개 (Overview)](#-프로젝트-소개-overview)
2. [주요 기능 (Key Features)](#-주요-기능-key-features)
3. [기술 스택 및 아키텍처 (Tech Stack & Architecture)](#-기술-스택-및-아키텍처-tech-stack--architecture)
4. [프로젝트 구조 (Project Structure)](#-프로젝트-구조-project-structure)
5. [화면 구성 (Screens)](#-화면-구성-screens)
6. [시작하기 (Getting Started)](#-시작하기-getting-started)
7. [주요 설계 특징 (Engineering Highlights)](#-주요-설계-특징-engineering-highlights)

---

## 📖 프로젝트 소개 (Overview)

**Today Volleyball (오늘의 배구)** 은 대한민국 프로배구 V-리그(KOVO) 팬들을 위한 최신 안드로이드 애플리케이션입니다.  

남녀부 경기 일정과 실시간 스코어/결과, 팀별 라운드 순위, 세트별 상세 점수를 한눈에 확인하고, 선호하는 팀이나 특정 경기에 대해 **경기 시작 알림 / 일일 경기 요약 알림 / 경기 결과 알림**을 맞춤 설정할 수 있습니다.

* **100% Jetpack Compose UI**: 최신 선언형 UI 프레임워크 기반의 유연하고 매끄러운 반응형 사용자 경험 제공
* **오프라인 우선 (Offline-First)**: Room 로컬 DB 캐싱 및 WorkManager 백그라운드 실시간 동기화 구현
* **정교한 알림 시스템**: AlarmManager, Notification Channel 및 기기 재부팅 대응(`BOOT_COMPLETED`) 반영

---

## ✨ 주요 기능 (Key Features)

### 1. 📅 경기 일정 및 스코어 캘린더 (Calendar & Matches)
* **주간 / 월간 커스텀 캘린더**: 날짜별 경기 스케줄 및 한국 공휴일/절기 연동 표시
* **리그 & 팀 필터링**: 남자부 / 여자부 구분 및 관심 구단 경기만 필터링 조회
* **실시간 / 경기 결과 상세**: 세트별 점수, 방송 중계 매체, 경기장 위치 정보 제공
* **원터치 경기 알림 예약**: 캘린더 화면에서 손쉽게 개별 경기 알림 핀 켜기/끄기

### 2. 🏆 V-리그 순위표 (Standings)
* **남녀부 구단 순위 정보**: 순위, 팀명, 경기 수, 승점, 승/패, 세트 득실률, 점수 득실률 종합 제공
* **실시간 당겨서 새로고침(Pull-to-Refresh)**: 최신 구단 순위 데이터 즉시 동기화

### 3. 🔔 스마트 알림 및 관리 (Notifications & Alarms)
* **경기 시작 알림**: 예약된 개별 경기 및 선호 구단의 경기 시작 전/시작 정시 알림
* **일일 경기 요약 알림**: 사용자가 지정한 시간(예: 매일 오전 9시)에 당일 예정 경기 목록 통지
* **경기 결과 알림**: 백그라운드에서 경기 종료 감지 시 실시간 스코어 결과 통지
* **재부팅 복구**: `BOOT_COMPLETED` 리시버를 통해 기기 재부팅 시에도 예약 알림 자동 재등록

### 4. 🎨 팀 컬러 테마 & 다크 모드 (Theme & UX)
* **구단 아이덴티티 컬러**: V-리그 구단별 고유 테마 컬러 반영
* **Material 3 적용**: dynamic color 및 다크 모드(Dark Mode) 지원
* **업데이트 변경 이력(Changelog)**: 앱 실행 시 주요 업데이트 변경 사항 팝업 안내

---

## 🛠 기술 스택 및 아키텍처 (Tech Stack & Architecture)

### Architecture Pattern
> **Modern Android Architecture (Clean Architecture + MVVM)**

```text
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                   │
│         [Screens] ◄──► [ViewModels] (StateFlow)        │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    Domain Layer                        │
│          [Models] ◄──► [Repository Interface]          │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                     Data Layer                         │
│  [RepositoryImpl] ──► [KovoApi] (Retrofit 2 / Gson)    │
│                  └──► [VolleyballDao] (Room SQLite)    │
│                  └──► [SettingsDataStore]              │
└────────────────────────────────────────────────────────┘
```

### Tech Stack
| 분류 | 기술 / 라이브러리 |
|---|---|
| **Language** | Kotlin 2.x |
| **Android SDK** | Min SDK 28 (Android 9.0) / Target SDK 37 |
| **UI Framework** | Jetpack Compose, Material 3, Compose Calendar (`composecalendar`) |
| **DI** | Hilt (Dagger-Hilt) + Hilt Navigation Compose + Hilt Work |
| **Network** | Retrofit 2, Gson |
| **Database** | Room (SQLite ORM, KSP Annotation Processor) |
| **Data Storage** | Preferences DataStore |
| **Asynchronous** | Kotlin Coroutines, Flow, StateFlow |
| **Background Work** | WorkManager (`PeriodicWorkRequestBuilder`) |
| **Notifications** | AlarmManager, BroadcastReceiver, NotificationManager |
| **Navigation** | Navigation Compose |

---

## 📁 프로젝트 구조 (Project Structure)

```text
com.example.todayvolleyball
├── alarm/                    # AlarmManager, BroadcastReceiver 및 알림 발송 관리
│   ├── AlarmReceiver.kt      # 개별 경기 시작 알림 수신기
│   ├── AlarmScheduler.kt     # NotificationChannel 생성 및 알람 스케줄링 logic
│   ├── BootReceiver.kt       # 기기 재부팅(BOOT_COMPLETED) 시 알림 자동 재등록
│   └── DailyAlarmReceiver.kt # 매일 지정된 시각의 당일 경기 안내 알림 수신기
├── data/                     # 데이터 레이어 (로컬, 원격, 리포지토리 구현)
│   ├── local/                # Room DB Entities, DAO, Database 및 DataStore
│   ├── remote/               # KOVO API DTO (ApiModels) 및 Retrofit Interface (KovoApi)
│   └── repository/           # VolleyballRepositoryImpl (원격/로컬 캐시 동기화)
├── di/                       # Dagger-Hilt 의존성 주입 모듈 (AppModule, WorkerModule)
├── domain/                   # 도메인 레이어 (순수 비즈니스 모델 및 인터페이스)
│   ├── model/                # Schedule, Team, TeamRank 등 순수 데이터 모델
│   └── repository/           # VolleyballRepository 인터페이스
├── ui/                       # UI 레이어 (Jetpack Compose 화면 및 테마)
│   ├── screen/               # Screen Composable & ViewModel
│   │   ├── CalendarScreen.kt / CalendarViewModel.kt    # 경기 일정 캘린더
│   │   ├── StandingsScreen.kt / StandingsViewModel.kt  # V-리그 팀 순위표
│   │   ├── SettingsScreen.kt / SettingsViewModel.kt    # 알림 및 설정
│   │   ├── MatchDetailScreen.kt                        # 경기 상세 정보
│   │   ├── SplashScreen.kt                             # 스플래시 화면
│   │   └── MainScreen.kt / MainViewModel.kt            # 내비게이션 주관 화면
│   └── theme/                # Color, Theme, Type 등 Material 3 테마 설정
├── util/                     # 유틸리티 (한국 공휴일 계산기, UI 확장함수)
└── worker/                   # WorkManager 백그라운드 주동기화 (SyncWorker)
```

---

## 📱 주요 화면 안내 (Screens)

| 화면 | 설명 |
|---|---|
| **캘린더 화면 (`CalendarScreen`)** | 날짜별 경기 스케줄, 실시간 스코어, 방송 중계 정보 확인 및 경기 알림 On/Off |
| **순위표 화면 (`StandingsScreen`)** | 남녀부 구단 승점, 승/패 수, 세트 득실률 및 점수 득실률 순위표 |
| **설정 화면 (`SettingsScreen`)** | 경기 시작 알림 설정, 일일 경기 알림 시간 선택, 관심 팀 설정 및 예약된 알림 목록 삭제 |
| **경기 상세 화면 (`MatchDetailScreen`)** | 경기장, 경기 시각, 홈/어웨이 팀별 세트 스코어 상세 정보 |

---

## 🚀 시작하기 (Getting Started)

### 사전 요구 사항 (Prerequisites)
* **Android Studio**: Ladybug (2024.2.1) 이상 권장
* **JDK**: Java 17 이상
* **Android SDK**: Compile SDK 37, Min SDK 28

### 빌드 및 실행 방법 (Build Instructions)

```bash
# 1. 저장소 클론
git clone https://github.com/your-username/today_volleyball.git

# 2. 프로젝트 디렉토리 이동
cd today_volleyball

# 3. Debug APK 빌드
./gradlew assembleDebug
```

---

## ⚙️ 주요 설계 특징 (Engineering Highlights)

1. **오프라인 우선 (Offline-First Architecture)**:
   * KOVO 네트워크 API 장애 시에도 Room DB에 저장된 경기 일정과 팀 순위를 즉시 보여줍니다.
   * `SyncWorker`가 15분 주기로 백그라운드에서 실행되며 최신 경기 스코어를 실시간 동기화 및 갱신합니다.

2. **신뢰할 수 있는 알림 복구 시스템 (Notification Recovery)**:
   * `AlarmManager.setExactAndAllowWhileIdle()` 및 Android `USE_EXACT_ALARM` 권한을 적용해 도즈(Doze) 모드에서도 정확한 정시에 알림을 보냅니다.
   * 기기 재부팅 시 `BootReceiver`가 실행되어 DataStore에 보관된 알람 예약 정보를 읽고 시스템 알람을 자동으로 다시 등록합니다.

3. **안전하고 효율적인 UI 상태 관리**:
   * `StateFlow` 및 Compose의 Lifecycle-aware state collection을 활용하여 메모리 누수를 방지하고 수명주기에 적절하게 반응하도록 설계했습니다.
