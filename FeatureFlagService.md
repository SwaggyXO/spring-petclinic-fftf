# Spring PetClinic with Feature Flag Service

A production-ready feature flag management system integrated into Spring PetClinic, demonstrating advanced flag strategies including percentage rollouts, whitelisting, blacklisting, and audit trails.

## 🚀 Features

### Feature Flag Capabilities
- ✅ **CRUD Operations** - Create, Read, Update, Delete feature flags via REST API
- ✅ **Multiple Strategies** - Boolean, Percentage Rollout, Whitelist, Blacklist, User Attribute, Kill Switch
- ✅ **Custom Annotation** - `@FeatureToggle` for declarative feature flagging
- ✅ **Audit Trail** - Complete history of all flag changes with timestamps and reasons
- ✅ **Admin Dashboard** - Web UI for managing flags without API calls
- ✅ **Session-based Auth** - Simple admin panel with login/logout
- ✅ **Environment Support** - Separate flags for dev/staging/prod
- ✅ **Database Persistence** - Flags persist across application restarts (PostgreSQL)
- ✅ **Caching** - In-memory cache for high-performance flag evaluation

### Database Schema

**feature_flags**
- Stores flag configuration, strategy type, and environment
- JSONB column for flexible strategy configuration

**flag_audit_log**
- Immutable log of all flag changes
- Tracks who changed what, when, and why

---

## 🔧 Setup & Installation

### Prerequisites
- Java 17+ (JDK 22 used in development)
- Docker & Docker Compose
- Maven 3.8+
- IntelliJ IDEA (recommended) or any Java IDE

### 1. Clone Repository

```bash
git clone https://github.com/SwaggyXO/spring-petclinic-fftf
cd spring-petclinic-fftf
```

### 2. Start PostgreSQL Database

```bash
docker-compose up -d
```

### 3. Run Application

**Using Maven:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

**Using IntelliJ:**
1. Open project in IntelliJ IDEA
2. Edit Run Configuration for `PetClinicApplication`
3. Add an override with key: `spring.profiles.active` and value: `postgres`
4. Run the application

Note: You may encounter a TimeZone error. To fix that, set another override with key: `user.TimeZone` and value: `UTC` or `Asia/Kolkata`.

### 4. Access Application

- **PetClinic Homepage**: http://localhost:8080
- **Admin Dashboard**: http://localhost:8080/admin/flags
- **H2 Console** (if using H2): http://localhost:8080/h2-console

**Admin Credentials:**
- Username: `admin`
- Password: `admin123`

---

## 📖 Usage Guide

### Example Flow: Complete Feature Flag Lifecycle

#### Step 1: Login to Admin Dashboard

```
1. Navigate to http://localhost:8080/admin/login
2. Enter credentials: admin / admin123
3. Click Login
```

#### Step 2: Create First Feature Flag (Boolean Strategy)

**Via Admin UI:**
1. Click "Create New Flag" button
2. Fill in:
   - Flag Key: `add_new_pet`
   - Description: `Controls ability to add new pets to owner accounts`
   - Enabled: ✓ (checked)
   - Strategy Type: `Boolean (Simple On/Off)`
   - Reason: `Initial flag creation for pet management feature`
3. Click "Save"

**Via API:**
```bash
curl -X POST http://localhost:8080/api/v1/flags \
  -H "Content-Type: application/json" \
  -d '{
    "flagKey": "add_new_pet",
    "description": "Controls ability to add new pets to owner accounts",
    "enabled": true,
    "strategyType": "BOOLEAN",
    "environment": "development",
    "createdBy": "admin",
    "reason": "Initial flag creation"
  }'
```

#### Step 3: Test the Feature

1. Navigate to http://localhost:8080
2. Click "Find Owners" → Search for "Franklin"
3. Click on "George Franklin"
4. Click "Add New Pet"
5. The form loads (feature is enabled)

#### Step 4: Disable the Feature

**Via Dashboard:**
1. Find the `add_new_pet` flag
2. Click "Toggle" button
3. Confirm the action

**Via API:**
```bash
curl -X POST http://localhost:8080/api/v1/flags/add_new_pet/toggle
```

Now try adding a pet again - you'll see:
```
403 Forbidden
"Adding new pets is temporarily disabled for maintenance"
```

#### Step 5: Create Percentage Rollout Flag

```bash
curl -X POST http://localhost:8080/api/v1/flags \
  -H "Content-Type: application/json" \
  -d '{
    "flagKey": "add_new_visit",
    "description": "Gradual rollout of new visit scheduling system",
    "enabled": true,
    "strategyType": "PERCENTAGE",
    "strategyConfig": {
      "percentage": 50
    },
    "environment": "development",
    "createdBy": "admin",
    "reason": "Testing new appointment system with 50% of users"
  }'
```

This flag will enable the feature for ~50% of users based on consistent hashing of user/session ID.

#### Step 6: Create Whitelist Flag

```bash
curl -X POST http://localhost:8080/api/v1/flags \
  -H "Content-Type: application/json" \
  -d '{
    "flagKey": "owner_search",
    "description": "Owner search functionality - restricted to beta testers",
    "enabled": true,
    "strategyType": "WHITELIST",
    "strategyConfig": {
      "whitelist": ["user123", "user456", "192.168.1.100"]
    },
    "environment": "development",
    "createdBy": "admin",
    "reason": "Beta testing with selected users and IPs"
  }'
```

#### Step 7: Update Flag Configuration

**Increase rollout percentage from 50% to 80%:**

```bash
curl -X PUT http://localhost:8080/api/v1/flags/add_new_visit \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "strategyType": "PERCENTAGE",
    "strategyConfig": {
      "percentage": 80
    },
    "updatedBy": "admin",
    "reason": "Increasing rollout after successful 50% deployment"
  }'
```

#### Step 8: View Audit History

**Via API:**
```bash
curl http://localhost:8080/api/v1/flags/add_new_visit/audit
```

**Response:**
```json
[
  {
    "id": 2,
    "flagKey": "add_new_visit",
    "action": "UPDATE",
    "oldValue": {
      "enabled": true,
      "strategyType": "PERCENTAGE",
      "strategyConfig": {"percentage": 50}
    },
    "newValue": {
      "enabled": true,
      "strategyType": "PERCENTAGE",
      "strategyConfig": {"percentage": 80}
    },
    "changedBy": "admin",
    "reason": "Increasing rollout after successful 50% deployment",
    "timestamp": "2026-02-07T23:30:00"
  },
  {
    "id": 1,
    "flagKey": "add_new_visit",
    "action": "CREATE",
    "newValue": {
      "enabled": true,
      "strategyType": "PERCENTAGE",
      "strategyConfig": {"percentage": 50}
    },
    "changedBy": "admin",
    "reason": "Testing new appointment system",
    "timestamp": "2026-02-07T23:15:00"
  }
]
```

#### Step 9: List All Flags

```bash
curl http://localhost:8080/api/v1/flags?environment=development
```

#### Step 10: Delete a Flag

**Via API:**
```bash
curl -X DELETE http://localhost:8080/api/v1/flags/owner_search
```

**Via Dashboard:**
1. Find the flag
2. Click "Delete" button (red trash icon)
3. Confirm deletion

---

## 🔌 API Documentation

### Base URL
```
http://localhost:8080/api/v1/flags
```

### Endpoints

#### 1. Get All Flags
```http
GET /api/v1/flags?environment={env}
```

**Query Parameters:**
- `environment` (optional): Filter by environment (default: `development`)

**Response:**
```json
[
  {
    "id": 1,
    "flagKey": "add_new_pet",
    "description": "Controls pet addition feature",
    "enabled": true,
    "strategyType": "BOOLEAN",
    "strategyConfig": {},
    "environment": "development",
    "createdAt": "2026-02-07T23:00:00",
    "updatedAt": "2026-02-07T23:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
]
```

#### 2. Get Single Flag
```http
GET /api/v1/flags/{flagKey}
```

#### 3. Create Flag
```http
POST /api/v1/flags
Content-Type: application/json

{
  "flagKey": "my_feature",
  "description": "My feature description",
  "enabled": true,
  "strategyType": "BOOLEAN",
  "strategyConfig": {},
  "environment": "development",
  "createdBy": "admin",
  "reason": "Creating new feature flag"
}
```

**Strategy Types & Configs:**

| Strategy | Config Example |
|----------|----------------|
| BOOLEAN | `{}` |
| PERCENTAGE | `{"percentage": 50}` |
| WHITELIST | `{"whitelist": ["user1", "192.168.1.1"]}` |
| BLACKLIST | `{"blacklist": ["baduser", "10.0.0.1"]}` |
| KILL_SWITCH | `{}` (always returns false) |

#### 4. Update Flag
```http
PUT /api/v1/flags/{flagKey}
Content-Type: application/json

{
  "enabled": false,
  "updatedBy": "admin",
  "reason": "Disabling due to bug in production"
}
```

#### 5. Toggle Flag (Quick Enable/Disable)
```http
POST /api/v1/flags/{flagKey}/toggle
```

#### 6. Delete Flag
```http
DELETE /api/v1/flags/{flagKey}
```

#### 7. Evaluate Flag (Check if Enabled for Context)
```http
GET /api/v1/flags/{flagKey}/evaluate?userId=user123&sessionId=abc123
```

**Response:**
```json
{
  "enabled": true
}
```

#### 8. Get Audit Log
```http
GET /api/v1/flags/{flagKey}/audit
```

---

## 🎯 Using the @FeatureToggle Annotation

### Basic Usage

```java
@PostMapping("/owners/{ownerId}/pets/new")
@FeatureToggle(
    flagKey = "add_new_pet",
    fallbackMessage = "Adding pets is temporarily disabled"
)
public String processCreationForm(@Valid Pet pet, BindingResult result) {
    // Your business logic
}
```

### With Exception Throwing

```java
@GetMapping("/premium-feature")
@FeatureToggle(
    flagKey = "premium_feature",
    fallbackMessage = "This feature requires premium subscription",
    throwException = true
)
public ResponseEntity<?> premiumFeature() {
    // Premium feature logic
}
```

### How It Works

The `@FeatureToggle` annotation uses Spring AOP (Aspect-Oriented Programming) to intercept method calls:

1. **Before method execution**: Checks if the feature flag is enabled
2. **Context building**: Extracts user ID, session ID, IP from request
3. **Strategy evaluation**: Evaluates based on flag's strategy type
4. **Decision**: 
   - If enabled → Proceed with method execution
   - If disabled → Return error response or throw exception

---

## 🏢 Production Considerations

#### 1. Audit Trail
Every flag change is logged with:
- Timestamp
- User who made the change
- Old and new values
- Reason for change

#### 2. Environment Isolation
Flags are environment-specific (dev/staging/prod), preventing accidental production changes.

#### 3. Gradual Rollout with Kill Switch
- Percentage-based rollouts minimize blast radius
- KILL_SWITCH strategy for emergency shutoff

#### 4. Immutable Audit Logs
Audit entries are append-only, ensuring tamper-proof history.

#### 5. Session-Based Admin Auth
Simple but effective access control for flag management UI.

### Scalability Enhancements

- **Caching**: In-memory cache reduces database load
- **JSONB Strategy Config**: Flexible configuration without schema changes
- **Indexed Queries**: Database indexes on flag_key and environment

---

## 🧪 System Robustness

### Edge Cases Handled

1. **Missing Flag**: Returns false (fail-safe)
2. **Invalid Strategy Config**: Falls back to enabled boolean
3. **Null Context**: Handles gracefully (no NPE)
4. **Concurrent Updates**: Database constraints prevent conflicts
5. **Invalid Flag Key Format**: Validation rejects non-alphanumeric keys

---

## 👨‍💻 Author

Forked and developed by: Devashish Soni\
Email: devashish.soni05@gmail.com\
GitHub: [@SwaggyXO](https://github.com/SwaggyXO/spring-petclinic-fftf)

