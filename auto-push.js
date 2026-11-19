const {exec } = require("child_process");
const chokidar = require("chokidar");

let timer;

console.log("Auto-push ON for All Files");
console.log("Press Ctrl + S to savve,it will push 3 seconds")