// import viewport from "./viewport/index.js";
import { latLonToMercator, addLatLonDelta, pixelToLatLon, normalizeMercator, resizeToShape, pixelDeltaToLatLonDelta, Style, drawPolygonToCanvas } from "./viewport/index.js";



/*

const viewport = new Viewport(800, 600, 'map-canvas');

// Change the year if needed
viewport.year = 2023;

// Initial update of chunks
viewport.updateChunks();
*/


const polygons = [
    [
        [46.5, 20.5],
        [46.8, 20.8],
        [46.7, 21.2],
        [46.4, 21.4],
        [46.1, 21.1],
        [46.2, 20.7],
        [46.5, 20.5]
    ],
    
    [
        [47.2, 20.2],
        [47.4, 20.4],
        [47.3, 20.7],
        [47.1, 20.6],
        [47.2, 20.2]
    ],
    
    [
        [45.8, 21.5],
        [46.1, 21.6],
        [46.0, 21.8],
        [45.7, 21.9],
        [45.5, 21.7],
        [45.6, 21.5],
        [45.8, 21.5]
    ],
    
    [
        [44.5, 20.8],
        [44.7, 20.9],
        [44.8, 21.2],
        [44.6, 21.3],
        [44.4, 21.1],
        [44.3, 20.9],
        [44.5, 20.8]
    ],
    
    [
        [45.2, 19.5],
        [45.4, 19.6],
        [45.3, 19.8],
        [45.1, 19.7],
        [45.2, 19.5]
    ]
];


window.addEventListener("load", () => {
    const syt = new Style([5, 10, 20], 0.2)

    
    let leftTop =      [50, 19];      
    let rightBottom = [43.8, 22];  
    let center = [57.5, 71.45];
    
    let canvas = document.createElement('canvas');
    canvas.id = "#supercanv"
    canvas.width = 600;
    canvas.height = 600;



    
    let lastX = 0;
    let lastY = 0;

    for (let polygon of polygons) { 
        canvas = drawPolygonToCanvas(canvas, polygon, leftTop, rightBottom, center, syt)
    }

    let isDragging = false;
    
    canvas.addEventListener("mousedown", (e) => {
        const rect = canvas.getBoundingClientRect();
        lastX = e.clientX - rect.left;
        lastY = e.clientY - rect.top;
        isDragging = true;
    });

    canvas.addEventListener("mousemove", (e) => {
        if (!isDragging) return;
        
        const rect = canvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        
        const dx = -(currentX - lastX);
        const dy = -(currentY - lastY);
        
        const dlatlonDelta = pixelDeltaToLatLonDelta(dx, dy, canvas, leftTop, rightBottom);
        
        leftTop = addLatLonDelta(leftTop, dlatlonDelta);
        rightBottom = addLatLonDelta(rightBottom, dlatlonDelta);
        
        lastX = currentX;
        lastY = currentY;
        
        console.log(leftTop, rightBottom)
        canvas.width = canvas.width;  // reset canvass
        for (let polygon of polygons) { 
            canvas = drawPolygonToCanvas(canvas, polygon, leftTop, rightBottom, center, syt)
        }
    });

    canvas.addEventListener("mouseup", () => {
        isDragging = false;
    });

    canvas.addEventListener("mouseleave", () => {
        isDragging = false;
    });


    canvas.addEventListener("wheel", (e) => {
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
        
        canvas.width = canvas.width;
        for (let polygon of polygons) { 
            canvas = drawPolygonToCanvas(canvas, polygon, leftTop, rightBottom, center, syt);
        }
    });

    document.querySelector("#app").appendChild(canvas)
});

document.querySelector("#zoomLevel").addEventListener("change", (e) => {
    currentZoomLevel = parseInt(e.target.value)
});