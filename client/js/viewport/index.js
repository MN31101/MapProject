import MapAPI from '../MapApi/index.js';


/*

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


export class Chunk {
    constructor(zones, leftTop, rightBottom, center, zoomLevel) {
        this.zones = zones;
        this.leftTop = leftTop;
        this.rightBottom = rightBottom;
        this.center = center;
        this.zoomLevel = zoomLevel;
    }
}

export function isPointInBounds(point, leftTop, rightBottom) {
    return point[0] >= leftTop[0] && 
           point[0] <= rightBottom[0] && 
           point[1] >= leftTop[1] && 
           point[1] <= rightBottom[1];
}

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

export class Style {
    constructor(rgb, a = 0.5) { 
        this.rgb = rgb;
        this.a = a;
    }

    get r() { return this.rgb[0] }
    get g() { return this.rgb[1] }
    get b() { return this.rgb[2] }
}



export class Viewport {
    #canvas;
    #chunks = [];
    #api;
    
    constructor(width, height, id = 'map-canvas') {
        this.#api = new MapAPI('http://127.0.0.1:8081/api');
        
        // Initial viewport state
        this.leftTop = [51, 14];          // Starting position
        this.rightBottom = [50, 15];  
        this.center = [
            (this.leftTop[0] + this.rightBottom[0]) / 2,
            (this.leftTop[1] + this.rightBottom[1]) / 2
        ];
        
        // Zoom state
        this.metersPerPixel = 50;  // Initial zoom level
        
        // Canvas setup
        this.#canvas = document.createElement('canvas');
        this.#canvas.width = width;
        this.#canvas.height = height;
        this.#canvas.id = id;
        
        // Mouse interaction state
        this.isDragging = false;
        this.lastMouseX = 0;
        this.lastMouseY = 0;
        
        // Current year for data
        this.year = 2024;
        
        // Add canvas to DOM and set up events
        document.querySelector("#app").appendChild(this.#canvas);
        this.#setupEventListeners();
        
        // Initial render
        this.updateChunks();
    }

    getCanvas() {
        return this.#canvas
    }
    
    #calculateChunks() {
        const viewportWidth = this.#canvas.width * this.metersPerPixel;
        const viewportHeight = this.#canvas.height * this.metersPerPixel;
        
        // Calculate grid size based on viewport dimensions
        const chunksPerRow = Math.ceil(viewportWidth / (viewportWidth / 3));
        const chunksPerCol = Math.ceil(viewportHeight / (viewportHeight / 3));
        
        const latDelta = (this.rightBottom[0] - this.leftTop[0]) / chunksPerCol;
        const lonDelta = (this.rightBottom[1] - this.leftTop[1]) / chunksPerRow;
        
        const chunks = [];
        
        for (let i = 0; i < chunksPerCol; i++) {
            for (let j = 0; j < chunksPerRow; j++) {
                const chunkLeftTop = [
                    this.leftTop[0] + i * latDelta,
                    this.leftTop[1] + j * lonDelta
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
    
    async updateChunks() {
        try {
            // Calculate chunks based on current viewport
            const chunkGrid = this.#calculateChunks();
            
            // Fetch data for each chunk
            const fetchPromises = chunkGrid.map(chunk => 
                this.#api.getLanguageZones({
                    year: this.year,
                    leftTop: chunk.leftTop,
                    rightBottom: chunk.rightBottom
                })
            );
            
            const chunksData = await Promise.all(fetchPromises);
            
            // Create chunk objects with fetched data
            this.#chunks = chunkGrid.map((chunk, index) => 
                new Chunk(
                    chunksData[index],
                    chunk.leftTop,
                    chunk.rightBottom,
                    chunk.center,
                    this.calculateZoomLevel()
                )
            );
            
            this.draw();
        } catch (error) {
            console.error('Failed to update chunks:', error);
        }
    }
    
    calculateZoomLevel() {
        const latSpan = Math.abs(this.rightBottom[0] - this.leftTop[0]);
        return Math.floor(Math.log2(360 / latSpan));
    }
    
    #setupEventListeners() {
        this.#canvas.addEventListener('wheel', this.#handleWheel.bind(this));
        this.#canvas.addEventListener('mousedown', this.#handleMouseDown.bind(this));
        this.#canvas.addEventListener('mousemove', this.#handleMouseMove.bind(this));
        this.#canvas.addEventListener('mouseup', this.#handleMouseUp.bind(this));
        this.#canvas.addEventListener('mouseleave', this.#handleMouseLeave.bind(this));
    }
    
    #handleWheel(e) {
        e.preventDefault();
        
        const rect = this.#canvas.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;
        
        // Convert mouse position to latlon before zoom
        const mouseLatLon = pixelToLatLon(
            [mouseX, mouseY],
            [this.#canvas.width, this.#canvas.height],
            this.leftTop,
            this.rightBottom
        );
        
        // Update zoom level
        const zoomFactor = e.deltaY > 0 ? 1.1 : 0.9;
        this.metersPerPixel *= zoomFactor;
        
        // Calculate new boundaries keeping mouse position fixed
        const latDelta = this.rightBottom[0] - this.leftTop[0];
        const lonDelta = this.rightBottom[1] - this.leftTop[1];
        
        this.leftTop = [
            mouseLatLon[0] - (mouseX / this.#canvas.width) * latDelta * zoomFactor,
            mouseLatLon[1] - (mouseY / this.#canvas.height) * lonDelta * zoomFactor
        ];
        
        this.rightBottom = [
            this.leftTop[0] + latDelta * zoomFactor,
            this.leftTop[1] + lonDelta * zoomFactor
        ];
        
        this.updateChunks();
    }
    
    #handleMouseDown(e) {
        const rect = this.#canvas.getBoundingClientRect();
        this.lastMouseX = e.clientX - rect.left;
        this.lastMouseY = e.clientY - rect.top;
        this.isDragging = true;
        this.#canvas.style.cursor = 'grabbing';
    }
    
    #handleMouseMove(e) {
        if (!this.isDragging) return;
        
        const rect = this.#canvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        
        const dx = -(currentX - this.lastMouseX);
        const dy = -(currentY - this.lastMouseY);
        
        const deltaLatLon = pixelDeltaToLatLonDelta(
            dx, dy,
            this.#canvas,
            this.leftTop,
            this.rightBottom
        );
        
        this.leftTop = addLatLonDelta(this.leftTop, deltaLatLon);
        this.rightBottom = addLatLonDelta(this.rightBottom, deltaLatLon);
        
        this.lastMouseX = currentX;
        this.lastMouseY = currentY;
        
        this.updateChunks();
    }
    
    #handleMouseUp() {
        this.isDragging = false;
        this.#canvas.style.cursor = 'grab';
    }
    
    #handleMouseLeave() {
        if (this.isDragging) {
            this.isDragging = false;
            this.#canvas.style.cursor = 'grab';
        }
    }
    
    draw() {
        const ctx = this.#canvas.getContext('2d');
        ctx.clearRect(0, 0, this.#canvas.width, this.#canvas.height);
        
        // Draw each chunk's zones
        for (const chunk of this.#chunks) {
            for (const zone of chunk.zones) {
                if (!zone.coords || !zone.coords.length) continue;
                
                const style = new Style(zone.color || [100, 149, 237], zone.intensity);
                
                // Draw each polygon in the zone
                zone.coords.forEach(coord => {
                    if (coord.type === 'Polygon' && coord.coordinates && coord.coordinates[0]) {
                        drawPolygonToCanvas(
                            this.#canvas,
                            coord.coordinates[0],
                            chunk.leftTop,
                            chunk.rightBottom,
                            chunk.center,
                            style
                        );
                    }
                });
            }
        }
    }
    
    setYear(year) {
        this.year = year;
        this.updateChunks();
    }
}

const viewport = new Viewport(800, 600);
export default viewport;
