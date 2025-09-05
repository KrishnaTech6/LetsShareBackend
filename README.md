# 📡 Let’s Share — Screen Sharing Backend & Web Viewer

A **Kotlin + Spring Boot** backend for **real-time Android screen viewing**, plus a simple **browser viewer** (HTML + JS).

Android devices connect as **senders** over WebSocket and push **JPEG frames**. A browser connects as a **viewer** and renders frames onto a `<canvas>` in real time.

---

## ✨ Features

- 📱 **Device registry API** (`/api/register`, `/api/devices`)
- 🧷 **In-memory device tracking** with online/streaming status
- 🔌 **Raw WebSocket** endpoint (`/ws/screen`) — no STOMP
- 🎬 **Start command**: backend → device (`START_STREAM`)
- 🖥️ **Web viewer** (`viewer.html`) to pick a device and watch live

---

## 🧭 Architecture (high-level)

```mermaid
sequenceDiagram
    participant Sender (Android)
    participant Backend (Spring Boot)
    participant Viewer (Browser)

    Sender->>Backend: WS connect /ws/screen?role=sender&deviceId=XYZ
    Viewer->>Backend: WS connect /ws/screen?role=viewer&watch=XYZ
    Viewer->>Backend: POST /api/start/XYZ
    Backend-->>Sender: START_STREAM (text)
    loop Streaming
        Sender-->>Backend: Binary frame (JPEG)
        Backend-->>Viewer: Binary frame (JPEG)
    end
```

