const input = { keys: {}, mouse: {} };

const CANVAS_SIZE = 28;

let network = null;
let dataset = null;
let summedWeights = null;
let drawing = new Array(CANVAS_SIZE * CANVAS_SIZE).fill(0);
let datasetIndex = -1;

function setup() {
    createCanvas(800, 600);

    document.getElementById('networkFile').onchange = e => {
        const csvFile = e.target.files[0];
        console.log('test!');
        Papa.parse(csvFile, {
            flexible: true,
            complete: (data) => {

                let layerSizes = data.data[0].map((val) => Number.parseFloat(val));
                data.data.shift();
                let weights = data.data.map((layer) => layer.map((weight) => Number.parseFloat(weight)));

                network = {
                    layerSizes,
                    weights,
                }

                summedWeights = [];
                let highestSum = 0;
                for (let i = 0; i < layerSizes[0]; i += 1) {
                    let sum = 0;
                    for (let j = 0; j < layerSizes[1]; j += 1) {
                        sum += abs(weights[0][(i + 1) * layerSizes[1] + j]);
                    }
                    
                    highestSum = max(sum, highestSum);
                    summedWeights.push(sum);
                }

                for (let i = 0; i < summedWeights.length; i += 1) {
                    summedWeights[i] = summedWeights[i] / highestSum;
                }
            }
        });
    }    

    document.getElementById('datasetFile').onchange = e => {
        const csvFile = e.target.files[0];
        console.log('test!');
        Papa.parse(csvFile, {
            flexible: true,
            complete: (data) => {

                data.data.shift();
                dataset = data.data.map(sample => {
                    let correct = Number.parseFloat(sample.shift());
                    
                    return {
                        correct,
                        image: sample.map((num) => Number.parseFloat(num) / 256.0)
                    }

                });
            }
        });
    }

    resetCanvas();
}

function resetCanvas() {
    drawing = new Array(CANVAS_SIZE * CANVAS_SIZE).fill(0);
}

function keyPressed() {
    input.keys[keyCode] = 0;
}

function keyReleased() {
    input.keys[keyCode] = -1;
}

function mousePressed() {
    input.mouse[mouseButton] = 0;
}

function mouseReleased() {
    input.mouse[mouseButton] = -1;
}

function updateInput() {
    for (let key in input.keys) {
        if (input.keys[key] >= 0) {
            input.keys[key] += 1;
        }
    }
    for (let button in input.mouse) {
        if (input.mouse[button] >= 0) {
            input.mouse[button] += 1;
        }
    }
}

function update() {
    if (!network) {
        return;
    }
    
    if (input.keys['R'.charCodeAt(0)] >= 0) {
        resetCanvas();

        if (datasetIndex >= 0) {
            datasetIndex = -1;
        }
    }

    if (input.keys['C'.charCodeAt(0)] === 0 && dataset) {
        datasetIndex = floor(random(0, dataset.length));
        drawing = [...dataset[datasetIndex].image];
    }

    for (let n = 0; n < 10; n += 1) {
        const sortedIndices = [...Array(summedWeights.length).keys()].sort((a, b) => summedWeights[b] - summedWeights[a]);
        
        if (input.keys[n.toString().charCodeAt(0)] === 0) {
            resetCanvas();
            const offset = floor(random(0, CANVAS_SIZE * CANVAS_SIZE));
            for (let p = 0; p < CANVAS_SIZE * CANVAS_SIZE; p += 1) {

                const i = (p + offset) % (CANVAS_SIZE * CANVAS_SIZE);

                console.log(p + ' / ' + (CANVAS_SIZE * CANVAS_SIZE));
                
                let lowestRmse = 999999;
                let lowestVal = 0;

                for (let j = 0; j < 20; j += 1) {
                    const val = random(0, 1);
                    drawing[sortedIndices[i]] = val;
                    const output = process(network, drawing);
                    const full = output[output.length - 1];

                    let rmse = 0;
                    for (let k = 0; k < 10; k += 1) {
                        rmse += (full[k] - (input.keys[SHIFT] >= 0 ? (k == n ? 0 : 1) : (k == n ? 1 : 0))) ** 2;
                    }

                    if (rmse < lowestRmse) {
                        lowestRmse = rmse;
                        lowestVal = val;
                    }
                }

                drawing[sortedIndices[i]] = lowestVal;
            }
        }
    }

    if (input.mouse[LEFT] >= 0 && mouseX < CANVAS_SIZE * 10 && mouseY < CANVAS_SIZE * 10) {
        let paintX = mouseX / 10;
        let paintY = mouseY / 10;

        if (datasetIndex >= 0) {
            datasetIndex = -1;
        }

        for (let y = 0; y < CANVAS_SIZE; y += 1) {
            for (let x = 0; x < CANVAS_SIZE; x += 1) {
                let dist = (paintX - x) ** 2 + (paintY - y) ** 2;
                let val = max(min(map(dist, 0, 2, 1, 0), 1), 0);
                drawing[y * CANVAS_SIZE + x] = max(min(drawing[y * CANVAS_SIZE + x] + (input.keys[SHIFT] >= 0 ? (val * -0.8) : (val * 0.8)), 1), 0);
            }
        }
    }
}

function forward(net, input) {

    if (input.size == net.layerSizes[0]) {
        console.error('invalid input!');
        return null;
    }

    let outputs = [[...input]];

    for (let l = 0; l < net.layerSizes.length - 1; l += 1) {
        let weights = net.weights[l];
        let output = [];

        let size = net.layerSizes[l];
        let outSize = net.layerSizes[l + 1];

        let inputs = outputs[l];

        for (let o = 0; o < outSize; o += 1) {
            let sum = 0;
            for (let i = 0; i < size + 1; i += 1) {
                let weightIndex = i * outSize + o;
                let weight = weights[weightIndex];
                let inputValue = (i === 0 ? 1 : inputs[i - 1]);
                sum += inputValue * weight;
            }
            let out = 1.0 / (1.0 + Math.exp(-sum));
            output.push(out);
        }

        outputs.push(output);
    }

    return outputs;
}

function process(net, image) {
    return forward(net, image);
}

function draw() {
    update();
    updateInput();
    background(250);

    if (!network) {
        fill(0);
        noStroke();
        textSize(32);
        textAlign(LEFT, TOP);
        textStyle(BOLD);
        text("upload network csv file to begin", 100, 100);
        text("(use numbers_200.csv)     ", 100, 200);
        return;
    }

    let data = process(network, drawing);
    
    noStroke();
    for (let y = 0; y < CANVAS_SIZE; y += 1) {
        for (let x = 0; x < CANVAS_SIZE; x += 1) {
            let val = drawing[y * CANVAS_SIZE + x];
            fill(val * 256);
            rect(x * 10, y * 10, 10, 10);
            if (mouseX < 100 && mouseY > 300 && mouseY < 500) {
                let weightIndex = y * CANVAS_SIZE + x;
                let nodeIndex = floor((mouseY - 300) / 10) * 10 + floor(mouseX / 10);
                let value = network.weights[0][(weightIndex + 1) * network.layerSizes[1] + nodeIndex];
                fill(sqrt(max(value, 0)) * 256, 0, sqrt(-min(value, 0)) * 256);
            } else {
                fill(summedWeights[y * CANVAS_SIZE + x] * 256);
            }
            rect(120 + x * 5, 300 + y * 5, 5, 5);
        }
    }

    for (let y = 0; y < 20; y += 1) {
        for (let x = 0; x < 10; x += 1) {
            let val = data[1][y * 10 + x];
            fill(val * 256);
            rect(x * 10, y * 10 + 300, 10, 10);
        }
    }
    

    let outputs = data[data.length - 1];
    let chosenOutput = 0;
    let highest = 0;
    for (let i = 0; i < 10; i += 1) {
        if (outputs[i] > highest) {
            chosenOutput = i;
            highest = outputs[i];
        }
    }
    
    textAlign(LEFT, TOP);
    fill(0);

    textSize(24);
    if (!dataset) {
        text('upload a dataset for testing!', 400, 400);
    } else {
        text('dataset loaded: ' + dataset.length + ' images', 400, 400);
        if (datasetIndex >= 0) {
            text('correct answer: ' + dataset[datasetIndex].correct, 400, 430)
        }
    }
    
    
    for (let i = 0; i < 10; i += 1) {
        let point = outputs[i];
        if (datasetIndex >= 0) {
            if (i === chosenOutput) {
                if (dataset[datasetIndex].correct === chosenOutput) {
                    fill(50, 200, 50);
                } else {
                    fill(200, 50, 50)
                }
            } else {
                if (dataset[datasetIndex].correct === i) {
                    fill(200, 50, 200);
                } else {
                    fill(0);
                }
            }
        } else {
            i === chosenOutput ? fill(50, 50, 200) : fill(0);
        }
        text(i, 350, 10 + i * 30);
        rect(370, 10 + i * 30, 200 * point, 20);
        text(point < 0.001 ? '0.000' : nf(point, 0, 3), 375 + 200 * point, 10 + i * 30);
    }

    fill(0);
    textSize(16);
    textAlign(RIGHT, TOP);
    text('output: ' + chosenOutput, 800, 20);
    fill(50, 50, 200);
    text('chosen output', 800, 40);
    fill(50, 200, 50);
    text('correct output', 800, 60);
    fill(200, 50, 50);
    text('incorrect output', 800, 80);
    fill(200, 50, 200);
    text('expected output', 800, 100);

    textAlign(LEFT, TOP);
    textSize(10);
    fill(0);
    text('hidden layer outputs', 1, 290);
    text('mouse over to view influence', 1, 500);

    fill(0);
    textSize(16);
    text('left click and drag to paint', 200, 500);
    text('hold shift to erase', 200, 520);
    text('[r] to reset canvas', 200, 540);
    text('[c] to load random dataset image', 200, 560);
    text('[0-9] EXPERIMENTAL AND LAGGY generate ideal image of that number', 200, 580);

    textSize(10);
    if (mouseX < 100 && mouseY > 300 && mouseY < 500) {
        text('influence on hidden output', 120, 290);
    } else {
        text('total influence on output by pixel', 120, 290);
    }
}
