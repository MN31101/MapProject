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
`http://127.0.0.1:8081/api`

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
  'http://127.0.0.1:8081/api/chunks' \
  -H 'Accept: application/json'
```

**Example Response:**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "languages_id": ["507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013"],
    "center": {
      "type": "Point",
      "coordinates": [14.41, 50.08]
    },
    "zoom_level": 5
  },
  {
    "id": "507f1f77bcf86cd799439014",
    "languages_id": ["507f1f77bcf86cd799439015"],
    "center": {
      "type": "Point",
      "coordinates": [2.35, 48.85]
    },
    "zoom_level": 6
  }
]
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
  'http://127.0.0.1:8081/api/chunks/507f1f77bcf86cd799439011' \
  -H 'Accept: application/json'
```

**Example Response:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "languages_id": ["507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013"],
  "center": {
    "type": "Point",
    "coordinates": [14.41, 50.08]
  },
  "zoom_level": 5
}
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
  'http://127.0.0.1:8081/api/chunk' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "languages_id": ["507f1f77bcf86cd799439012"],
    "center": {
      "type": "Point",
      "coordinates": [14.41, 50.08]
    },
    "zoom_level": 5
  }'
```

**Example Response:**
```json
{
  "id": "507f1f77bcf86cd799439016",
  "languages_id": ["507f1f77bcf86cd799439012"],
  "center": {
    "type": "Point",
    "coordinates": [14.41, 50.08]
  },
  "zoom_level": 5
}
```

## Language Zones API

### Get Language Zones by Year
Retrieves all language zones for a specific year.

**Endpoint:**
```http
GET /all/{year}
```

**Curl Example:**
```bash
curl -X GET \
  'http://127.0.0.1:8081/api/all/2024' \
  -H 'Accept: application/json'
```

**Example Response:**
```json
[
  {
    "id": "507f1f77bcf86cd799439020",
    "coords": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [14.41, 50.08],
            [14.42, 50.08],
            [14.42, 50.07],
            [14.41, 50.07],
            [14.41, 50.08]
          ]
        ]
      }
    ],
    "description": "Urban dialect zone A",
    "name": "Zone A",
    "intensity": 0.75,
    "color": [255, 128, 0],
    "year": 2024
  }
]
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
  'http://127.0.0.1:8081/api/areas/2024' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "leftTopPointLatLon": [50.08, 14.41],
    "rightBottomPointLatLon": [50.07, 14.42]
  }'
```

**Example Response:**
```json
[
  {
    "id": "507f1f77bcf86cd799439020",
    "coords": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [14.41, 50.08],
            [14.42, 50.08],
            [14.42, 50.07],
            [14.41, 50.07],
            [14.41, 50.08]
          ]
        ]
      }
    ],
    "description": "Historical dialect area B",
    "name": "Zone B",
    "intensity": 0.75,
    "color": [255, 128, 0],
    "year": 2024
  }
]
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
  'http://127.0.0.1:8081/api/area/507f1f77bcf86cd799439020' \
  -H 'Accept: application/json'
```

**Example Response:**
```json
{
  "id": "507f1f77bcf86cd799439020",
  "coords": [
    {
      "type": "Polygon",
      "coordinates": [
        [
          [14.41, 50.08],
          [14.42, 50.08],
          [14.42, 50.07],
          [14.41, 50.07],
          [14.41, 50.08]
        ]
      ]
    }
  ],
  "description": "Mixed dialect region C",
  "name": "Zone C",
  "intensity": 0.75,
  "color": [255, 128, 0],
  "year": 2024
}
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
  'http://127.0.0.1:8081/api/area' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "coords": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [14.41, 50.08],
            [14.42, 50.08],
            [14.42, 50.07],
            [14.41, 50.07],
            [14.41, 50.08]
          ]
        ]
      }
    ],
    "description": "New dialect zone",
    "name": "Zone D",
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
  'http://127.0.0.1:8081/api/area/507f1f77bcf86cd799439020' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "coords": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [14.41, 50.08],
            [14.42, 50.08],
            [14.42, 50.07],
            [14.41, 50.07],
            [14.41, 50.08]
          ]
        ]
      }
    ],
    "description": "Updated dialect zone",
    "name": "Updated Zone D",
    "intensity": 0.8,
    "color": [255, 128, 0],
    "year": 2024
  }'
```

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-16T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid bounding box coordinates"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-16T10:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Language zone not found with id: 507f1f77bcf86cd799439020"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-16T10:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Notes

1. All IDs are in MongoDB ObjectId format (24-character hexadecimal)
2. GeoJSON coordinates are in [longitude, latitude] format
3. The color array must contain exactly 3 integers (RGB values, 0-255)
4. All requests return JSON responses
5. When creating or updating language zones, all fields are required
6. The `intensity` value should be between 0 and 1
7. For polygon coordinates, the first and last points must be identical to close the polygon
8. Years should be provided as integers (e.g., 2024)
9. Zoom levels typically range from 0 (most zoomed out) to 18 (most zoomed in)
10. Bounding box coordinates must be provided in [latitude, longitude] format