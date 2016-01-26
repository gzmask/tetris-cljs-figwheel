(ns threejs-figwheel.tetris
    (:require three
              clojure.set
              stats))

(def colors
  {:green 0x00FF00
   :purple 0xCC33FF
   :pink 0xCC0066
   :black 0x000000
   :ocean-blue 0x0066ff})

(def grid-size
  {:x 10
   :y 20})

(def drop-time-step 1000) ;in milliseconds

(defn cube [width height depth & [x y z color]]
  (let [geometry (js/THREE.BoxGeometry. width height depth)
        material (js/THREE.MeshBasicMaterial. 
                   (clj->js {:color color :wireframe false})) 
        cube (js/THREE.Mesh. geometry material)]
    (when-not (nil? x) 
      (set! (.. cube -position -x) x))
    (when-not (nil? y) 
      (set! (.. cube -position -y) y))
    (when-not (nil? z) 
      (set! (.. cube -position -z) z))
    cube))

(defn game-board [columns rows width height depth x y z]
  (let [cell-width (/ width columns) 
        cell-height (/ height rows)
        cells (for [
                    cell-y (range y (+ y height) cell-height)
                    cell-x (range x (+ x width) cell-width)
                    ]
                (cube (- cell-width (/ cell-width 5)) 
                      (- cell-height (/ cell-height 5)) 
                      depth 
                      cell-x 
                      cell-y 
                      (+ z (/ cell-y 2)) 
                      (:ocean-blue colors)))]
    cells))

(defn cell-index 
  "translate x y axis to array index for cells, x/y starts from 0"
  [x y]
  (+ (* y (:x grid-size)) x))

(def blocks
  {:L #{[0 0] [0 1] [0 -1] [1 -1]}
  :J #{[0 0] [0 1] [0 -1] [-1 -1]}
  :O #{[0 0] [1 0] [1 -1] [0 -1]}
  :S #{[0 0] [-1 0] [0 -1] [1 -1]}
  :Z #{[0 0] [1 0] [0 -1] [-1 -1]}
  :I #{[0 0] [0 1] [0 -1] [0 2]}
  :T #{[0 0] [1 0] [-1 0] [0 1]}})

(defn block-rotated [block-axes]
  (let [local-axes block-axes
        rotated-axes (vec (map (fn [[x y]] [(- 0 y) x] ) local-axes))]
    rotated-axes
    ))

(defn color-gameboard-with-block!
  "I.E put block :L to position [x,y]"
  [gameboard block [x y :as position] & [rotation]]
  (let [rotate-times (if (nil? rotation) 0 rotation)
        color-block {:J :pink :L :ocean-blue :Z :purple :S :pink :O :green :I :ocean-blue :T :purple}
        block-local-axes (nth (iterate block-rotated (block blocks)) rotate-times)
        block-axes (map (fn [[bx by :as loc]] [(+ x bx) (+ y by)]) 
                        block-local-axes)
        block-indexes (map #(cell-index (first %1) (second %1)) block-axes)]
    (doseq [i block-indexes] 
      (when (>= i (count gameboard))
        (prn "out of index:" i " x:" x " y:" y))
      (when (< i (count gameboard))
        (set! (.. (nth gameboard i) -rotation -x) (+ 0.08 (rand 0.01) (.. (nth gameboard i) -rotation -x))) 
        (set! (.. (nth gameboard i) -rotation -y) (+ 0.04 (.. (nth gameboard i) -rotation -y)))
        (set! (.. (nth gameboard i) -material -opacity) 0.8) 
        (.setHex (.. (nth gameboard i) -material -color) ((block color-block) colors)))
      )))

(defn color-gameboard-with-cell!
  "give x y axis of a cell"
  [gameboard [x y]]
  (set! (.. (nth gameboard (cell-index x y)) -material -opacity) 0.8) 
  (set! (.. (nth gameboard (cell-index x y)) -rotation -x) 0) 
  (set! (.. (nth gameboard (cell-index x y)) -rotation -y) 0)
  (.setHex (.. (nth gameboard (cell-index x y)) -material -color) (:green colors)))

(defn color-gameboard!
  [gameboard]
  (doseq [cell gameboard] 
    (set! (.. cell -material -opacity) 0.1) 
    (set! (.. cell -material -transparent) true) 
    (.setHex (.. cell -material -color) (:ocean-blue colors))))

(defn collided? [{fallen-cells :fallen-blocks 
                  {t :type p :position r :rotation} :falling-block 
                  :as GAME-STATE} & [new-position new-rotation]]
  (let [block-local-axes (nth (iterate block-rotated (t blocks)) (if (nil? new-rotation) r new-rotation))
        block-cells (set (map #(vec (map + %1 (if (nil? new-position) p new-position))) block-local-axes))
        max-x (apply max (map first block-cells))
        max-y (apply max (map second block-cells))
        min-x (apply min (map first block-cells))
        min-y (apply min (map second block-cells))]
  (cond (not (empty? (clojure.set/intersection fallen-cells block-cells))) true
        (> max-x (dec (:x grid-size))) true
        (> max-y (dec (:y grid-size))) true
        (neg? min-x) true
        (neg? min-y) true
        :else false)))

(defn axis [direction [x y]]
  (case direction
    :left [(dec x) y]
    :right [(inc x) y]
    :up [x (inc y)]
    :down [x (dec y)]))

(defn key-control [GAME-STATE e] 
  (cond (= (.-keyCode e) 37) (swap! GAME-STATE update-in 
                                    [:falling-block :position] 
                                    #(if (collided? @GAME-STATE (axis :left %1))
                                       %1 
                                       (axis :left %1)))
        (= (.-keyCode e) 38) (swap! GAME-STATE update-in 
                                    [:falling-block :rotation] 
                                    #(if (collided? @GAME-STATE nil (mod (dec %1) 4))
                                       %1 
                                       (mod (dec %1) 4)))
        (= (.-keyCode e) 39) (swap! GAME-STATE update-in 
                                    [:falling-block :position] 
                                    #(if (collided? @GAME-STATE (axis :right %1))
                                       %1 
                                       (axis :right %1)))
        (= (.-keyCode e) 40) (swap! GAME-STATE update-in 
                                    [:falling-block :position] 
                                    #(if (collided? @GAME-STATE (axis :down %1))
                                       %1 
                                       (axis :down %1)))
        (= (.-keyCode e) 32) (swap! GAME-STATE update-in 
                                    [:falling-block :rotation] 
                                    #(if (collided? @GAME-STATE nil (mod (inc %1) 4))
                                       %1 
                                       (mod (inc %1) 4)))) 
  (set! (.-returnValue e) false))

(defn downshift-rows!
  [GAME-STATE heights]
  (doseq [y (reverse heights)]
    (let [cells-above (filter #(> (second %1) y) (:fallen-blocks @GAME-STATE))
          shifted-down-cells (set (map (fn [[x y]] [x (dec y)]) cells-above))]
      (swap! GAME-STATE update-in [:fallen-blocks] #(clojure.set/difference %1 cells-above))
      (swap! GAME-STATE update-in [:fallen-blocks] #(clojure.set/union %1 shifted-down-cells)))))

(defn cancel-rows! 
  [GAME-STATE]
  (let [rows (for [h (range (:y grid-size))]
           (set (filter #(= h (second %1)) (:fallen-blocks @GAME-STATE)))) 
        cancelled-rows (filter #(= (:x grid-size) (count %1)) rows)
        cancelled-cells (reduce clojure.set/union cancelled-rows)
        heights (sort (vec (set (for [r cancelled-cells] (second r)))))]
    (swap! GAME-STATE update-in [:fallen-blocks] #(clojure.set/difference %1 cancelled-cells))
    (downshift-rows! GAME-STATE heights)))

(defn free-fall!
  [GAME-STATE]
  (if (collided? @GAME-STATE (axis :down (:position (:falling-block @GAME-STATE))))
    (let [r (:rotation (:falling-block @GAME-STATE))
          t (:type (:falling-block @GAME-STATE))
          p (:position (:falling-block @GAME-STATE))
          block-local-axes (nth (iterate block-rotated (t blocks)) r)
          block-cells (set (map #(vec (map + %1 p)) block-local-axes))
          new-block {:type (rand-nth (keys blocks)) :rotation (rand-int 4) :position [4 17]}] 
      (swap! GAME-STATE update-in [:fallen-blocks] clojure.set/union block-cells) 
      (reset! GAME-STATE (assoc @GAME-STATE :falling-block new-block)) 
      (cancel-rows! GAME-STATE))
    (swap! GAME-STATE update-in [:falling-block :position] 
           #(axis :down %1))))
