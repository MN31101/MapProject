import viewport, { Viewport } from "./viewport/index.js";


window.addEventListener("load", () => {
    let view = new Viewport(200, 200, "cnv");
    view.draw();

    let canvas = view.getCanvas();
    document.querySelector("#app").appendChild(canvas)
});

document.querySelector("#zoomLevel").addEventListener("change", (e) => {
    currentZoomLevel = parseInt(e.target.value)
});