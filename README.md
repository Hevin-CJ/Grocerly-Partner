# Grocerly Partners — Seller & Dashboard Application

Grocerly Partners is a native Android application designed for merchants and vendors. It provides real-time catalog administration, coupon management, a multi-stage order dispatch pipeline, and an offline-first sales analytics dashboard.

> [!NOTE]
> **Companion Application**: This is the seller-facing dashboard application. For the customer-facing shopping application, see the [Grocerly (Customer App)](https://github.com/Hevin-CJ/Grocerly) repository.

---


## 🚀 Key Features

* **Dashboard & Sales Analytics**: Real-time evaluation of active revenue, total orders, revenue loss, and cancellation metrics. Offers comparative trend analysis (growth/decline) against previous periods with filters for Today, Yesterday, Current Week, Current Month, and Custom Date Ranges.
* **Offline-First Persistence**: Caches critical dashboard statistics in a local **Room Database** (`BusinessUiStateEntity`), allowing merchants to view performance metrics offline.
* **Catalog Management**: Add, update, and toggle the availability of products. Handles image uploads to **Firebase Storage**.
* **Order Processing Pipeline**: Listen to incoming sub-orders containing store-specific items. Manage fulfillment through explicit states (`ACCEPTED`, `READY`, `SHIPPED`), trigger per-item cancellations with custom reasons, and send real-time update notifications to customers via **Firebase Cloud Messaging**.
* **Coupon & Promotion Engine**: Create, update, and toggle active coupon rules (`PartnerCouponRule`) that dictate rewards given to customers (e.g., minimum spend thresholds, discount values, and coupon expiration intervals).
* **Robust Testing Framework**: Comprehensive unit tests written using **Mockito** and **Kotlinx-Coroutines-Test** to validate sales statistics calculations and date ranges.

---

## 🛠️ Architecture & Tech Stack

* **Language**: Kotlin
* **Architecture**: MVVM (Model-View-ViewModel)
* **UI Framework**: XML Layouts + ViewBinding & DataBinding (Jetpack Navigation Component with Safe Args)
* **Dependency Injection**: Dagger Hilt
* **Local Caching**: Room DB & Preferences DataStore
* **JSON Serialization**: Kotlinx Serialization & Moshi
* **Testing Library**: JUnit, Mockito Kotlin, Mockito Core, Coroutines Test

---

## 📂 Project Structure

```
app/src/main/java/com/example/grocerlypartners/
│
├── activity/          # MainActivity (App controller)
├── fragments/         # UI Screens (Dashboard, Inventory, Order Fulfillment, Promos, etc.)
├── adaptor/           # RecyclerView Adapters for orders, products, and analytics lists
├── viewmodel/         # ViewModels managing state flows and dashboard metrics
├── repository/        # Remote and Local repositories (business logic separation)
├── room/              # Room database components (BusinessUiState DAOs, entities)
├── preferences/       # Preferences DataStore helper scripts
├── di/                # Hilt modules (Firebase, Local DB, Connectivity helper)
├── model/             # Shared data models (Order, Product, CouponRule, Analytics)
├── uistate/           # UI wrapper wrappers for dashboard states
└── utils/             # Helper classes (OrderStatus enums, Network connectivity observers)
```

---

## 🔄 Firestore Database Schema

Grocerly Partners synchronizes with the shared **Cloud Firestore** backend:
* `partners/{partnerId}/products`: Store inventory entries.
* `partners/{partnerId}/offers`: Active product-level discounts.
* `partners/{partnerId}/coupons`: Promo codes rule-sets configured by the merchant.
* `partners/{partnerId}/orders`: Sub-orders containing items matching this seller.
* `orders/{orderId}`: Writes updates back to the global order directory.
* `users/{userId}/orders/{orderId}`: Syncs item status updates for customer notification and tracking.

---

## ⚙️ Getting Started

### 📋 Prerequisites
1. Android Studio Ladybug (or newer)
2. JDK 11+
3. Shared Firebase project credentials.

### 🔑 Configuration Setup
1. Download `google-services.json` from the shared Firebase console project and copy it to the `app/` folder of this project.
2. Enable Firestore, Auth, and Storage in your console configuration.

### 🏃 Running the Project
1. Clone this repository:
   ```bash
   git clone https://github.com/Hevin-CJ/Grocerly-Partner.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and compile the project on an active device or emulator (target SDK 34).
