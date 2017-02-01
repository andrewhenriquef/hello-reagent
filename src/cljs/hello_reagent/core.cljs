(ns hello-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Views
(defn home-page []

;;players cols and data
;
;   (def player-cols [:name :nickname ])
;
;   (def players [{:name "Andrew" :nickname "Slow Murder" :date 1994}
;                 {:name "Henrique" :nickname "Betsy" :date 1994}
;                 {:name "Paulo" :nickname "Sanduba" :date 1875}])
;
;
; ;;philosophers cols and data
;   (def philosopher-cols
;     [:name :country :date])
;
;   (def philosophers
;     [{:name "Descartes" :country "France" :date 1596}
;     {:name "Kant" :country "Prussia" :date 1724}
;     {:name "Quine" :country "U.S.A." :date 1908}])
;
; ;;ui data
;   (defn row-ui [cols m]
;    [:tr {:key (:name m)} (map (fn [col] [:td {:key col} (get m col)]) cols)])
;
;   (defn hello [name]
;     [:div
;     [:h1 {:style {:font-family "ubuntu" :color  "orange" :text-align "center" :background-color "red"}} "Hello, " name]])


;;-----------------------------------------
;;circle
;;----------------------------------------
  (defn concentric-circles []
    [:svg {:style {:border "0px solid"
               :background "white"
               :width "300px"
               :height "300px"}}
               [:circle {:r 100, :cx 150, :cy 150, :fill "red"}]
               [:circle {:r 50, :cx 150, :cy 150, :fill "black"}]
               [:path {:stroke-width 20
                 :stroke "white"
                 :fill "none"
                 :d "M 60,80 C 200,80 100,220 240,220"}]])

;;-----------------------------------------
;;circle inc and dec
;;----------------------------------------

  (def c
    (reagent/atom 1))

  (defn counter []
    [:div
     [:div "Current counter value: " @c]
     [:button
      {:disabled (>= @c 4)
       :on-click
       (fn clicked [e]
         (swap! c inc))}
      "inc"]
     [:button
      {:disabled (<= @c 1)
       :on-click
       (fn clicked [e]
         (swap! c dec))}
      "dec"]
     (into [:div] (repeat @c [concentric-circles]))])

;;-----------------------------------------
;;Many Circles
;;----------------------------------------

  (defn many-circles []
   (into
     [:svg {:style {:border "1px solid"
                    :background "white"
                    :width "600px"
                    :height "600px"}}]
     (for [i (range 12)]
       [:g
        {:transform (str
                      "translate(300,300) "
                      "rotate(" (* i 30) ") "
                      "translate(100)")}
        [concentric-circles]])))

;;-----------------------------------------
;;Sort Rolls
;;----------------------------------------

  (def rolls (reagent/atom [7 8 9 4]))
  (def sorted-rolls (reagent.ratom/reaction (sort @rolls)))

  (defn sorted-d20 []
    [:div
     [:button {:on-click (fn [e] (swap! rolls conj (rand-int 500)))} "Roll!"]
     [:p (pr-str @sorted-rolls)]
     [:p (pr-str (reverse @sorted-rolls))]])

;;-----------------------------------------
;;Hello Functions
;;----------------------------------------

     (defn greetings []
       (fn []
         [:h3 "Hello world, Clojurian"]))



;;-----------------------------------------
;;mouse trap function
;;----------------------------------------

     (defn a-better-mouse-trap [mouse]
       (let [mice (reagent/atom 1)]
         (fn render-mouse-trap [mouse]
           (into
             [:div
              [:button
               {:on-click
                (fn [e]
                  (swap! mice (fn [m] (inc (mod m 4)))))}
               "Catch!"]]
             (repeat @mice mouse)))))

  (defn lambda [rotation x y]
    [:g {:transform (str "translate(" x "," y ")"
                       "rotate(" rotation ") ")}
   [:circle {:r 50, :fill "red"}]
   [:circle {:r 25, :fill "black"}]

   [:path {:stroke-width 12
           :stroke "white"
           :fill "none"
           :d "M -45,-35 C 25,-35 -25,35 45,35 M 0,0 -45,45"}]])

(defn spinnable []
  (reagent/with-let [rotation (reagent/atom 0)]
    [:svg
     {:width 150 :height 150
      :on-mouse-move
      (fn [e]
        (swap! rotation + 30))}
     [lambda @rotation 75 75]]))

(defn several-spinnables []
  [:div
   [:h3 "Move your mouse over me"]
   [a-better-mouse-trap [spinnable]]])


;;-----------------------------------------
;;creating and returning a class
;;----------------------------------------
   (defn announcement []
     (reagent/create-class
       {:reagent-render
        (fn []
          [:h3 "I for one welcome our new insect overlords."])}))

;;-----------------------------------------
;;Function of mouse position
;;----------------------------------------

  (defn mouse-position []
    (reagent/with-let [pointer (reagent/atom nil)
                       handler (fn [e]
                                 (swap! pointer assoc
                                        :x (.-pageX e)
                                        :y (.-pageY e)))
                       _ (js/document.addEventListener "mousemove" handler)]
      [:div "Pointer moved to: " (str @pointer)]
      (finally
        (js/document.removeEventListener "mousemove" handler))))

;;-----------------------------------------
;;Creating a threejs canvas
;;----------------------------------------

        (defn create-renderer [element]
          (doto (js/THREE.WebGLRenderer. #js {:canvas element :antialias true})
            (.setPixelRatio js/window.devicePixelRatio)))

        (defn three-canvas [attributes camera scene tick]
          (let [requested-animation (atom nil)]
            (reagent/create-class
              {:display-name "three-canvas"
               :reagent-render
               (fn three-canvas-render []
                 [:canvas attributes])
               :component-did-mount
               (fn three-canvas-did-mount [this]
                 (let [e (reagent/dom-node this)
                       r (create-renderer e)]
                   ((fn animate []
                      (tick)
                      (.render r scene camera)
                      (reset! requested-animation (js/window.requestAnimationFrame animate))))))
               :component-will-unmount
               (fn [this]
                 (js/window.cancelAnimationFrame @requested-animation))})))

;;-----------------------------------------
;;ThreeJS concentric-circles
;;----------------------------------------

(defn create-scene []
  (doto (js/THREE.Scene.)
    (.add (js/THREE.AmbientLight. 0x888888))
    (.add (doto (js/THREE.DirectionalLight. 0xffff88 0.5)
            (-> (.-position) (.set -600 300 600))))
    (.add (js/THREE.AxisHelper. 50))))

(defn mesh [geometry color]
  (js/THREE.SceneUtils.createMultiMaterialObject.
    geometry
    #js [(js/THREE.MeshBasicMaterial. #js {:color color :wireframe true})
         (js/THREE.MeshLambertMaterial. #js {:color color})]))

(defn fly-around-z-axis [camera scene]
  (let [t (* (js/Date.now) 0.0002)]
    (doto camera
      (-> (.-position) (.set (* 100 (js/Math.cos t)) (* 100 (js/Math.sin t)) 100))
      (.lookAt (.-position scene)))))

(defn v3 [x y z]
  (js/THREE.Vector3. x y z))

(defn lambda-3d []
  (let [camera (js/THREE.PerspectiveCamera. 45 1 1 2000)
        curve (js/THREE.CubicBezierCurve3.
                (v3 -30 -30 10)
                (v3 0 -30 10)
                (v3 0 30 10)
                (v3 30 30 10))
        path-geometry (js/THREE.TubeGeometry. curve 20 4 8 false)
        scene (doto (create-scene)
                (.add
                  (doto (mesh (js/THREE.CylinderGeometry. 40 40 5 24) "green")
                    (-> (.-rotation) (.set (/ js/Math.PI 2) 0 0))))
                (.add
                  (doto (mesh (js/THREE.CylinderGeometry. 20 20 10 24) "blue")
                    (-> (.-rotation) (.set (/ js/Math.PI 2) 0 0))))
                (.add (mesh path-geometry "white")))
        tick (fn []
               (fly-around-z-axis camera scene))]
                [three-canvas {:width 150 :height 150} camera scene tick]))


  (defn main-panel []
    [:div
      [:center


        [:h4 [mouse-position]]
        [announcement]
        [several-spinnables]
        [:h1 "Clojure"]
        [greetings]
        [:h2 "What do you think about ?"]
        [many-circles]
        [counter]
        [sorted-d20]


        [:form
          {:on-submit
            (fn [e]
              (.preventDefault e)
              (js/alert
                (str "Your said: "(.. e -target -elements -message -value))))}
                [:label "Answer"
                [:input {:name "message"
                         :type "text"
                         :default-value "Tell me"}]]
                         [:input
                          {:type "submit"}]]]




                          [:div
                            [a-better-mouse-trap
                            [:img
                              {:src "https://www.domyownpestcontrol.com/images/content/mouse.jpg"
                               :style {:width "150px" :border "1px solid"}}]]
                            [:div
                              [a-better-mouse-trap
                                [:img
                                  {:src "https://avatars1.githubusercontent.com/u/9254615?v=3&s=150"
                                   :style {:border "1px solid"}}]]]]])


)


(defn about-page []
  [:div [:h2 "About hello-reagent"]
  [:div [:a {:href "/"} "go to the home page"]]])
(defn current-page []
  [:div [(session/get :current-page)]])

[about-page]
;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app
;
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

(defn root []
  [:div "Where the magic happens"]
  (reagent/render (fn [] [root])
    (.getElementById js/document "container")))
