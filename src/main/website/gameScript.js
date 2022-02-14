const c = document.getElementById("game");
const ctx = c.getContext("2d");

const IP = prompt('IP to webserver', 'ws://localhost:8080');

webSocket = new WebSocket(IP);

var map = [];
for(var x = 0; x < 60; x++){
    map[x] = []
    for(var y = 0; y < 60; y++){
        map[x][y] = {placed:false, id:0, finished:false};
    }
}

var lines = [];

var offset = {x:map.length/2-30, y:map[0].length/2-30};

webSocket.onmessage = function (event){
    console.info(event.data);
    var data = JSON.parse(event.data);
    var type = data.type;

    if(type == "TILE"){
        var TileData = data.data;
        var x = TileData.x;
        var y = TileData.y;

        map[x][y].placed = TileData.placed;
        map[x][y].id = TileData.id;
        map[x][y].finished = TileData.finished;
    }else if(type == "LINE"){
        lines[lines.length] = {start:{x:data.data.start.x, y:data.data.start.y}, end:{x:data.data.end.x, y:data.data.end.y}};
    }else if(type == "NAME"){
        document.title = data.data.name;
    }else if(type == "FAIL"){
        console.error(data.error);
        window.location.reload();
    }else if(type == "MAP"){
        var tmpMap = data.data;
        for(var tileData in tmpMap){
            var tile = tmpMap[tileData].value;
            var x = tile.x;
            var y = tile.y;

            map[x][y].placed = tile.placed;
            map[x][y].finished = tile.finished;
            map[x][y].id = tile.id;
        }
    }
}

setInterval(function(){

    //ctx.clearRect(0, 0, c.width, c.height);

    ctx.lineWidth = 1;

    for(var x = 0; x < 60; x++){
        for(var y = 0; y < 60; y++){
            var xPos = x*10;
            var yPos = y*10;
            var tile = map[x+offset.x][y+offset.y];

            var id = tile.id;
            if(tile.finished){
                ctx.fillStyle = '#a4a4a4'
                ctx.fillRect(xPos, yPos, 10, 10);
            }
            if(id == 1){
                ctx.strokeStyle = '#00F'
                if(tile.finished){
                    ctx.strokeStyle = '#00A'
                }
                ctx.beginPath();
                ctx.moveTo(xPos+1, yPos+1);
                ctx.lineTo(xPos+9, yPos+9);
                ctx.moveTo(xPos+9, yPos+1);
                ctx.lineTo(xPos+1, yPos+9);
                ctx.stroke();
                ctx.closePath();
            }else if(id == 2){
                ctx.strokeStyle = '#F00'
                if(tile.finished){
                    ctx.strokeStyle = '#A00'
                }
                ctx.beginPath();
                ctx.arc(xPos+5, yPos+5, 4, 0, 2 * Math.PI);
                ctx.stroke();
                ctx.closePath()
            }
        }
    }

    

    ctx.strokeStyle = '#000'

    ctx.beginPath();

    for(var x = 0; x < c.width; x+=10){
        ctx.moveTo(x, 0)
        ctx.lineTo(x, c.height);
        ctx.stroke();
    }

    for(var y = 0; y < c.height; y+=10){
        ctx.moveTo(0, y)
        ctx.lineTo(c.width, y);
        ctx.stroke();
    }

    ctx.closePath()

    ctx.strokeStyle = '#0F0'
    ctx.lineWidth = 2;

    ctx.beginPath()

    for(var i = 0; i < lines.length; i++){
        var line = lines[i];
        ctx.moveTo((line.start.x-offset.x)*10+5, (line.start.y-offset.y)*10+5);
        ctx.lineTo((line.end.x-offset.x)*10+5, (line.end.y-offset.x)*10+5);
    }
    
    ctx.stroke()
    ctx.closePath()
}, 100);

c.addEventListener('mouseup', e => {
    webSocket.send(JSON.stringify({type:'CLICK', data:{x:e.offsetX/10+offset.x, y:e.offsetY/10+offset.y}, error:''}));
});