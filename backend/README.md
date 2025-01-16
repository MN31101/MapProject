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

# Doc
## Chunks API

### Get All Chunks
```bash
curl -X GET \
  'http://127.0.0.1:8081/api/chunks' \
  -H 'Accept: application/json'
```

### Create Basic Chunk
```bash
curl -X POST \
  'http://127.0.0.1:8081/api/chunk' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "center": {
      "type": "Point",
      "coordinates": [15.41, 51.08]
    },
    "zoom_level": 5
  }'
```

### Create Detailed Zoom Chunk
```bash
curl -X POST \
  'http://127.0.0.1:8081/api/chunk' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "center": {
      "type": "Point",
      "coordinates": [14.42, 50.09]
    },
    "zoom_level": 16
  }'
```

### Create Medium Zoom Chunk
```bash
curl -X POST \
  'http://127.0.0.1:8081/api/chunk' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "center": {
      "type": "Point",
      "coordinates": [14.42, 50.09]
    },
    "zoom_level": 10
  }'
```

### Create Wide Area Chunk
```bash
curl -X POST \
  'http://127.0.0.1:8081/api/chunk' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "center": {
      "type": "Point",
      "coordinates": [14.42, 50.09]
    },
    "zoom_level": 4
  }'
```

## Language Zones API

### Get Zones by Year (Large Area)
```bash
curl -X GET \
  'http://127.0.0.1:8081/api/areas/2024' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "leftTopPointLatLon": [52.08, 14.41],
    "rightBottomPointLatLon": [50.07, 16.42]
  }'
```

### Get Zones by Year (Precise Area)
```bash
curl -X GET \
  'http://127.0.0.1:8081/api/areas/2024' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "leftTopPointLatLon": [50.085, 14.415],
    "rightBottomPointLatLon": [50.075, 14.425]
  }'
```

### Create Basic Language Zone
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
            [15.41, 51.08],
            [15.42, 51.08],
            [15.42, 51.07],
            [15.41, 51.07],
            [15.41, 51.08]
          ]
        ]
      }
    ],
    "description": "Test Zone",
    "name": "Test Zone Name",
    "intensity": 0.75,
    "color": [128, 64, 128],
    "year": 2024
  }'
```

### Create Multi-Polygon Language Zone
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
            [15.41, 51.08],
            [15.42, 51.08],
            [15.42, 51.07],
            [15.41, 51.07],
            [15.41, 51.08]
          ]
        ]
      },
      {
        "type": "Polygon",
        "coordinates": [
          [
            [15.43, 51.09],
            [15.44, 51.09],
            [15.44, 51.08],
            [15.43, 51.08],
            [15.43, 51.09]
          ]
        ]
      }
    ],
    "description": "Multi Polygon Test",
    "name": "Multi Zone",
    "intensity": 0.85,
    "color": [64, 128, 255],
    "year": 2024
  }'
```

### Create High Intensity Zone
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
            [15.41, 51.08],
            [15.42, 51.08],
            [15.42, 51.07],
            [15.41, 51.07],
            [15.41, 51.08]
          ]
        ]
      }
    ],
    "description": "High Intensity Zone",
    "name": "Bright Zone",
    "intensity": 0.95,
    "color": [255, 255, 0],
    "year": 2024
  }'
```

## Query Other Years

### Get 2023 Zones
```bash
curl -X GET \
  'http://127.0.0.1:8081/api/areas/2023' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "leftTopPointLatLon": [51.08, 14.41],
    "rightBottomPointLatLon": [50.07, 15.42]
  }'
```

### Get 2022 Zones
```bash
curl -X GET \
  'http://127.0.0.1:8081/api/areas/2022' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "leftTopPointLatLon": [51.08, 14.41],
    "rightBottomPointLatLon": [50.07, 15.42]
  }'
```

## Usage Notes
1. You can adjust zoom levels (1-16), intensity (0-1), and colors ([0-255, 0-255, 0-255])
2. Always ensure the polygon coordinates form a closed loop (first and last points match)
3. The server returns JSON responses for all requests
4. The timestamps in IDs are automatically generated by the server