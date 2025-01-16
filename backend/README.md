# Backend

# Setup
## Prerequisites

- Docker
- Docker Compose
- Java 17

### Ubuntu 22.04 

#### Install Docker & Docker Compose

```bash
sudo -qqq -y apt-get update
sudo -qqq -y apt-get install ca-certificates curl gnupg
sudo -qqq -y install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo -qqq -y apt-get update
sudo -qqq -y apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

#### Install Java 17

```bash
sudo -qqq -y apt update
sudo -qqq -y apt install openjdk-17-jdk
java -version
```

## Build

```bash
chmod +x ./gradlew
./gradlew build
```

## Run

```bash
# 127.0.0.1:8080
sudo docker compose up
```

# Doc [AI GENERATED]

## Headers

All requests should include the following headers:

```
Content-Type: application/json
Accept: application/json
```

## Base URL
`http://127.0.0.1:8080/api`

## Chunks API

### Get All Chunks
Retrieves a list of all map chunks.

**Endpoint:**
```http
GET /chunks
```

**Curl Example:**
```bash
curl -X GET \
  'http://127.0.0.1:8080/api/chunks' \
  -H 'Accept: application/json'
```

### Get Specific Chunk
Retrieves a specific chunk by its ID.

**Endpoint:**
```http
GET /chunks/{chunk_id}
```

**Curl Example:**
```bash
curl -X GET \
  'http://127.0.0.1:8080/api/chunks/507f1f77bcf86cd799439011' \
  -H 'Accept: application/json'
```

### Create New Chunk
Creates a new chunk in the system.

**Endpoint:**
```http
POST /chunk
```

**Curl Example:**
```bash
curl -X POST \
  'http://127.0.0.1:8080/api/chunk' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "languages_id": ["507f1f77bcf86cd799439012"],
    "center": {
      "type": "Point",
      "coordinates": [37.61, 55.75]
    },
    "zoom_level": 5
  }'
```

## Language Zones API

### Get Language Zones by Year
Retrieves all language zones for a specific year.

**Endpoint:**
```http
GET //all/{year}
```

**Curl Example:**
```bash
curl -X GET \
  'http://127.0.0.1:8080/api//all/2024' \
  -H 'Accept: application/json'
```

### Get Language Zones Within Bounding Box
Retrieves language zones within specified geographical boundaries for a given year.

**Endpoint:**
```http
GET /areas/{year}
```

**Curl Example:**
```bash
curl -X GET \
  'http://127.0.0.1:8080/api/areas/2024' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "leftTopPointLatLon": [55.75, 37.61],
    "rightBottomPointLatLon": [55.70, 37.66]
  }'
```

### Get Specific Language Zone
Retrieves a specific language zone by its ID.

**Endpoint:**
```http
GET /area/{area_id}
```

**Curl Example:**
```bash
curl -X GET \
  'http://127.0.0.1:8080/api/area/507f1f77bcf86cd799439011' \
  -H 'Accept: application/json'
```

### Create New Language Zone
Creates a new language zone.

**Endpoint:**
```http
POST /area
```

**Curl Example:**
```bash
curl -X POST \
  'http://127.0.0.1:8080/api/area' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "coords": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [37.61, 55.75],
            [37.62, 55.75],
            [37.62, 55.74],
            [37.61, 55.74],
            [37.61, 55.75]
          ]
        ]
      }
    ],
    "description": "Example language zone",
    "name": "Example Zone",
    "intensity": 0.75,
    "color": [255, 128, 0],
    "year": 2024
  }'
```

### Update Language Zone
Updates an existing language zone.

**Endpoint:**
```http
PUT /area/{area_id}
```

**Curl Example:**
```bash
curl -X PUT \
  'http://127.0.0.1:8080/api/area/507f1f77bcf86cd799439011' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "coords": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [37.61, 55.75],
            [37.62, 55.75],
            [37.62, 55.74],
            [37.61, 55.74],
            [37.61, 55.75]
          ]
        ]
      }
    ],
    "description": "Updated description",
    "name": "Updated Zone",
    "intensity": 0.8,
    "color": [255, 128, 0],
    "year": 2024
  }'
```

## Response Status Codes

- `200 OK`: Request successful
- `400 Bad Request`: Invalid request parameters or body
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Notes

1. All IDs are in MongoDB ObjectId format
2. GeoJSON coordinates are in [longitude, latitude] format
3. The color array must contain exactly 3 integers (RGB values)
4. All requests return JSON responses
5. Authentication headers may be required depending on your server configuration