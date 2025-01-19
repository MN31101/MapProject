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

function calculatePolygonCenter(coordinates) {
    let sumLat = 0;
    let sumLon = 0;
    const numPoints = coordinates.length;
    
    coordinates.forEach(coord => {
        sumLat += coord[0];
        sumLon += coord[1];
    });
    
    return [
        sumLat / numPoints,
        sumLon / numPoints
    ];
}

// Helper function to convert lat/lon to pixel coordinates
function latLonToPixel(latLon, canvasSize, leftTop, rightBottom) {
    const [width, height] = canvasSize;
    const [lat, lon] = latLon;
    
    const x = (lon - leftTop[1]) / (rightBottom[1] - leftTop[1]) * width;
    const y = (lat - leftTop[0]) / (rightBottom[0] - leftTop[0]) * height;
    
    return [x, y];
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
    #chunk = null;  
    #api;
    
    constructor(width, height, id = 'map-canvas') {
        this.#api = new MapAPI('http://localhost:8080/api');
        
        // Initial viewport state
        this.leftTop = [51, 14];          
        this.rightBottom = [50, 15];  
        this.center = [
            (this.leftTop[0] + this.rightBottom[0]) / 2,
            (this.leftTop[1] + this.rightBottom[1]) / 2
        ];
        
        // Zoom state
        this.metersPerPixel = 50;
        
        setInterval(() => {
            this.updateChunks();
        }, 25);
        setInterval(() => {
            this.draw();
        }, 25);

        // Canvas setup
        this.#canvas = document.createElement('canvas');
        this.#canvas.width = width;
        this.#canvas.height = height;
        this.#canvas.id = id;
        
        // Mouse interaction state
        this.isDragging = false;
        this.lastMouseX = 0;
        this.lastMouseY = 0;
        
        this.year = 2024;
        
        document.querySelector("#app").appendChild(this.#canvas);
        this.#setupEventListeners();
        
        this.updateChunks();
    }

    getCanvas() {
        return this.#canvas;
    }
    
    async updateChunks() {
        try {
            const chunksData = await this.#api.getLanguageZones({
                year: this.year,
                leftTop: this.leftTop,
                rightBottom: this.rightBottom
            });
            
            this.#chunk = new Chunk(
                chunksData,
                this.leftTop,
                this.rightBottom,
                this.center,
                this.calculateZoomLevel()
            );
            
            this.draw();
        } catch (error) {
            console.error('Failed to update chunk:', error);
        }
    }
    
    calculateZoomLevel() {
        const latSpan = Math.abs(this.rightBottom[0] - this.leftTop[0]);
        document.querySelector("#debug").textContent = `
        lattitude span: ${latSpan}
        right bottom: ${this.rightBottom}
        left top: ${this.leftTop}
        `;
        return Math.floor(Math.log2(360 / latSpan));
    }
    
    #setupEventListeners() {
        this.#canvas.addEventListener('wheel', this.#handleWheel.bind(this));
        this.#canvas.addEventListener('mousedown', this.#handleMouseDown.bind(this));
        this.#canvas.addEventListener('mousemove', this.#handleMouseMove.bind(this));
        this.#canvas.addEventListener('mouseup', this.#handleMouseUp.bind(this));
        this.#canvas.addEventListener('mouseleave', this.#handleMouseLeave.bind(this));
    }

    #cropCoordinates(coords) {
        const MIN_LAT = -90;
        const MAX_LAT = 90;
        const MIN_LON = -180;
        const MAX_LON = 180;
        
        return [
            Math.max(MIN_LAT, Math.min(MAX_LAT, coords[0])),
            Math.max(MIN_LON, Math.min(MAX_LON, coords[1]))
        ];
    }

    
    #handleWheel(e) {
        e.preventDefault();
        
        const MIN_METERS_PER_PIXEL = 0.1;
        const MAX_METERS_PER_PIXEL = 6000;
        
        const zoomFactor = e.deltaY > 0 ? 1.05 : 0.95;
        const newMetersPerPixel = this.metersPerPixel * zoomFactor;
        
        if (newMetersPerPixel < MIN_METERS_PER_PIXEL || 
            newMetersPerPixel > MAX_METERS_PER_PIXEL ||
            isNaN(newMetersPerPixel)) {
            return;
        }
        
        this.metersPerPixel = newMetersPerPixel;
        
        // Calculate current center with NaN checks
        const centerLat = (this.leftTop[0] + this.rightBottom[0]) / 2 || 0;
        const centerLon = (this.leftTop[1] + this.rightBottom[1]) / 2 || 0;
        
        if (isNaN(centerLat) || isNaN(centerLon)) {
            console.error('Invalid center coordinates');
            return;
        }
        
        // Calculate current spans with NaN checks
        const latSpan = this.rightBottom[0] - this.leftTop[0];
        const lonSpan = this.rightBottom[1] - this.leftTop[1];
        
        if (isNaN(latSpan) || isNaN(lonSpan)) {
            console.error('Invalid span calculation');
            return;
        }
        
        // Calculate new spans
        const newLatSpan = latSpan * zoomFactor;
        const newLonSpan = lonSpan * zoomFactor;
        
        if (isNaN(newLatSpan) || isNaN(newLonSpan)) {
            console.error('Invalid new span calculation');
            return;
        }
        
        const newLeftTop = [
            centerLat - (newLatSpan / 2),
            centerLon - (newLonSpan / 2)
        ];
        
        const newRightBottom = [
            centerLat + (newLatSpan / 2),
            centerLon + (newLonSpan / 2)
        ];
        
        // Final validation before assignment
        if (newLeftTop.some(isNaN) || newRightBottom.some(isNaN)) {
            console.error('Invalid coordinate calculation');
            return;
        }
        
        this.leftTop = newLeftTop;
        this.rightBottom = newRightBottom;
    }


    #handleMouseMove(e) {
        if (!this.isDragging) return;
        
        const rect = this.#canvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        
        const dx = -(currentX - this.lastMouseX) || 0;
        const dy = -(currentY - this.lastMouseY) || 0;
    
        // Limit latitude range to prevent extreme distortion
        const MAX_LATITUDE = 75;  // Prevent scrolling too far north/south
        const MIN_LATITUDE = -75;
        
        const deltaLatLon = pixelDeltaToLatLonDelta(
            dx, dy,
            this.#canvas,
            this.leftTop,
            this.rightBottom
        );
        
        let newLeftTop = addLatLonDelta(this.leftTop, deltaLatLon);
        let newRightBottom = addLatLonDelta(this.rightBottom, deltaLatLon);
        
        if (newLeftTop[0] > MAX_LATITUDE) {
            const diff = newLeftTop[0] - MAX_LATITUDE;
            newLeftTop[0] = MAX_LATITUDE;
            newRightBottom[0] = newRightBottom[0] - diff;
        }
        if (newRightBottom[0] < MIN_LATITUDE) {
            const diff = MIN_LATITUDE - newRightBottom[0];
            newRightBottom[0] = MIN_LATITUDE;
            newLeftTop[0] = newLeftTop[0] + diff;
        }
        
        this.leftTop = newLeftTop;
        this.rightBottom = newRightBottom;
        
        this.lastMouseX = currentX;
        this.lastMouseY = currentY;
    }

    #handleMouseDown(e) {
        const rect = this.#canvas.getBoundingClientRect();
        this.lastMouseX = e.clientX - rect.left;
        this.lastMouseY = e.clientY - rect.top;
        this.isDragging = true;
        this.#canvas.style.cursor = 'grabbing';
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
        ctx.fillStyle = 'rgb(70, 70, 70)';
        ctx.fillRect(0, 0, this.#canvas.width, this.#canvas.height);
        if (!this.#chunk) return;
    
        // Store all polygon pixel coordinates for overlap detection
        const polygons = [];
        
        // First pass: Draw fills and collect polygon data
        for (const zone of this.#chunk.zones) {
            if (!zone.coords || !zone.coords.length) continue;
            
            zone.coords.forEach(coord => {
                if (coord.type === 'Polygon' && coord.coordinates && coord.coordinates[0]) {
                    const mercatorPoints = latLonToMercator(coord.coordinates[0]);
                    const normalized = normalizeMercator(
                        this.#chunk.center,
                        this.#chunk.leftTop,
                        this.#chunk.rightBottom,
                        mercatorPoints
                    );
                    const pixels = resizeToShape(normalized, [this.#canvas.width, this.#canvas.height]);
                    
                    // Store polygon data
                    polygons.push({
                        pixels,
                        color: zone.color || [100, 149, 237],
                        intensity: zone.intensity
                    });
                    
                    // Draw fill
                    ctx.beginPath();
                    ctx.moveTo(pixels[0][0], pixels[0][1]);
                    for (let i = 1; i < pixels.length; i++) {
                        ctx.lineTo(pixels[i][0], pixels[i][1]);
                    }
                    ctx.closePath();
                    ctx.fillStyle = `rgba(${zone.color?.[0] || 100}, ${zone.color?.[1] || 149}, ${zone.color?.[2] || 237}, ${zone.intensity * 0.3})`;
                    ctx.fill();
                }
            });
        }
    
        // Second pass: Draw borders with glow and dotted overlaps
        ctx.lineWidth = 2;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';
        
        // Draw glow effect first
        polygons.forEach((polygon, i) => {
            ctx.beginPath();
            ctx.moveTo(polygon.pixels[0][0], polygon.pixels[0][1]);
            
            for (let j = 1; j < polygon.pixels.length; j++) {
                ctx.lineTo(polygon.pixels[j][0], polygon.pixels[j][1]);
            }
            
            ctx.closePath();
            
            // Create glow effect
            ctx.shadowColor = `rgba(${polygon.color[0]}, ${polygon.color[1]}, ${polygon.color[2]}, 0.5)`;
            ctx.shadowBlur = 10;
            ctx.strokeStyle = `rgba(${polygon.color[0]}, ${polygon.color[1]}, ${polygon.color[2]}, 0.8)`;
            ctx.stroke();
        });
    
        // Reset shadow for clean borders
        ctx.shadowBlur = 0;
        
        // Draw overlapping borders with dotted style
        for (let i = 0; i < polygons.length; i++) {
            for (let j = i + 1; j < polygons.length; j++) {
                const overlappingSegments = findOverlappingSegments(polygons[i].pixels, polygons[j].pixels);
                
                if (overlappingSegments.length > 0) {
                    ctx.setLineDash([4, 4]); // Create dotted line
                    ctx.lineWidth = 2;
                    ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
                    
                    overlappingSegments.forEach(segment => {
                        ctx.beginPath();
                        ctx.moveTo(segment.start[0], segment.start[1]);
                        ctx.lineTo(segment.end[0], segment.end[1]);
                        ctx.stroke();
                    });
                }
            }
        }
        
        // Reset line dash for normal borders
        ctx.setLineDash([]);
        
        // Draw regular borders
        polygons.forEach(polygon => {
            ctx.beginPath();
            ctx.moveTo(polygon.pixels[0][0], polygon.pixels[0][1]);
            
            for (let i = 1; i < polygon.pixels.length; i++) {
                ctx.lineTo(polygon.pixels[i][0], polygon.pixels[i][1]);
            }
            
            ctx.closePath();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
            ctx.stroke();
        });
    
        // Draw labels (keeping existing label logic)
        const labels = [];
        
        for (const zone of this.#chunk.zones) {
            if (!zone.coords || !zone.coords.length) continue;
            
            zone.coords.forEach(coord => {
                if (coord.type === 'Polygon' && coord.coordinates && coord.coordinates[0]) {
                    const centerPoint = calculatePolygonCenter(coord.coordinates[0]);
                    const pixelCoords = latLonToPixel(
                        centerPoint,
                        [this.#canvas.width, this.#canvas.height],
                        this.#chunk.leftTop,
                        this.#chunk.rightBottom
                    );
                    
                    const area = calculatePolygonArea(coord.coordinates[0]);
                    
                    labels.push({
                        x: pixelCoords[0],
                        y: pixelCoords[1],
                        text: zone.name,
                        area: area
                    });
                }
            });
        }
    
        labels.sort((a, b) => b.area - a.area);
        const visibleLabels = filterOverlappingLabels(labels);
    
        ctx.save();
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        visibleLabels.forEach(label => {
            ctx.strokeStyle = 'white';
            ctx.lineWidth = 3;
            ctx.lineJoin = 'round';
            ctx.miterLimit = 2;
            ctx.strokeText(label.text, label.x, label.y);
            
            ctx.fillStyle = 'black';
            ctx.fillText(label.text, label.x, label.y);
        });
        
        ctx.restore();
    }

    setYear(year) {
        this.year = year;
        this.updateChunks();
    }
}


function calculatePolygonArea(coordinates) {
    let area = 0;
    for (let i = 0; i < coordinates.length; i++) {
        const j = (i + 1) % coordinates.length;
        area += coordinates[i][0] * coordinates[j][1];
        area -= coordinates[j][0] * coordinates[i][1];
    }
    return Math.abs(area / 2);
}

function filterOverlappingLabels(labels) {
    const visibleLabels = [];
    const labelPadding = 30; // Minimum pixels between labels
    
    for (const label of labels) {
        let overlapping = false;
        
        for (const visibleLabel of visibleLabels) {
            const dx = label.x - visibleLabel.x;
            const dy = label.y - visibleLabel.y;
            const distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < labelPadding) {
                overlapping = true;
                break;
            }
        }
        
        if (!overlapping) {
            visibleLabels.push(label);
        }
    }

    return visibleLabels;
}




function createOverlapPattern(colors) {
    const patternCanvas = document.createElement('canvas');
    patternCanvas.width = 10;
    patternCanvas.height = 10;
    const patternCtx = patternCanvas.getContext('2d');

    colors.forEach((color, index) => {
        const stripeWidth = patternCanvas.width / colors.length;
        patternCtx.fillStyle = `rgba(${color[0]}, ${color[1]}, ${color[2]}, 0.5)`;
        patternCtx.fillRect(index * stripeWidth, 0, stripeWidth, patternCanvas.height);
    });

    return patternCtx.createPattern(patternCanvas, 'repeat');
}

function fillColorMap(colorMap, points, color) {
    points.forEach(point => {
        const key = `${point[0]},${point[1]}`;
        if (!colorMap.has(key)) {
            colorMap.set(key, []);
        }
        const colors = colorMap.get(key);
        if (!colors.some(c => c[0] === color[0] && c[1] === color[1] && c[2] === color[2])) {
            colors.push(color);
        }
    });
}

function isPointInPolygon(point, polygon) {
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
        const xi = polygon[i][0], yi = polygon[i][1];
        const xj = polygon[j][0], yj = polygon[j][1];
        
        const intersect = ((yi > point[1]) !== (yj > point[1]))
            && (point[0] < (xj - xi) * (point[1] - yi) / (yj - yi) + xi);
        if (intersect) inside = !inside;
    }
    return inside;
}

// Helper function to find border segments that overlap
function findOverlappingSegments(polygon1Pixels, polygon2Pixels) {
    const overlappingSegments = [];
    
    for (let i = 0; i < polygon1Pixels.length; i++) {
        const start1 = polygon1Pixels[i];
        const end1 = polygon1Pixels[(i + 1) % polygon1Pixels.length];
        
        for (let j = 0; j < polygon2Pixels.length; j++) {
            const start2 = polygon2Pixels[j];
            const end2 = polygon2Pixels[(j + 1) % polygon2Pixels.length];
            
            // Check if segments are close enough to be considered overlapping
            const dist = segmentDistance(start1, end1, start2, end2);
            if (dist < 2) { // 2 pixels threshold
                overlappingSegments.push({
                    start: start1,
                    end: end1
                });
            }
        }
    }
    return overlappingSegments;
}

function segmentDistance(start1, end1, start2, end2) {
    const x1 = start1[0], y1 = start1[1];
    const x2 = end1[0], y2 = end1[1];
    const x3 = start2[0], y3 = start2[1];
    const x4 = end2[0], y4 = end2[1];
    
    return Math.min(
        pointToSegmentDistance(x1, y1, x3, y3, x4, y4),
        pointToSegmentDistance(x2, y2, x3, y3, x4, y4),
        pointToSegmentDistance(x3, y3, x1, y1, x2, y2),
        pointToSegmentDistance(x4, y4, x1, y1, x2, y2)
    );
}

function pointToSegmentDistance(px, py, x1, y1, x2, y2) {
    const A = px - x1;
    const B = py - y1;
    const C = x2 - x1;
    const D = y2 - y1;
    
    const dot = A * C + B * D;
    const len_sq = C * C + D * D;
    let param = -1;
    
    if (len_sq !== 0)
        param = dot / len_sq;
    
    let xx, yy;
    
    if (param < 0) {
        xx = x1;
        yy = y1;
    } else if (param > 1) {
        xx = x2;
        yy = y2;
    } else {
        xx = x1 + param * C;
        yy = y1 + param * D;
    }
    
    const dx = px - xx;
    const dy = py - yy;
    return Math.sqrt(dx * dx + dy * dy);
}

const viewport = new Viewport(800, 600);
export default viewport;
