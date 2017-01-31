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

  (defn main-panel []
    [:div
      [:center


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
