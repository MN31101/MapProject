import viewport from "./viewport/index.js";


window.addEventListener("load", () => {
    
    let canvas = view.getCanvas();
    document.querySelector("#app").appendChild(canvas)
});

document.querySelector("#zoomLevel").addEventListener("change", (e) => {
    currentZoomLevel = parseInt(e.target.value)
});