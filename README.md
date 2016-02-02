# A tetris of ThreeJS, CLJS and Figwheel trio.

[![screenshot](https://github.com/gzmask/tetris-cljs-figwheel/raw/master/tetris.jpg "Play Tetris - WebGL required")](http://gzmask.github.io/tetris-cljs-figwheel)

# `threejs-figwheel`

An (yet another) Tetris for ThreeJS and Clojure in the browser [ClojureScript](https://github.com/clojure/clojurescript), [three.js](http://threejs.org/) and [Figwheel](https://github.com/bhauman/lein-figwheel).

## Setup (from Figwheel docs)

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).


and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

Then open your browser in `resources/public/index.html`.

## License

Copyright © 2015 Gzmask@gmail.com

Distributed under the WTFPL – Do What the Fuck You Want to Public License.
