/**
 * 建议直接在浏览器运行，vscode中容易假死
 * @param {echarts 地图json} json 
 * @returns 
 */
function encode(json) {
    if (json.UTF8Encoding) {
        return;
    }

    json.UTF8Encoding = true;

    var features = json.features;

    features.forEach(function (feature){
        var encodeOffsets = feature.geometry.encodeOffsets = [];
        var coordinates = feature.geometry.coordinates;
        if (feature.geometry.type === 'MultiPolygon') {
            coordinates.forEach(function (polygon, idx1){
                encodeOffsets[idx1] = [];
                polygon.forEach(function (coordinate, idx2) {
                    coordinates[idx1][idx2] = encodeRing(
                        coordinate, encodeOffsets[idx1][idx2] = []
                    );
                });
            });
        }
        else if (feature.geometry.type == 'Polygon'
            || feature.geometry.type == 'MultiLineString'
        ) {
            coordinates.forEach(function (coordinate, idx){
                coordinates[idx] = encodeRing(
                    coordinate, encodeOffsets[idx] = []
                );
            });
        }
        else if (feature.geometry.type === 'LineString') {
            feature.geometry.coordinates = encodeRing(coordinates, encodeOffsets)
        }
    });

    return json;
}

function encodeRing(coordinates, encodeOffsets) {

    var result = '';

    var prevX = quantize(coordinates[0][0]);
    var prevY = quantize(coordinates[0][1]);
    // Store the origin offset
    encodeOffsets[0] = prevX;
    encodeOffsets[1] = prevY;

    for (var i = 0; i < coordinates.length; i++) {
        var point = coordinates[i];
        result += encodePoint(point[0], prevX);
        result += encodePoint(point[1], prevY);

        prevX = quantize(point[0]);
        prevY = quantize(point[1]);
    }

    return result;
}

function quantize(val) {
    return Math.ceil(val * 1024);
}

function encodePoint(val, prev){
    // Quantization
    val = quantize(val);
    // var tmp = val;
    // Delta
    var delta = val - prev;

    if (((delta << 1) ^ (delta >> 15)) + 64 === 8232) {
        //WTF, 8232 will get syntax error in js code
        delta--;
    }
    // ZigZag
    delta = (delta << 1) ^ (delta >> 15);
    // add offset and get unicode
    return String.fromCharCode(delta + 64);
}

// Export for testing.
if (typeof module !== 'undefined') {
    module.exports = encode;
}
else {
    window.encodeGeoJSON = encode;
}