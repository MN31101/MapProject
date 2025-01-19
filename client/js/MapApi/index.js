// MapApi.js
export class MapAPI {
    /**
     * Creates a new instance of MapAPI
     * @param {string} baseUrl - Base URL for the API
     */
    constructor(baseUrl = 'http://127.0.0.1:8080/api') {
        this.baseUrl = baseUrl.replace(/\/$/, ''); // Remove trailing slash if present
        this.baseUrl = 'http://127.0.0.1:8080/api'

        this.headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    /**
     * Validates coordinates [longitude, latitude]
     * @private
     * @param {Array} coords - Array containing [longitude, latitude]
     * @returns {boolean} True if coordinates are valid
     * @throws {Error} If coordinates are invalid
     */
    _validateCoordinates(coords) {
        if (!Array.isArray(coords) || coords.length !== 2) {
            throw new Error('Coordinates must be an array of [longitude, latitude]');
        }
        const [lon, lat] = coords;
        if (typeof lon !== 'number' || typeof lat !== 'number') {
            throw new Error('Coordinates must be numbers');
        }
        if (lon < -180 || lon > 180) {
            throw new Error('Longitude must be between -180 and 180');
        }
        if (lat < -90 || lat > 90) {
            throw new Error('Latitude must be between -90 and 90');
        }
        return true;
    }

    /**
     * Validates a polygon
     * @private
     * @param {Array} coordinates - Array of coordinate arrays
     * @throws {Error} If polygon is invalid
     */
    _validatePolygon(coordinates) {
        if (!Array.isArray(coordinates) || !Array.isArray(coordinates[0])) {
            throw new Error('Polygon coordinates must be an array of coordinate arrays');
        }
        
        // Check if polygon is closed (first and last points match)
        const first = coordinates[0];
        const last = coordinates[coordinates.length - 1];
        if (first[0] !== last[0] || first[1] !== last[1]) {
            throw new Error('Polygon must be closed (first and last points must be identical)');
        }

        // Validate each coordinate
        coordinates.forEach(coord => this._validateCoordinates(coord));
    }

    /**
     * Validates color array [r, g, b]
     * @private
     * @param {Array} color - RGB color array
     * @throws {Error} If color is invalid
     */
    _validateColor(color) {
        if (!Array.isArray(color) || color.length !== 3) {
            throw new Error('Color must be an RGB array [r, g, b]');
        }
        if (!color.every(c => Number.isInteger(c) && c >= 0 && c <= 255)) {
            throw new Error('Color values must be integers between 0 and 255');
        }
    }

    /**
     * Validates zoom level
     * @private
     * @param {number} zoom - Zoom level
     * @throws {Error} If zoom level is invalid
     */
    _validateZoom(zoom) {
        if (!Number.isInteger(zoom) || zoom < 1 || zoom > 16) {
            throw new Error('Zoom level must be an integer between 1 and 16');
        }
    }

    /**
     * Validates year
     * @private
     * @param {number} year - Year to validate
     * @throws {Error} If year is invalid
     */
    _validateYear(year) {
        if (!Number.isInteger(year) || year < 1900 || year > 2100) {
            throw new Error('Year must be a valid integer between 1900 and 2100');
        }
    }

    /**
     * Validates intensity value
     * @private
     * @param {number} intensity - Intensity value
     * @throws {Error} If intensity is invalid
     */
    _validateIntensity(intensity) {
        if (typeof intensity !== 'number' || intensity < 0 || intensity > 1) {
            throw new Error('Intensity must be a number between 0 and 1');
        }
    }

    /**
     * Gets all chunks
     * @returns {Promise} Resolves to array of chunks
     */
    async getAllChunks() {
        const response = await fetch(`${this.baseUrl}/chunks`, {
            method: 'GET',
            headers: this.headers
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    }

    /**
     * Creates a new chunk
     * @param {Object} params - Chunk parameters
     * @param {Array} params.coordinates - [longitude, latitude] array
     * @param {number} params.zoomLevel - Zoom level (1-16)
     * @returns {Promise} Resolves to created chunk
     */
    async createChunk({ coordinates, zoomLevel }) {
        this._validateCoordinates(coordinates);
        this._validateZoom(zoomLevel);

        const body = {
            center: {
                type: 'Point',
                coordinates: coordinates
            },
            zoom_level: zoomLevel
        };

        const response = await fetch(`${this.baseUrl}/chunk`, {
            method: 'POST',
            headers: this.headers,
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    }

    /**
     * Gets language zones for a specific year within a bounding box
     * @param {Object} params - Query parameters
     * @param {number} params.year - Year to query
     * @param {Array} params.leftTop - [latitude, longitude] of top-left corner
     * @param {Array} params.rightBottom - [latitude, longitude] of bottom-right corner
     * @returns {Promise} Resolves to array of language zones
     */
    async getLanguageZones({ year, leftTop, rightBottom }) {
        this._validateYear(year);
        if (!Array.isArray(leftTop) || !Array.isArray(rightBottom)) {
            throw new Error('Bounding box coordinates must be arrays');
        }

        // Construct the body of the request
        const body = {
            leftTopPointLatLon: leftTop,
            rightBottomPointLatLon: rightBottom
        };

        const response = await fetch(`${this.baseUrl}/areas/${year}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',  // Ensure Content-Type is set to application/json
                ...this.headers
            },
            body: JSON.stringify(body),  // Send the body as JSON
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const res = await response.json(); 
        console.log(res)
        return res;
    }


    /**
     * Creates a new language zone
     * @param {Object} params - Zone parameters
     * @param {Array} params.polygons - Array of polygon coordinates
     * @param {string} params.description - Zone description
     * @param {string} params.name - Zone name
     * @param {number} params.intensity - Zone intensity (0-1)
     * @param {Array} params.color - RGB color array
     * @param {number} params.year - Year
     * @returns {Promise} Resolves to created language zone
     */
    async createLanguageZone({ polygons, description, name, intensity, color, year }) {
        // Validate all inputs
        polygons.forEach(poly => this._validatePolygon(poly));
        this._validateColor(color);
        this._validateIntensity(intensity);
        this._validateYear(year);

        if (!description || !name) {
            throw new Error('Description and name are required');
        }

        const body = {
            coords: polygons.map(poly => ({
                type: 'Polygon',
                coordinates: [poly]
            })),
            description,
            name,
            intensity,
            color,
            year
        };

        const response = await fetch(`${this.baseUrl}/area`, {
            method: 'POST',
            headers: this.headers,
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    }
}

// Default export
export default MapAPI;