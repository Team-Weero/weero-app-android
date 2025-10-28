# JSBridge API Documentation

Android WebView와 JavaScript 간의 통신 인터페이스입니다.

## Interface Name
```javascript
Android
```

## API Methods

### 1. Refresh Token 관리

#### `saveRefreshToken(token: string): void`
Refresh token을 안전하게 저장합니다.

**Parameters:**
- `token` (string): 저장할 refresh token

**Example:**
```javascript
Android.saveRefreshToken("your_refresh_token_here");
```

---

#### `getRefreshToken(): string`
저장된 refresh token을 반환합니다.

**Returns:**
- (string): 저장된 refresh token. 없으면 빈 문자열("")

**Example:**
```javascript
const refreshToken = Android.getRefreshToken();
console.log("Refresh Token:", refreshToken);
```

---

### 2. FCM Token 관리

#### `getFirebaseToken(): string`
저장된 FCM 토큰을 동기적으로 반환합니다.

**Returns:**
- (string): 저장된 FCM token. 없으면 빈 문자열("")

**Example:**
```javascript
const fcmToken = Android.getFirebaseToken();
console.log("FCM Token:", fcmToken);
```

---

#### `getFirebaseTokenAsync(callbackName: string): void`
Firebase에서 최신 FCM 토큰을 비동기로 가져옵니다. 저장된 토큰을 무시하고 Firebase에서 직접 가져옵니다.

**Parameters:**
- `callbackName` (string): 토큰을 받을 JavaScript 콜백 함수 이름

**Callback Signature:**
```javascript
function callbackName(token: string): void
```

**Example:**
```javascript
// 콜백 함수 정의
function onTokenReceived(token) {
    if (token) {
        console.log("FCM Token received:", token);
        // 서버에 토큰 전송 등
    } else {
        console.error("Failed to get FCM token");
    }
}

// 토큰 요청
Android.getFirebaseTokenAsync("onTokenReceived");
```

---

#### `refreshFirebaseToken(callbackName: string): void`
기존 FCM 토큰을 삭제하고 새로운 토큰을 발급받습니다.

**Parameters:**
- `callbackName` (string): 새 토큰을 받을 JavaScript 콜백 함수 이름

**Callback Signature:**
```javascript
function callbackName(token: string): void
```

**Example:**
```javascript
// 콜백 함수 정의
function onTokenRefreshed(token) {
    if (token) {
        console.log("New FCM Token:", token);
        // 서버에 새 토큰 업데이트
    } else {
        console.error("Failed to refresh FCM token");
    }
}

// 토큰 재발급 요청
Android.refreshFirebaseToken("onTokenRefreshed");
```

---

#### `deleteFirebaseToken(callbackName: string): void`
FCM 토큰을 완전히 삭제합니다 (Firebase 및 로컬 저장소).

**Parameters:**
- `callbackName` (string): 삭제 결과를 받을 JavaScript 콜백 함수 이름

**Callback Signature:**
```javascript
function callbackName(success: boolean): void
```

**Example:**
```javascript
// 콜백 함수 정의
function onTokenDeleted(success) {
    if (success) {
        console.log("FCM token successfully deleted");
    } else {
        console.error("Failed to delete FCM token");
    }
}

// 토큰 삭제 요청
Android.deleteFirebaseToken("onTokenDeleted");
```

---

## Data Storage

### Secure Storage
모든 토큰은 `EncryptedSharedPreferences`를 사용하여 AES256-GCM 암호화로 안전하게 저장됩니다.

**Storage Keys:**
- `refresh_token`: Refresh token
- `fcm_token`: FCM token

---

## Usage Examples

### 앱 시작 시 FCM 토큰 가져오기
```javascript
window.addEventListener('load', function() {
    Android.getFirebaseTokenAsync("handleFCMToken");
});

function handleFCMToken(token) {
    if (token) {
        console.log("FCM Token:", token);
        // 서버에 전송
        sendTokenToServer(token);
    }
}
```

### 로그인 시 Refresh Token 저장
```javascript
function onLoginSuccess(response) {
    const refreshToken = response.refreshToken;
    Android.saveRefreshToken(refreshToken);

    // FCM 토큰도 함께 서버에 전송
    Android.getFirebaseTokenAsync("sendFCMTokenToServer");
}
```

### 로그아웃 시 토큰 삭제
```javascript
function logout() {
    // Refresh token은 로컬에서만 삭제
    Android.saveRefreshToken("");

    // FCM token은 Firebase에서도 삭제
    Android.deleteFirebaseToken("onLogoutComplete");
}

function onLogoutComplete(success) {
    console.log("Logout completed, FCM token deleted:", success);
    // 로그아웃 완료 처리
}
```

---

## Notes

1. **동기 vs 비동기**
   - `getFirebaseToken()`: 즉시 반환 (저장된 값만)
   - `getFirebaseTokenAsync()`: Firebase에서 최신 토큰 가져오기 (권장)

2. **콜백 함수**
   - 모든 비동기 메서드는 전역 함수 이름을 문자열로 받습니다
   - 콜백 함수는 반드시 전역 스코프에 정의되어야 합니다

3. **에러 처리**
   - 비동기 메서드 실패 시 콜백에 빈 문자열("") 또는 false 전달
   - 항상 콜백에서 값을 확인하세요

4. **보안**
   - 모든 토큰은 암호화되어 저장됩니다
   - HTTPS 통신을 사용하여 서버에 토큰을 전송하세요
