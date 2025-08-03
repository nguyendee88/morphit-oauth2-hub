# 🔐 Morphit OAuth2 Hub

**Morphit OAuth2 Hub** is a lightweight and extensible engine that standardizes OAuth2 authorization flows across multiple third-party platforms.

It supports authorization code flow, token exchange, refresh token, and token storage — all abstracted in a clean, provider-based architecture.

---

## 🚀 What It Does

- 🔐 **OAuth2 Flow**:  
  Full support for Authorization Code → Access Token → Refresh Token

- ⚙️ **Provider Abstraction**:  
  Plug in your own connectors via simple interface  
  (Zalo, Facebook, Google…)

- 🧩 **Token Handling**:  
  Normalize token responses, track expiry, scopes, and refresh logic

- 📦 **Pluggable Token Store**:  
  Stateless (in-memory) or extendable with Redis, SQL, etc.

- 🧪 **Embeddable**:  
  Designed to run inside microservices or integration platforms

---

## 🌍 Supported Platforms (Extensible)

| Provider   | Status   |
|------------|----------|
| Facebook   | 🛠️ Doing |
| Zalo       | 🛠️ Doing |
| Google     | 🔜 Planned |
| TikTok     | 🔜 Planned |
| GitHub     | 🔜 Planned |

---

## 📦 Core Components

- `OAuth2Provider`: base interface for provider config and token logic
- `OAuth2TokenResponse`: normalized access/refresh token structure
- `TokenStore`: optional interface for managing token lifecycle
- Built-in implementations: Facebook, Zalo (more coming soon)

---

## 🧪 Example Usage

Examples will be added under `/examples`:

- [ ] Facebook OAuth2 authorization & token refresh
- [ ] Zalo login flow with Redis token store

---

## 🛠 Status

This is a **core backend library**.  
It **does not** expose any HTTP endpoints or UI.  
Embed it into your own services or combine with other Morphit modules.

---

## 🧰 Tech Stack

- **Language**: Java
- **Protocol**: OAuth2.0 (Authorization Code Grant)
- **Build Tool**: Maven

---

## 🤝 Contribution

PRs and provider implementations are welcome!  
Fork → Feature → PR → ❤️

---

## ☕ Support

If this library helps you build better integrations — feel free to support:

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/nguyendee88)

---

## 📫 Contact

- GitHub: [@nguyendee88](https://github.com/nguyendee88)
- Email: nguyendee.tech@gmail.com

