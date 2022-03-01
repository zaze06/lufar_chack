const game = document.getElementById("game");
const gameCtx = game.getContext("2d");
const chat = document.getElementById("chat");
const chatCtx = chat.getContext("2d");
const chatMessage = document.getElementById("chatMessage");
//const darkMode = document.getElementById("darkMode");

var lobbyId = 0;
var latestTile;
var chatData = [];

chatData[0] = "Welcome!";

webSocket = new WebSocket("ws://94.136.83.59:8080");

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
        latestTile = map[x][y];
    }else if(type == "LINE"){
        lines[lines.length] = {start:{x:data.data.start.x, y:data.data.start.y}, end:{x:data.data.end.x, y:data.data.end.y}};
    }else if(type == "NAME"){
        document.title = data.data.name;
    }else if(type == "FAIL"){
        console.error(data.error);
        window.location.reload();
    }else if(type == "MAP"){
        /*var tmpMap = data.data;
        for(var tileData in tmpMap){
            var tile = tmpMap[tileData].value;
            var x = tile.x;
            var y = tile.y;

            map[x][y].placed = tile.placed;
            map[x][y].finished = tile.finished;
            map[x][y].id = tile.id;
        }*/
    }else if(type == "CREATE_LOBBY"){
        var info = data.data.message;
        var create = confirm(info);
        if(create){
            var md = parseInt(prompt('what should the max amount of tiles that can be between a placed marker and a new one'));
            var mode = parseInt(prompt("select mode. 1: 2 players two clients(computers). 2: 2 players one client(one computer). 3: 1 player (player agenst computer) one client(one computer)"));
            webSocket.send(JSON.stringify({type:"CREATE_LOBBY", data:{md:md, mode:mode, lobby:lobbyId}, error:""}));
        }else{
            window.location.reload();
        }
    }else if(type == "JOIN"){
        lobbyId = prompt('lobby id. This is the id for the lobby if the lobby docent exist it will ask you if you wanna create it');

        webSocket.send(JSON.stringify({type:"DATA", data:{lobby:lobbyId}, error:""}));
    }else if(type == "MESSAGE"){
        chatData[chatData.length] = data.data.message;
    }
}

setInterval(function(){

    gameCtx.clearRect(0, 0, game.width, game.height);
    chatCtx.clearRect(0, 0, chat.width, chat.height);

    gameCtx.lineWidth = 1;

    for(var x = 0; x < 60; x++){
        for(var y = 0; y < 60; y++){
            var xPos = x*10;
            var yPos = y*10;
            var tile = map[x+offset.x][y+offset.y];

            var id = tile.id;
            if(tile.finished){
                gameCtx.fillStyle = '#a4a4a4';
                gameCtx.fillRect(xPos, yPos, 10, 10);
            }else if(tile == latestTile){
                gameCtx.fillStyle = '#47b548';
                gameCtx.fillRect(xPos, yPos, 10, 10);
            }
            if(id == 1){
                gameCtx.strokeStyle = '#00F';
                if(tile.finished){
                    gameCtx.strokeStyle = '#00A';
                }
                gameCtx.beginPath();
                gameCtx.moveTo(xPos+1, yPos+1);
                gameCtx.lineTo(xPos+9, yPos+9);
                gameCtx.moveTo(xPos+9, yPos+1);
                gameCtx.lineTo(xPos+1, yPos+9);
                gameCtx.stroke();
                gameCtx.closePath();
            }else if(id == 2){
                gameCtx.strokeStyle = '#F00';
                if(tile.finished){
                    gameCtx.strokeStyle = '#A00';
                }
                gameCtx.beginPath();
                gameCtx.arc(xPos+5, yPos+5, 4, 0, 2 * Math.PI);
                gameCtx.arc(xPos+5, yPos+5, 4, 0, 2 * Math.PI);
                gameCtx.stroke();
                gameCtx.closePath();
            }
        }
    }

    

    gameCtx.strokeStyle = '#000';

    gameCtx.beginPath();

    for(var x = 0; x < game.width; x+=10){
        gameCtx.moveTo(x, 0)
        gameCtx.lineTo(x, game.height);
        gameCtx.stroke();
    }

    for(var y = 0; y < game.height; y+=10){
        gameCtx.moveTo(0, y)
        gameCtx.lineTo(game.width, y);
        gameCtx.stroke();
    }

    gameCtx.closePath();

    gameCtx.strokeStyle = '#0F0';
    gameCtx.lineWidth = 2;

    gameCtx.beginPath();

    for(var i = 0; i < lines.length; i++){
        var line = lines[i];
        gameCtx.moveTo((line.start.x-offset.x)*10+5, (line.start.y-offset.y)*10+5);
        gameCtx.lineTo((line.end.x-offset.x)*10+5, (line.end.y-offset.x)*10+5);
    }
    
    gameCtx.stroke();
    gameCtx.closePath();
    
    var y = chat.height-2;

    chatCtx.fillStyle = '#044';
    chatCtx.font = '14px san-serif';

    for(var i = chatData.length-1; i >= 0; i--){
        var message = chatData[i];
        chatCtx.fillText(message, 0, y, 200);
        y -= 16;
    }

    /*if(darkMode.value){
        chat.style.backgroundColor = '#333';
        game.style.backgroundColor = '#333';
        document.style.backgroundColor = '#000';
    }else{
        chat.style.backgroundColor = '#FFF';
        game.style.backgroundColor = '#FFF';
        document.style.backgroundColor = '#FFF';
    }*/
}, 100);

game.addEventListener('mouseup', e => {
    webSocket.send(JSON.stringify({type:'CLICK', data:{x:e.offsetX/10+offset.x, y:e.offsetY/10+offset.y, lobby:lobbyId}, error:''}));
});

chatMessage.addEventListener('keyup', e => {
    if(e.code == 'Enter'){
        webSocket.send(JSON.stringify({type:'MESSAGE', data:{message:chatMessage.value, lobby:lobbyId}, error:""}))
        chatMessage.value = "";
    }
});