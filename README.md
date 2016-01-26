`-*- mode: markdown; mode: visual-line; mode: adaptive-wrap-prefix; -*-`

# A tetris of ThreeJS, CLJS and Figwheel trio.

[Tetris Game(WebGL)](http://gzmask.github.io/tetris-cljs-figwheel)

# `threejs-figwheel`

An (yet another) Tetris which brings up a minimal "live coding" environment for ThreeJS and Clojure in the browser, courtesy of [ClojureScript](https://github.com/clojure/clojurescript), [three.js](http://threejs.org/) and [Figwheel](https://github.com/bhauman/lein-figwheel).

This project is partially inspired by [Cassie](https://github.com/cassiel), [this by Chris McCormick](https://github.com/chr15m/clojurescript-threejs-playground), [Henry Garner's Multisnake](https://github.com/henrygarner/multisnake), and [Chestnut](https://github.com/plexus/chestnut).

## Setup (from Figwheel docs)

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2015 Gzmask@gmail.com

Distributed under the WTFPL – Do What the Fuck You Want to Public License.
