# Lufar check
![](https://img.shields.io/badge/using-org.json-blue?logo=json)
## What is it?
Well it's a game where you play 3 in a row but its 5 in a row and on a non-standard size map, so it's playable on a 5x5 to inf x inf grid
## How do I play it?
So the game rules is that you place your marker (a blue square or a red circle) and try to get 5 in a row while blocking the other player
from getting 5 in a row
## How do I play this with my friend?
So this game requires command line to run
- Star the server
  1. Open a command line
     - Windows: press win key(or cmd) then type `cmd` and press enter
     - macOS: press cmd(win key)+space and type `termianl`
     - Ubuntu(might work on other Linux distros too) press ctrl+alt+t
  2. Now go to the directory where you downloaded the latest version
  3. run `$ java -jar './lufar chack<VERSION>.jar'- server` NOTE: `$` is to indicate that it's a command, `<VERSION>` is the latest version
  4. now you need your ip for the computer that host the server(if you and your friend is not on the same network as the server then you need the public ip by visiting this [site](https://www.whatismyip.com/))
- Join the server
  1. follow step 1 and 2 from `Start the server`
  2. run `$ java -jar './lufar chack<VERSION>.jar' -client -ip <server ip>` NOTE: `$` is to indicate that it's a command, `<VERSION>` is the latest version, the `-ip <server ip>` is not needed if you ar on the same computer as the server,
  `<server ip>` is the local(if you ar on the same network as the server) or public(if you ar not on the same network as the server) ip of the server
  3. ENJOY!