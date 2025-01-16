/*

    // An idiot admires complexity.

    Features: 
        + Mobile Responsivness
        + Draw Languages by yourself

    Style:
        - color (rgb)
        - alpha

[done] + drawCoordinates(canvas, arrayLatLonCoordinates, centerLatLon, style)
[done] + LatLonToMercator(point)
[done] + LatLonToMercator(array of points)
[done] + MercatorToPX(center, leftTopPointLatLon, rightBottomPointLatLon, mercatorPoint)
[done] + MercatorToPX(center, leftTopPointLatLon, rightBottomPointLatLon, mercatorPointArray)

[rejected] + Simplify(center, leftTopPointMercator, rightBottomPointMercator, mercatorPointArray, zoomLevel)
        // deletes points from structure if two points closer then 1px apart. 
    + GetChunkGrid(center, leftTopPointLatLon, rightBottomPointLatLon, chunkAmount)
        // Returns LeftTop RightBottom Center of each Chunk

    API
        getChunk body: 
            {
                year
                leftTopPointLatLon: [x, y],
                rightBottomPointLatLon: [x, y]
            } 
        returns {
                [
                    ..., {
                        *languageZone*, 
                        coordinates: [...[lattitude, longitude]], 
                    }
                ]
            }

    ChunkManager
        + calculateRequiredChunks
        + getChunks
            // API Call?

    Viewport: 
        - (meters per pixel in one chunk) // for display purposes 
        - array of all chunks?
        - center (latlon)
        + clear
        + drawBackground 
        + drawChunks(chunks)
        + updateCenter(dx_pixels, dy_pixels)
            // update center (latlon) converting pixels do distance
            // Use of zoomLevel

    Chunk: 
        - center (latlon)
        - rect ((latlon, latlon), (latlon, latlon))
        - languageZones [...]
        - Year  

    LanguageZone: 
        - coords [..., (latlon)]
        - Name, etc. 


Algorythm: 
    we have viewport 
    viewport has shape in px
    we can calulate borders of viewport (latlon) using it's shape and zoomlevel.
    zoomlevel tells how much meters in one pixel. (horizontally/vertically)
        { zoomlevel: 1.5meters/1pixel, zoomlevel 21: 1000meters/1pixel }
    
    this zoomlevel is for chunks that are contained in viewport.
        we calculate grid, having latlon points 
    
    so, each cell in this grid that calculated from latlon coordinates is chunk. 
    each chunk has it's center, and zoomlevel, same as in viewport. 
        // we store zoomlevel for further caching. 
    
    XXXX client sends request about each chunk. (center, zoomlevel) 
        // nah
    > client sends request about each chunk (latlon coordinates of each square point)
        backend take all languagezones, and filters coordinates by which of them can fit in square.     
        > there is no zoomlevel on side of api then

    > client receives zones, in which there also latlon coordinates
    chunk by chunk client draws zones onto canvas, converting latlon to mercator and fitting mercator into screen 

    */





export class Style {
    constructor(rgb, a) { 
        this.rgb = rgb;
        this.a = a;
    }

    get r() { return this.rgb[0]}
    get g() { return this.rgb[1]}
    get b() { return this.rgb[2]}
    
}

export function latLonToMercator(point) {
    if (Array.isArray(point[0])) {
        return point.map(latLonToMercator);
    }
    
    const [lat, lon] = point;
    const x = lon * Math.PI / 180;
    const y = Math.log(Math.tan((90 + lat) * Math.PI / 360));
    return [x, y];
}

export function normalizeMercator(center, leftTop, rightBottom, mercatorPoint) {
    if (Array.isArray(mercatorPoint[0])) {
        return mercatorPoint.map(p => normalizeMercator(center, leftTop, rightBottom, p));
    }

    const [leftX, topY] = latLonToMercator(leftTop);
    const [rightX, bottomY] = latLonToMercator(rightBottom);
    
    const [x, y] = mercatorPoint;
    const normalizedX = (x - leftX) / (rightX - leftX);
    const normalizedY = (y - topY) / (bottomY - topY);
    
    return [normalizedX, normalizedY];
}


export function resizeToShape(normalizedPoints, shape) {
    if (Array.isArray(normalizedPoints[0])) {
        return normalizedPoints.map(p => resizeToShape(p, shape));
    }
    
    return [
        normalizedPoints[0] * shape[0], 
        normalizedPoints[1] * shape[1]
    ];
}


export function drawPolygonToCanvas(canvas, polygonLatLon, leftTop, rightBottom, center, style) { 
    let width = canvas.width;
    let height = canvas.height;
    let polygonMercator = latLonToMercator(polygonLatLon);
    let normalized = normalizeMercator(center, leftTop, rightBottom, polygonMercator);
    let pixels = resizeToShape(normalized, [width, height]);
    
    const ctx = canvas.getContext('2d');
    ctx.beginPath();
    ctx.moveTo(pixels[0][0], pixels[0][1]);

    for(let i = 1; i < pixels.length; i++) {
        ctx.lineTo(pixels[i][0], pixels[i][1]);
    }

    ctx.fillStyle = `rgba(${style.r}, ${style.g}, ${style.b}, ${style.a})`;
    ctx.fill();
    
    ctx.strokeStyle = 'black';
    ctx.stroke();
    return canvas;
}


export function pixelToLatLon(pixel, shape, leftTop, rightBottom) {
    const normalizedPoint = [
        pixel[0] / shape[0],
        pixel[1] / shape[1]
    ];
    
    const [leftX, topY] = latLonToMercator(leftTop);
    const [rightX, bottomY] = latLonToMercator(rightBottom);
    
    const mercatorX = leftX + (rightX - leftX) * normalizedPoint[0];
    const mercatorY = topY + (bottomY - topY) * normalizedPoint[1];
    
    const lon = (mercatorX * 180) / Math.PI;
    const lat = (Math.atan(Math.exp(mercatorY)) * 360) / Math.PI - 90;
    
    return [lat, lon];
}


export function pixelDeltaToLatLonDelta(dx, dy, canvas, leftTop, rightBottom) {
    const centerPx = [canvas.width/2, canvas.height/2];
    const centerLatLon = pixelToLatLon(centerPx, [canvas.width, canvas.height], leftTop, rightBottom);
    
    const movedPx = [centerPx[0] + dx, centerPx[1] + dy];
    const movedLatLon = pixelToLatLon(movedPx, [canvas.width, canvas.height], leftTop, rightBottom);
    
    return [
        movedLatLon[0] - centerLatLon[0],  // dlat
        movedLatLon[1] - centerLatLon[1]   // dlon
    ];
}


export function addLatLonDelta(latlon, dlatlon) {
    return [
        latlon[0] + dlatlon[0],
        latlon[1] + dlatlon[1] 
    ];
}


export function getCanvasBoundaries(canvas, leftTop, rightBottom) {
    const topLeft = pixelToLatLon([0, 0], [canvas.width, canvas.height], leftTop, rightBottom);
    const topRight = pixelToLatLon([canvas.width, 0], [canvas.width, canvas.height], leftTop, rightBottom);
    const bottomLeft = pixelToLatLon([0, canvas.height], [canvas.width, canvas.height], leftTop, rightBottom);
    const bottomRight = pixelToLatLon([canvas.width, canvas.height], [canvas.width, canvas.height], leftTop, rightBottom);
    
    const center = pixelToLatLon(
        [canvas.width/2, canvas.height/2], 
        [canvas.width, canvas.height], 
        leftTop, 
        rightBottom
    );
    
    return {
        topLeft,
        topRight,
        bottomLeft,
        bottomRight,
        center
    };
}

export class LanguageZone {
    constructor(coords, name, description, color, intensity, year) {
        this.coords = coords;
        this.name = name;
        this.description = description;
        this.color = color;
        this.intensity = intensity;
        this.year = year;
    }
}

// Simplified Chunk class
export class Chunk {
    constructor(zones, leftTop, rightBottom, center) {
        this.zones = zones;
        this.leftTop = leftTop;
        this.rightBottom = rightBottom;
        this.center = center;
    }
}

// Helper function to check if a point is within bounds
export function isPointInBounds(point, leftTop, rightBottom) {
    return point[0] >= leftTop[0] && 
           point[0] <= rightBottom[0] && 
           point[1] >= leftTop[1] && 
           point[1] <= rightBottom[1];
}

// Helper function to calculate the distance between two points in meters
export function calculateDistance(point1, point2) {
    const R = 6371e3; // Earth's radius in meters
    const φ1 = point1[0] * Math.PI / 180;
    const φ2 = point2[0] * Math.PI / 180;
    const Δφ = (point2[0] - point1[0]) * Math.PI / 180;
    const Δλ = (point2[1] - point1[1]) * Math.PI / 180;
    
    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ/2) * Math.sin(Δλ/2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    
    return R * c;
}


export function calculateChunkGrid(center, leftTop, rightBottom, chunksPerAxis = 3) {
    const latDelta = (rightBottom[0] - leftTop[0]) / chunksPerAxis;
    const lonDelta = (rightBottom[1] - leftTop[1]) / chunksPerAxis;
    
    const chunks = [];
    
    for (let i = 0; i < chunksPerAxis; i++) {
        for (let j = 0; j < chunksPerAxis; j++) {
            const chunkLeftTop = [
                leftTop[0] + i * latDelta,
                leftTop[1] + j * lonDelta
            ];
            
            const chunkRightBottom = [
                chunkLeftTop[0] + latDelta,
                chunkLeftTop[1] + lonDelta
            ];
            
            const chunkCenter = [
                (chunkLeftTop[0] + chunkRightBottom[0]) / 2,
                (chunkLeftTop[1] + chunkRightBottom[1]) / 2
            ];
            
            chunks.push({
                leftTop: chunkLeftTop,
                rightBottom: chunkRightBottom,
                center: chunkCenter
            });
        }
    }
    
    return chunks;
}

// Fetch chunk data from API
export async function fetchChunkData(chunkBounds, year) {
    const response = await fetch('/api/areas/' + year, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            leftTopPointLatLon: chunkBounds.leftTop,
            rightBottomPointLatLon: chunkBounds.rightBottom
        })
    });
    
    if (!response.ok) {
        throw new Error('Failed to fetch chunk data');
    }
    
    return await response.json();
}

export function calculateZoomLevel(leftTop, rightBottom) {
    const latSpan = Math.abs(rightBottom[0] - leftTop[0]);
    const baseZoom = Math.log2(360 / latSpan);
    return Math.floor(baseZoom);
}

export class Viewport {
    #canvas;
    #chunks = [];
    
    constructor(width, height, id) {
        this.leftTop = [90, -180];
        this.rightBottom = [-90, 180];
        this.center = [0, 0];
        
        this.#canvas = document.createElement('canvas');
        this.#canvas.width = width;
        this.#canvas.height = height;
        this.#canvas.id = id;
        
        this.isDragging = false;
        this.lastMouseX = 0;
        this.lastMouseY = 0;
        
        this.year = 2024; // Default year
        
        document.querySelector("#app").appendChild(this.#canvas);
        this.setupEventListeners();
    }
    
    setupEventListeners() {
        this.#canvas.addEventListener('wheel', this.wheel.bind(this));
        this.#canvas.addEventListener('mousedown', this.mousedown.bind(this));
        this.#canvas.addEventListener('mousemove', this.mousemove.bind(this));
        this.#canvas.addEventListener('mouseup', this.mouseup.bind(this));
        this.#canvas.addEventListener('mouseleave', this.mouseleave.bind(this));
    }
    
    async updateChunks() {
        const chunkGrid = calculateChunkGrid(this.center, this.leftTop, this.rightBottom);
        const zoomLevel = calculateZoomLevel(this.leftTop, this.rightBottom);
        
        const newChunks = await Promise.all(
            chunkGrid.map(async chunkBounds => {
                try {
                    const zones = await fetchChunkData(chunkBounds, this.year);
                    return new Chunk(zones, chunkBounds.leftTop, chunkBounds.rightBottom, chunkBounds.center);
                } catch (error) {
                    console.error('Failed to fetch chunk:', error);
                    return null;
                }
            })
        );
        
        this.#chunks = newChunks.filter(chunk => chunk !== null);
        this.draw();
    }
    
    clear() {
        const ctx = this.#canvas.getContext('2d');
        ctx.clearRect(0, 0, this.#canvas.width, this.#canvas.height);
    }
    
    draw() {
        this.clear();
        
        for (const chunk of this.#chunks) {
            for (const zone of chunk.zones) {
                const style = new Style(zone.color || [100, 149, 237], zone.intensity || 0.5);
                drawPolygonToCanvas(
                    this.#canvas,
                    zone.coords,
                    chunk.leftTop,
                    chunk.rightBottom,
                    chunk.center,
                    style
                );
            }
        }
    }
    

    wheel(e) {
        e.preventDefault();
        
        const zoomIn = e.deltaY < 0;
        
        const factor = zoomIn ? 0.9 : 1.1;
        
        const centerLat = (leftTop[0] + rightBottom[0]) / 2;
        const centerLon = (leftTop[1] + rightBottom[1]) / 2;
        
        const newLeftTop = [
            centerLat + (leftTop[0] - centerLat) * factor,
            centerLon + (leftTop[1] - centerLon) * factor
        ];
        
        const newRightBottom = [
            centerLat + (rightBottom[0] - centerLat) * factor,
            centerLon + (rightBottom[1] - centerLon) * factor
        ];

        leftTop = newLeftTop;
        rightBottom = newRightBottom;
        
        this.clear();
        this.draw();
    }

    mousedown(e) {
        const rect = this.#canvas.getBoundingClientRect();
        this.lastMouseX = e.clientX - rect.left;
        this.lastMouseY = e.clientY - rect.top;
        this.isDragging = true;
    }

    mousemove(e) {
        if (!this.isDragging) return;
        
        const rect = this.#canvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        
        const dx = -(currentX - this.lastMouseX);
        const dy = -(currentY - this.lastMouseY);
        
        const dlatlonDelta = pixelDeltaToLatLonDelta(dx, dy, canvas, leftTop, rightBottom);
        
        this.leftTop = addLatLonDelta(this.leftTop, dlatlonDelta);
        this.rightBottom = addLatLonDelta(this.rightBottom, dlatlonDelta);
        
        this.lastMouseX = currentX;
        this.lastMouseY = currentY;
        
        this.clear();
        this.draw ();
    }

    mouseup(e) {
        this.isDragging = false;
    }

    mouseleave(e) {
        this.isDragging = false;
    }
    
}








export default new Viewport();

