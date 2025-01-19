import viewport from "./viewport/index.js";

const app = document.querySelector("#app");

if (!app) {
    console.error("Could not find #app element");
} else {
    const updateSizes = () => {
        const padding = 20;
        const width = window.innerWidth - padding * 2;
        const height = window.innerHeight - padding * 2;
        
        app.style.width = `${width}px`;
        app.style.height = `${height}px`;
        
        viewport.updateCanvasSize(width, height);
    };
    
    console.log("-")
    window.addEventListener("resize", updateSizes);
    updateSizes();
}