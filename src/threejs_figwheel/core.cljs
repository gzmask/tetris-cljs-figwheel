(ns threejs-figwheel.core
    (:require [threejs-figwheel.tetris :as tetris]
              clojure.set
              three
              stats))

(enable-console-print!)

;; There's a little debugging counter in here. This state variable
;; tracks a couple of components: the three.js renderer and the stats object.
(def GAME-STATE (atom {:c 0
                       :last-dropped-time 0
                       :falling-block {:type (rand-nth (keys tetris/blocks))
                                       :rotation (rand-int 4)
                                       :position [4 17]}
                       :fallen-blocks #{[0 0] [1 0]}}))

;; The startup and teardown routines need to basically be idempotent; in
;; particular, when a page loads we might see two startup calls.

(defn startup-stats
  "Install the stats into the page, and into the app state."
  []
  (when-not (:stats @GAME-STATE)
    (let [stats (js/Stats.)]
      (set! (.. stats -domElement -style -position) "absolute")
      (set! (.. stats -domElement -style -left) "0px")
      (set! (.. stats -domElement -style -top) "0px")
      (.appendChild (.-body js/document) (.-domElement stats))
      (swap! GAME-STATE assoc :stats stats))))

(defn teardown-stats
  "Remove the stats from the page and the app state."
  []
  (when-let [stats (:stats @GAME-STATE)]
    (.removeChild (.-body js/document) (.-domElement stats))
    (swap! GAME-STATE dissoc :stats)))

(defn startup-app
  "Swap into the app state the renderer, and a function to stop the current animation loop."
  []
  (when-not (:renderer @GAME-STATE)
    (let [scene (js/THREE.Scene.)
          camera (js/THREE.PerspectiveCamera. 75
                                              (/ (.-innerWidth js/window) (.-innerHeight js/window))
                                              0.1
                                              1000)
          renderer (js/THREE.WebGLRenderer.)
          gameboard (tetris/game-board (:x tetris/grid-size) (:y tetris/grid-size) 2 4 0.05 -0.9 -2.5 0.4)
          key-control #(tetris/key-control GAME-STATE %1)
          RUNNING (atom true)]
      (set! (.-onkeydown js/window) key-control)
      (set! (.-xxid renderer)
            (:c (swap! GAME-STATE update :c inc)))
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.log js/console "Adding: " (.-xxid renderer))
      (.appendChild (.-body js/document) (.-domElement renderer))
      (doseq [cube gameboard]
        (.add scene cube))
      (set! (.. camera -position -z) 3)

      (letfn [(animate []
                (when @RUNNING (js/requestAnimationFrame animate)) 
                (tetris/color-gameboard! gameboard)
                (tetris/color-gameboard-with-block! 
                  gameboard 
                  (:type (:falling-block @GAME-STATE)) 
                  (:position (:falling-block @GAME-STATE)) 
                  (:rotation (:falling-block @GAME-STATE)))
                (doseq [cell (:fallen-blocks @GAME-STATE)] (tetris/color-gameboard-with-cell! gameboard cell))
                (when (> (- (.now js/Date) 
                            (:last-dropped-time @GAME-STATE)) 
                       tetris/drop-time-step) 
                  (reset! GAME-STATE (assoc @GAME-STATE :last-dropped-time (.now js/Date)))
                  (tetris/free-fall! GAME-STATE))
                (.render renderer scene camera)
                (when-let [stats (:stats @GAME-STATE)] (.update stats)))]
        (animate) 
        (swap! GAME-STATE assoc :renderer renderer)
        (swap! GAME-STATE assoc :stopper (fn [] (reset! RUNNING false)))))))

(defn teardown-app
  "Stop animation cycle, tear out renderer."
  []
  (when-let [stopper (:stopper @GAME-STATE)] (stopper))
  (when-let [renderer (:renderer @GAME-STATE)]
    (.log js/console "Removing: " (.-xxid renderer))
    (.removeChild (.-body js/document) (.-domElement renderer)))
  (swap! GAME-STATE dissoc :stopper)
  (swap! GAME-STATE dissoc :renderer))

(defn render! [] 
  (teardown-app)
  (teardown-stats))

(startup-app)
(startup-stats)
